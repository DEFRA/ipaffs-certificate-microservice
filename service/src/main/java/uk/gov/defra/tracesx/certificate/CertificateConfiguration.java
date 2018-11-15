package uk.gov.defra.tracesx.certificate;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.defra.tracesx.certificate.service.blobstorage.BlobStorage;
import uk.gov.defra.tracesx.certificate.service.blobstorage.Storage;

@Configuration
@EnableConfigurationProperties
public class CertificateConfiguration {
  // Custom Configuration properties can be loaded here

  @Bean
  public Storage getBlobStorage() {
    return new BlobStorage();
  }
}
