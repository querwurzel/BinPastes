package it.wylke.binpastes.paste.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Primary
@Profile("mysql")
@Component
class MySqlFullTextSupportImpl implements FullTextSearchSupport {

    private final R2dbcEntityTemplate entityTemplate;

    @Autowired
    public MySqlFullTextSupportImpl(final R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Flux<Paste> searchByFullText(final String text) {
        /**
         * Seems not to be supported by dev.miku:r2dbc-mysql
         * java.lang.IllegalArgumentException: Cannot encode value of type 'class io.r2dbc.spi.Parameters$InParameter'
         **/
/*
        entityTemplate
                .getDatabaseClient()
                .sql("SELECT * FROM pastes WHERE date_deleted IS NULL AND (date_of_expiry IS NULL OR date_of_expiry > CURRENT_TIMESTAMP) AND MATCH(title, content) AGAINST(?text IN BOOLEAN MODE)")
                .bind("text", Parameters.inOut(R2dbcType.VARCHAR, text + '*'))
*/

        var connectionFactory = entityTemplate.getDatabaseClient().getConnectionFactory();

        return Mono.from(connectionFactory.create())
            .flatMap(mySqlConnection -> Mono.from(mySqlConnection
                        .createStatement("SELECT * FROM pastes WHERE date_deleted IS NULL AND (date_of_expiry IS NULL OR date_of_expiry > ?expiryAfter) AND MATCH(title, content) AGAINST(?text IN BOOLEAN MODE)")
                        .bind("expiryAfter", LocalDateTime.now())
                        .bind("text", text + '*')
                        .execute()
            ))
            .flatMapMany(mySqlResult -> Flux.from(mySqlResult.map((row, rowMetadata) -> {
                var paste = new Paste();
                paste.setId(row.get("id").toString());

                paste.setTitle(row.get("title") == null
                        ? null
                        : row.get("title").toString());
                paste.setContent(row.get("content").toString());

                paste.setDateCreated(LocalDateTime.parse(row.get("date_created").toString()));
                paste.setDateOfExpiry(row.get("date_of_expiry") == null
                        ? null
                        : LocalDateTime.parse(row.get("date_of_expiry").toString()));
                paste.setDateDeleted(row.get("date_deleted") == null
                        ? null
                        : LocalDateTime.parse(row.get("date_deleted").toString()));

                paste.setRemoteIp(row.get("title") == null
                        ? null
                        : row.get("title").toString());
                return paste;
            })));
    }
}
