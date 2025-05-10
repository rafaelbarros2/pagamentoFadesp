# Guia de Segurança com Keycloak

Este documento explica detalhadamente como a segurança é implementada na API de Pagamentos usando Keycloak, e como configurar e utilizar autenticação e autorização corretamente.

## 📋 Índice

- [Visão Geral da Segurança](#-visão-geral-da-segurança)
- [Configuração do Keycloak](#-configuração-do-keycloak)
- [Gerando e Usando Tokens](#-gerando-e-usando-tokens)
- [Segurança nos Endpoints](#-segurança-nos-endpoints)
- [Troubleshooting](#-troubleshooting)
- [Configuração para Produção](#-configuração-para-produção)

## 🔒 Visão Geral da Segurança

A API de Pagamentos usa Keycloak como provedor OAuth 2.0/OpenID Connect para autenticação e autorização. Este modelo de segurança oferece várias vantagens:

- **Autenticação robusta**: Suporte a diversos métodos de autenticação
- **Autorização baseada em papéis (RBAC)**: Controle granular de acesso
- **Tokens JWT**: Autenticação stateless e eficiente
- **Padrões de indústria**: Baseado em padrões de segurança amplamente adotados
- **Federação de identidade**: Possibilidade de integração com LDAP, Active Directory, etc.

### Fluxo de Autenticação e Autorização

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

## 🛠 Configuração do Keycloak

### Pré-requisitos

- Docker e Docker Compose instalados
- Java 17 ou superior para a API

### Iniciando o Keycloak

1. **Crie a estrutura de diretórios**:
   ```
   seu-projeto/
   ├── docker-compose.yml
   └── keycloak/
       └── imports/
           └── pagamentos-realm.json
   ```

2. **Execute o Keycloak**:
   ```bash
   docker-compose up -d
   ```

3. **Verifique se o Keycloak está rodando**:
   ```bash
   docker ps
   ```
   Você deve ver um contêiner chamado `keycloak` rodando na porta 8180.

4. **Acesse o Console Admin do Keycloak**:
    - URL: http://localhost:8180
    - Usuário: admin
    - Senha: admin

### Estrutura do Realm Pré-configurado

O arquivo `pagamentos-realm.json` contém a seguinte configuração:

1. **Realm**: `pagamentos-realm`

2. **Clientes**:
    - `pagamentos-api`:
        - Tipo: confidential (com secret)
        - Secret: YQwLiTeFMJEqY9JZx6W8RJ0tQlSAhiYQ
        - Redirects: http://localhost:8080/*
        - Direct Access Grants: Habilitado (para fluxo de password grant)

3. **Papéis do Realm**:
    - `pagamento_admin`: Papel principal para gerenciar pagamentos

4. **Usuários**:
    - `usuario1`:
        - Senha: password
        - Papel atribuído: pagamento_admin

## 🔑 Gerando e Usando Tokens

### Obtendo um Token de Acesso

Usando curl:

```bash
curl -X POST http://localhost:8180/auth/realms/pagamentos-realm/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'client_id=pagamentos-api' \
  --data-urlencode 'client_secret=YQwLiTeFMJEqY9JZx6W8RJ0tQlSAhiYQ' \
  --data-urlencode 'username=usuario1' \
  --data-urlencode 'password=password'
```

Usando Postman:
1. Selecione método POST
2. URL: http://localhost:8180/auth/realms/pagamentos-realm/protocol/openid-connect/token
3. Na aba "Body", selecione "x-www-form-urlencoded"
4. Adicione os campos:
    - grant_type: password
    - client_id: pagamentos-api
    - client_secret: YQwLiTeFMJEqY9JZx6W8RJ0tQlSAhiYQ
    - username: usuario1
    - password: password
5. Clique em "Send"

### Estrutura da Resposta

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

O token tem validade de 5 minutos por padrão. Após esse período, você precisará:
- Obter um novo token com o endpoint /token
- Ou usar o refresh_token para obter um novo access_token

### Renovando o Token com Refresh Token

```bash
curl -X POST http://localhost:8180/auth/realms/pagamentos-realm/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=refresh_token' \
  --data-urlencode 'client_id=pagamentos-api' \
  --data-urlencode 'client_secret=YQwLiTeFMJEqY9JZx6W8RJ0tQlSAhiYQ' \
  --data-urlencode 'refresh_token=SEU_REFRESH_TOKEN_AQUI'
```

## 🔐 Segurança nos Endpoints

A API implementa dois níveis de segurança:

1. **Segurança Global**: Todos os endpoints da API exigem autenticação, exceto:
    - `/api-docs/**` (Documentação OpenAPI)
    - `/swagger-ui/**` (Interface Swagger)
    - `/h2-console/**` (Console H2 para desenvolvimento)

2. **Segurança por Método**: Anotações `@PreAuthorize` são usadas para controle granular de acesso:
    - Todos os endpoints requerem o papel `pagamento_admin`

### Códigos de Resposta Relacionados à Segurança

| Código | Descrição |
|--------|-----------|
| 401 | Não autenticado (token ausente ou inválido) |
| 403 | Não autorizado (autenticado, mas sem permissão) |

## 🔍 Troubleshooting

### Problemas Comuns e Soluções

1. **Erro "401 Unauthorized"**:
    - Verifique se o token foi incluído no cabeçalho Authorization
    - Verifique se o token não expirou (tokens duram 5 minutos por padrão)
    - Verifique se o token está no formato correto: `Bearer seu_token_aqui`

2. **Erro "403 Forbidden"**:
    - Verifique se o usuário tem o papel `pagamento_admin`
    - Verifique no Console do Keycloak se os papéis estão atribuídos corretamente

3. **Não consigo obter um token**:
    - Verifique se o Keycloak está rodando: `docker ps`
    - Verifique se as credenciais (usuário/senha) estão corretas
    - Verifique se o client_id e client_secret estão corretos

4. **Verificar o conteúdo de um token JWT**:
    - Acesse https://jwt.io
    - Cole seu token para decodificar e verificar as claims, incluindo papéis (roles)

### Logs para Debug

Para habilitar logs detalhados de segurança, adicione ao `application.properties`:

```properties
logging.level.org.springframework.security=DEBUG
logging.level.org.keycloak=DEBUG
```

## 🚀 Configuração para Produção

Para ambientes de produção, considere os seguintes ajustes:

1. **SSL/TLS**: Configure HTTPS tanto para o Keycloak quanto para a API
   ```properties
   # No application.properties
   keycloak.ssl-required=all
   server.ssl.enabled=true
   # ... outras configurações SSL
   ```

2. **Segredos**: Use variáveis de ambiente ou ferramentas como Vault para gerenciar segredos
   ```properties
   keycloak.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
   ```

3. **Tempos de Expiração**: Ajuste os tempos de expiração dos tokens conforme necessidade
    - No Console do Keycloak: Realm Settings > Tokens

4. **Outras Melhorias de Segurança**:
    - Habilite autenticação de dois fatores (2FA)
    - Configure políticas de senha mais fortes
    - Implemente limitação de taxa (rate limiting)
    - Configure auditoria de eventos de segurança

---

## 📚 Recursos Adicionais

- [Documentação Oficial do Keycloak](https://www.keycloak.org/documentation)
- [Guia do Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Padrões de Segurança OWASP](https://owasp.org/www-project-top-ten/)

---

*Para qualquer dúvida adicional sobre a implementação de segurança, entre em contato com a equipe de desenvolvimento.*