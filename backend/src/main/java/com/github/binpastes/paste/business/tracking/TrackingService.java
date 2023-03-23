package com.github.binpastes.paste.business.tracking;

import com.github.binpastes.paste.business.tracking.MessagingClient.Message;
import com.github.binpastes.paste.domain.PasteRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    private final PasteRepository pasteRepository;
    private final MessagingClient messagingClient;

    @Autowired
    public TrackingService(
            final PasteRepository pasteRepository,
            final MessagingClient messagingClient
    ) {
        this.pasteRepository = pasteRepository;
        this.messagingClient = messagingClient;
    }

    @PostConstruct
    private void run() {
        this.messagingClient
                .receiveMessage()
                .doOnNext(this::receiveView)
                .repeat()
                .subscribe();
    }

    public void trackView(String pasteId) {
        log.debug("Tracking view on paste {}", pasteId);
        messagingClient.sendMessage(pasteId);
    }

    public void receiveView(String pasteId, LocalDateTime timeViewed) {
        pasteRepository
                .findById(pasteId)
                .flatMap(paste -> pasteRepository.save(paste.trackView(timeViewed)))
                .doOnNext(paste -> log.debug("Tracked view on paste {}", paste.getId()))
                .subscribe();
    }

    private void receiveView(Message message) {
        this.receiveView(message.pasteId(), message.timeViewed());
    }
}
