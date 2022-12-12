package it.wylke.binpastes.paste.domain;

import it.wylke.binpastes.paste.domain.PasteRepositoryCustom.FullTextSearchSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Profile("mysql")
@Component
public class PasteRepositoryFullTextMySQLImpl implements FullTextSearchSupport {

    private static final Logger log = LoggerFactory.getLogger(FullTextSearchSupport.class);

    private final R2dbcEntityTemplate entityTemplate;

    @Autowired
    public PasteRepositoryFullTextMySQLImpl(final R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Flux<Paste> searchByFullText(final String text) {
        // TODO
        //SELECT * FROM pastes WHERE MATCH(title, content) AGAINST('asda' IN BOOLEAN MODE);

        return entityTemplate
                .getDatabaseClient()
                // TODO handle SQL injection
                .sql("SELECT * FROM pastes WHERE date_deleted IS NULL AND (date_of_expiry IS NULL OR date_of_expiry > CURRENT_TIMESTAMP) AND MATCH(title, content) AGAINST('" + text + "*' IN BOOLEAN MODE)")
                .fetch()
                .all()
                .doOnNext(stringObjectMap -> log.error(stringObjectMap.toString()))
                .map(row -> {
                    Paste paste = new Paste();
                    paste.setId(row.get("id").toString());
                    paste.setTitle(row.get("title") == null ? null : row.get("title").toString());
                    paste.setContent(row.get("content").toString());
                    paste.setDateCreated(LocalDateTime.parse(row.get("date_created").toString()));
                    return paste;
                });
    }
}
