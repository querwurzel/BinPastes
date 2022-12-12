package it.wylke.binpastes.paste.domain;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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
                .where("id").is(id)
                .and("date_deleted").isNull()
                .and(
                        Criteria
                                .where("date_of_expiry").isNull()
                                .or("date_of_expiry").greaterThan(LocalDateTime.now())
                );

        return entityTemplate
                .selectOne(query(criteria), Paste.class);
    }

    public Flux<Paste> findAllLegit() {
        var criteria = Criteria
                .where("date_deleted").isNull()
                .and(
                        Criteria
                                .where("date_of_expiry").isNull()
                                .or("date_of_expiry").greaterThan(LocalDateTime.now())
                );

        return entityTemplate
                .select(query(criteria), Paste.class);
    }

    @Override
    public Mono<Long> markExpiredPastesForDeletion(final LocalDateTime expiryBefore) {
        var criteria = Criteria
                .where("date_deleted").isNull()
                .and("date_of_expiry").lessThan(expiryBefore);

        var update = Update.update("date_deleted", LocalDateTime.now());

        return entityTemplate.update(query(criteria), update, Paste.class);
    }

    @Override
    public Flux<Paste> searchAllLegitByFullText(String text) {
        return fullTextSearchSupport.searchByFullText(text);
    }

}
