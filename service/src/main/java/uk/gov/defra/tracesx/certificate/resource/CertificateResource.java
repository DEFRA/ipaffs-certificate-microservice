package uk.gov.defra.tracesx.certificate.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.defra.tracesx.certificate.service.CertificateService;

@RestController
@RequestMapping("/certificate")
public class CertificateResource {

  private static final String CONTENT_TYPE_MERGE_PATCH = "application/merge-patch+json";
  private static final String CONTENT_TYPE_COMMAND_PATCH = "application/json-patch+json";
  private static final Logger LOGGER = LoggerFactory.getLogger(CertificateResource.class);

  private final CertificateService certificateService;

  @Autowired
  public CertificateResource(CertificateService certificateService) {
    this.certificateService = certificateService;
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity insert(@RequestBody JsonNode entity) throws URISyntaxException {
    UUID id = certificateService.create(entity);
    LOGGER.info("POST id: {}", id);
    URI createdLocation = new URI("/certificate/" + id.toString());
    return ResponseEntity.created(createdLocation).build();
  }

  @GetMapping(value = "/download/{id}")
  public ResponseEntity<byte[]> downloadCertificate(@PathVariable("id") UUID id)
      throws IOException {
    LOGGER.info("GET id: {}", id);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + id + ".pdf\"")
        .body(certificateService.downloadCertificate(id));
  }

  @PatchMapping(
      value = "/{id}",
      consumes = {
          CONTENT_TYPE_MERGE_PATCH,
          CONTENT_TYPE_COMMAND_PATCH,
          MediaType.APPLICATION_JSON_VALUE,
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity patch(
      @PathVariable("id") UUID id,
      @RequestHeader(value = "Content-Type") String contentType,
      @RequestBody JsonNode patch)
      throws JsonPatchException, IOException {

    boolean mergePatch = false;
    if (contentType.equals(CONTENT_TYPE_MERGE_PATCH)) {
      mergePatch = true;
    }

    certificateService.update(id, patch, mergePatch);
    LOGGER.info("PATCH id: {}", id);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity delete(@PathVariable("id") UUID id) {
    certificateService.deleteData(id);
    LOGGER.info("DELETE id: {}", id);
    return ResponseEntity.ok().build();
  }
}