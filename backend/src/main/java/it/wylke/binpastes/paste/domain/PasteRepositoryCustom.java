package it.wylke.binpastes.paste.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

interface PasteRepositoryCustom {

    Mono<Paste> findOneLegitById(String id);

    Flux<Paste> findAllLegit();

    Flux<Paste> searchByFullText(String text);

    Mono<Long> markExpiredPastesForDeletion(LocalDateTime expiryBefore);

    interface FullTextSearchSupport {
        default Flux<Paste> searchByFullText(String text) {
            return Flux.empty();
        }
    }

}
