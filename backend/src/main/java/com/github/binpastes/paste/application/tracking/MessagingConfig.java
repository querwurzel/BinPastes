package com.github.binpastes.paste.application.tracking;

import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.config.CoreAddressConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@Configuration
class MessagingConfig {

    @Bean
    @DependsOnDatabaseInitialization
    public MessagingClient messagingClient(
            final ClientSessionFactory clientSessionFactory,
            final Scheduler consumerThreadPool,
            final Scheduler producerThreadPool
    ) {
        return new MessagingClient(clientSessionFactory, consumerThreadPool, producerThreadPool);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public EmbeddedActiveMQ activeMqServer() throws Exception {
        var config = new ConfigurationImpl();

        config.addAddressConfiguration(new CoreAddressConfiguration()
            .setName("binpastes")
            .addQueueConfig(QueueConfiguration
                .of("pasteTrackingQueue")
                .setAddress("binpastes")
                .setMaxConsumers(1)
                .setExclusive(true) // dispatch all messages to only one consumer at a time
                .setConsumersBeforeDispatch(1)
                .setDelayBeforeDispatch(TimeUnit.SECONDS.toMillis(5))
                .setDurable(true)
            )
            .addRoutingType(RoutingType.ANYCAST));

        config.addAddressSetting("binpastes", new AddressSettings()
            .setDefaultAddressRoutingType(RoutingType.ANYCAST)
            .setDefaultQueueRoutingType(RoutingType.ANYCAST)
            .setRedeliveryDelay(TimeUnit.SECONDS.toMillis(5))
            .setRedeliveryMultiplier(1.5)
            .setMaxDeliveryAttempts(5)
            .setAutoCreateDeadLetterResources(true)
        );

        config.setName("binpastesMQ");
        config.addAcceptorConfiguration("in-vm", "vm://0");

        config.setGracefulShutdownEnabled(true);
        config.setGracefulShutdownTimeout(TimeUnit.SECONDS.toMillis(1));
        config.setJournalPoolFiles(3);
        config.setSecurityEnabled(false);
        config.setJMXManagementEnabled(true);

        config.setLargeMessageSync(false);

        config.setPersistDeliveryCountBeforeDelivery(true);

        config.setScheduledThreadPoolMaxSize(1);
        config.setThreadPoolMaxSize(Runtime.getRuntime().availableProcessors());

        config.setPersistenceEnabled(true);

        config.setBindingsDirectory("./tracking/bindings");
        config.setJournalDirectory("./tracking/journal");
        config.setLargeMessagesDirectory("./tracking/large_messages");
        config.setPagingDirectory("./tracking/paging");

        var server = new EmbeddedActiveMQ()
            .setConfiguration(config);

        return server;
    }

    @Bean(initMethod = "init", destroyMethod = "dispose")
    public Scheduler consumerThreadPool() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(1);
        pool.setMaxPoolSize(1);
        pool.setAllowCoreThreadTimeOut(true);
        pool.setThreadNamePrefix("amq-consumer-");
        pool.initialize();
        return Schedulers.fromExecutor(pool);
    }

    @Bean(initMethod = "init", destroyMethod = "dispose")
    public Scheduler producerThreadPool() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(1);
        pool.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        pool.setAllowCoreThreadTimeOut(true);
        pool.setThreadNamePrefix("amq-producer-");
        pool.initialize();
        return Schedulers.fromExecutor(pool);
    }

    @Bean(destroyMethod = "close")
    @DependsOn("activeMqServer")
    public ServerLocator serverLocator() throws Exception {
        var serverLocator = ActiveMQClient.createServerLocator("vm://0")
                .setReconnectAttempts(1)
                .setFlowControlThreadPoolMaxSize(1)
                .setScheduledThreadPoolMaxSize(1)
                .setThreadPoolMaxSize(1)
                .setUseGlobalPools(false)
                .setAutoGroup(false)
                .setPreAcknowledge(true); // tradeoff to lose views potentially
        return serverLocator;
    }

    @Bean(destroyMethod = "close")
    public ClientSessionFactory clientSession(final ServerLocator serverLocator) throws Exception {
        return serverLocator.createSessionFactory();
    }
}
