package it.wylke.binpastes.paste.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

import static org.springframework.data.relational.core.query.Query.query;

@Component
class SimpleFullTextSupportImpl implements FullTextSearchSupport {

    private final R2dbcEntityTemplate entityTemplate;

    @Autowired
    public SimpleFullTextSupportImpl(final R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Flux<Paste> searchByFullText(final String text) {
        var criteria = Criteria
                .where("date_deleted").isNull()
                .and(Criteria
                        .where("date_of_expiry").isNull()
                        .or("date_of_expiry").greaterThan(LocalDateTime.now())
                )
                .and(Criteria
                        .where("title").like(text + '%')
                        .or("content").like(text + '%')
                );

        return entityTemplate
                .select(query(criteria), Paste.class);
    }
}
