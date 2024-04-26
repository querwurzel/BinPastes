package com.github.binpastes.paste.business.tracking;

import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.config.CoreAddressConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
class MessagingConfig {

    @Bean
    @DependsOn("flywayInitializer")
    public MessagingClient messagingClient(
            ClientSessionFactory clientSessionFactory,
            Scheduler consumerThreadPool,
            Scheduler producerThreadPool
    ) {
        return new MessagingClient(clientSessionFactory, consumerThreadPool, producerThreadPool);
    }

    @Bean(destroyMethod = "stop")
    public EmbeddedActiveMQ activeMqServer() throws Exception {
        org.apache.activemq.artemis.core.config.Configuration config = new ConfigurationImpl();

        CoreAddressConfiguration addr = new CoreAddressConfiguration();
        addr
                .setName("binpastes")
                .addQueueConfig(new QueueConfiguration()
                        .setName(new SimpleString("pasteTrackingQueue"))
                        .setAddress(new SimpleString("binpastes"))
                        .setMaxConsumers(1)
                        .setDelayBeforeDispatch(SECONDS.toMillis(3))
                        .setDurable(true))
                .addRoutingType(RoutingType.ANYCAST);

        AddressSettings addressSettings = new AddressSettings()
                .setDefaultAddressRoutingType(RoutingType.ANYCAST)
                .setDefaultQueueRoutingType(RoutingType.ANYCAST)
                .setExpiryDelay(TimeUnit.DAYS.toMillis(7));

        config.addAddressConfiguration(addr);
        config.addAddressSetting("binpastes", addressSettings);

        config.setMessageExpiryScanPeriod(SECONDS.toMillis(3));

        config.setName("binpastesMQ");
        config.addAcceptorConfiguration("in-vm", "vm://0");

        config.setGracefulShutdownEnabled(true);
        config.setGracefulShutdownTimeout(SECONDS.toMillis(1));
        config.setJournalPoolFiles(3);
        config.setSecurityEnabled(false);
        config.setJMXManagementEnabled(true);

        config.setPersistDeliveryCountBeforeDelivery(true);

        config.setScheduledThreadPoolMaxSize(1);
        config.setThreadPoolMaxSize(1);
        config.setPersistenceEnabled(true);

        config.setBindingsDirectory("./tracking/bindings");
        config.setJournalDirectory("./tracking/journal");
        config.setLargeMessagesDirectory("./tracking/large_messages");
        config.setPagingDirectory("./tracking/paging");

        var embeddedActiveMQ = new EmbeddedActiveMQ();
        embeddedActiveMQ.setConfiguration(config);

        return embeddedActiveMQ.start();
    }
/*
    @Bean
    ConnectionFactory activeMqConnectionFactory() throws Exception {

        JGroupsFileBroadcastEndpointFactory jGroupsFileBroadcastEndpointFactory = new JGroupsFileBroadcastEndpointFactory();
        jGroupsFileBroadcastEndpointFactory.setChannelName("binpastesChannel");

        DiscoveryGroupConfiguration discoveryGroupConfiguration = new DiscoveryGroupConfiguration();
        discoveryGroupConfiguration.setName("binpastesGroup");
        discoveryGroupConfiguration.setBroadcastEndpointFactory(jGroupsFileBroadcastEndpointFactory);

        new TransportConfiguration();

        ServerLocator serverLocator = new ServerLocatorImpl(false, discoveryGroupConfiguration);

        ServerLocator serverLocator1 = ActiveMQClient.createServerLocator("vm://0");

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://0", null, null);
        //activeMQConnectionFactory.setUs

        return activeMQConnectionFactory;
    }*/

    @Bean(initMethod = "init", destroyMethod = "dispose")
    public Scheduler consumerThreadPool() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(1);
        pool.setMaxPoolSize(1);
        pool.setAllowCoreThreadTimeOut(true);
        pool.setThreadNamePrefix("activemq-consumer-");
        pool.initialize();
        return Schedulers.fromExecutor(pool);
    }

    @Bean(initMethod = "init", destroyMethod = "dispose")
    public Scheduler producerThreadPool() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        pool.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        pool.setAllowCoreThreadTimeOut(true);
        pool.setThreadNamePrefix("activemq-producer-");
        pool.initialize();
        return Schedulers.fromExecutor(pool);
    }

    @Bean(destroyMethod = "close")
    @DependsOn("activeMqServer")
    public ServerLocator serverLocator() throws Exception {
        ServerLocator serverLocator = ActiveMQClient.createServerLocator("vm://0");
        serverLocator.setReconnectAttempts(1);
        serverLocator.setUseGlobalPools(false);
        serverLocator.setAutoGroup(true);
        serverLocator.setGroupID("binpastes-views");
        serverLocator.setPreAcknowledge(true);
        return serverLocator;
    }

    @Bean(destroyMethod = "close")
    public ClientSessionFactory clientSession(ServerLocator serverLocator) throws Exception {
        return serverLocator.createSessionFactory();
    }

}
