package uk.gov.defra.tracesx.certificate.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

  @GetMapping(value = "/{referenceNumber}/{etag}")
  public ResponseEntity<byte[]> getCertificate(
      @PathVariable("referenceNumber") String referenceNumber, @PathVariable("etag") String etag) {
    LOGGER.info("GET referenceNumber: {}, etag: {}", referenceNumber, etag);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + referenceNumber + ".pdf\"")
        .body(certificateService.getCertificate(referenceNumber, etag).getDocument());
  }
}
