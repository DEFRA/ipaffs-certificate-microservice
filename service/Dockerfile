FROM sndeuxfesacr001.azurecr.io/java-20191011:alpine3.10.2-javaJRE11

RUN mkdir -p /usr/src/certificate-service
WORKDIR /usr/src/certificate-service

COPY target/TracesX_Certificate.jar /usr/src/certificate-service/
COPY lib/applicationinsights-agent-2.5.0.jar /usr/src/certificate-service/
COPY lib/AI-Agent.xml /usr/src/certificate-service/

RUN chown jreuser /usr/src/certificate-service
USER jreuser

EXPOSE 4000

CMD ["java", "-javaagent:/usr/src/certificate-service/applicationinsights-agent-2.5.0.jar", "-jar", "TracesX_Certificate.jar"]
