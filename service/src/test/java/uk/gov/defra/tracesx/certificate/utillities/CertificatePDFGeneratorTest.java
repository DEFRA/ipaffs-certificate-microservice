package uk.gov.defra.tracesx.certificate.utillities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.net.URI;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class CertificatePDFGeneratorTest {

  public static final URI BASE_URI = URI.create("");
  private CertificatePDFGenerator pdfGenerator;
  private FontFile fontFile;

  @Before
  public void setUp() throws Exception {
    fontFile = new FontFile("Arial", "Arial Unicode.ttf");
    pdfGenerator = new CertificatePDFGenerator(fontFile);
  }

  @Test
  public void shouldThrowExceptionIfFontFileIsMissing() throws Exception {
    assertThatThrownBy(() -> new CertificatePDFGenerator(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldGeneratePdf() throws Exception {
    final byte[] bytes = pdfGenerator.createPdf("<p>hello</p>", BASE_URI);
    FileUtils.writeByteArrayToFile(new File("target/hello-test.pdf"), bytes);
  }

  @Test
  public void shouldThrowExceptionOnPdfGeneration() throws Exception {
    assertThatThrownBy(() -> pdfGenerator.createPdf("<broken>content<///", BASE_URI))
        .isInstanceOf(InvalidHtmlException.class);
  }
}
