#!/bin/bash
# Script para deploy da API de Pagamentos com Keycloak
# Para uso em ambientes Linux/macOS

echo "===== Deploy da API de Pagamentos com Keycloak ====="
echo "Iniciando o processo..."

# Verificar se o Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "ERRO: Docker não está instalado. Por favor, instale o Docker primeiro."
    exit 1
fi

# Verificar se o Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
    echo "ERRO: Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro."
    exit 1
fi

# Verificar se o Maven está instalado (necessário apenas se for compilar)
if ! command -v mvn &> /dev/null; then
    echo "AVISO: Maven não está instalado. Pulando etapa de compilação."
    MVN_INSTALLED=false
else
    MVN_INSTALLED=true
fi

# Verificar se o JAR já existe
if [ ! -f "target/pagamento-api-0.0.1-SNAPSHOT.jar" ]; then
    if [ "$MVN_INSTALLED" = true ]; then
        echo "Compilando o projeto..."
        mvn clean package -DskipTests
        if [ $? -ne 0 ]; then
            echo "ERRO: Falha na compilação do projeto."
            exit 1
        fi
    else
        echo "ERRO: O arquivo JAR não existe e o Maven não está instalado para compilá-lo."
        exit 1
    fi
fi

# Criar a estrutura de diretórios para o Keycloak
echo "Criando estrutura de diretórios..."
mkdir -p keycloak/imports/

# Verificar se o arquivo de configuração do realm existe
if [ ! -f "keycloak/imports/pagamentos-realm.json" ]; then
    echo "AVISO: Arquivo pagamentos-realm.json não encontrado."
    echo "Por favor, crie o arquivo keycloak/imports/pagamentos-realm.json antes de continuar."
    exit 1
fi

# Verificar se o docker-compose.yml existe
if [ ! -f "docker-compose.yml" ]; then
    echo "ERRO: Arquivo docker-compose.yml não encontrado."
    exit 1
fi

# Verificar se o Dockerfile existe
if [ ! -f "Dockerfile" ]; then
    echo "ERRO: Arquivo Dockerfile não encontrado."
    exit 1
fi

# Iniciar os serviços com Docker Compose
echo "Iniciando serviços com Docker Compose..."
docker-compose up -d --build

if [ $? -ne 0 ]; then
    echo "ERRO: Falha ao iniciar os serviços com Docker Compose."
    exit 1
fi

# Verificar se os serviços estão em execução
echo "Verificando serviços..."
sleep 5
docker-compose ps

echo "===== Deploy concluído com sucesso! ====="
echo "Acesse os serviços em:"
echo "API: http://localhost:8080"
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
echo "Keycloak: http://localhost:8180"
echo ""
echo "Para parar os serviços, execute: docker-compose down"
echo "Para ver os logs, execute: docker-compose logs"