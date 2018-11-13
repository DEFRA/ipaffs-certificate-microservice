package uk.gov.defra.tracesx.certificate.resource;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;
import uk.gov.defra.tracesx.certificate.service.CertificateService;

public class CertificateResourceTest {

  @Mock CertificateService certificateService;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void downloadCertificateReturnsExpectedResponse() {
    CertificateResource resource = new CertificateResource(certificateService);
    String referenceNumber = UUID.randomUUID().toString();
    String etag = UUID.randomUUID().toString();

    byte[] expectedBinaryData = new byte[20];
    new Random().nextBytes(expectedBinaryData);
    Certificate expectedCertificate = new Certificate(referenceNumber, expectedBinaryData);

    when(certificateService.getCertificate(any(), any())).thenReturn(expectedCertificate);


    ResponseEntity response = resource.getCertificate(referenceNumber, etag);

    assertThat(HttpStatus.OK, equalTo(response.getStatusCode()));
    assertEquals((byte[]) response.getBody(), expectedBinaryData);
  }
}
