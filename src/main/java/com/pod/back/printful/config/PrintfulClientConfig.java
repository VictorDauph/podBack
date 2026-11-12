package com.pod.back.printful.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(PrintfulProperties.class)
public class PrintfulClientConfig {

    // Jackson dédié à Printful (snake_case) : construit en dehors du contexte Spring et
    // JAMAIS exposé comme @Bean. Spring Boot auto-configure son convertisseur JSON global en
    // réutilisant le seul bean JsonMapper/ObjectMapper présent dans le contexte s'il n'y en a
    // qu'un ; en exposer un ici (même sans @Primary) écrase silencieusement le camelCase
    // attendu par le reste de l'app (vérifié : /api/auth/register exigeait first_name au lieu
    // de firstName tant que ce bean existait). PrintfulUtils.buildPrintfulJsonMapper() fournit
    // la même instance, réutilisée aussi par PrintfulClient, sans jamais passer par le
    // conteneur Spring.
    @Bean
    public RestClient printfulRestClient(PrintfulProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMs());
        requestFactory.setReadTimeout(properties.readTimeoutMs());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .messageConverters(converters -> {
                    converters.removeIf(JacksonJsonHttpMessageConverter.class::isInstance);
                    converters.add(0, new JacksonJsonHttpMessageConverter(PrintfulJsonMapperFactory.create()));
                })
                .defaultHeaders(headers -> headers.setBearerAuth(properties.key()))
                .build();
    }
}
