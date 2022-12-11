package it.wylke.binpastes.paste.domain;

import it.wylke.binpastes.paste.domain.PasteRepositoryCustom.FullTextSearchSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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


        return Flux.empty();
    }
}
