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

import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
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
    void getPublicPaste() {
        var paste = givenPublicPaste();

        webClient.get()
                .uri("/api/v1/paste/" + paste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("GET /{pasteId} - public paste is cached only until expiry")
    void getExpiringPublicPaste() {
        var paste = givenPaste(Paste.newInstance(
                "someTitle",
                "Lorem ipsum dolor sit amet",
                now().plusMinutes(5).minusSeconds(1), // under 5min remaining
                false,
                PasteExposure.PUBLIC,
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
    @DisplayName("POST / - public paste is created with minimal input")
    void createPublicPasteMinimalRequest() {
            webClient.post()
                .uri("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just("""
                        {
                            "content": "validContent"
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
                            assertThat(parse(dateOfExpiry)).isBefore(now().plusDays(1))
                    ).json("""
                            {
                              "title": null,
                              "content": "validContent",
                              "sizeInBytes": 12,
                              "isPublic": true,
                              "isErasable": true,
                              "isEncrypted": false,
                              "isOneTime": false,
                              "lastViewed": null,
                              "views": 0
                            }
                """, false);
    }

    @Test
    @DisplayName("POST / - public paste is created using all options")
    void createPublicPasteMaximalRequest() {
        webClient.post()
                .uri("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just("""
                        {
                            "title": "  someTitle  ",
                            "content": "someContent",
                            "exposure": "PUBLIC",
                            "isEncrypted": true,
                            "expiry": "NEVER"
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
                .json("""
                            {
                              "title": "someTitle",
                              "content": "someContent",
                              "sizeInBytes": 11,
                              "isPublic": true,
                              "isErasable": true,
                              "isEncrypted": true,
                              "isOneTime": false,
                              "dateOfExpiry": null,
                              "lastViewed": null,
                              "views": 0
                            }
                """, false);
    }

    @Test
    @DisplayName("DELETE /{pasteId} - public paste might be deleted")
    void deletePublicPaste() {
        var paste = givenPublicPaste();

        webClient.delete()
                .uri("/api/v1/paste/" + paste.getId())
                .header("X-Forwarded-For", "someAuthor")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webClient.get()
                .uri("/api/v1/paste/" + paste.getId())
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noCache().getHeaderValue())
                .exchange()
                .expectStatus().isNotFound();
    }

    private Paste givenPublicPaste() {
        return givenPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        null,
                        false,
                        PasteExposure.PUBLIC,
                        "someAuthor"
                )
        );
    }

    private Paste givenPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
