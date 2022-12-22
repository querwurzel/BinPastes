package it.wylke.binpastes.paste.business.tracking;

import it.wylke.binpastes.paste.domain.PasteRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static it.wylke.binpastes.paste.business.tracking.MessagingClient.Message;

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
    private void configure() {
        this.messagingClient.setMessageConsumer(new MessageConsumer());
    }

    public void trackView(String pasteId) {
        log.debug("Tracking view on paste {}", pasteId);
        messagingClient.sendMessage(pasteId);
    }

    private void receiveView(String pasteId, LocalDateTime timeViewed) {
        pasteRepository
                .findById(pasteId)
                .flatMap(paste -> pasteRepository.save(paste.trackView(timeViewed)))
                .doOnNext(paste -> log.debug("Tracked view on paste {}", paste.getId()))
                //.subscribeOn(Schedulers.single())
                //.subscribe();
                .block();
    }

    private final class MessageConsumer implements Consumer<Message> {

        @Override
        public void accept(final Message message) {
            TrackingService.this.receiveView(message.pasteId(), message.timeViewed());
        }
    }
}
