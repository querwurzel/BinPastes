package it.wylke.binpastes.paste.domain;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PasteRepository extends R2dbcRepository<Paste, String>, ReactiveQueryByExampleExecutor<Paste> {

    Flux<Paste> findByIsDeletedFalseAndExpiryAfter(LocalDateTime expiryAfter);

    Mono<Paste> findByIsDeletedFalseAndExpiryAfterAndId(LocalDateTime expiryAfter, String id);

    @Modifying
    @Query("UPDATE pastes SET is_deleted = true WHERE is_deleted = false AND expiry < :date")
    Mono<Long> markExpiredPastesForDeletion(LocalDateTime expiryBefore);

    @Modifying
    Mono<Long> deleteByIsDeletedTrue();


    // TODO fix OR relation to circumvent expiry and deletion flag
    Flux<Paste> findByIsDeletedFalseAndExpiryAfterAndContentContainsIgnoreCaseOrTitleContainsIgnoreCase(LocalDateTime expiryAfter, String content, String title);

}
