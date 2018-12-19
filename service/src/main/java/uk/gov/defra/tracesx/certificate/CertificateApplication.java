package uk.gov.defra.tracesx.certificate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"uk.gov.defra.tracesx"})
public class CertificateApplication {
  public static void main(String[] args) {
    SpringApplication.run(CertificateApplication.class, args);
  }
}
