#!/bin/bash

echo "===== Build e Execução da API de Pagamentos ====="

# Verificar se o Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "ERRO: Docker não está instalado. Por favor, instale o Docker primeiro."
    exit 1
fi

# Opção 1: Compilar dentro do Docker e depois executar
echo "Compilando a aplicação dentro do Docker..."

# Criar o diretório target se não existir
mkdir -p target

# Compilar usando um container Docker temporário
echo "Executando Maven dentro do Docker..."
docker run --rm -v "$(pwd)":/app -w /app maven:3.8-openjdk-17 mvn clean package -DskipTests

# Opção 1: Imagem única
if [ "$1" = "single" ]; then
    echo "Construindo imagem única com Keycloak e aplicação..."
    docker build -t pagamentos-api-with-keycloak -f Dockerfile-single .

    echo "Iniciando container..."
    docker run -p 8080:8080 -p 8180:8180 pagamentos-api-with-keycloak
else
    # Opção 2: Docker Compose (padrão)
    echo "Iniciando com Docker Compose..."

    # Garantir que a pasta para o Keycloak existe
    mkdir -p keycloak/imports

    # Verificar se o arquivo de configuração do realm existe
    if [ ! -f "keycloak/imports/pagamentos-realm.json" ]; then
        echo "AVISO: Arquivo pagamentos-realm.json não encontrado."
        echo "Por favor, crie o arquivo keycloak/imports/pagamentos-realm.json antes de continuar."
        exit 1
    fi

    docker-compose up --build
fi