package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.openhtmltopdf.extend.FSSupplier;
import org.junit.jupiter.api.Test;
import uk.gov.defra.tracesx.certificate.utils.exception.FontNotFoundException;

import java.io.InputStream;

public class FontFileTest {

  private FontFile arial;

  @Test
  void shouldCreateFontFile() throws Exception {
    arial = new FontFile("Arial", "Arial Unicode.ttf");
    assertThat(arial.getName()).isEqualTo("Arial");
  }

  @Test
  void shouldReturnInputStream() throws Exception {
    arial = new FontFile("Arial", "Arial Unicode.ttf");
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