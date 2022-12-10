package it.wylke.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
class PasteCleanupService {

    private static final Logger log = LoggerFactory.getLogger(PasteCleanupService.class);

    private final PasteRepository pasteRepository;

    @Autowired
    public PasteCleanupService(final PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    @Scheduled(cron = "3 3 * * * *" /* 3:03 each day */)
    private void cleanUpExpiredPastes() {
        pasteRepository
                .markExpiredPastesForDeletion(LocalDateTime.now())
                .doFirst(() -> log.info("Marking expired pastes for deletion .."))
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.warn("Found {} expired pastes, now marked for deletion", count);
                    } else {
                        log.info("Found no expired pastes");
                    }
                })
                .block();
    }

    @Scheduled(cron = "4 4 * * * *" /* 4:04 each day */)
    private void cleanUpDeletedPastes() {
        pasteRepository
                .deleteByDateDeletedBefore(LocalDateTime.now().minusMonths(6))
                .doFirst(() -> log.info("Deleting pastes marked for deletion permanently .."))
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.warn("Deleted {} pastes permanently", count);
                    }
                })
                .block();
    }

}
