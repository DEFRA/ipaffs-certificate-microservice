package uk.gov.defra.tracesx.certificate.utillities;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import org.springframework.stereotype.Component;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;

@Component("pdfGenerator")
public class CertificatePDFGenerator {
  public byte[] generate(Certificate certificate) {
    String filePath = "tempCertificate-" + certificate.getId() + ".pdf";
    try {
      Document document = new Document();
      PdfWriter.getInstance(document, new FileOutputStream(filePath));
      document.open();
      document.add(new Paragraph(certificate.getId().toString()));
      document.close();
      File pdf = new File(filePath);
      byte[] file = Files.readAllBytes(pdf.toPath());
      Files.delete(pdf.toPath());
      return file;
    } catch (Exception e) {
      return null;
    }
  }
}
