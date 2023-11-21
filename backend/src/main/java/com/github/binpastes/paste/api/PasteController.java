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
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    public Mono<SingleView> findPaste(@PathVariable("pasteId") String pasteId, ServerHttpRequest request, ServerHttpResponse response) {
        return pasteService
                .find(pasteId)
                .doOnNext(paste -> {
                    if (paste.isOneTime()) {
                        response.getHeaders().setCacheControl(CacheControl.noStore());
                        return;
                    }

                    if (paste.isPermanent() || isAfter(paste.getDateOfExpiry(), 5, ChronoUnit.MINUTES)) {
                        response.getHeaders().setCacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES));
                        return;
                    }

                    response.getHeaders().setCacheControl(
                            CacheControl.maxAge(Duration.between(LocalDateTime.now(), paste.getDateOfExpiry())));
                })
                .map(reference -> SingleView.of(reference, remoteAddress(request)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    private static boolean isAfter(LocalDateTime dateTime, long amount, ChronoUnit unit) {
        return LocalDateTime.now().plus(amount, unit).isBefore(dateTime);
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
