package com.github.binpastes.paste.api;

import com.github.binpastes.paste.domain.Paste;
import com.github.binpastes.paste.domain.PasteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
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
    void getOneTimePaste() {
        var unlistedPaste = givenUnlistedPaste();

        webClient.get()
                .uri("/api/v1/paste/" + unlistedPaste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("GET /{pasteId} - unlisted paste is cached until expiry")
    void getExpiringOneTimePaste() {
        var unlistedPaste = givenUnlistedPaste(Paste.newInstance(
                "someTitle",
                "Lorem ipsum dolor sit amet",
                LocalDateTime.now().plusMinutes(5).minusSeconds(1),
                false,
                Paste.PasteExposure.UNLISTED,
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
    @DisplayName("DELETE /{pasteId} - unlisted paste might always be deleted")
    void deleteOneTimePaste() {
        var unlistedPaste = givenUnlistedPaste();

        webClient.get()
                .uri("/api/v1/paste/" + unlistedPaste.getId())
                .exchange()
                .expectStatus().isOk();

        webClient.delete()
                .uri("/api/v1/paste/" + unlistedPaste.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webClient.get()
                .uri("/api/v1/paste/" + unlistedPaste.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    private Paste givenUnlistedPaste() {
        return givenUnlistedPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        null,
                        false,
                        Paste.PasteExposure.UNLISTED,
                        "1.1.1.1"
                )
        );
    }

    private Paste givenUnlistedPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
