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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
class PublicPastesIT {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private PasteRepository pasteRepository;

    @BeforeEach
    void setUp() {
        pasteRepository.deleteAll().block();
    }

    @Test
    @DisplayName("GET /{pasteId} - public paste is cached")
    void getOneTimePaste() {
        var paste = givenPublicPaste();

        webClient.get()
                .uri("/api/v1/paste/" + paste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("GET /{pasteId} - public paste is cached until expiry")
    void getExpiringOneTimePaste() {
        var paste = givenPublicPaste(Paste.newInstance(
                "someTitle",
                "Lorem ipsum dolor sit amet",
                LocalDateTime.now().plusMinutes(5).minusSeconds(1),
                false,
                Paste.PasteExposure.PUBLIC,
                "1.1.1.1"
        ));

        webClient.get()
                .uri("/api/v1/paste/" + paste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value(
                        HttpHeaders.CACHE_CONTROL,
                        (value) -> assertThat(Long.valueOf(value.replace("max-age=", "")))
                                .isLessThanOrEqualTo(TimeUnit.MINUTES.toSeconds(5)));
    }

    @Test
    @DisplayName("GET / - public paste is listed")
    void findAllPastes() {
        givenPublicPaste();

        assertThat(pasteRepository.count().block()).isOne();
        webClient.get()
                .uri("/api/v1/paste/")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.pastes.length()", 1);
    }

    @Test
    @DisplayName("DELETE /{pasteId} - public paste might be deleted")
    void deleteOneTimePasteBySameAuthor() {
        var paste = givenPublicPaste();

        webClient.get()
                .uri("/api/v1/paste/" + paste.getId())
                .exchange()
                .expectStatus().isOk();

        webClient.delete()
                .uri("/api/v1/paste/" + paste.getId())
                .header("X-Forwarded-For", "someAuthor")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webClient.get()
                .uri("/api/v1/paste/" + paste.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    private Paste givenPublicPaste() {
        return givenPublicPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        null,
                        false,
                        Paste.PasteExposure.PUBLIC,
                        "someAuthor"
                )
        );
    }

    private Paste givenPublicPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
