package com.github.binpastes.paste.application.tracking;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.ActiveMQObjectClosedException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Instant;
import java.util.Objects;

public class MessagingClient {

    private static final Logger log = LoggerFactory.getLogger(MessagingClient.class);

    private final Scheduler consumerThreadPool;
    private final Scheduler producerThreadPool;
    private final ClientSessionFactory sessionFactory;
    private final ThreadLocal<ClientSession> sessions = new ThreadLocal<>();
    private final ThreadLocal<ClientProducer> clientProducers = new ThreadLocal<>();
    private final ThreadLocal<ClientConsumer> clientConsumers = new ThreadLocal<>();

    public MessagingClient(
            final ClientSessionFactory clientSessionFactory,
            final Scheduler consumerThreadPool,
            final Scheduler producerThreadPool
    ) {
        this.sessionFactory = clientSessionFactory;
        this.consumerThreadPool = consumerThreadPool;
        this.producerThreadPool = producerThreadPool;
    }

    public void sendMessage(String pasteId, Instant timeViewed) {
        Mono.fromRunnable(() -> {
                    try {
                        var session = session();
                        var clientProducer = producer();
                        var clientMessage = session
                                .createMessage(true)
                                .putStringProperty(Message.PASTE_ID_PROPERTY, pasteId)
                                .putLongProperty(Message.TIME_VIEWED_PROPERTY, timeViewed.toEpochMilli());

                        clientProducer.send(clientMessage);
                        log.debug("Sent tracking message for paste {}", pasteId);
                    } catch (ActiveMQException e) {
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(producerThreadPool)
                .subscribe();
    }

    public Mono<Message> receiveMessage() {
        return Mono.fromCallable(() -> consumer().receive())
                .filter(Objects::nonNull)
                .map(clientMessage -> {
                    var pasteId = clientMessage.getStringProperty(Message.PASTE_ID_PROPERTY);
                    var timeViewed = Instant.ofEpochMilli(clientMessage.getLongProperty(Message.TIME_VIEWED_PROPERTY));
                    log.debug("Received tracking message for paste {}", pasteId);

                    return new Message(pasteId, timeViewed);
                })
                .onErrorComplete(ActiveMQObjectClosedException.class)
                .subscribeOn(consumerThreadPool);
    }

    private ClientProducer producer() throws ActiveMQException {
        var clientProducer = this.clientProducers.get();

        if (clientProducer == null) {
            clientProducer = session().createProducer("binpastes");
            this.clientProducers.set(clientProducer);
        }

        return clientProducer;
    }

    private ClientConsumer consumer() throws ActiveMQException {
        var clientConsumer = this.clientConsumers.get();

        if (clientConsumer == null) {
            clientConsumer = session().createConsumer("pasteTrackingQueue");
            this.clientConsumers.set(clientConsumer);
        }

        return clientConsumer;
    }

    private ClientSession session() throws ActiveMQException {
        var session = sessions.get();

        if (session == null) {
            session = this.sessionFactory.createSession();
            session.start();
            sessions.set(session);
        }

        return session;
    }

    public record Message(
            String pasteId,
            Instant timeViewed
    ) {
        private static final String PASTE_ID_PROPERTY = "pasteId";
        private static final String TIME_VIEWED_PROPERTY = "timeViewed";
    }
}
