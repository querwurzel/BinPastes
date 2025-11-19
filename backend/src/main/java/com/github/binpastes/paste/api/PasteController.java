package com.github.binpastes.paste.api;

import com.github.binpastes.paste.application.PasteViewService;
import com.github.binpastes.paste.application.model.CreateCmd;
import com.github.binpastes.paste.application.model.DetailView;
import com.github.binpastes.paste.application.model.ListView;
import com.github.binpastes.paste.application.model.SearchView;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping(
    path = {"/api/v1/paste", "/api/v1/paste/"},
    produces = MediaType.APPLICATION_JSON_VALUE
)
class PasteController {

    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteViewService pasteViewService;

    @Autowired
    public PasteController(final PasteViewService pasteViewService) {
        this.pasteViewService = pasteViewService;
    }

    @GetMapping("/{pasteId:[a-z0-9]{40}}")
    public Mono<DetailView> findPaste(
        @PathVariable("pasteId")
        final String pasteId,
        final ServerHttpRequest request,
        final ServerHttpResponse response
    ) {
        return pasteViewService
            .viewPaste(pasteId, remoteAddress(request))
            .doOnNext(paste -> {
                if (paste.isOneTime()) {
                    response.getHeaders().setCacheControl(CacheControl.noStore());
                    return;
                }
                if (paste.isPermanent()) {
                    response.getHeaders().setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
                    return;
                }

                var now = LocalDateTime.now();
                var maxAge = paste.dateOfExpiry()
                    .filter(exp -> exp.plusMinutes(1).isAfter(now))
                    .map(exp -> Duration.between(now, exp))
                    .orElse(Duration.ofMinutes(1));

                response.getHeaders().setCacheControl(CacheControl.maxAge(maxAge).mustRevalidate());
            })
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PostMapping("/{pasteId:[a-z0-9]{40}}")
    public Mono<DetailView> findAndBurnOneTimePaste(
        @PathVariable("pasteId")
        final String pasteId,
        final ServerHttpResponse response
    ) {
        response.getHeaders().setCacheControl(CacheControl.noStore());
        return pasteViewService
            .viewOneTimePaste(pasteId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping
    public Mono<ListView> findPastes() {
        return pasteViewService.viewAllPastes();
    }

    @GetMapping("/search")
    public Mono<SearchView> searchPastes(
        @RequestParam("term")
        @NotBlank
        @Pattern(regexp = "[\\p{L}\\p{N}\\p{P}\\s]{3,50}")
        final String term,
        final ServerHttpResponse response
    ) {
        var decodedTerm = URLDecoder.decode(term, Charset.defaultCharset());
        response.getHeaders().setCacheControl(CacheControl.maxAge(Duration.ofMinutes(1)));
        return pasteViewService.searchByFullText(decodedTerm);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DetailView> createPaste(@Valid @RequestBody final CreateCmd createCmd, final ServerHttpRequest request) {
        return pasteViewService.createPaste(createCmd, remoteAddress(request));
    }

    @DeleteMapping("/{pasteId:[a-z0-9]{40}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deletePaste(@PathVariable("pasteId") final String pasteId, final ServerHttpRequest request) {
        return pasteViewService.requestDeletion(pasteId, remoteAddress(request));
    }

    @ExceptionHandler({ConstraintViolationException.class, WebExchangeBindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private void handleValidationException(final RuntimeException e) {
        log.info("Received invalid request [{}]: {}", e.getClass().getSimpleName(), e.getMessage());
    }

    private static String remoteAddress(final ServerHttpRequest request) {
        if (request.getHeaders().containsKey("X-Forwarded-For")) {
            return request.getHeaders().getFirst("X-Forwarded-For");
        }

        if (request.getRemoteAddress() == null) {
            return null;
        }

        return request.getRemoteAddress().getAddress().getHostAddress();
    }
}
