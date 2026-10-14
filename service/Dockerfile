ARG IMAGE_ACR=docker.io

# Stage 1: Extraction & Preparation (base image mirrored into ACR by the pipeline)
FROM ${IMAGE_ACR}/defradigital/java:jre-21 AS builder

USER root
WORKDIR /builder

# Copy pre-built JARs and config into the builder stage
COPY target/application.jar ./application.jar
COPY target/agent-libs/applicationinsights-agent.jar ./applicationinsights-agent.jar
COPY lib/applicationinsights.json ./applicationinsights.json

# Extract Spring Boot fat JAR into layers (layertools was removed in Spring Boot 4)
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

# ==============================================================================
# Stage 2: Hardened Runtime (Google Distroless)
# ==============================================================================
FROM gcr.io/distroless/java21-debian13:nonroot
WORKDIR /app

# Copy Java cacerts containing Defra certificates from builder (the JRE's default truststore)
COPY --from=builder /opt/java/openjdk/lib/security/cacerts /etc/ssl/certs/java/cacerts

# Copy Azure Application Insights agent & config
COPY --from=builder /builder/applicationinsights-agent.jar ./applicationinsights-agent.jar
COPY --from=builder /builder/applicationinsights.json ./applicationinsights.json

# Copy extracted Spring Boot layers
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

EXPOSE 8000

# Numeric UID/GID (distroless 'nonroot') so runAsNonRoot can verify it without runAsUser
USER 65532:65532

ENTRYPOINT ["java", \
            "-javaagent:/app/applicationinsights-agent.jar", \
            "org.springframework.boot.loader.launch.JarLauncher"]
