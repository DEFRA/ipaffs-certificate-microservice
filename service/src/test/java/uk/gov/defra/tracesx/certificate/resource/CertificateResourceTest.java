package uk.gov.defra.tracesx.certificate.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.defra.tracesx.certificate.model.Certificate;
import uk.gov.defra.tracesx.certificate.service.CertificateService;
import uk.gov.defra.tracesx.certificate.utils.ReferenceNumberGenerator;

@ExtendWith(MockitoExtension.class)
class CertificateResourceTest {

  private static final ReferenceNumberGenerator REFERENCE = new ReferenceNumberGenerator("CHEDD.GB.2018.1010007");
  private static final String URL = "http://somewebsite.com/certificate/001";
  private static final URI URI = java.net.URI.create(URL);

  @Mock
  private CertificateService certificateService;
  private CertificateResource resource;
  private byte[] expectedBinaryData = new byte[20];
  private Certificate certificate = new Certificate(REFERENCE, expectedBinaryData);

  @Test
  void shouldCreateCertificateWithHtmlContent() throws Exception {
    givenValidResource();
    when(certificateService.getPdf(eq(REFERENCE), any(Supplier.class), eq(URI))).thenReturn(certificate);

    final ResponseEntity<byte[]> response = resource.getCertificateFromContent(
        REFERENCE, "<html></html>", URL);

    assertThat(response.getBody()).isEqualTo(expectedBinaryData);
    ArgumentCaptor<Supplier<String>> argCaptor = ArgumentCaptor.forClass(Supplier.class);
    verify(certificateService).getPdf(eq(REFERENCE), argCaptor.capture(), eq(URI));
    assertThat(argCaptor.getValue().get()).isEqualTo("<html></html>");
  }

  private void givenValidResource() {
    new Random().nextBytes(expectedBinaryData);
    certificate = new Certificate(REFERENCE, expectedBinaryData);
    certificateService = mock(CertificateService.class);
    resource = new CertificateResource(certificateService);
  }
}
