# Imagem do backend para o Render (que nao tem runtime nativo de Java, so Docker).
# Build local: docker build -t consulta-api server
# Rodar local: docker run --rm -p 8080:8080 --env-file server/.env consulta-api

# ---- Estagio 1: compila o jar com o JDK ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Dependencias primeiro: so baixam de novo quando o pom.xml muda (cache de camada do Docker)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src/ src/
# Testes ficam na CI (GitHub Actions); aqui so empacota
RUN ./mvnw -B -ntp -DskipTests package && cp target/*.jar app.jar

# ---- Estagio 2: so o JRE e o jar ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sem privilegio: se a aplicacao for comprometida, nao roda como root
RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=build --chown=app:app /app/app.jar app.jar
USER app

# Usa ate 75% da memoria do container (plano free do Render: 512 MB) e reinicia em vez de travar sem memoria
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

# O Render injeta PORT; a aplicacao le em server.port (padrao 8080)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
