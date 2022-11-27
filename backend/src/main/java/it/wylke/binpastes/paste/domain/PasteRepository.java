package it.wylke.binpastes.paste.domain;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PasteRepository extends R2dbcRepository<Paste, String>, ReactiveQueryByExampleExecutor<Paste>, PasteRepositoryCustom {

    /**
     * deleteAll permanently
     * @param timestamp time window of tolerance
     */
    @Modifying
    Mono<Long> deleteByDateDeletedBefore(LocalDateTime timestamp);

}
