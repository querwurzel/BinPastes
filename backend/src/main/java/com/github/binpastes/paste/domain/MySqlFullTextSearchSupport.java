package com.github.binpastes.paste.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.github.binpastes.paste.domain.Paste.PasteExposure;
import static com.github.binpastes.paste.domain.Paste.PasteSchema;

@Component
@Profile("mysql")
@Order(0)
class MySqlFullTextSearchSupport implements FullTextSearchSupport {

    private final R2dbcEntityTemplate entityTemplate;

    @Autowired
    public MySqlFullTextSearchSupport(final R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Flux<Paste> searchByFullText(final String text) {
        var query = String.format("SELECT * FROM %s WHERE %s = ? AND (%s IS NULL OR %s > ?) AND (MATCH(%s) AGAINST(?) OR (%s IS FALSE AND MATCH(%s) AGAINST(?)))",
                PasteSchema.TABLE_NAME,
                PasteSchema.EXPOSURE,
                PasteSchema.DATE_OF_EXPIRY,
                PasteSchema.DATE_OF_EXPIRY,
                PasteSchema.TITLE,
                PasteSchema.IS_ENCRYPTED,
                PasteSchema.CONTENT
        );

        var connectionFactory = entityTemplate.getDatabaseClient().getConnectionFactory();
        return Mono.from(connectionFactory.create())
                .flatMap(mySqlConnection -> Mono.from(mySqlConnection
                            .createStatement(query)
                            .bind(0, PasteExposure.PUBLIC)
                            .bind(1, LocalDateTime.now())
                            .bind(2, text)
                            .bind(3, text)
                            .execute()
                ))
                .flatMapMany(mySqlResult -> Flux.from(mySqlResult.map((row) -> {
                    var paste = new Paste();
                    paste.setId(row.get(PasteSchema.ID, String.class));
                    paste.setVersion(row.get(PasteSchema.VERSION, Long.class));

                    paste.setTitle(row.get(PasteSchema.TITLE) == null
                            ? null
                            : row.get(PasteSchema.TITLE, String.class));
                    paste.setContent(row.get(PasteSchema.CONTENT, String.class));
                    paste.setIsEncrypted(row.get(PasteSchema.IS_ENCRYPTED, Byte.class) == (byte)1);
                    paste.setExposure(PasteExposure.valueOf(row.get(PasteSchema.EXPOSURE, String.class)));

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
