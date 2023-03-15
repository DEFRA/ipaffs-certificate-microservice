package uk.gov.defra.tracesx.certificate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.function.Supplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.tracesx.certificate.model.Certificate;
import uk.gov.defra.tracesx.certificate.utils.CertificatePdfGenerator;
import uk.gov.defra.tracesx.certificate.utils.ReferenceNumberGenerator;
import uk.gov.defra.tracesx.certificate.utils.UriParser;

@RunWith(MockitoJUnitRunner.class)
public class CertificateServiceTest {

  private static final ReferenceNumberGenerator REFERENCE = ReferenceNumberGenerator.valueOf("CHEDP.GB.2018.1234567");
  private static final URI CERT_LOCATION = java.net.URI.create("http://ins.gov/certificate/001");
  private static final URI BASE_URI = URI.create("http://ins.gov");
  private static final String CERT_HTML_CONTENT = "<html lang='en'>cert content</html>";

  @Mock
  private CertificatePdfGenerator pdfGenerator;
  private Supplier<String> stringSupplier = () -> CERT_HTML_CONTENT;
  private UriParser uriParser = new UriParser();
  @Mock
  private CertificateService certificateService;
  private byte[] pdfBytes = new byte[200];

  @Test
  public void shouldCreateCertificateWithHtmlContent() throws UnsupportedEncodingException {
    givenService();
    when(pdfGenerator.createPdf(CERT_HTML_CONTENT, BASE_URI)).thenReturn(pdfBytes);
    final Certificate pdf = certificateService.getPdf(REFERENCE, stringSupplier, CERT_LOCATION);

    assertThat(pdf.getReferenceNumber()).isEqualTo(REFERENCE);
    assertThat(pdf.getDocument()).isEqualTo(pdfBytes);
    verify(pdfGenerator).createPdf(CERT_HTML_CONTENT, BASE_URI);
  }

  private void givenService() {
    certificateService = new CertificateService(pdfGenerator, uriParser);
  }
}
