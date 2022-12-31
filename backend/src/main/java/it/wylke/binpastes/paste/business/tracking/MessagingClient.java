package it.wylke.binpastes.paste.business.tracking;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class MessagingClient {

    private static final Logger log = LoggerFactory.getLogger(MessagingClient.class);

    private static final String PASTE_ID_PROPERTY = "pasteId";

    private final Executor consumerThreadPool;
    private final Executor producerThreadPool;

    private final ThreadLocal<ClientSession> clientSessionThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<ClientProducer> clientProducers = new ThreadLocal<>();
    private final ThreadLocal<ClientConsumer> clientConsumers = new ThreadLocal<>();

    private final ClientSessionFactory sessionFactory;

    public MessagingClient(final ClientSessionFactory clientSessionFactory, Executor consumerThreadPool, Executor producerThreadPool) {
        this.sessionFactory = clientSessionFactory;
        this.consumerThreadPool = consumerThreadPool;
        this.producerThreadPool = producerThreadPool;
    }

    public void setMessageConsumer(Consumer<Message> messageConsumer) {
            Mono.fromRunnable(() -> {
                try {
                    var session = clientSessionThreadLocal.get();
                    if (session == null) {
                        try {
                            session = this.sessionFactory.createSession(true, true);
                            session.start();

                            clientSessionThreadLocal.set(session);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (this.clientConsumers.get() == null) {
                        var clientConsumer = session.createConsumer(new SimpleString("pasteTrackingQueue"));
                        this.clientConsumers.set(clientConsumer);
                    }

                    var clientConsumer = this.clientConsumers.get();
                    clientConsumer.setMessageHandler(new MessageHandler(messageConsumer));

                } catch (ActiveMQException e) {
                    throw new IllegalStateException(e);
                }
            })
            .subscribeOn(Schedulers.fromExecutor(consumerThreadPool))
            .subscribe();
    }

    public void sendMessage(String pasteId) {
        Mono.fromRunnable(() -> {
            var session = clientSessionThreadLocal.get();

            if (session == null) {
                try {
                    session = this.sessionFactory.createSession(true, true);
                    clientSessionThreadLocal.set(session);

                    session.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            try {
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
            } catch (ActiveMQException e) {
                e.printStackTrace();
            }

        })
        .subscribeOn(Schedulers.fromExecutor(producerThreadPool))
        .subscribe();
    }

    public record Message (
            String pasteId,
            LocalDateTime timeViewed
    ) {}

    private static final class MessageHandler implements org.apache.activemq.artemis.api.core.client.MessageHandler {

        private final Consumer<Message> messageConsumer;

        private MessageHandler(final Consumer<Message> messageConsumer) {
            this.messageConsumer = messageConsumer;
        }

        @Override
        public void onMessage(final ClientMessage message) {
            log.debug("Receiving tracking message for paste");

            var timestamp = Instant.ofEpochMilli(message.getTimestamp());
            var timeViewed = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
            var pasteId = message.getStringProperty(PASTE_ID_PROPERTY);

            messageConsumer.accept(new Message(pasteId, timeViewed));
        }
    }

}
