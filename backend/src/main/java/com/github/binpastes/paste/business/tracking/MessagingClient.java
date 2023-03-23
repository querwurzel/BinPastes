package com.github.binpastes.paste.business.tracking;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Executor;

public class MessagingClient {

    private static final Logger log = LoggerFactory.getLogger(MessagingClient.class);

    private static final String PASTE_ID_PROPERTY = "pasteId";

    private final Scheduler consumerThreadPool;
    private final Scheduler producerThreadPool;
    private final ClientSessionFactory sessionFactory;
    private final ThreadLocal<ClientSession> clientSessionThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<ClientProducer> clientProducers = new ThreadLocal<>();
    private final ThreadLocal<ClientConsumer> clientConsumers = new ThreadLocal<>();

    public MessagingClient(final ClientSessionFactory clientSessionFactory, Executor consumerThreadPool, Executor producerThreadPool) {
        this.sessionFactory = clientSessionFactory;
        this.consumerThreadPool = Schedulers.fromExecutor(consumerThreadPool);
        this.producerThreadPool = Schedulers.fromExecutor(producerThreadPool);
    }

    public void sendMessage(String pasteId) {
        Mono.fromRunnable(() -> {
            try {
                var session = clientSessionThreadLocal.get();
                if (session == null) {
                    session = this.sessionFactory.createSession(true, true);
                    clientSessionThreadLocal.set(session);

                    session.start();
                }

                var clientProducer = this.clientProducers.get();

                if (clientProducer == null) {
                    clientProducer = session.createProducer(new SimpleString("binpastes"));
                    this.clientProducers.set(clientProducer);
                }

                var clientMessage = session
                        .createMessage(true)
                        .setTimestamp(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .putStringProperty("pasteId", pasteId);

                clientProducer.send(clientMessage);
                log.debug("Sent tracking message for paste");
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
                var session = clientSessionThreadLocal.get();
                if (session == null) {
                    session = this.sessionFactory.createSession(true, true);
                    session.start();

                    clientSessionThreadLocal.set(session);
                }

                if (this.clientConsumers.get() == null) {
                    var clientConsumer = session.createConsumer(new SimpleString("pasteTrackingQueue"));
                    this.clientConsumers.set(clientConsumer);
                }

                var clientConsumer = this.clientConsumers.get();

                ClientMessage message = clientConsumer.receive();
                log.debug("Received tracking message for paste");

                var timestamp = Instant.ofEpochMilli(message.getTimestamp());
                var timeViewed = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
                var pasteId = message.getStringProperty(PASTE_ID_PROPERTY);

                return new Message(pasteId, timeViewed);
            } catch (ActiveMQException e) {
                throw e;
            }
        })
        .subscribeOn(consumerThreadPool);
    }

    public record Message (
            String pasteId,
            LocalDateTime timeViewed
    ) {}

}
