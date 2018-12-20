package uk.gov.defra.tracesx.certificate.utilities;

import static org.springframework.util.Assert.notNull;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXParseException;
import uk.gov.defra.tracesx.certificate.utilities.exception.InvalidHtmlException;
import uk.gov.defra.tracesx.certificate.utilities.exception.PdfGenerationException;

@Component("pdfGenerator")
public class CertificatePDFGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(CertificatePDFGenerator.class);

  private final FontFile fontFile;
  private final PdfHttpProvider httpProvider;

  public CertificatePDFGenerator(FontFile fontFile, PdfHttpProvider httpProvider) {
    this.httpProvider = httpProvider;
    notNull(fontFile, "fontFile is required");
    this.fontFile = fontFile;
  }

  public byte[] createPdf(String htmlContent, URI baseUri) {
    try {
      long start = System.currentTimeMillis();
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, baseUri.toString());
        builder.useFont(fontFile.getInputStreamSupplier(), fontFile.getName());
        builder.useHttpStreamImplementation(httpProvider);
        builder.toStream(os);
        builder.run();
        LOGGER.info("conversion took: " + (System.currentTimeMillis() - start));
        return os.toByteArray();
      }
    } catch (Exception ex) {
      Throwable rootCause = NestedExceptionUtils.getRootCause(ex);
      if (rootCause instanceof SAXParseException) {
        throw new InvalidHtmlException("exception parsing html", ex);
      }
      throw new PdfGenerationException("exception creating pdf", ex);
    }
  }

}
