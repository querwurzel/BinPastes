package com.github.binpastes.paste.application.tracking;

import com.github.binpastes.paste.application.tracking.MessagingClient.Message;
import com.github.binpastes.paste.domain.PasteService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    private final PasteService pasteService;
    private final MessagingClient messagingClient;

    private Disposable subscription;

    public TrackingService(
            final PasteService pasteService,
            final MessagingClient messagingClient
    ) {
        this.pasteService = pasteService;
        this.messagingClient = messagingClient;
    }

    @PostConstruct
    private void postConstruct() {
        subscription = this.messagingClient
                .receiveMessage()
                .doOnNext(this::receiveView)
                .repeat()
                .subscribe();
    }

    @PreDestroy
    private void preDestroy() {
        subscription.dispose();
    }

    public void trackView(String pasteId) {
        log.debug("Tracking view on paste {}", pasteId);
        messagingClient.sendMessage(pasteId, LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    public void receiveView(String pasteId, LocalDateTime timeViewed) {
        pasteService.trackView(pasteId, timeViewed);
        log.debug("Tracked view on paste {} at {}", pasteId, timeViewed);
    }

    private void receiveView(Message message) {
        var timestamp = LocalDateTime.ofInstant(message.timeViewed(), ZoneOffset.UTC);
        this.receiveView(message.pasteId(), timestamp);
    }
}
