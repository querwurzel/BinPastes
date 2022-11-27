package it.wylke.binpastes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories
public class BinPastesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinPastesApplication.class, args);
    }

/*
    @Bean
    @ConditionalOnProperty(prefix = "spring.sql.init", name = "mode", havingValue = "always")
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory, SqlInitializationProperties props)  {

        var initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        return initializer;
    }
 */
}
