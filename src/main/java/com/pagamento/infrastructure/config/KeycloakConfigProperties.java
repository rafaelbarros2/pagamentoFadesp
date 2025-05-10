package com.pagamento.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "keycloak")
@Component
@Getter
@Setter
public class KeycloakConfigProperties {
    private String authServerUrl;
    private String realm;
    private String resource;
    private String credentials;


    public String getTokenEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token",
                authServerUrl, realm);
    }
}