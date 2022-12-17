package it.wylke.binpastes.paste.api;

import it.wylke.binpastes.paste.api.model.CreateCmd;
import it.wylke.binpastes.paste.api.model.ListView;
import it.wylke.binpastes.paste.api.model.SingleView;
import it.wylke.binpastes.paste.domain.PasteService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static it.wylke.binpastes.paste.api.model.ListView.ListItemView;

@CrossOrigin("https://paste.wilke-it.com")
@Controller
@Validated
@RequestMapping("/api/v1/paste")
class PasteController {

    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteService pasteService;

    @Autowired
    public PasteController(final PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @GetMapping("/{pasteId:[a-zA-Z0-9]{40}}")
    @ResponseBody
    public Mono<SingleView> findPaste(@PathVariable("pasteId") String pasteId, ServerHttpResponse response) {
        return pasteService
                .find(pasteId)
                .doOnNext(paste -> {
                    if (paste.isOneTime()) {
                        response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "no-store");
                    }
                })
                .map(SingleView::from)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping
    @ResponseBody
    public Mono<ListView> findPastes() {
        return pasteService
                .findAll()
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NO_CONTENT)))
                .map(ListItemView::from)
                .collectList()
                .map(ListView::from);
    }

    @GetMapping("/search")
    @ResponseBody
    public Mono<ListView> searchPastes(@RequestParam("text") @NotBlank @Size(min = 3) @Pattern(regexp = "[\\pL\\pN\\s]+") String text) {
        return pasteService
                .findByFullText(text)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NO_CONTENT)))
                .map(ListItemView::from)
                .collectList()
                .map(ListView::from);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SingleView> createPaste(@Valid @RequestBody Mono<CreateCmd> createCmd, ServerHttpRequest request) {
        return createCmd
                .flatMap(cmd -> pasteService.create(
                        cmd.title(),
                        cmd.content(),
                        cmd.dateOfExpiry(),
                        cmd.isEncrypted(),
                        cmd.pasteExposure(),
                        remoteAddress(request)
                ))
                .map(SingleView::from);
    }

    @DeleteMapping("/{pasteId:[a-zA-Z0-9]{40}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePaste(@PathVariable("pasteId") String pasteId) {
        pasteService.delete(pasteId);
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
                        formData.getFirst("expiry"),
                        formData.getFirst("exposure")
                    );

                    return pasteService.create(
                            createCmd.title(),
                            createCmd.content(),
                            createCmd.dateOfExpiry(),
                            createCmd.isEncrypted(),
                            createCmd.pasteExposure(),
                            remoteAddress(ctx.getRequest())
                    );
                })
                .map(paste -> new RedirectView("/#" + paste.getId()));
    }

    @ExceptionHandler({ConstraintViolationException.class, WebExchangeBindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private void handleValidationException(RuntimeException e) {
        log.info("Received invalid request: {}", e.getClass().getSimpleName());
    }

    private static String remoteAddress(ServerHttpRequest request) {
        if (request.getHeaders().containsKey("X-Forwarded-For")) {
            return request.getHeaders().getFirst("X-Forwarded-For");
        }

        if (request.getRemoteAddress().getAddress() == null) {
            return null;
        }

        return request.getRemoteAddress().getAddress().getHostAddress();
    }

}
