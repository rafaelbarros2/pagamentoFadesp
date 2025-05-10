# Segurança com Keycloak

Este documento explica os aspectos de segurança da API de Pagamentos com Keycloak, focando na autenticação, autorização e gerenciamento de tokens.

## 📋 Índice

- [Fluxo de Autenticação](#-fluxo-de-autenticação)
- [Obtendo e Utilizando Tokens](#-obtendo-e-utilizando-tokens)
- [Estrutura dos Tokens JWT](#-estrutura-dos-tokens-jwt)
- [Segurança nos Endpoints](#-segurança-nos-endpoints)
- [Troubleshooting](#-troubleshooting)
- [Melhores Práticas](#-melhores-práticas)

## 🔄 Fluxo de Autenticação

A API de Pagamentos utiliza o fluxo OAuth 2.0 com OpenID Connect para autenticação:

```
┌─────────┐                                  ┌────────────┐                             ┌─────────────┐
│ Cliente │                                  │  Keycloak  │                             │ API Payment │
└────┬────┘                                  └─────┬──────┘                             └──────┬──────┘
     │                                             │                                            │
     │ 1. Credenciais (username/password)          │                                            │
     ├────────────────────────────────────────────>│                                            │
     │                                             │                                            │
     │ 2. Token JWT (access_token)                 │                                            │
     │<────────────────────────────────────────────┤                                            │
     │                                             │                                            │
     │                   3. Requisição API + Bearer Token                                       │
     │───────────────────────────────────────────────────────────────────────────────────────-->│
     │                                             │                                            │
     │                                             │  4. Valida token e                         │
     │                                             │<───────── extrai permissões                │
     │                                             │                                            │
     │                   5. Resposta (se autorizado)                                            │
     │<─────────────────────────────────────────────────────────────────────────────────────────┤
     │                                             │                                            │
```

### Fluxos Suportados

1. **Resource Owner Password Credentials Grant** (usado nos exemplos)
   - O cliente envia usuário/senha diretamente ao servidor de autenticação
   - Útil para aplicações confiáveis ou testes

2. **Authorization Code Flow**
   - Mais seguro para aplicações web
   - O usuário é redirecionado para o Keycloak para autenticação

3. **Client Credentials Grant**
   - Para comunicação sistema-a-sistema
   - Usa o segredo do cliente para autenticação

## 🔑 Obtendo e Utilizando Tokens

### Obtendo um Token com Username/Password

```bash
curl -X POST http://localhost:8180/auth/realms/pagamentos-realm/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'client_id=pagamentos-api' \
  --data-urlencode 'client_secret=YQwLiTeFMJEqY9JZx6W8RJ0tQlSAhiYQ' \
  --data-urlencode 'username=usuario1' \
  --data-urlencode 'password=password'
```

### Resposta do Token

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "not-before-policy": 0,
  "session_state": "10c4cde4-250a-4633-9ab1-6b41dec75c2a",
  "scope": "email profile"
}
```

### Utilizando o Token

Em todas as requisições para a API, inclua o token no cabeçalho "Authorization":

```bash
curl -X GET http://localhost:8080/api/pagamentos \
  --header 'Authorization: Bearer SEU_ACCESS_TOKEN_AQUI'
```

### Renovando o Token

Quando o access_token expirar (padrão: 5 minutos), use o refresh_token para obter um novo:

```bash
curl -X POST http://localhost:8180/auth/realms/pagamentos-realm/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=refresh_token' \
  --data-urlencode 'client_id=pagamentos-api' \
  --data-urlencode 'client_secret=YQwLiTeFMJEqY9JZx6W8RJ0tQlSAhiYQ' \
  --data-urlencode 'refresh_token=SEU_REFRESH_TOKEN_AQUI'
```

## 🔍 Estrutura dos Tokens JWT

Os tokens JWT (JSON Web Tokens) emitidos pelo Keycloak contêm informações importantes:

### Header
```json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "..."
}
```

### Payload
```json
{
  "exp": 1634567890,
  "iat": 1634567590,
  "jti": "...",
  "iss": "http://localhost:8180/auth/realms/pagamentos-realm",
  "sub": "1234567890",
  "typ": "Bearer",
  "azp": "pagamentos-api",
  "session_state": "...",
  "acr": "1",
  "realm_access": {
    "roles": [
      "pagamento_admin"
    ]
  },
  "resource_access": {
    "pagamentos-api": {
      "roles": [
        "pagamento_admin"
      ]
    }
  },
  "scope": "email profile",
  "email_verified": true,
  "name": "Usuário Administrador",
  "preferred_username": "usuario1",
  "given_name": "Usuário",
  "family_name": "Administrador",
  "email": "usuario@pagamentos.com"
}
```

### Entendendo as Claims

- **exp**: Data de expiração do token
- **iss**: Emissor do token (endereço do Keycloak)
- **sub**: Subject (ID do usuário)
- **realm_access.roles**: Papéis do usuário no realm
- **resource_access.{client}.roles**: Papéis do usuário específicos do cliente
- **preferred_username**: Nome de usuário

### Verificando Tokens

Para inspecionar o conteúdo de um token JWT:
1. Acesse https://jwt.io
2. Cole o token no campo "Encoded"
3. O conteúdo decodificado será exibido

## 🔐 Segurança nos Endpoints

### Implementação na API

A API utiliza Spring Security com adaptadores para Keycloak:

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // Configuração básica de segurança
}
```

### Proteção Global

Todos os endpoints da API exigem autenticação, exceto:
- `/api-docs/**` (Documentação OpenAPI)
- `/swagger-ui/**` (Interface Swagger)
- `/h2-console/**` (Console H2 para desenvolvimento)

### Proteção por Método

Controle de acesso granular usando anotações `@PreAuthorize`:

```java
@PreAuthorize("hasRole('pagamento_admin')")
public ResponseEntity<PagamentoDTO> criarPagamento(...) { ... }

@PreAuthorize("hasAnyRole('pagamento_admin', 'pagamento_consulta')")
public ResponseEntity<List<PagamentoDTO>> listarTodos() { ... }
```

### Códigos de Resposta HTTP

| Código | Descrição | Causa |
|--------|-----------|-------|
| 401 Unauthorized | Não autenticado | Token ausente, inválido ou expirado |
| 403 Forbidden | Não autorizado | Token válido, mas sem permissão para o recurso |

## 🔧 Troubleshooting

### Problemas Comuns

1. **Token Inválido (401 Unauthorized)**
   - **Problema**: Token ausente, mal formatado ou expirado
   - **Verificação**: Inspecione o token em jwt.io para ver a data de expiração
   - **Solução**: Obtenha um novo token ou verifique o formato do cabeçalho

2. **Permissões Insuficientes (403 Forbidden)**
   - **Problema**: Usuário não tem os papéis necessários
   - **Verificação**: Verifique o campo "realm_access.roles" no token
   - **Solução**: Use um usuário com os papéis adequados

3. **Erro ao Obter Token**
   - **Problema**: Credenciais incorretas ou cliente inválido
   - **Verificação**: Verifique usuário, senha, client_id e client_secret
   - **Solução**: Corrija as credenciais ou verifique se o Keycloak está rodando

4. **Token Não Renovável**
   - **Problema**: refresh_token expirado ou inválido
   - **Verificação**: Verifique se está usando o refresh_token correto e recente
   - **Solução**: Obtenha um novo par de tokens com autenticação completa

### Logs para Debug

Para habilitar logs detalhados de segurança, adicione ao `application.properties`:

```properties
logging.level.org.springframework.security=DEBUG
logging.level.org.keycloak=DEBUG
```

## 📝 Melhores Práticas

### 1. Gerenciamento de Tokens

- **Armazenamento**: Nunca armazene tokens em localStorage em aplicações web (vulnerável a XSS)
- **Preferência**: Use cookies HttpOnly para aplicações web
- **Renovação**: Implemente renovação automática de tokens antes da expiração
- **Logout**: Revogue tokens ao fazer logout

### 2. Segurança em Produção

- **HTTPS**: Sempre use SSL/TLS em produção
- **Expiração**: Ajuste os tempos de expiração conforme necessidade
   - Access Token: 5-15 minutos
   - Refresh Token: 1-24 horas
- **Secrets**: Use variáveis de ambiente ou cofres de segredos
- **2FA**: Considere ativar autenticação de dois fatores

### 3. Auditoria e Monitoramento

- Ative o log de eventos no Keycloak
- Monitore falhas de autenticação
- Implemente alertas para tentativas suspeitas de acesso

## 📚 Recursos Adicionais

- [Documentação Oficial do Keycloak](https://www.keycloak.org/documentation)
- [Guia do Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [JWT.io](https://jwt.io/) - Ferramenta para decodificar e verificar JWTs
- [OAuth 2.0](https://oauth.net/2/) - Especificação OAuth 2.0
- [OpenID Connect](https://openid.net/connect/) - Especificação OpenID Connect