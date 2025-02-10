package com.github.binpastes.paste;

import com.github.binpastes.paste.domain.Paste;
import com.github.binpastes.paste.domain.Paste.PasteExposure;
import com.github.binpastes.paste.domain.PasteRepository;
import org.assertj.core.data.TemporalUnitLessThanOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.time.Duration.ofMillis;
import static java.time.LocalDateTime.parse;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
class OneTimePasteIT {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private PasteRepository pasteRepository;

    @BeforeEach
    void setUp() {
        pasteRepository.deleteAll().block();
    }

    @Test
    @DisplayName("GET /{pasteId} - one-time paste hides title & content and discourages caching")
    void getOneTimePasteHidesContent() {
        var oneTimePaste = givenOneTimePaste();

        webClient.get()
            .uri("/api/v1/paste/{id}", oneTimePaste.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().cacheControl(CacheControl.noStore())
            .expectBody()
            .jsonPath("$.content").doesNotExist()
            .jsonPath("$.title").doesNotExist()
            .jsonPath("$.sizeInBytes").doesNotExist()
            .json("""
                        {
                            "isErasable": true,
                            "isOneTime": true,
                            "isPermanent" :true
                        }
                """);
    }

    @Test
    @DisplayName("POST /{pasteId} - one-time paste is only read once even under load")
    void viewOneTimePasteConcurrently() {
        var oneTimePaste = givenOneTimePaste();
        var okCount = new AtomicInteger();
        var notFoundCount = new AtomicInteger();

        final Runnable request = () -> {
            try {
                webClient.post()
                    .uri("/api/v1/paste/{id}", oneTimePaste.getId())
                    .exchange()
                    .expectStatus().isOk();

                okCount.incrementAndGet();
            } catch (AssertionError ex) {
                if (ex.getMessage().contains("404")) {
                    notFoundCount.incrementAndGet();
                }
            }
        };

        Flux.fromStream(Stream.generate(() -> request))
            .take(50)
            .parallel()
            .doOnNext(Runnable::run)
            .runOn(Schedulers.boundedElastic())
            .subscribe();

        assertThat(okCount.get()).isOne();
        assertThat(notFoundCount.get()).isEqualTo(50 - 1);
    }

    @Test
    @DisplayName("GET / - one-time paste is never listed")
    void findAllPastes() {
        givenOneTimePaste();

        assertThat(pasteRepository.count().block()).isOne();

        webClient.get()
            .uri("/api/v1/paste")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.pastes").isEmpty();
    }

    @Test
    @DisplayName("GET /search - one-time paste cannot be searched for")
    void searchAllPastes() {
        var oneTimePaste = givenOneTimePaste();

        assertThat(pasteRepository.count().block()).isOne();

        webClient.get()
            .uri("/api/v1/paste/search?term={term}", oneTimePaste.getTitle().orElseThrow())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.pastes").isEmpty();
    }

    @Test
    @DisplayName("POST / - one-time paste is created and hides content")
    void createOneTimePaste() {
        var now = LocalDateTime.now();
        webClient.post()
            .uri("/api/v1/paste")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just("""
                {
                    "title": "someTitle",
                    "content": "someContent",
                    "exposure": "ONCE",
                    "isEncrypted": true,
                    "expiry": "THREE_MONTHS"
                }
                """), String.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().cacheControl(CacheControl.empty())
            .expectBody()
            .jsonPath("$.id").<String>value(id ->
                assertThat(id).matches("^[a-z0-9]{40}$")
            )
            .jsonPath("$.dateCreated").<String>value(dateCreated ->
                assertThat(parse(dateCreated)).isCloseTo(now, new TemporalUnitLessThanOffset(3, ChronoUnit.SECONDS))
            )
            .jsonPath("$.dateOfExpiry").<String>value(dateOfExpiry ->
                assertThat(parse(dateOfExpiry)).isCloseTo(now.plusMonths(3), new TemporalUnitLessThanOffset(3, ChronoUnit.SECONDS))
            )
            .jsonPath("$.content").doesNotExist()
            .jsonPath("$.title").doesNotExist()
            .jsonPath("$.sizeInBytes").doesNotExist()
            .json("""
                {
                    "isErasable": true,
                    "isEncrypted": true,
                    "isOneTime": true
                }
                """);
    }

    @Test
    @DisplayName("DELETE /{pasteId} - one-time paste might always be deleted before read")
    void deleteOneTimePaste() {
        var oneTimePaste = givenOneTimePaste();

        webClient.delete()
            .uri("/api/v1/paste/{id}", oneTimePaste.getId())
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();

        waitAtMost(ofMillis(500)).untilAsserted(() -> webClient
            .get()
            .uri("/api/v1/paste/{id}", oneTimePaste.getId())
            .exchange()
            .expectStatus().isNotFound()
        );
    }

    private Paste givenOneTimePaste() {
        return persistedPaste(
            Paste.newInstance(
                "someTitle",
                "Lorem ipsum dolor sit amet",
                false,
                PasteExposure.ONCE,
                null,
                "1.1.1.1"
            )
        );
    }

    private Paste persistedPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
