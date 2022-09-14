package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidHtmlException;
import uk.gov.defra.tracesx.certificate.utils.exception.PdfGenerationException;

import java.io.File;
import java.net.URI;
import java.util.Locale;

@ExtendWith(MockitoExtension.class)
class CertificatePdfGeneratorTest {

  public static final URI BASE_URI = URI.create("");
  @InjectMocks
  private CertificatePdfGenerator pdfGenerator;
  @Mock
  private FontFile fontFile;
  @Mock
  private PdfHttpProvider httpProvider;

  @Test
  void shouldThrowExceptionIfFontFileIsMissing() {
    assertThatThrownBy(() -> new CertificatePdfGenerator(null, null, httpProvider))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldGeneratePdf() throws Exception {
    FontFile fontFile = new FontFile("Times New Roman", "Times New Roman.ttf");
    FontFile fontFileBold = new FontFile("Times New Roman", "Times New Roman.ttf");
    CertificatePdfGenerator testSubject = new CertificatePdfGenerator(fontFile, fontFileBold,
        httpProvider);
    byte[] bytes = testSubject.createPdf("<html lang=\"en\"><p>hello</p></html>", BASE_URI);
    File file = new File("target/hello-test.pdf");
    FileUtils.writeByteArrayToFile(file, bytes);
    PDDocument document = PDDocument.load(file);
    assertThat(document.getDocumentCatalog().getLanguage()).isEqualTo(Locale.UK.getLanguage());
  }

  @Test
  void shouldThrowInvalidHtmlException() {
    assertThatThrownBy(() -> pdfGenerator.createPdf("<broken>content<///", BASE_URI))
        .isInstanceOf(InvalidHtmlException.class);
  }

  @Test
  void shouldThrowPdfGenerationException() {
    assertThatThrownBy(
        () -> pdfGenerator.createPdf("<html lang=\"en\"><p>hello</p></html>", BASE_URI))
        .isInstanceOf(PdfGenerationException.class);
  }
}
