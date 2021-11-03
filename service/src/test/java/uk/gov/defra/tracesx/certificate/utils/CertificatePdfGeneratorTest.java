package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidHtmlException;

import java.io.File;
import java.net.URI;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
public class CertificatePdfGeneratorTest {

  public static final URI BASE_URI = URI.create("");
  private CertificatePdfGenerator pdfGenerator;
  private FontFile fontFile;
  private FontFile fontFileBold;
  @Mock
  private PdfHttpProvider httpProvider;

  @Before
  public void setUp() throws Exception {
    fontFile = new FontFile("Times New Roman", "Times New Roman.ttf");
    fontFileBold = new FontFile("Tomes New Roman Bold", "Times New Roman Bold.ttf");
    pdfGenerator = new CertificatePdfGenerator(fontFile, fontFileBold, httpProvider);
  }

  @Test
  public void shouldThrowExceptionIfFontFileIsMissing() throws Exception {
    assertThatThrownBy(() -> new CertificatePdfGenerator(null, null, httpProvider))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldGeneratePdf() throws Exception {
    final byte[] bytes = pdfGenerator.createPdf("<html lang=\"en\"><p>hello</p></html>", BASE_URI);
    File file = new File("target/hello-test.pdf");
    FileUtils.writeByteArrayToFile(file, bytes);
    PDDocument document = PDDocument.load(file);
    assertThat(document.getDocumentCatalog().getLanguage()).isEqualTo(Locale.UK.getLanguage());
  }

  @Test
  public void shouldThrowExceptionOnPdfGeneration() throws Exception {
    assertThatThrownBy(() -> pdfGenerator.createPdf("<broken>content<///", BASE_URI))
        .isInstanceOf(InvalidHtmlException.class);
  }
}
