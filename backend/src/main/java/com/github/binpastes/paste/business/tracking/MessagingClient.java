package com.github.binpastes.paste.business.tracking;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.concurrent.Executor;

public class MessagingClient {

    private static final Logger log = LoggerFactory.getLogger(MessagingClient.class);

    private final Scheduler consumerThreadPool;
    private final Scheduler producerThreadPool;
    private final ClientSessionFactory sessionFactory;
    private final ThreadLocal<ClientSession> sessions = new ThreadLocal<>();
    private final ThreadLocal<ClientProducer> clientProducers = new ThreadLocal<>();
    private final ThreadLocal<ClientConsumer> clientConsumers = new ThreadLocal<>();

    public MessagingClient(final ClientSessionFactory clientSessionFactory, Executor consumerThreadPool, Executor producerThreadPool) {
        this.sessionFactory = clientSessionFactory;
        this.consumerThreadPool = Schedulers.fromExecutor(consumerThreadPool);
        this.producerThreadPool = Schedulers.fromExecutor(producerThreadPool);
    }

    public void sendMessage(String pasteId, Instant timeViewed) {
        Mono.fromRunnable(() -> {
            try {
                var session = session();
                var clientProducer = this.clientProducers.get();

                if (clientProducer == null) {
                    clientProducer = session.createProducer(new SimpleString("binpastes"));
                    this.clientProducers.set(clientProducer);
                }

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
        return Mono.fromCallable(() -> {
            try {
                var session = session();
                var clientConsumer = this.clientConsumers.get();

                if (clientConsumer == null) {
                    clientConsumer = session.createConsumer(new SimpleString("pasteTrackingQueue"));
                    this.clientConsumers.set(clientConsumer);
                }

                ClientMessage message = clientConsumer.receive();
                var pasteId = message.getStringProperty(Message.PASTE_ID_PROPERTY);
                var timeViewed = Instant.ofEpochMilli(message.getLongProperty(Message.TIME_VIEWED_PROPERTY));
                log.debug("Received tracking message for paste {}", pasteId);

                return new Message(pasteId, timeViewed);
            } catch (ActiveMQException e) {
                throw new RuntimeException(e);
            }
        })
        .subscribeOn(consumerThreadPool);
    }

    private ClientSession session() throws ActiveMQException {
        var session = sessions.get();

        if (session == null) {
            session = this.sessionFactory.createSession(true, true);
            session.start();
            sessions.set(session);
        }

        return session;
    }

    public record Message (
            String pasteId,
            Instant timeViewed
    ) {
        private static final String PASTE_ID_PROPERTY = "pasteId";
        private static final String TIME_VIEWED_PROPERTY = "timeViewed";
    }

}
