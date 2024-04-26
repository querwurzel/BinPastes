package com.github.binpastes.paste.api;

import com.github.binpastes.paste.api.model.CreateCmd;
import com.github.binpastes.paste.api.model.ListView;
import com.github.binpastes.paste.api.model.SearchView;
import com.github.binpastes.paste.api.model.SearchView.SearchItemView;
import com.github.binpastes.paste.api.model.SingleView;
import com.github.binpastes.paste.domain.Paste;
import com.github.binpastes.paste.domain.PasteService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.github.binpastes.paste.api.model.ListView.ListItemView;

@Validated
@RestController
@RequestMapping("/api/v1/paste")
class PasteController {

    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteService pasteService;

    @Autowired
    public PasteController(final PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @GetMapping("/{pasteId:[a-zA-Z0-9]{40}}")
    public Mono<SingleView> findPaste(@PathVariable("pasteId") String pasteId, ServerHttpRequest request, ServerHttpResponse response) {
        return pasteService
                .find(pasteId)
                .doOnNext(paste -> {
                    if (paste.isOneTime()) {
                        response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "no-store");
                    } else {
                        if (paste.getDateOfExpiry() != null ) {
                            var in5min = LocalDateTime.now().plusMinutes(5);
                            if (in5min.isAfter(paste.getDateOfExpiry())) {
                                response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "max-age=" + Duration.between(LocalDateTime.now(), paste.getDateOfExpiry()).toSeconds());
                                return;
                            }
                        }

                        response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "max-age=300");
                    }
                })
                .map(reference -> SingleView.of(reference, remoteAddress(request)))
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
        response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "max-age=60");
        return pasteService
                .findByFullText(term)
                .map(paste -> SearchItemView.of(paste, term))
                .collectList()
                .map(SearchView::of);
    }

    @PostMapping
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
                .map((Paste reference) -> SingleView.of(reference, remoteAddress(request)));
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
