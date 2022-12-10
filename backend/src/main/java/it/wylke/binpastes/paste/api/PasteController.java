package it.wylke.binpastes.paste.api;

import it.wylke.binpastes.paste.api.model.CreatePaste;
import it.wylke.binpastes.paste.api.model.ListPastes;
import it.wylke.binpastes.paste.api.model.ReadPaste;
import it.wylke.binpastes.paste.domain.PasteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/v1/paste")
class PasteController {

    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteService pasteService;

    @Autowired
    PasteController(final PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @GetMapping("/{pasteId}")
    @ResponseBody
    public Mono<ReadPaste> findPaste(@PathVariable("pasteId") String pasteId) {
        log.info("findPaste {}", pasteId);
        return pasteService
                .find(pasteId)
                .map(ReadPaste::from)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/{pasteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePaste(@PathVariable("pasteId") String pasteId) {
        pasteService.delete(pasteId);
    }

    @GetMapping("/search/{text}")
    @ResponseBody
    public Mono<ListPastes> findPastesByFullText(@PathVariable("text") String text) {
        if (!StringUtils.hasText(text)) {
            log.info("No search results");
            return Mono.just(new ListPastes(List.of()));
        }



    log.info("searching for {}", text);
        return pasteService
                .findByFullText(text)
                .map(ReadPaste::from)
                .collectList()
                .map(ListPastes::from);
    }

    @GetMapping
    @ResponseBody
    public Mono<ListPastes> findPastes() {
        log.info("Request: findPastes");
        return pasteService
                .findAll()
                .map(ReadPaste::from)
                .collectList()
                .map(ListPastes::from);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<RedirectView> createPaste(ServerWebExchange ctx, ServerHttpRequest request) {
        return ctx
                .getFormData()
                .flatMap(formData -> {
                    var createCmd = new CreatePaste(
                        formData.getFirst("title"),
                        formData.getFirst("content"),
                        null
                    );

                    return pasteService.create(
                            createCmd.content(),
                            createCmd.title(),
                            ctx.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                            LocalDateTime.now().minusSeconds(10)
                    );
                })
                .map(paste -> new RedirectView("/api/v1/paste/" + paste.getId()));
    }

}
