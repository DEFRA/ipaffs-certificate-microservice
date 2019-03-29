package uk.gov.defra.tracesx.certificate.resource;

import java.io.IOException;
import java.net.URI;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;
import uk.gov.defra.tracesx.certificate.service.CertificateService;
import uk.gov.defra.tracesx.certificate.utilities.HtmlValidator;

@RestController
@RequestMapping("/certificate")
public class CertificateResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(CertificateResource.class);

  private final CertificateService certificateService;

  @Autowired
  public CertificateResource(CertificateService certificateService) {
    this.certificateService = certificateService;
  }

  @PostMapping(value = "/{reference}")
  @PreAuthorize("hasAuthority('certificate.create')")
  public ResponseEntity<byte[]> getCertificateFromContent(
      @PathVariable ReferenceNumber reference,
      @RequestBody String unsafeHtmlContent,
      @RequestParam String url) throws ParserConfigurationException, IOException {

    LOGGER.info("POST reference: {}", reference);
    HtmlValidator.validate(unsafeHtmlContent);
    Certificate certificate = certificateService.getPdf(reference, () -> Sanitizer.sanitize(unsafeHtmlContent), URI.create(url));

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reference.valueOf() + ".pdf\"")
        .body(certificate.getDocument());
  }
}
