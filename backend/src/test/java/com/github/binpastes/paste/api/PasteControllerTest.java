package com.github.binpastes.paste.api;

import com.github.binpastes.paste.application.PasteViewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@WebFluxTest
class PasteControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private PasteViewService pasteViewService;

    private static final String samplePasteId = "47116941fd49eda1b6c8abec63dbf8afe2fad088";

    @Test
    @DisplayName("GET /{pasteId} - 404 on unknown paste, no cacheControl header")
    void findUnknownPaste() {
        doReturn(Mono.empty()).when(pasteViewService).viewPaste(eq(samplePasteId), any());

        webClient.get()
                .uri("/api/v1/paste/{id}", samplePasteId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().cacheControl(CacheControl.empty());
    }

    @Test
    @DisplayName("GET / - empty list, no cacheControl header")
    void listPastes() {
        doReturn(Mono.empty()).when(pasteViewService).viewAllPastes();

        webClient.get()
                .uri("/api/v1/paste")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.empty())
                .expectBody().jsonPath("pastes", emptyList());

        webClient.get()
                .uri("/api/v1/paste/") // trailing slash
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.empty())
                .expectBody().jsonPath("pastes", emptyList());
    }

    @Test
    @DisplayName("GET /search - term parameter url-decoded, cache header set")
    void searchPastesDecodesParameter() {
        doReturn(Mono.empty()).when(pasteViewService).searchByFullText(anyString());

        webClient.get()
                .uri("/api/v1/paste/search?term={term}", "%3A-)")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES))
                .expectBody().jsonPath("pastes", emptyList());

        verify(pasteViewService).searchByFullText(eq(":-)"));
    }

    @Test
    @DisplayName("DELETE /{pasteId} - always return 202")
    void deletePaste() {
        webClient.delete()
                .uri("/api/v1/paste/{id}", samplePasteId)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody().isEmpty();
    }

    @ParameterizedTest
    @DisplayName("POST / - 400 on invalid input")
    @MethodSource("invalidPayloads")
    void createPaste(Mono<String> payload) {
        webClient.post()
                .uri("/api/v1/paste")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload, String.class)
                .exchange()
                .expectStatus().isBadRequest();

        webClient.post()
                .uri("/api/v1/paste/") // trailing slash
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload, String.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static Stream<Arguments> invalidPayloads() {
        return Stream.of(
                arguments(named("body is null", Mono.empty())),
                arguments(named("body is blank", Mono.just(""))),
                arguments(named("title blank", Mono.just("""
                        {
                            "title": "              ",
                            "content": "validContent",
                        }
                """))),
                arguments(named("title too long", Mono.just("""
                        {
                            "title": "%s",
                            "content": "validContent",
                        }
                """.formatted("X".repeat(256 + 1))))),
                arguments(named("content blank", Mono.just("""
                        {
                            "content": "            ",
                        }
                """))),
                arguments(named("content too short", Mono.just("""
                        {
                            "content": "1234",
                        }
                """))),
                arguments(named("content too long", Mono.just("""
                        {
                            "content": "%s",
                        }
                """.formatted("X".repeat(4096 + 1)))))
        );
    }
}
