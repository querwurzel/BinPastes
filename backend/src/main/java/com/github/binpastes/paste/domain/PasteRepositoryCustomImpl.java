package com.github.binpastes.paste.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.binpastes.paste.domain.Paste.PasteExposure;
import static com.github.binpastes.paste.domain.Paste.PasteSchema;
import static org.springframework.data.relational.core.query.Query.query;

class PasteRepositoryCustomImpl implements PasteRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(PasteRepository.class);

    private final R2dbcEntityTemplate entityTemplate;

    private final List<FullTextSearchSupport> fullTextSearchSupport;

    public PasteRepositoryCustomImpl(R2dbcEntityTemplate entityManager, List<FullTextSearchSupport> fullTextSearchSupport) {
        Assert.notEmpty(fullTextSearchSupport, "Require at least one FullTextSearchSupport implementation");
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
        var result = fullTextSearchSupport.getFirst().searchByFullText(text);

        for (int idx = 1; idx < fullTextSearchSupport.size(); idx++) {
            final var alternativeImplementation = fullTextSearchSupport.get(idx);

            result = result.switchIfEmpty(subscriber -> {
                log.warn("Utilising alternative FullTextSearch implementation {} for: {}", alternativeImplementation.getClass().getSimpleName(), text);
                alternativeImplementation
                        .searchByFullText(text)
                        .subscribe(subscriber);
            });
        }

        return result;
    }
}
