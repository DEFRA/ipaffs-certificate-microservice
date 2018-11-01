FROM baseimagesrepo/java:1.8.0

RUN mkdir -p /usr/src/certificate-service
WORKDIR /usr/src/certificate-service

COPY target/TracesX_Certificate.jar /usr/src/certificate-service/
COPY lib/applicationinsights-agent-2.0.0.jar /usr/src/certificate-service/
COPY lib/AI-Agent.xml /usr/src/certificate-service/
EXPOSE 4000

CMD ["java", "-javaagent:/usr/src/certificate-service/applicationinsights-agent-2.0.0.jar", "-jar", "TracesX_Certificate.jar"]
