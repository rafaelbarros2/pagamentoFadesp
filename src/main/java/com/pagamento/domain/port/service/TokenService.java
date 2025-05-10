package com.pagamento.domain.port.service;


public interface TokenService {
    String obterToken();
    String obterTokenComCredenciais(String username, String password);
    String renovarToken(String refreshToken);
    boolean validarToken(String token);
}
