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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
class PublicPasteIT {

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
                .uri("/api/v1/paste/{id}", paste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)));
    }

    @Test
    @DisplayName("GET /{pasteId} - public paste is cached only until expiry")
    void getExpiringPublicPaste() {
        var paste = givenPaste(Paste.newInstance(
                "someTitle",
                "Lorem ipsum dolor sit amet",
                false,
                PasteExposure.PUBLIC,
                LocalDateTime.now().plusMinutes(1).minusSeconds(3), // expiry before max-age
                "1.1.1.1"
        ));

        webClient.get()
                .uri("/api/v1/paste/{id}", paste.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value(
                        HttpHeaders.CACHE_CONTROL,
                        (value) -> assertThat(Long.valueOf(value.replace("max-age=", "")))
                                .isLessThanOrEqualTo(TimeUnit.MINUTES.toSeconds(1)));
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
        var now = LocalDateTime.now();
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
                        assertThat(parse(dateCreated)).isEqualToIgnoringSeconds(now)
                )
                .jsonPath("$.dateOfExpiry").<String>value(dateOfExpiry ->
                        assertThat(parse(dateOfExpiry)).isEqualToIgnoringSeconds(now.plusDays(1))
                ).json("""
                                    {
                                      "content": "validContent",
                                      "sizeInBytes": 12,
                                      "isPublic": true,
                                      "isErasable": true
                                    }
                        """);
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
                        assertThat(parse(dateCreated)).isEqualToIgnoringSeconds(LocalDateTime.now())
                )
                .json("""
                                    {
                                      "title": "someTitle",
                                      "content": "someContent",
                                      "sizeInBytes": 11,
                                      "isPublic": true,
                                      "isErasable": true,
                                      "isEncrypted": true,
                                      "isPermanent": true
                                    }
                        """);
    }

    @Test
    @DisplayName("DELETE /{pasteId} - public paste might be deleted")
    void deletePublicPaste() {
        var paste = givenPublicPaste();

        webClient.delete()
                .uri("/api/v1/paste/{id}", paste.getId())
                .header("X-Forwarded-For", ReflectionTestUtils.getField(paste, "remoteAddress").toString())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        waitAtMost(Duration.ofMillis(500)).untilAsserted(() -> webClient
                .get()
                .uri("/api/v1/paste/{id}", paste.getId())
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noCache().getHeaderValue())
                .exchange()
                .expectStatus().isNotFound());
    }

    private Paste givenPublicPaste() {
        return givenPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        false,
                        PasteExposure.PUBLIC,
                        null,
                        "someRemoteAddress"
                )
        );
    }

    private Paste givenPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
