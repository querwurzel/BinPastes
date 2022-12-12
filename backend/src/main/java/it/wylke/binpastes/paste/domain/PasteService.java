package it.wylke.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PasteService {

    private static final Logger log = LoggerFactory.getLogger(PasteService.class);

    private final PasteRepository pasteRepository;

    @Autowired
    public PasteService(final PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    public Mono<Paste> create(String content, String title, String authorRemoteAddress, LocalDateTime dateOfExpiry) {
        return Mono
                .just(Paste.newInstance(content, title, authorRemoteAddress, dateOfExpiry))
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
        if (StringUtils.hasText(text) && text.strip().length() < 3) {
            throw new IllegalArgumentException("Input must have at least 3 characters!");
        }

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
                .subscribe();
    }

}
