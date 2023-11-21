package com.github.binpastes.paste.api;

import com.github.binpastes.paste.business.tracking.MessagingClient;
import com.github.binpastes.paste.business.tracking.TrackingService;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext
class TrackingIT {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private PasteRepository pasteRepository;

    @Autowired
    private TrackingService trackingService;

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
                .uri("/api/v1/paste/" + paste.getId())
                .exchange()
                .expectStatus().isOk();

        waitAtMost(2, TimeUnit.SECONDS).until(() -> {
            var latestPaste = pasteRepository.findById(paste.getId()).block();
            return latestPaste.getViews() == 1;
        });
    }

    @Test
    @DisplayName("tracking - deal with concurrent tracking events")
    void trackConcurrentPasteViews() {
        var intialPaste = givenPublicPaste();

        Flux.fromStream(Stream.generate(intialPaste::getId))
            .take(1000)
            .doOnNext(trackingService::trackView)
            // enforce a concurrent update
            .doOnNext(id -> pasteRepository.findById(id)
                    .doOnNext(paste -> setField(paste, "remoteAddress", "concurrentUpdate"))
                    .flatMap(paste -> pasteRepository.save(paste))
                    .onErrorComplete() // ignore errors in test
                    .subscribe())
            .subscribeOn(Schedulers.parallel())
            .subscribe();

        await().atMost(Duration.ofMinutes(1)).pollInterval(500, TimeUnit.MILLISECONDS).until(
                () -> pasteRepository.findById(intialPaste.getId()).block().getViews(),
                equalTo(1000L)
        );

        Mockito.verify(messagingClient, Mockito.atLeast(1001)).sendMessage(any(), any());
    }

    @Test
    @DisplayName("tracking - unknown paste is not repeatedly tracked")
    void trackUnknownPaste() throws InterruptedException {
        trackingService.trackView("4711");
        TimeUnit.SECONDS.sleep(1);

        Mockito.verify(messagingClient).sendMessage(eq("4711"), any());
        Mockito.verify(messagingClient).receiveMessage(); // TODOO
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
