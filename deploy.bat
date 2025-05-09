@echo off
REM Script para deploy da API de Pagamentos com Keycloak
REM Para uso em ambientes Windows

echo ===== Deploy da API de Pagamentos com Keycloak =====
echo Iniciando o processo...

REM Verificar se o Docker está instalado
WHERE docker >nul 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo ERRO: Docker não está instalado. Por favor, instale o Docker primeiro.
    exit /b 1
)

REM Verificar se o Docker Compose está instalado
WHERE docker-compose >nul 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo ERRO: Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro.
    exit /b 1
)

REM Verificar se o Maven está instalado
WHERE mvn >nul 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo ERRO: Maven não está instalado. Maven é necessário para compilar o projeto.
    exit /b 1
)

REM Sempre compilar o projeto usando mvn clean install
echo Compilando o projeto com Maven...
mvn clean install
IF %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilação do projeto.
    exit /b 1
)

REM Criar a estrutura de diretórios para o Keycloak
echo Criando estrutura de diretórios...
mkdir keycloak\imports 2>nul

REM Verificar se o arquivo de configuração do realm existe
IF NOT EXIST "keycloak\imports\pagamentos-realm.json" (
    echo AVISO: Arquivo pagamentos-realm.json não encontrado.
    echo Por favor, crie o arquivo keycloak\imports\pagamentos-realm.json antes de continuar.
    exit /b 1
)

REM Verificar se o docker-compose.yml existe
IF NOT EXIST "docker-compose.yml" (
    echo ERRO: Arquivo docker-compose.yml não encontrado.
    exit /b 1
)

REM Verificar se o Dockerfile existe
IF NOT EXIST "Dockerfile" (
    echo ERRO: Arquivo Dockerfile não encontrado.
    exit /b 1
)

REM Iniciar os serviços com Docker Compose
echo Parando serviços existentes...
docker-compose down

echo Iniciando serviços com Docker Compose...
docker-compose up -d --build

IF %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao iniciar os serviços com Docker Compose.
    exit /b 1
)

REM Verificar se os serviços estão em execução
echo Verificando serviços...
timeout /t 5 >nul
docker-compose ps

echo ===== Deploy concluído com sucesso! =====
echo Acesse os serviços em:
echo API: http://localhost:8080
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo Keycloak: http://localhost:8180
echo.
echo Para parar os serviços, execute: docker-compose down
echo Para ver os logs, execute: docker-compose logs