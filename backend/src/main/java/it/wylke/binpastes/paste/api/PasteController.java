package it.wylke.binpastes.paste.api;

import it.wylke.binpastes.paste.api.model.CreateCmd;
import it.wylke.binpastes.paste.api.model.ListView;
import it.wylke.binpastes.paste.api.model.SingleView;
import it.wylke.binpastes.paste.domain.PasteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@CrossOrigin("paste.wilke-it.com")
@Controller
@RequestMapping("/api/v1/paste")
class PasteController {

    private final PasteService pasteService;

    @Autowired
    PasteController(final PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @GetMapping("/{pasteId:[a-zA-Z0-9]+}")
    @ResponseBody
    public Mono<SingleView> findPaste(@PathVariable("pasteId") String pasteId) {
        return pasteService
                .find(pasteId)
                .map(SingleView::from)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/{pasteId:[a-zA-Z0-9]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePaste(@PathVariable("pasteId") String pasteId) {
        pasteService.delete(pasteId);
    }

    @GetMapping("/search")
    @ResponseBody
    public Mono<ListView> findPastesByFullText(@RequestParam("text") String text) {
        if (!StringUtils.hasText(text) || text.strip().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return pasteService
                .findByFullText(text)
                .map(SingleView::from)
                .collectList()
                .map(ListView::from);
    }

    @GetMapping
    @ResponseBody
    public Mono<ListView> findPastes() {
        return pasteService
                .findAll()
                .map(SingleView::from)
                .collectList()
                .map(ListView::from);
    }

    @PostMapping
    public Mono<RedirectView> createPaste(@Valid @RequestBody Mono<CreateCmd> createCmd, ServerHttpRequest request) {
        return createCmd
                .flatMap(cmd -> pasteService.create(
                        cmd.content(),
                        cmd.title(),
                        cmd.isEncrypted(),
                        request.getRemoteAddress().getAddress().getHostAddress(),
                        cmd.dateOfExpiry()
                )
                .map(paste -> new RedirectView("/api/v1/paste/" + paste.getId())));
    }

    @Deprecated
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<RedirectView> createPasteByForm(ServerWebExchange ctx) {
        return ctx
                .getFormData()
                .flatMap(formData -> {
                    var createCmd = new CreateCmd(
                        formData.getFirst("title"),
                        formData.getFirst("content"),
                        formData.getFirst("isEncrypted"),
                        formData.getFirst("expiry")
                    );

                    return pasteService.create(
                            createCmd.content(),
                            createCmd.title(),
                            createCmd.isEncrypted(),
                            ctx.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                            createCmd.dateOfExpiry()
                    );
                })
                .map(paste -> new RedirectView("/api/v1/paste/" + paste.getId()));
    }

}
