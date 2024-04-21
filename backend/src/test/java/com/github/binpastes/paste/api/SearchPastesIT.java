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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
class SearchPastesIT {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private PasteRepository pasteRepository;

    @BeforeEach
    void setUp() {
        pasteRepository.deleteAll().block();
    }

    @Test
    @DisplayName("GET /search - paste is found if text matches")
    void findMatch() {
        var paste = givenPublicPaste();

        assertThat(pasteRepository.count().block()).isOne();
        webClient.get()
                .uri("/api/v1/paste/search?term={term}", paste.getTitle())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)))
                .expectBody().jsonPath("$.pastes.length()", 1);
    }

    @Test
    @DisplayName("GET /search - paste is not found if no match")
    void findNothing() {
        givenPublicPaste();

        assertThat(pasteRepository.count().block()).isOne();
        webClient.get()
                .uri("/api/v1/paste/search?term={term}", "foobar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)))
                .expectBody().jsonPath("$.pastes.length()", 0);
    }

    private Paste givenPublicPaste() {
        return givenPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        null,
                        false,
                        Paste.PasteExposure.PUBLIC,
                        "someRemoteAddress"
                )
        );
    }

    private Paste givenPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
