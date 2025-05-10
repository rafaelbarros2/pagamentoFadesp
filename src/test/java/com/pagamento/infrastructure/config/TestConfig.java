package com.pagamento.infrastructure.config;

import com.pagamento.domain.port.service.TokenService;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TestConfig {

    // Mock do JwtDecoder para evitar conflitos
    @MockBean
    private JwtDecoder jwtDecoder;

    /**
     * Cria um mock do TokenService para uso nos testes
     */
    @Bean
    @Primary
    public TokenService tokenService() {
        TokenService mockTokenService = mock(TokenService.class);
        // Configurar o mock para validar qualquer token nos testes
        when(mockTokenService.validarToken(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        return mockTokenService;
    }

    /**
     * Cria um TokenValidationFilter com o mock do TokenService para testes
     */
    @Bean
    public TokenValidationFilter tokenValidationFilter(TokenService tokenService) {
        return new TokenValidationFilter(tokenService);
    }

    /**
     * Configuração de segurança para testes que permite a autenticação mock,
     * mas mantém verificação de autorização baseada em roles
     */
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http, TokenValidationFilter tokenValidationFilter) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests(authorizeRequests -> authorizeRequests
                        // Endpoints públicos (sem autenticação)
                        .antMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**", "/auth/login").permitAll()
                        // Endpoints protegidos por papel - manter a verificação de papéis
                        .antMatchers("/api/pagamentos/**").hasRole("pagamento_admin")
                        // Permitir outras requisições para usuários autenticados
                        .anyRequest().authenticated())
                // Configurar filtro de validação de token personalizado
                .addFilterBefore(tokenValidationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                // Desabilitar o JWT para usar autenticação mock em testes
                .httpBasic();

        // Para permitir acesso ao console H2 em testes
        http.headers().frameOptions().sameOrigin();

        return http.build();
    }
}