ARG BASE_IMAGE

FROM sndeuxfesacr001.azurecr.io/$BASE_IMAGE

RUN mkdir -p /usr/src/certificate-service
WORKDIR /usr/src/certificate-service

COPY target/TracesX_Certificate.jar /usr/src/certificate-service/
COPY lib/applicationinsights-agent.jar /usr/src/certificate-service/
COPY lib/AI-Agent.xml /usr/src/certificate-service/

RUN chown jreuser /usr/src/certificate-service
USER jreuser

EXPOSE 4000

CMD ["java", "-javaagent:/usr/src/certificate-service/applicationinsights-agent.jar", "-jar", "TracesX_Certificate.jar"]
