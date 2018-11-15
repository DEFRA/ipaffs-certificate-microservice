package uk.gov.defra.tracesx.certificate.utillities;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("pdfGenerator")
public class CertificatePDFGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger(CertificatePDFGenerator.class);

  public byte[] generate(String referenceNumber) {
    String filePath = "tempCertificate-" + referenceNumber + ".pdf";
    try {
      Document document = new Document();
      PdfWriter.getInstance(document, new FileOutputStream(filePath));
      document.open();
      document.add(new Paragraph(referenceNumber));
      document.close();
      File pdf = new File(filePath);
      byte[] file = Files.readAllBytes(pdf.toPath());
      Files.delete(pdf.toPath());
      return file;
    } catch (IOException | DocumentException e) {
      LOGGER.error(String.format("Exception occurred while generating PDF: %s", e.getMessage()));
      return null;
    }
  }
}
