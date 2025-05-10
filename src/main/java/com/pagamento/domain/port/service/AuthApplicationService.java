package com.pagamento.domain.port.service;


import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final TokenService tokenService;

    public AuthApplicationService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public String autenticarUsuario(String username, String password) {
        return tokenService.obterTokenComCredenciais(username, password);
    }

    public String renovarToken(String refreshToken) {
        return tokenService.renovarToken(refreshToken);
    }

    public boolean validarToken(String token) {
        return tokenService.validarToken(token);
    }
}