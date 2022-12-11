package it.wylke.binpastes.paste.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

interface PasteRepositoryCustom {

    default Mono<Paste> findOneLegitById(String id) {
        return Mono.empty();
    }

    default Flux<Paste> findAllLegit() {
        return Flux.empty();
    }

    default Flux<Paste> searchByFullText(String text) {
        return Flux.empty();
    }

    default Mono<Long> markExpiredPastesForDeletion(LocalDateTime expiryBefore) {
        return Mono.empty();
    }

    interface FullTextSearchSupport {
        Flux<Paste> searchByFullText(String text);
    }

}
