package it.wylke.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
class PasteCleanupService {

    private static final Logger log = LoggerFactory.getLogger(PasteCleanupService.class);

    private final PasteRepository pasteRepository;

    @Autowired
    public PasteCleanupService(final PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    @Scheduled(initialDelay = 10, fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    @Scheduled(cron = "0 3 3 * * *" /* 3:03 each day */)
    private void cleanUpExpiredPastes() {
        pasteRepository
                .markExpiredPastesForDeletion(LocalDateTime.now())
                .doFirst(() -> log.info("Expiry: marking expired pastes for deletion .."))
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.warn("Expiry: found {} expired pastes, now marked for deletion", count);
                    } else {
                        log.info("Expiry: no expired pastes found");
                    }
                })
                .block();
    }

    @Scheduled(cron = "0 4 4 * * *" /* 4:04 each day */)
    private void cleanUpDeletedPastes() {
        pasteRepository
                .deleteByDateDeletedBefore(LocalDateTime.now().minusMonths(6))
                .doFirst(() -> log.info("Deletion: Deleting pastes marked for deletion permanently .."))
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.warn("Deletion: deleted {} pastes permanently", count);
                    } else {
                        log.info("Deletion: no qualified pastes found for deletion");
                    }
                })
                .block();
    }

}
