ARG IMAGE_ACR
ARG IMAGE_REPOSITORY
ARG IMAGE_TAG

FROM $IMAGE_ACR/$IMAGE_REPOSITORY:$IMAGE_TAG AS base

USER root

RUN mkdir -p /usr/src/certificate-service
WORKDIR /usr/src/certificate-service

COPY target/TracesX_Certificate.jar /usr/src/certificate-service/
COPY lib/applicationinsights-agent.jar /usr/src/certificate-service/
COPY lib/applicationinsights.json /usr/src/certificate-service/

RUN chown jreuser /usr/src/certificate-service
USER jreuser

EXPOSE 8080

CMD ["java", "-javaagent:/usr/src/certificate-service/applicationinsights-agent.jar", "-jar", "TracesX_Certificate.jar"]
