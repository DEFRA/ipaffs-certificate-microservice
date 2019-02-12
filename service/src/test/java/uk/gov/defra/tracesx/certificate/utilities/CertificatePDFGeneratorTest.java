package uk.gov.defra.tracesx.certificate.utilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.StringReader;
import java.net.URI;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.assertj.core.internal.ByteArrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.tracesx.certificate.utilities.exception.InvalidHtmlException;

@RunWith(MockitoJUnitRunner.class)
public class CertificatePDFGeneratorTest {

  public static final URI BASE_URI = URI.create("");
  private CertificatePDFGenerator pdfGenerator;
  private FontFile fontFile;
  @Mock
  private PdfHttpProvider httpProvider;

  @Before
  public void setUp() throws Exception {
    fontFile = new FontFile("Arial", "Arial Unicode.ttf");
    pdfGenerator = new CertificatePDFGenerator(fontFile, httpProvider);
  }

  @Test
  public void shouldThrowExceptionIfFontFileIsMissing() throws Exception {
    assertThatThrownBy(() -> new CertificatePDFGenerator(null, httpProvider))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldGeneratePdf() throws Exception {
    final byte[] bytes = pdfGenerator.createPdf("<p>hello</p>", BASE_URI);
    File file = new File("target/hello-test.pdf");
    FileUtils.writeByteArrayToFile(file, bytes);
    PDDocument document = PDDocument.load(file);
    assertThat(document.getDocumentCatalog().getLanguage()).isEqualTo(CertificatePDFGenerator.ENGLISH);
  }

  @Test
  public void shouldThrowExceptionOnPdfGeneration() throws Exception {
    assertThatThrownBy(() -> pdfGenerator.createPdf("<broken>content<///", BASE_URI))
        .isInstanceOf(InvalidHtmlException.class);
  }
}
