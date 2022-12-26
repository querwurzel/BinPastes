package it.wylke.binpastes.paste.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface PasteRepositoryCustom {

    Mono<Paste> findOneLegitById(String id);

    Flux<Paste> findAllLegit();

    Flux<Paste> searchAllLegitByFullText(String text);

    Mono<Long> markExpiredPastesForDeletion();

}
