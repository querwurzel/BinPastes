package com.github.binpastes.paste.api;

import com.github.binpastes.paste.domain.Paste;
import com.github.binpastes.paste.domain.Paste.PasteExposure;
import com.github.binpastes.paste.domain.PasteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofMillis;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
class UnlistedPastesIT {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private PasteRepository pasteRepository;

    @BeforeEach
    void setUp() {
        pasteRepository.deleteAll().block();
    }

    @Test
    @DisplayName("GET /{pasteId} - unlisted paste is cached")
    void getUnlistedPaste() {
        var unlistedPaste = givenUnlistedPaste();

        webClient.get()
                .uri("/api/v1/paste/" + unlistedPaste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("GET /{pasteId} - unlisted paste is cached only until expiry")
    void getExpiringUnlistedPaste() {
        var unlistedPaste = givenPaste(Paste.newInstance(
                "someTitle",
                "Lorem ipsum dolor sit amet",
                LocalDateTime.now().plusMinutes(5).minusSeconds(1),
                false,
                PasteExposure.UNLISTED,
                "1.1.1.1"
        ));

        webClient.get()
                .uri("/api/v1/paste/" + unlistedPaste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value(
                        HttpHeaders.CACHE_CONTROL,
                        (value) -> assertThat(Long.valueOf(value.replace("max-age=", "")))
                                .isLessThanOrEqualTo(TimeUnit.MINUTES.toSeconds(5)));
    }

    @Test
    @DisplayName("GET / - unlisted paste is never listed")
    void findAllPastes() {
        givenUnlistedPaste();

        assertThat(pasteRepository.count().block()).isOne();
        webClient.get()
                .uri("/api/v1/paste/")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("pastes", emptyList());
    }

    @Test
    @DisplayName("GET /search - unlisted paste cannot be searched for")
    void searchAllPastes() {
        givenUnlistedPaste();

        assertThat(pasteRepository.count().block()).isOne();
        webClient.get()
                .uri("/api/v1/paste/search?term={term}", "ipsum")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("pastes", emptyList());
    }

    @Test
    @DisplayName("POST / - unlisted paste is created using all options")
    void createOneTimePaste() {
        webClient.post()
                .uri("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just("""
                        {
                            "title": "someTitle",
                            "content": "someContent",
                            "exposure": "UNLISTED",
                            "isEncrypted": true,
                            "expiry": "THREE_MONTHS"
                        }
                        """), String.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().cacheControl(CacheControl.empty())
                .expectBody()
                .jsonPath("$.id").<String>value(id ->
                        assertThat(id).matches("^[a-zA-Z0-9]{40}$")
                )
                .jsonPath("$.dateCreated").<String>value(dateCreated ->
                        assertThat(parse(dateCreated)).isBefore(now())
                )
                .jsonPath("$.dateOfExpiry").<String>value(dateOfExpiry ->
                        assertThat(parse(dateOfExpiry))
                                .isBefore(now().plusMonths(3))
                                .isAfter(now().plusMonths(3).minusDays(1))
                )
                .json("""
                            {
                              "title": "someTitle",
                              "content": "someContent",
                              "sizeInBytes": 11,
                              "isPublic": false,
                              "isErasable": true,
                              "isEncrypted": true,
                              "isOneTime": false,
                              "lastViewed": null,
                              "views": 0
                            }
                """, false);
    }

    @Test
    @DisplayName("DELETE /{pasteId} - unlisted paste might always be deleted")
    void deleteUnlistedPaste() throws InterruptedException {
        var unlistedPaste = givenUnlistedPaste();

        webClient.delete()
                .uri("/api/v1/paste/" + unlistedPaste.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        waitAtMost(ofMillis(500)).untilAsserted(() -> webClient.get()
                .uri("/api/v1/paste/" + unlistedPaste.getId())
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noCache().getHeaderValue())
                .exchange()
                .expectStatus().isNotFound());
    }

    private Paste givenUnlistedPaste() {
        return givenPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        null,
                        false,
                        PasteExposure.UNLISTED,
                        "1.1.1.1"
                )
        );
    }

    private Paste givenPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
