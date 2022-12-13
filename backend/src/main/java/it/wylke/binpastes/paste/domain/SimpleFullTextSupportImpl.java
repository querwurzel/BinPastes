package it.wylke.binpastes.paste.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

import static it.wylke.binpastes.paste.domain.Paste.PasteSchema;
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
                .where(PasteSchema.DATE_DELETED).isNull()
                .and(Criteria
                        .where(PasteSchema.DATE_OF_EXPIRY).isNull()
                        .or(PasteSchema.DATE_OF_EXPIRY).greaterThan(LocalDateTime.now())
                )
                .and(Criteria
                        .where(PasteSchema.TITLE).like(text + '%')
                        .or(PasteSchema.CONTENT).like(text + '%')
                );

        return entityTemplate
                .select(query(criteria), Paste.class);
    }
}
