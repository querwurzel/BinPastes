package com.github.binpastes.paste;

import com.github.binpastes.paste.application.tracking.MessagingClient;
import com.github.binpastes.paste.application.tracking.TrackingService;
import com.github.binpastes.paste.domain.Paste;
import com.github.binpastes.paste.domain.Paste.PasteExposure;
import com.github.binpastes.paste.domain.PasteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.timeout;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest(properties = {
        "logging.level.com.github.binpastes.paste.application.tracking=INFO"
})
@AutoConfigureWebTestClient
@DirtiesContext
class TrackingPasteIT {

    @Autowired
    private WebTestClient webClient;
    @Autowired
    private TrackingService trackingService;
    @SpyBean
    private PasteRepository pasteRepository;
    @SpyBean
    private MessagingClient messagingClient;

    @BeforeEach
    void setUp() {
        pasteRepository.deleteAll().block();
    }

    @Test
    @DisplayName("tracking - paste is tracked on direct access eventually")
    void trackPaste() {
        var paste = givenPublicPaste();

        assertThat(paste.getViews()).isZero();

        webClient.get()
                .uri("/api/v1/paste/{id}", paste.getId())
                .exchange()
                .expectStatus().isOk();

        waitAtMost(Duration.ofSeconds(1)).until(
                () -> pasteRepository.findById(paste.getId()).block().getViews(),
                equalTo(1L)
        );
    }

    @Test
    @DisplayName("tracking - concurrent tracking events result in exact view count")
    void trackConcurrentPasteViews() {
        var intialPaste = givenPublicPaste();
        var random = new Random();
        var concurrency = 500;

        Flux.fromStream(Stream.generate(intialPaste::getId))
                .take(concurrency)
                .doOnNext(trackingService::trackView)
                // simulate a concurrent update
                .doOnNext(id -> pasteRepository.findById(id)
                        .doOnNext(paste -> setField(paste, "remoteAddress", String.valueOf(random.nextInt())))
                        .flatMap(paste -> pasteRepository.save(paste))
                        .retry()
                        .subscribe())
                .subscribeOn(Schedulers.parallel())
                .subscribe();

        waitAtMost(Duration.ofMinutes(1)).pollInterval(Duration.ofSeconds(1)).until(
                () -> pasteRepository.findById(intialPaste.getId()).block().getViews(),
                equalTo((long) concurrency)
        );

        Mockito
                .verify(pasteRepository, atLeast((int) (concurrency * 1.5) /* experienced minimum contention */))
                .save(eq(intialPaste));
    }

    @Test
    @DisplayName("tracking - unknown paste is not tracked indefinitely")
    void trackUnknownPaste() {
        trackingService.trackView("4711");

        Mockito.verify(messagingClient).sendMessage(eq("4711"), any());
        Mockito.verify(messagingClient, timeout(TimeUnit.SECONDS.toMillis(1))).receiveMessage();
    }

    private Paste givenPublicPaste() {
        return givenPaste(
                Paste.newInstance(
                        "someTitle",
                        "Lorem ipsum dolor sit amet",
                        null,
                        false,
                        PasteExposure.PUBLIC,
                        "someRemoteAddress"
                )
        );
    }

    private Paste givenPaste(Paste paste) {
        return pasteRepository.save(paste).block();
    }
}
