package com.pagamento.infrastructure.adapter.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagamento.domain.port.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

@Service
public class KeycloakTokenServiceImpl implements TokenService {

    private final RestTemplate restTemplate;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final org.springframework.core.env.Environment environment;
    public KeycloakTokenServiceImpl(
            RestTemplate restTemplate,
            @Value("${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/token") String tokenUrl,
            @Value("${keycloak.resource}") String clientId,
            @Value("${keycloak.credentials.secret}") String clientSecret,
            org.springframework.core.env.Environment springEnvironment) {
        this.restTemplate = restTemplate;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.environment = springEnvironment;
    }

    @Override
    public String obterToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    @Override
    public String obterTokenComCredenciais(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    @Override
    public String renovarToken(String refreshToken) {
        return "";
    }

    @Override
    public boolean validarToken(String token) {
        try {
            // 1. Verificação básica de formato
            if (token == null || token.trim().isEmpty() || !token.contains(".")) {
                return false;
            }

            if (isDevelopmentOrTestEnvironment()) {
                return verificarExpiracao(token);
            }

            String introspectUrl = tokenUrl.replace("/token", "/token/introspect");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("token", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(introspectUrl, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return Boolean.TRUE.equals(response.getBody().get("active"));
                }
                return false;
            } catch (RestClientException e) {

                return verificarExpiracao(token);
            }
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * Verifica se o ambiente atual é de desenvolvimento ou teste
     */
    private boolean isDevelopmentOrTestEnvironment() {
        if (environment instanceof ConfigurableEnvironment) {
            String[] profiles = ((ConfigurableEnvironment) environment).getActiveProfiles();
            return Arrays.stream(profiles)
                    .anyMatch(profile -> profile.equals("dev") || profile.equals("test"));
        }
        return false;
    }

    /**
     * Verifica apenas a expiração do token analisando o payload
     */
    private boolean verificarExpiracao(String token) {
        try {
            // Dividir o token nos seus componentes
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return false;
            }

            // Decodificar o payload (segunda parte)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Converter para JSON para extrair exp (expiração)
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // Verificar expiração
            if (claims.containsKey("exp")) {
                long expiration = ((Number) claims.get("exp")).longValue();
                return (expiration * 1000) > System.currentTimeMillis();
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }


}