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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
class OneTimePastesIT {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private PasteRepository pasteRepository;

    @BeforeEach
    void setUp() {
        pasteRepository.deleteAll().block();
    }

    @Test
    @DisplayName("GET /{pasteId} - one-time paste is never cached")
    void getOneTimePaste() {
        var oneTimePaste = givenOneTimePaste();

        webClient.get()
                .uri("/api/v1/paste/" + oneTimePaste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.noStore());
    }

    @Test
    @DisplayName("GET /{pasteId} - one-time paste is read-once")
    void getOneTimePasteTwice() {
        var oneTimePaste = givenOneTimePaste();

        webClient.get()
                .uri("/api/v1/paste/" + oneTimePaste.getId())
                .exchange()
                .expectStatus().isOk();

        webClient.get()
                .uri("/api/v1/paste/" + oneTimePaste.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET / - one-time paste is never listed")
    void findAllPastes() {
        givenOneTimePaste();

        assertThat(pasteRepository.count().block()).isOne();
        webClient.get()
                .uri("/api/v1/paste/")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("pastes", emptyList());
    }

    @Test
    @DisplayName("GET /search - one-time paste cannot be searched for")
    void searchAllPastes() {
        givenOneTimePaste();

        assertThat(pasteRepository.count().block()).isOne();
        webClient.get()
                .uri("/api/v1/paste/search?term={term}", "ipsum")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("pastes", emptyList());
    }

    @Test
    @DisplayName("POST / - one-time paste is created using all options")
    void createOneTimePaste() {
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
                              "isOneTime": true,
                              "lastViewed": null,
                              "views": 0
                            }
                """, false);
    }

    @Test
    @DisplayName("DELETE /{pasteId} - one-time paste might always be deleted before reading")
    void deleteOneTimePaste() {
        var oneTimePaste = givenOneTimePaste();

        webClient.delete()
                .uri("/api/v1/paste/" + oneTimePaste.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webClient.get()
                .uri("/api/v1/paste/" + oneTimePaste.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    private Paste givenOneTimePaste() {
        return givenPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        null,
                        false,
                        PasteExposure.ONCE,
                        "1.1.1.1"
                )
        );
    }

    private Paste givenPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
