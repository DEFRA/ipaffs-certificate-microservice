package uk.gov.defra.tracesx.certificate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
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
