ARG IMAGE_ACR=docker.io
ARG IMAGE_REPOSITORY=defradigital/java
ARG IMAGE_TAG=jre-21

FROM ${IMAGE_ACR}/${IMAGE_REPOSITORY}:${IMAGE_TAG} AS base

USER root

RUN mkdir -p /usr/src/certificate-service
WORKDIR /usr/src/certificate-service

COPY target/TracesX_Certificate.jar /usr/src/certificate-service/
COPY target/agent-libs /usr/src/certificate-service/
COPY lib/applicationinsights.json /usr/src/certificate-service/

RUN chown jreuser /usr/src/certificate-service
USER jreuser

EXPOSE 8000

CMD ["java", "-javaagent:/usr/src/certificate-service/applicationinsights-agent.jar", "-jar", "TracesX_Certificate.jar"]
