package it.wylke.binpastes.paste.api;

import it.wylke.binpastes.paste.domain.Paste;
import it.wylke.binpastes.paste.domain.PasteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/api/v1/paste")
class PasteController {

    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteRepository pasteRepository;

    @Autowired
    PasteController(final PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    @GetMapping("/{pasteId}")
    @ResponseBody
    public Mono<Paste> readPaste(@PathVariable String pasteId, ServerWebExchange ctx) {
        return pasteRepository
                .findById(pasteId)
                //.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping
    @ResponseBody
    public Flux<Paste> redirect() {
        return pasteRepository.findAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<RedirectView> createPaste(ServerWebExchange ctx) {
        return ctx
                .getFormData()
                .map(formData -> {
                    var paste = Paste.newInstance(
                            ctx.getRequest().getRemoteAddress().toString().substring(1),
                            formData.getFirst("content"),
                            formData.getFirst("title")
                    );

                    var id = paste.getId();

                    pasteRepository.save(paste);
                    log.info("created new paste {}", id);

                    return id;
                })
                .map(pasteId -> new RedirectView("/api/v1/paste/" + pasteId))
                .doOnError(throwable -> {
                    throwable.printStackTrace();
                    log.error(throwable.getMessage(), throwable);
                });
    }

}
