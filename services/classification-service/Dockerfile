# services/classification-service/Dockerfile
FROM openjdk:17-jre-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/Classification-Service-*.jar app.jar

EXPOSE 8087

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

ENTRYPOINT ["java", "-Xmx768m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]