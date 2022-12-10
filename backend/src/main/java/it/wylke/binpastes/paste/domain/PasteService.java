package it.wylke.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.color.ICC_ColorSpace;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class PasteService {

    private static final Logger log = LoggerFactory.getLogger(PasteService.class);

    private final PasteRepository pasteRepository;

    @Autowired
    public PasteService(final PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    public Mono<Paste> create(String content, String title, String clientRemoteAddress, LocalDateTime expiry) {
        return Mono
                .just(Paste.newInstance(content, title, clientRemoteAddress, expiry))
                .flatMap(pasteRepository::save)
                .doOnSuccess(newPaste -> log.info("Created new paste {}", newPaste.getId()))
                .doOnError(throwable -> log.error("Failed to create new paste", throwable));
    }

    public Mono<Paste> find(String id) {
        return pasteRepository.findByIsDeletedFalseAndExpiryAfterAndId(LocalDateTime.now(), id);
    }

    public Flux<Paste> findAll() {
        return pasteRepository.findByIsDeletedFalseAndExpiryAfter(LocalDateTime.now());
    }

    public Flux<Paste> findByFullText(String text) {
        var results = pasteRepository
                .findByIsDeletedFalseAndExpiryAfterAndContentContainsIgnoreCaseOrTitleContainsIgnoreCase(
                        LocalDateTime.now(),
                        text, text
                );

        try {
            return results;
        } finally {
            results
                .count()
                .subscribe(count -> log.info("Found {} pastes searching for {}", count, text)); // TODO clarify
        }
    }

    public void delete(String id) {
        pasteRepository
                .findById(id)
                .map(Paste::markAsDeleted)
                .flatMap(pasteRepository::save)
                .subscribe();
    }

    @Scheduled(initialDelay = 5, fixedDelay = 30, timeUnit = TimeUnit.SECONDS) // TODO init delay 1min
    private void cleanUpExpiredScheduledPastes() {
        pasteRepository
                .markExpiredPastesForDeletion(LocalDateTime.now())
                .doFirst(() -> log.info("Starting cleanup job"))
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.info("Expired {} pastes, now marked for deletion", count);
                    } else {
                        log.info("No expired pastes found.");
                    }
                })
                .doFinally(ignored -> log.info("Finishing cleanup job"))
                .block();
    }

    @Scheduled(cron = "0 0 0 1 3,9 *") // 1.3. and 1.9. each year
    //@Scheduled(cron = "0 * * * * *")
    private void cleanUpDeletedPastes() {
        log.warn("Deleting pastes marked for deletion permanently");

        pasteRepository
                .deleteByIsDeletedTrue()
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.warn("Deleted {} pastes permanently", count);
                    }
                })
                .block();



    }

    private void logDuration(LocalDateTime start) {
    }

}
