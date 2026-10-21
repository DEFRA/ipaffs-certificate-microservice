package uk.gov.defra.tracesx.certificate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class})
@ComponentScan({"uk.gov.defra.tracesx"})
public class CertificateApplication {
  public static void main(String[] args) {
    SpringApplication.run(CertificateApplication.class, args);
  }
}
