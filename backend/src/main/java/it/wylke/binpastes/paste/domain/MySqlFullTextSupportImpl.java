package it.wylke.binpastes.paste.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static it.wylke.binpastes.paste.domain.Paste.PasteExposure;
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
        /*
         * Seems not to be supported by dev.miku:r2dbc-mysql
         * java.lang.IllegalArgumentException: Cannot encode value of type 'class io.r2dbc.spi.Parameters$InParameter'
         **/
/*
        entityTemplate
                .getDatabaseClient()
                .sql("SELECT * FROM pastes WHERE (date_of_expiry IS NULL OR date_of_expiry > CURRENT_TIMESTAMP) AND MATCH(title, content) AGAINST(?text IN BOOLEAN MODE)")
                .bind("text", text + '*'))
*/

        var connectionFactory = entityTemplate.getDatabaseClient().getConnectionFactory();

        var query = String.format("SELECT * FROM %s WHERE %s = ? AND (%s IS NULL OR %s > ?) AND (MATCH(%s) AGAINST(? IN BOOLEAN MODE) OR (MATCH(%s) AGAINST(? IN BOOLEAN MODE) AND %s IS FALSE)) ORDER BY %s DESC",
                PasteSchema.TABLE_NAME,
                PasteSchema.EXPOSURE,
                PasteSchema.DATE_OF_EXPIRY,
                PasteSchema.DATE_OF_EXPIRY,
                PasteSchema.TITLE,
                PasteSchema.CONTENT,
                PasteSchema.IS_ENCRYPTED,
                PasteSchema.DATE_CREATED
        );

        return Mono.from(connectionFactory.create())
                .flatMap(mySqlConnection -> Mono.from(mySqlConnection
                        .createStatement(query)
                        .bind(0, PasteExposure.PUBLIC.name())
                        .bind(1, LocalDateTime.now())
                        .bind(2, text + '*')
                        .bind(3, text + '*')
                        .execute()
                ))
                .flatMapMany(mySqlResult -> Flux.from(mySqlResult.map((row, rowMetadata) -> {
                    var paste = new Paste();
                    paste.setId(row.get(PasteSchema.ID, String.class));
                    paste.setVersion(row.get(PasteSchema.VERSION, Long.class));

                    paste.setTitle(row.get(PasteSchema.TITLE) == null
                            ? null
                            : row.get(PasteSchema.TITLE, String.class));
                    paste.setContent(row.get(PasteSchema.CONTENT, String.class));
                    paste.setIsEncrypted(row.get(PasteSchema.IS_ENCRYPTED, Byte.class) == (byte)1);
                    paste.setExposure(row.get(PasteSchema.EXPOSURE, PasteExposure.class));

                    paste.setDateCreated(row.get(PasteSchema.DATE_CREATED, LocalDateTime.class));
                    paste.setDateOfExpiry(row.get(PasteSchema.DATE_OF_EXPIRY) == null
                            ? null
                            : row.get(PasteSchema.DATE_OF_EXPIRY, LocalDateTime.class));
                    paste.setDateDeleted(row.get(PasteSchema.DATE_DELETED) == null
                            ? null
                            : row.get(PasteSchema.DATE_DELETED, LocalDateTime.class));

                    paste.setRemoteAddress(row.get(PasteSchema.REMOTE_ADDRESS) == null
                            ? null
                            : row.get(PasteSchema.REMOTE_ADDRESS, String.class));
                    return paste;
                })));
    }
}
