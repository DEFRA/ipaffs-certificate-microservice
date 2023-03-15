package uk.gov.defra.tracesx.certificate.service;

import java.net.URI;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.defra.tracesx.certificate.model.Certificate;
import uk.gov.defra.tracesx.certificate.utils.CertificatePdfGenerator;
import uk.gov.defra.tracesx.certificate.utils.ReferenceNumberGenerator;
import uk.gov.defra.tracesx.certificate.utils.UriParser;

@Service
public class CertificateService {

  private final CertificatePdfGenerator pdfGenerator;
  private final UriParser uriParser;

  @Autowired
  public CertificateService(CertificatePdfGenerator pdfGenerator, UriParser uriParser) {
    this.pdfGenerator = pdfGenerator;
    this.uriParser = uriParser;
  }

  public Certificate getPdf(ReferenceNumberGenerator reference, Supplier<String> htmlProvider,
      URI uri) {
    String content = htmlProvider.get();
    URI baseUri = uriParser.getBaseUri(uri);
    return new Certificate(reference, pdfGenerator.createPdf(content, baseUri));
  }
}
