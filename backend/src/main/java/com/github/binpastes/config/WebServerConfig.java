package com.github.binpastes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class WebServerConfig implements WebFluxConfigurer {

    private final Environment environment;

    @Autowired
    public WebServerConfig(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        if (this.environment.matchesProfiles("dev")) {
            registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("*")
                    .allowedHeaders("*");
        }
    }

    /**
     * This gives back control to the SPA (index.html) for paths/routes that are not served by the backend.
     */
    @Bean
    public RouterFunctionMapping indexRoute(@Value("static/index.html") final ClassPathResource indexHtml) {
        Assert.isTrue(indexHtml.exists(), "index.html must exist");

        var route = route(RequestPredicates
                        .method(HttpMethod.GET)
                        .and(path("/robots.txt").negate())
                        .and(path("/favicon.png").negate())
                        .and(path("/manifest.json").negate())
                        .and(path("/assets/**").negate())
                        .and(path("/api/**").negate()),
                request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));

        var routerFunctionMapping = new RouterFunctionMapping(route);
        routerFunctionMapping.setOrder(500); // after WelcomePageRouterFunctionMapping (order:1)
        return routerFunctionMapping;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.setOrder(Ordered.LOWEST_PRECEDENCE - 147483647);
    }

}
