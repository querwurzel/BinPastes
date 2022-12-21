package it.wylke.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

import static it.wylke.binpastes.paste.domain.Paste.PasteExposure;

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
            String exposure,
            String remoteAddress
    ) {
        return pasteRepository
                .save(Paste.newInstance(title, content, dateOfExpiry, isEncrypted, PasteExposure.valueOf(exposure), remoteAddress))
                .doOnSuccess(newPaste -> log.info("Created new paste {}", newPaste.getId()))
                .doOnError(throwable -> log.error("Failed to create new paste", throwable));
    }

    public Mono<Paste> find(String id) {
        return pasteRepository
                .findOneLegitById(id)
                .flatMap(this::trackAccess);
    }

    private Mono<Paste> trackAccess(Paste paste) {
        if (paste.isOneTime()) {
            log.info("OneTime paste {} viewed and burnt", paste.getId());
            return pasteRepository.save(paste.markAsExpired());
        }

        return Mono.just(paste);
    }

    public Flux<Paste> findAll() {
        return pasteRepository.findAllLegit();
    }

    public Flux<Paste> findByFullText(String text) {
        return pasteRepository
                .searchAllLegitByFullText(text)
                .collectList()
                .doOnSuccess(pastes -> log.info("Found {} pastes searching for: {}", pastes.size(), text))
                .flatMapMany(Flux::fromIterable);
    }

    public void delete(String id) {
        pasteRepository
                .findOneLegitById(id)
                .map(Paste::markAsExpired)
                .flatMap(pasteRepository::save)
                .doOnNext(paste -> log.info("Deleted paste {}", id))
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

}
