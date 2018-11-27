package uk.gov.defra.tracesx.certificate.resource;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;
import uk.gov.defra.tracesx.certificate.service.CertificateService;

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
  public ResponseEntity<byte[]> getCertificateFromContent(
      @PathVariable String reference,
      @RequestBody String htmlContent,
      @RequestParam String url) {
    LOGGER.info("POST reference: {}", reference);
    Certificate certificate = certificateService.getPdf(reference, () -> htmlContent, URI.create(url));
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reference + ".pdf\"")
        .body(certificate.getDocument());
  }

}
