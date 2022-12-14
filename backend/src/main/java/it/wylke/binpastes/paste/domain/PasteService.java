package it.wylke.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
            PasteExposure exposure,
            String remoteAddress
    ) {
        return Mono
                .just(Paste.newInstance(title, content, dateOfExpiry, isEncrypted, exposure, remoteAddress))
                .flatMap(pasteRepository::save)
                .doOnSuccess(newPaste -> log.info("Created new paste {}", newPaste.getId()))
                .doOnError(throwable -> log.error("Failed to create new paste", throwable));
    }

    public Mono<Paste> find(String id) {
        return pasteRepository.findOneLegitById(id);
    }

    public Flux<Paste> findAll() {
        return pasteRepository.findAllLegit();
    }

    public Flux<Paste> findByFullText(String text) {
        var results = pasteRepository.searchAllLegitByFullText(text);

        try {
            return results;
        } finally {
            results
                .count()
                // TODO clarify: Calling 'subscribe' in non-blocking context is not recommended
                .subscribe(count -> log.info("Found {} pastes searching for: {}", count, text));
        }
    }

    public void delete(String id) {
        pasteRepository
                .findOneLegitById(id)
                .map(Paste::markAsDeleted)
                .flatMap(pasteRepository::save)
                .doOnSuccess(paste -> log.info("Deleted paste {}", id))
                .subscribe();
    }

}
