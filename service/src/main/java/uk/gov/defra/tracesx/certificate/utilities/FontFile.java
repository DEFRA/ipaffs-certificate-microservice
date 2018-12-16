package uk.gov.defra.tracesx.certificate.utilities;

import com.openhtmltopdf.extend.FSSupplier;
import java.io.IOException;
import java.io.InputStream;
import uk.gov.defra.tracesx.certificate.utilities.exception.FontNotFoundException;

public class FontFile {

  private final String name;
  private final FSSupplier<InputStream> fsSupplier;

  public FontFile(String name, String filename) {
    this.name = name;
    fsSupplier = new FSSupplier<InputStream>() {
      @Override
      public InputStream supply() {
        return getClass().getClassLoader().getResourceAsStream(filename);
      }
    };
    validateInputStream(filename);
  }

  private void validateInputStream(String filename) {
    try (InputStream inputStream = fsSupplier.supply()) {
      if(inputStream == null) {
        throw new FontNotFoundException(filename);
      }
    } catch (IOException ex) {
      throw new FontNotFoundException(filename);
    }
  }

  public String getName() {
    return name;
  }

  public FSSupplier<InputStream> getInputStreamSupplier() {
    return fsSupplier;
  }
}
