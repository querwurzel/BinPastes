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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

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
