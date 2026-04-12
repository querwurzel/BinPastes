package com.github.binpastes.paste.application.tracking;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Instant;

public class MessagingClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(MessagingClient.class);

    private final ClientSessionFactory sessionFactory;
    private final Scheduler consumerThreadPool;
    private final Scheduler producerThreadPool;
    private final ClientSession clientSession;
    private final ClientConsumer clientConsumer;

    public MessagingClient(
        final ClientSessionFactory clientSessionFactory,
        final Scheduler consumerThreadPool,
        final Scheduler producerThreadPool
    ) throws ActiveMQException {
        this.sessionFactory = clientSessionFactory;
        this.consumerThreadPool = consumerThreadPool;
        this.producerThreadPool = producerThreadPool;
        this.clientSession = clientSessionFactory.createSession().start();
        this.clientConsumer = clientSession.createConsumer("pasteTrackingQueue");
    }

    public void sendMessage(String pasteId, Instant timeViewed) {
        Mono.fromRunnable(() -> {
                try (
                    var session = sessionFactory.createSession().start();
                    var producer = session.createProducer("binpastes")
                ) {
                    var clientMessage = session
                        .createMessage(true)
                        .putStringProperty(Message.PASTE_ID_PROPERTY, pasteId)
                        .putLongProperty(Message.TIME_VIEWED_PROPERTY, timeViewed.toEpochMilli());

                    producer.send(clientMessage);
                    log.debug("Sent tracking message for paste {}", pasteId);
                } catch (ActiveMQException e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            })
            .subscribeOn(producerThreadPool)
            .subscribe();
    }

    public Mono<Message> receiveMessage() {
        return Mono.fromCallable(clientConsumer::receive)
            .map(clientMessage -> {
                try {
                    var pasteId = clientMessage.getStringProperty(Message.PASTE_ID_PROPERTY);
                    var timeViewed = Instant.ofEpochMilli(clientMessage.getLongProperty(Message.TIME_VIEWED_PROPERTY));
                    log.debug("Received tracking message for paste {}", pasteId);

                    return new Message(pasteId, timeViewed);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw e;
                }
            })
            .onErrorComplete(ActiveMQObjectClosedException.class)
            .subscribeOn(consumerThreadPool);
    }

    @Override
    public void close() throws Exception {
        clientSession.close();
    }

    public record Message(
        String pasteId,
        Instant timeViewed
    ) {
        private static final String PASTE_ID_PROPERTY = "pasteId";
        private static final String TIME_VIEWED_PROPERTY = "timeViewed";
    }
}
