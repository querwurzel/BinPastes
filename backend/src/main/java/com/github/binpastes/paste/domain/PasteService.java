package com.github.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.github.binpastes.paste.domain.Paste.PasteExposure;

@Service
public class PasteService {

    private static final Logger log = LoggerFactory.getLogger(PasteService.class);

    private final PasteRepository pasteRepository;

    @Autowired
    public PasteService(final PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    public Mono<Paste> create(
            String title,
            String content,
            LocalDateTime dateOfExpiry,
            boolean isEncrypted,
            PasteExposure exposure,
            String remoteAddress
    ) {
        return pasteRepository
                .save(Paste.newInstance(title, content, isEncrypted, exposure, dateOfExpiry, remoteAddress))
                .doOnSuccess(newPaste -> log.info("Created new paste {}", newPaste.getId()))
                .doOnError(throwable -> log.error("Failed to create new paste", throwable));
    }

    public Mono<Paste> find(String id) {
        return pasteRepository.findOneLegitById(id);
    }

    /**
     * Requests and expires one-time paste.
     */
    public Mono<Paste> findAndBurn(String id) {
        return pasteRepository.findOneLegitById(id)
                .filter(Paste::isOneTime)
                .map(Paste::markAsExpired)
                .flatMap(pasteRepository::save)
                .doOnNext(paste -> log.info("OneTime paste {} viewed and burnt", paste.getId()))
                .onErrorComplete(OptimisticLockingFailureException.class);
    }

    public Flux<Paste> findAll() {
        return pasteRepository.findAllLegit();
    }

    public Flux<Paste> findByFullText(String text) {
        return pasteRepository.searchAllLegitByFullText(text);
    }

    public void trackView(String id, LocalDateTime viewedAt) {
        pasteRepository
                .findById(id)
                .filter(Paste::isPublic) // only track public pastes
                .map(paste -> paste.trackView(viewedAt))
                .flatMap(pasteRepository::save)
                .retryWhen(Retry
                        .backoff(5, Duration.ofMillis(500))
                        .filter(ex -> ex instanceof OptimisticLockingFailureException))
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

    public Mono<Void> requestDeletion(String id, String remoteAddress) {
        return pasteRepository
                .findOneLegitById(id)
                .filter(paste -> paste.isErasable(remoteAddress))
                .map(Paste::markAsExpired)
                .flatMap(pasteRepository::save)
                .retryWhen(Retry
                        .backoff(3, Duration.ofMillis(100))
                        .filter(ex -> ex instanceof OptimisticLockingFailureException))
                .doOnNext(paste -> log.atInfo()
                        .addArgument(paste.getId())
                        .addArgument(Objects.toString(remoteAddress, "anonymous"))
                        .log("Deleted paste {} on behalf of {}")
                )
                .then();
    }
}
