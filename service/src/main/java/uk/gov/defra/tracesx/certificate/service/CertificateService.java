package uk.gov.defra.tracesx.certificate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;
import java.io.IOException;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;
import uk.gov.defra.tracesx.certificate.dao.repositories.CertificateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.everit.json.schema.ValidationException;
import uk.gov.defra.tracesx.certificate.utillities.CertificatePDFGenerator;

@Service
public class CertificateService {

  private final CertificateRepository certificateRepository;
  private final CertificatePDFGenerator pdfGenerator;
  protected final Schema schema;
  private final ObjectMapper mapper;

  @Autowired
  public CertificateService(CertificateRepository certificateRepository,
      CertificatePDFGenerator pdfGenerator, ObjectMapper mapper) throws IOException {
    this.certificateRepository = certificateRepository;
    this.pdfGenerator = pdfGenerator;
    this.mapper = mapper;
    this.schema = loadSchema();
  }

  public UUID create(JsonNode document) {
    validateAgainstSchema(document);
    Certificate certificate = new Certificate();
    certificate.setDocument(document.toString());
    return certificateRepository.save(certificate).getId();
  }

  public JsonNode get(UUID id) throws IOException {
    Certificate certificate = certificateRepository.findById(id).get();
    return mapper.readTree(certificate.getDocument());
  }

  public byte[] downloadCertificate(UUID id) {
    Certificate certificate = new Certificate();
    certificate.setId(id);
    return pdfGenerator.generate(certificate);
  }

public void update(UUID id, JsonNode patch, boolean isMergePatch)
      throws JsonPatchException, IOException {
    Certificate certificateOrig = certificateRepository.findById(id).get();
    JsonNode certificateUpdated = performPatch(certificateOrig, isMergePatch, patch);
    validateAgainstSchema(certificateUpdated);
    certificateRepository.save(new Certificate(id, certificateUpdated.toString()));
  }

  public void deleteData(UUID id) {
    certificateRepository.deleteById(id);
  }

  protected void validateAgainstSchema(JsonNode jsonToValidate) throws ValidationException {
    JSONObject jsonObjectToValidate = new JSONObject(jsonToValidate.toString());
    schema.validate(jsonObjectToValidate);
  }

  protected JsonNode performPatch(
      Certificate certificateOrig, boolean isMergePatch, JsonNode patch)
      throws IOException, JsonPatchException {
    
    JsonNode jsonOriginalDocument = mapper.readTree(certificateOrig.getDocument());
    final JsonNode certificateUpdated;
    if (isMergePatch) {
      final JsonMergePatch preparedMergePatch = JsonMergePatch.fromJson(patch);
      certificateUpdated = preparedMergePatch.apply(jsonOriginalDocument);
    } else {
      final JsonPatch preparedCommandPatch = JsonPatch.fromJson(patch);
      certificateUpdated = preparedCommandPatch.apply(jsonOriginalDocument);
    }
    return certificateUpdated;
  }

  protected final Schema loadSchema() throws IOException, JSONException {
    InputStream resource = new ClassPathResource("serviceSchema.json").getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
    String schemaString = reader.lines().collect(Collectors.joining(""));
    JSONObject jsonSchema = new JSONObject(schemaString);
    SchemaLoader loader = SchemaLoader.builder().schemaJson(jsonSchema).build();
    return loader.load().build();
  }
}
