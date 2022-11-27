package com.github.binpastes.paste.domain;

import reactor.core.publisher.Flux;

interface FullTextSearchSupport {
    default Flux<Paste> searchByFullText(String text) {
        return Flux.empty();
    }
}
