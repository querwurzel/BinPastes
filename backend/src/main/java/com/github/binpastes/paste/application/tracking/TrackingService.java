package com.github.binpastes.paste.application.tracking;

import com.github.binpastes.paste.domain.PasteRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
                .doOnNext(message -> receiveView(message.pasteId(), message.timeViewed()))
                .repeat()
                .subscribe();
    }

    public void trackView(String pasteId) {
        log.debug("Tracking view on paste {}", pasteId);
        messagingClient.sendMessage(pasteId, LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    public void receiveView(String pasteId, Instant timeViewed) {
        var timestamp = LocalDateTime.ofInstant(timeViewed, ZoneOffset.UTC);
        pasteRepository
                .findById(pasteId)
                .flatMap(paste -> pasteRepository.save(paste.trackView(timestamp)))
                .doOnNext(paste -> log.debug("Tracked view on paste {}", paste.getId()))
                .doOnError(OptimisticLockingFailureException.class, e -> messagingClient.sendMessage(pasteId, timeViewed))
                .onErrorResume(OptimisticLockingFailureException.class, e -> Mono.empty())
                .subscribe();
    }
}
