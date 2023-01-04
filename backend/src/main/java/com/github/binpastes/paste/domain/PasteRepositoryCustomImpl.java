package com.github.binpastes.paste.domain;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.github.binpastes.paste.domain.Paste.PasteExposure;
import static com.github.binpastes.paste.domain.Paste.PasteSchema;
import static org.springframework.data.relational.core.query.Query.query;

class PasteRepositoryCustomImpl implements PasteRepositoryCustom {

    private final R2dbcEntityTemplate entityTemplate;

    private final FullTextSearchSupport fullTextSearchSupport;

    public PasteRepositoryCustomImpl(R2dbcEntityTemplate entityManager, FullTextSearchSupport fullTextSearchSupport) {
        this.entityTemplate = entityManager;
        this.fullTextSearchSupport = fullTextSearchSupport;
    }

    public Mono<Paste> findOneLegitById(String id) {
        var criteria = Criteria
                .where(PasteSchema.ID).is(id)
                .and(
                        Criteria
                                .where(PasteSchema.DATE_OF_EXPIRY).isNull()
                                .or(PasteSchema.DATE_OF_EXPIRY).greaterThan(LocalDateTime.now())
                );

        return entityTemplate
                .selectOne(query(criteria), Paste.class);
    }

    public Flux<Paste> findAllLegit() {
        return entityTemplate
                .select(Paste.class)
                .matching(
                        Query
                                .query(
                                        Criteria
                                                .where(PasteSchema.EXPOSURE).is(PasteExposure.PUBLIC.name())
                                                .and(
                                                        Criteria
                                                                .where(PasteSchema.DATE_OF_EXPIRY).isNull()
                                                                .or(PasteSchema.DATE_OF_EXPIRY).greaterThan(LocalDateTime.now())
                                                )
                                )
                                .sort(Sort.by(Sort.Direction.DESC, PasteSchema.DATE_CREATED))
                )
                .all();
    }

    @Override
    public Mono<Long> markExpiredPastesForDeletion() {
        var dateOfExpiry = LocalDateTime.now();

        var criteria = Criteria
                .where(PasteSchema.DATE_DELETED).isNull()
                .and(PasteSchema.DATE_OF_EXPIRY).lessThan(dateOfExpiry);

        var update = Update.update(PasteSchema.DATE_DELETED, dateOfExpiry);

        return entityTemplate.update(query(criteria), update, Paste.class);
    }

    @Override
    public Flux<Paste> searchAllLegitByFullText(String text) {
        return fullTextSearchSupport.searchByFullText(text);
    }

}
