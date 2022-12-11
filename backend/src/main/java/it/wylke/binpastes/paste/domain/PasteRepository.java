package it.wylke.binpastes.paste.domain;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PasteRepository extends R2dbcRepository<Paste, String>, ReactiveQueryByExampleExecutor<Paste> {

    default Mono<Paste> find(String id) {
        return this.findByIdAndDateDeletedNullAndDateOfExpiryNullOrDateOfExpiryAfter(id, LocalDateTime.now());
    }

    /**
     * @deprecated {@link #find(String)}
     */
    @Deprecated
    Mono<Paste> findByIdAndDateDeletedNullAndDateOfExpiryNullOrDateOfExpiryAfter(String id, LocalDateTime expiryAfter);

    default Flux<Paste> find() {
        return this.findByDateDeletedNullAndDateOfExpiryNullOrDateOfExpiryAfter(LocalDateTime.now());
    }

    /**
     * @deprecated {@link #find()}
     */
    @Deprecated
    Flux<Paste> findByDateDeletedNullAndDateOfExpiryNullOrDateOfExpiryAfter(LocalDateTime expiryAfter);

    @Query("SELECT * FROM " + Paste.TABLE_NAME + " WHERE date_deleted IS NULL AND (date_of_expiry IS NULL OR date_of_expiry > current_timestamp()) AND (content LIKE '%'||:text||'%' OR title LIKE '%'||:text||'%')")
    Flux<Paste> findByFullText(@Param("text") String text);

    /**
     * expireAll
     * @param expiryBefore LocalDateTime.now()
     */
    @Modifying
    @Query("UPDATE " + Paste.TABLE_NAME + " SET date_deleted = current_timestamp() WHERE date_deleted IS NULL AND date_of_expiry < :expiryBefore")
    Mono<Long> markExpiredPastesForDeletion(@Param("expiryBefore") LocalDateTime expiryBefore);

    /**
     * deleteAll permanently
     * @param timestamp time window of tolerance
     */
    @Modifying
    Mono<Long> deleteByDateDeletedBefore(LocalDateTime timestamp);

}
