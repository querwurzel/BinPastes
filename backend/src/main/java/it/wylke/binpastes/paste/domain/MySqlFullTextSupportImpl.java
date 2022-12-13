package it.wylke.binpastes.paste.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static it.wylke.binpastes.paste.domain.Paste.PasteSchema;

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

        var query = String.format("""
                    SELECT * FROM %s
                    WHERE %s IS NULL
                    AND (%s IS NULL OR %s > ?expiryAfter)
                    AND MATCH(%s, %s) AGAINST(?text IN BOOLEAN MODE)""".strip(),
                PasteSchema.TABLE_NAME,
                PasteSchema.DATE_DELETED,
                PasteSchema.DATE_OF_EXPIRY,
                PasteSchema.DATE_OF_EXPIRY,
                PasteSchema.TITLE,
                PasteSchema.CONTENT
        );

        return Mono.from(connectionFactory.create())
            .flatMap(mySqlConnection -> Mono.from(mySqlConnection
                        .createStatement(query)
                        .bind("expiryAfter", LocalDateTime.now())
                        .bind("text", text + '*')
                        .execute()
            ))
            .flatMapMany(mySqlResult -> Flux.from(mySqlResult.map((row, rowMetadata) -> {
                var paste = new Paste();
                paste.setId(row.get(PasteSchema.ID).toString());

                paste.setTitle(row.get(PasteSchema.TITLE) == null
                        ? null
                        : row.get(PasteSchema.TITLE).toString());
                paste.setContent(row.get(PasteSchema.CONTENT).toString());
                paste.setIsEncrypted(Boolean.parseBoolean(row.get(PasteSchema.IS_ENCRYPTED).toString()));

                paste.setDateCreated(LocalDateTime.parse(row.get(PasteSchema.DATE_CREATED).toString()));
                paste.setDateOfExpiry(row.get(PasteSchema.DATE_OF_EXPIRY) == null
                        ? null
                        : LocalDateTime.parse(row.get(PasteSchema.DATE_OF_EXPIRY).toString()));
                paste.setDateDeleted(row.get(PasteSchema.DATE_DELETED) == null
                        ? null
                        : LocalDateTime.parse(row.get(PasteSchema.DATE_DELETED).toString()));

                paste.setRemoteAddress(row.get(PasteSchema.TITLE) == null
                        ? null
                        : row.get(PasteSchema.TITLE).toString());
                return paste;
            })));
    }
}