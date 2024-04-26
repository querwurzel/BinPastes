package com.github.binpastes.paste.api;

import com.github.binpastes.paste.api.model.CreateCmd;
import com.github.binpastes.paste.api.model.DetailView;
import com.github.binpastes.paste.api.model.ListView;
import com.github.binpastes.paste.api.model.SearchView;
import com.github.binpastes.paste.api.model.SearchView.SearchItemView;
import com.github.binpastes.paste.application.PasteService;
import com.github.binpastes.paste.domain.Paste;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.github.binpastes.paste.api.model.ListView.ListItemView;

@Validated
@RestController
@RequestMapping({"/api/v1/paste", "/api/v1/paste/"})
class PasteController {

    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteService pasteService;

    @Autowired
    public PasteController(final PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @GetMapping("/{pasteId:[a-zA-Z0-9]{40}}")
    public Mono<DetailView> findPaste(
            @PathVariable("pasteId")
            String pasteId,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        return pasteService
                .find(pasteId)
                .doOnNext(paste -> {
                    if (paste.isOneTime()) {
                        response.getHeaders().setCacheControl(CacheControl.noStore());
                        return;
                    }

                    var now = LocalDateTime.now();
                    if (paste.isPermanent() || paste.getDateOfExpiry().plusMinutes(5).isAfter(now)) {
                        response.getHeaders().setCacheControl(
                                CacheControl.maxAge(5, TimeUnit.MINUTES));
                    } else {
                        response.getHeaders().setCacheControl(
                                CacheControl.maxAge(Duration.between(now, paste.getDateOfExpiry())));
                    }
                })
                .map(reference -> DetailView.of(reference, remoteAddress(request)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping
    public Mono<ListView> findPastes() {
        return pasteService
                .findAll()
                .map(ListItemView::of)
                .collectList()
                .map(ListView::of);
    }

    @GetMapping("/search")
    public Mono<SearchView> searchPastes(
            @RequestParam("term")
            @NotBlank
            @Pattern(regexp = "[\\pL\\pN\\p{P}\\s]{3,25}")
            final String term,
            final ServerHttpResponse response
    ) {
        response.getHeaders().setCacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS));
        return pasteService
                .findByFullText(URLDecoder.decode(term, Charset.defaultCharset()))
                .map(paste -> SearchItemView.of(paste, term))
                .collectList()
                .map(SearchView::of);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DetailView> createPaste(@Valid @RequestBody Mono<CreateCmd> createCmd, ServerHttpRequest request) {
        return createCmd
                .flatMap(cmd -> pasteService.create(
                        cmd.title(),
                        cmd.content(),
                        cmd.dateOfExpiry(),
                        cmd.isEncrypted(),
                        cmd.pasteExposure(),
                        remoteAddress(request)
                ))
                .map((Paste reference) -> DetailView.of(reference, remoteAddress(request)));
    }

    @DeleteMapping("/{pasteId:[a-zA-Z0-9]{40}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePaste(@PathVariable("pasteId") String pasteId, ServerHttpRequest request) {
        pasteService.delete(pasteId, remoteAddress(request));
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

        if (request.getRemoteAddress() == null) {
            return null;
        }

        return request.getRemoteAddress().getAddress().getHostAddress();
    }
}
