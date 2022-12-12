package it.wylke.binpastes.paste.domain;

import it.wylke.binpastes.paste.domain.PasteRepositoryCustom.FullTextSearchSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Profile("dev")
@Component
class PasteRepositoryFullTextH2Impl implements FullTextSearchSupport {

    private static final Logger log = LoggerFactory.getLogger(FullTextSearchSupport.class);

    private final R2dbcEntityTemplate entityTemplate;

    @Autowired
    public PasteRepositoryFullTextH2Impl(final R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Flux<Paste> searchByFullText(final String text) {
        // SELECT * FROM FTL_SEARCH('loru', 0, 0);

        DatabaseClient.GenericExecuteSpec sql = entityTemplate
                .getDatabaseClient()
                .sql("SELECT * FROM FTL_SEARCH('" + text + "*', 0, 0)");

        sql.fetch()
                .all()
                .doOnNext(stringObjectMap -> log.error(stringObjectMap.toString()))
                .subscribe();

        return Flux.empty();
    }
}
