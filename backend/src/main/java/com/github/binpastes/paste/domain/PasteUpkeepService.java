package com.github.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Component
class PasteUpkeepService {

    private static final Logger log = LoggerFactory.getLogger(PasteUpkeepService.class);

    private final PasteRepository pasteRepository;

    @Autowired
    public PasteUpkeepService(final PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    @Scheduled(cron = "0 3 3 * * *" /* 3:03am each day */)
    private void cleanUpExpiredPastes() {
        pasteRepository
                .markExpiredPastesForDeletion()
                .doFirst(() -> log.info("Expiry: marking expired pastes for deletion .."))
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.warn("Expiry: found {} expired pastes, now marked for deletion", count);
                    } else {
                        log.info("Expiry: no expired pastes found");
                    }
                })
                .subscribeOn(Schedulers.immediate())
                .subscribe();
    }

    @Scheduled(cron = "0 4 4 * * *" /* 4:04am each day */)
    private void cleanUpDeletedPastes() {
        pasteRepository
                .deleteByDateDeletedBefore(LocalDateTime.now().minusMonths(6))
                .doFirst(() -> log.info("Deletion: deleting pastes marked for deletion .."))
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.warn("Deletion: deleted {} pastes", count);
                    } else {
                        log.info("Deletion: no qualified pastes found for deletion");
                    }
                })
                .subscribeOn(Schedulers.immediate())
                .subscribe();
    }
}
