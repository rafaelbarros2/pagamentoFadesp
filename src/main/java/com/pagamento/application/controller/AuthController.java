// Coloque em: com.pagamento.application.controller.AuthController.java
package com.pagamento.application.controller;

import com.pagamento.domain.port.service.AuthApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gestão de tokens")
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Autenticar usuário",
            description = "Autentica um usuário com credenciais e retorna um token JWT.<br><br>" +
                    "<b>Credenciais de teste:</b><br>" +
                    "• Admin: usuario1 / password <i>(tem permissão pagamento_admin)</i><br>" +
                    "• Consulta: consulta / consulta123 <i>(tem permissão pagamento_consulta)</i>"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticação bem-sucedida",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public Map<String, String> login(
            @RequestBody
            @Parameter(description = "Credenciais do usuário", required = true)
            LoginRequest request
    ) {
        String token = authService.autenticarUsuario(
                request.getUsername(),
                request.getPassword()
        );

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

    @Schema(description = "Requisição de login com credenciais do usuário")
    public static class LoginRequest {
        @Schema(description = "Nome de usuário", example = "usuario1", required = true)
        private String username;

        @Schema(description = "Senha do usuário", example = "password", required = true)
        private String password;

        // Adicionar getters e setters manualmente
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Schema(description = "Resposta contendo token de acesso")
    public static class TokenResponse {
        @Schema(description = "Token JWT para autorização", example = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJfT2...")
        private String token;

        // Getter e setter para token
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}