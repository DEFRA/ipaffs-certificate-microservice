package uk.gov.defra.tracesx.certificate.utils;

import static org.springframework.util.Assert.notNull;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXParseException;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidHtmlException;
import uk.gov.defra.tracesx.certificate.utils.exception.PdfGenerationException;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Locale;

@Component("pdfGenerator")
public class CertificatePdfGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(CertificatePdfGenerator.class);

  private final FontFile fontFile;
  private final FontFile fontFileBold;
  private final PdfHttpProvider httpProvider;

  public CertificatePdfGenerator(FontFile fontFile, FontFile fontFileBold,
      PdfHttpProvider httpProvider) {
    this.httpProvider = httpProvider;
    notNull(fontFile, "fontFile is required");
    this.fontFile = fontFile;
    this.fontFileBold = fontFileBold;
  }

  public byte[] createPdf(String htmlContent, URI baseUri) {
    try {
      long start = System.currentTimeMillis();
      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, baseUri.toString());
        builder.useFastMode();
        builder.usePdfUaAccessbility(true);
        builder.useFont(fontFile.getInputStreamSupplier(), fontFile.getName());
        builder.useFont(fontFileBold.getInputStreamSupplier(), fontFileBold.getName(),
            500, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.useHttpStreamImplementation(httpProvider);
        PdfBoxRenderer pdfBoxRenderer = builder.buildPdfRenderer();
        PDDocument pdDocument = pdfBoxRenderer.getPdfDocument();
        setLanguage(pdDocument, Locale.UK.getLanguage());
        builder.toStream(os);
        builder.usePDDocument(pdDocument);
        builder.run();
        Long conversionDuration = (System.currentTimeMillis() - start);
        LOGGER.info("conversion took: {}", conversionDuration);
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

  private void setLanguage(PDDocument pdDocument, String language) {
    PDDocumentCatalog catalog = pdDocument.getDocumentCatalog();
    catalog.setLanguage(language);
  }
}
