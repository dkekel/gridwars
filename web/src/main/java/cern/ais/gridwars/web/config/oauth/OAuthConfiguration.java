package cern.ais.gridwars.web.config.oauth;

import cern.ais.gridwars.web.config.GridWarsProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Configuration
public class OAuthConfiguration {

    private final transient GridWarsProperties gridWarsProperties;

    public OAuthConfiguration(final GridWarsProperties gridWarsProperties) {
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    @Bean
    public RestTemplate restTemplateOAuth(final RestTemplateBuilder builder) {
        String clientId = gridWarsProperties.getOAuth().getClientId();
        String clientPassword = gridWarsProperties.getOAuth().getClientSecret();
        return builder.basicAuthorization(clientId, clientPassword).build();
    }
}
