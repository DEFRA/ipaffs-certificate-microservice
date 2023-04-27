package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.openhtmltopdf.extend.FSSupplier;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import uk.gov.defra.tracesx.certificate.utils.exception.FontNotFoundException;

public class FontFileTest {

  private FontFile arial;

  @Test
  void shouldCreateFontFile() throws Exception {
    arial = new FontFile("Times New Roman", "Times New Roman.ttf");
    assertThat(arial.getName()).isEqualTo("Times New Roman");
  }

  @Test
  void shouldReturnInputStream() throws Exception {
    arial = new FontFile("Times New Roman", "Times New Roman.ttf");
    FSSupplier<InputStream> supplier = arial.getInputStreamSupplier();
    InputStream inputStream = supplier.supply();
    assertThat(inputStream.available()).isGreaterThan(0);
    inputStream.close();
  }

  @Test
  void shouldThrowFontNotFoundException() {
    assertThatThrownBy(() -> new FontFile("Arial", "Missing Font.ttf"))
        .isInstanceOf(FontNotFoundException.class);
  }
}
