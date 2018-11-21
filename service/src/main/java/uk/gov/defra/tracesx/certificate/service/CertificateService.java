package uk.gov.defra.tracesx.certificate.service;

import java.net.URI;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;
import uk.gov.defra.tracesx.certificate.utillities.CertificatePDFGenerator;
import uk.gov.defra.tracesx.certificate.utillities.UriParser;

@Service
public class CertificateService {

  private final CertificatePDFGenerator pdfGenerator;
  private final UriParser uriParser;

  @Autowired
  public CertificateService(CertificatePDFGenerator pdfGenerator, UriParser uriParser) {
    this.pdfGenerator = pdfGenerator;
    this.uriParser = uriParser;
  }

  public Certificate getPdf(String reference, Supplier<String> htmlProvider, URI uri) {
    String content = htmlProvider.get();
    URI baseUri = uriParser.getBaseUri(uri);
    return new Certificate(reference, pdfGenerator.createPdf(content, baseUri));
  }
}
