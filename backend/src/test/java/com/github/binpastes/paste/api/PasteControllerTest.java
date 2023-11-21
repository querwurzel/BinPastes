package com.github.binpastes.paste.api;

import com.github.binpastes.paste.domain.PasteService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@WebFluxTest
class PasteControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private PasteService pasteService;

    private static final String somePasteId = "4711471147114711471147114711471147114711";

    @Test
    @DisplayName("GET /{pasteId} - 404 on unknown paste, no caching")
    void findUnknownPaste() {
        doReturn(Mono.empty()).when(pasteService).find(anyString());

        webClient.get()
                .uri("/api/v1/paste/" + somePasteId)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().cacheControl(CacheControl.empty());
    }

    @Test
    @DisplayName("GET / - empty list on no results")
    void listPastes() {
        doReturn(Flux.empty()).when(pasteService).findAll();

        webClient.get()
                .uri("/api/v1/paste")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("pastes", emptyList());
    }

    @Test
    @DisplayName("GET /search - set cache header")
    void searchPastes() {
        doReturn(Flux.empty()).when(pasteService).findByFullText(anyString());

        webClient.get()
                .uri("/api/v1/paste/search?term={term}", "foobar")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .expectBody().jsonPath("pastes", emptyList());
    }

    @Test
    @DisplayName("DELETE /{pasteId} - always return 204")
    void deletePaste() {
        webClient.delete()
                .uri("/api/v1/paste/" + somePasteId)
                .exchange()
                .expectStatus().isNoContent()
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
    }

    private static Stream<Arguments> invalidPayloads() {
        return Stream.of(
                arguments(named("body is null", Mono.empty())),
                arguments(named("body blank", Mono.just(""))),
                arguments(named("title too long", Mono.just("""
                        {
                            "title": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                            "content": "validContent"
                        }
                """))),
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
                arguments(named("content too long", Mono.just("{\"content\": \"" + "X".repeat(4096 + 1) + "\"}")))
        );
    }
}
