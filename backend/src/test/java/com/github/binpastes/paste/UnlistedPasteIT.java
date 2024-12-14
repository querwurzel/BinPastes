package com.github.binpastes.paste;

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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofMillis;
import static java.time.LocalDateTime.parse;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
class UnlistedPasteIT {

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
                .uri("/api/v1/paste/{id}", unlistedPaste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)));
    }

    @Test
    @DisplayName("GET /{pasteId} - unlisted paste is cached only until expiry")
    void getExpiringUnlistedPaste() {
        var unlistedPaste = givenPaste(Paste.newInstance(
                "someTitle",
                "Lorem ipsum dolor sit amet",
                false,
                PasteExposure.UNLISTED,
                LocalDateTime.now().plusMinutes(1).minusSeconds(3), // expiry before max-age
                "1.1.1.1"
        ));

        webClient.get()
                .uri("/api/v1/paste/{id}", unlistedPaste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value(
                        HttpHeaders.CACHE_CONTROL,
                        (value) -> assertThat(Long.valueOf(value.replace("max-age=", "")))
                                .isLessThanOrEqualTo(TimeUnit.MINUTES.toSeconds(1)));
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
        var paste = givenUnlistedPaste();

        assertThat(pasteRepository.count().block()).isOne();
        webClient.get()
                .uri("/api/v1/paste/search?term={term}", paste.getTitle())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("pastes", emptyList());
    }

    @Test
    @DisplayName("POST / - unlisted paste is created using all options")
    void createOneTimePaste() {
        var now = LocalDateTime.now();
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
                        assertThat(parse(dateCreated)).isEqualToIgnoringSeconds(now)
                )
                .jsonPath("$.dateOfExpiry").<String>value(dateOfExpiry ->
                        assertThat(parse(dateOfExpiry)).isEqualToIgnoringSeconds(now.plusMonths(3))
                )
                .json("""
                            {
                              "title": "someTitle",
                              "content": "someContent",
                              "sizeInBytes": 11,
                              "isErasable": true,
                              "isEncrypted": true
                            }
                """);
    }

    @Test
    @DisplayName("DELETE /{pasteId} - unlisted paste might always be deleted")
    void deleteUnlistedPaste() {
        var unlistedPaste = givenUnlistedPaste();

        webClient.delete()
                .uri("/api/v1/paste/{id}", unlistedPaste.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        waitAtMost(ofMillis(500)).untilAsserted(() -> webClient
                .get()
                .uri("/api/v1/paste/{id}", unlistedPaste.getId())
                .exchange()
                .expectStatus().isNotFound());
    }

    private Paste givenUnlistedPaste() {
        return givenPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        false,
                        PasteExposure.UNLISTED,
                        null,
                        "1.1.1.1"
                )
        );
    }

    private Paste givenPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
