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

# Verificar se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "ERRO: Maven não está instalado. Por favor, instale o Maven primeiro."
    exit 1
fi

# Verificar Java e ajustar se necessário
echo "Verificando versão do Java..."
java -version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
USE_JAVA_OPTS=true

# Compilar o projeto com configurações explícitas para Java 17
echo "Compilando o projeto..."
if [ "$USE_JAVA_OPTS" = true ]; then
    echo "Usando configurações explícitas para Java 17..."
    mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 -Djava.version=17 -Dfile.encoding=UTF-8
else
    mvn clean package -DskipTests
fi

# Verificar se a compilação foi bem-sucedida
if [ $? -ne 0 ]; then
    echo "ERRO: Falha na compilação do projeto."
    echo "Tentando método alternativo..."

    # Tentar encontrar uma instalação do Java 17
    JAVA17_PATH=$(find /usr/lib/jvm /Library/Java/JavaVirtualMachines -name "*17*" -type d 2>/dev/null | head -n 1)

    if [ -n "$JAVA17_PATH" ]; then
        echo "Encontrado Java 17 em $JAVA17_PATH, tentando usar..."

        # Para Linux
        if [ -d "$JAVA17_PATH/bin" ]; then
            TEMP_JAVA_HOME=$JAVA_HOME
            export JAVA_HOME=$JAVA17_PATH
        # Para macOS
        elif [ -d "$JAVA17_PATH/Contents/Home" ]; then
            TEMP_JAVA_HOME=$JAVA_HOME
            export JAVA_HOME=$JAVA17_PATH/Contents/Home
        fi

        mvn clean package -DskipTests
        export JAVA_HOME=$TEMP_JAVA_HOME
    else
        echo "ERRO: Não foi possível encontrar o Java 17. Verifique se está instalado."
        exit 1
    fi

    if [ $? -ne 0 ]; then
        echo "ERRO: Falha na compilação do projeto após tentativas alternativas."
        exit 1
    fi
fi

# Verificar se o JAR foi criado
if [ ! -f "target/pagamento-api-0.0.1-SNAPSHOT.jar" ]; then
    echo "ERRO: O arquivo JAR não foi gerado na compilação."
    exit 1
fi

# Criar a estrutura de diretórios para o Keycloak
echo "Criando estrutura de diretórios..."
mkdir -p keycloak/imports/

# Verificar se o arquivo de configuração do realm existe
if [ ! -f "keycloak/imports/pagamentos-realm.json" ]; then
    echo "AVISO: Arquivo pagamentos-realm.json não encontrado."
    echo "Criando arquivo de configuração do realm..."

    # Copiar o arquivo de configuração do realm de um local padrão ou criar um básico
    if [ -f "src/main/resources/pagamentos-realm.json" ]; then
        cp "src/main/resources/pagamentos-realm.json" "keycloak/imports/"
    else
        echo "ERRO: Não foi possível encontrar o arquivo de configuração do realm."
        echo "Por favor, crie o arquivo keycloak/imports/pagamentos-realm.json antes de continuar."
        exit 1
    fi
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

# Parando serviços existentes antes de iniciar
echo "Parando serviços existentes..."
docker-compose down

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

# Esperar um pouco mais para os serviços estarem totalmente inicializados
echo "Aguardando inicialização completa dos serviços..."
sleep 20

echo "===== Deploy concluído com sucesso! ====="
echo "Acesse os serviços em:"
echo "API: http://localhost:8080"
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
echo "Keycloak: http://localhost:8180"
echo ""
echo "Para obter um token de acesso, execute:"
echo "curl -X POST http://localhost:8180/auth/realms/pagamentos-realm/protocol/openid-connect/token \\"
echo "  --header 'Content-Type: application/x-www-form-urlencoded' \\"
echo "  --data-urlencode 'grant_type=password' \\"
echo "  --data-urlencode 'client_id=pagamentos-api' \\"
echo "  --data-urlencode 'client_secret=YQwLiTeFMJEqY9JZx6W8RJ0tQlSAhiYQ' \\"
echo "  --data-urlencode 'username=usuario1' \\"
echo "  --data-urlencode 'password=password'"
echo ""
echo "Para parar os serviços, execute: docker-compose down"
echo "Para ver os logs, execute: docker-compose logs"