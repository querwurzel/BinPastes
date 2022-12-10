package it.wylke.binpastes.paste.api;

import it.wylke.binpastes.paste.api.model.CreatePaste;
import it.wylke.binpastes.paste.api.model.CreatePaste.ExpirationRanges;
import it.wylke.binpastes.paste.api.model.ListPastes;
import it.wylke.binpastes.paste.api.model.ReadPaste;
import it.wylke.binpastes.paste.domain.PasteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import reactor.core.publisher.Mono;

@CrossOrigin
@Controller
@RequestMapping("/api/v1/paste")
class PasteController {

    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteService pasteService;

    @Autowired
    PasteController(final PasteService pasteService) {
        this.pasteService = pasteService;
    }

    /*
    @GetMapping(value = "/{pasteId}")
    public Mono<RedirectView> redirect(@PathVariable("pasteId") String pasteId) {
        log.trace("redirect {}", pasteId);
        return Mono.just(new RedirectView("/"));
    }
    */

    @GetMapping(value = "/{pasteId}")
    @ResponseBody
    public Mono<ReadPaste> findPaste(@PathVariable("pasteId") String pasteId) {
        log.trace("findPaste {}", pasteId);
        return pasteService
                .find(pasteId)
                .map(ReadPaste::from)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/{pasteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePaste(@PathVariable("pasteId") String pasteId) {
        log.trace("deletePaste {}", pasteId);
        pasteService.delete(pasteId);
    }

    @GetMapping("/search/{text}")
    @ResponseBody
    public Mono<ListPastes> findPastesByFullText(@PathVariable("text") String text) {
        log.trace("findPastesByFullText {}", text);
        return pasteService
                .findByFullText(text)
                .map(ReadPaste::from)
                .collectList()
                .map(ListPastes::from);
    }

    @GetMapping
    @ResponseBody
    public Mono<ListPastes> findPastes() {
        log.trace("findPastes");
        return pasteService
                .findAll()
                .map(ReadPaste::from)
                .collectList()
                .map(ListPastes::from);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<RedirectView> createPaste(ServerWebExchange ctx) {
        log.trace("createPaste");

        return ctx
                .getFormData()
                .flatMap(formData -> {

                    var expiry = ExpirationRanges.valueOf(formData.getFirst("expiry"));
                    log.warn("expiry parsed: {}", expiry);

                    var createCmd = new CreatePaste(
                        formData.getFirst("title"),
                        formData.getFirst("content"),
                        expiry
                    );

                    return pasteService.create(
                            createCmd.content(),
                            createCmd.title(),
                            ctx.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                            expiry == null ? null : expiry.toTimestamp()
                    );
                })
                .map(paste -> new RedirectView("/api/v1/paste/" + paste.getId()));
    }

}
