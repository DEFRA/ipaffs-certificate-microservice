package uk.gov.defra.tracesx.integration.certificate.models.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import uk.gov.defra.tracesx.integration.certificate.helpers.CertificateServiceHelper;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Certificate {

  private String id;
  private byte[] content;

  private static Logger logger;

  public static uk.gov.defra.tracesx.integration.certificate.entity.Certificate newInstance() {

    logger = Logger.getLogger(CertificateServiceHelper.class.getName());

    uk.gov.defra.tracesx.integration.certificate.entity.Certificate certificate =
        new uk.gov.defra.tracesx.integration.certificate.entity.Certificate();
    certificate.setReferenceNumber(UUID.randomUUID().toString());
    certificate.setDocument(new byte[30]);
    return certificate;
  }
}
