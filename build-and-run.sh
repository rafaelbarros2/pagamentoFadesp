#!/bin/bash

# Compilar a aplicação
echo "Compilando a aplicação..."
mvn clean package -DskipTests

# Opção 1: Imagem única
if [ "$1" = "single" ]; then
    echo "Construindo imagem única com Keycloak e aplicação..."
    docker build -t pagamentos-api-with-keycloak -f Dockerfile-single .

    echo "Iniciando container..."
    docker run -p 8080:8080 -p 8180:8180 pagamentos-api-with-keycloak
else
    # Opção 2: Docker Compose (padrão)
    echo "Iniciando com Docker Compose..."
    docker-compose up --build
fi