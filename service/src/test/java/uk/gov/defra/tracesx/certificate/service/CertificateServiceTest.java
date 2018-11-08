package uk.gov.defra.tracesx.certificate.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;
import uk.gov.defra.tracesx.certificate.dao.repositories.CertificateRepository;
import uk.gov.defra.tracesx.certificate.utillities.CertificatePDFGenerator;

public class CertificateServiceTest {

  @Mock
  CertificateRepository certificateRepository;

  @Mock
  CertificatePDFGenerator pdfGenerator;

  @Mock
  Schema schema;

  private ObjectMapper mapper = new ObjectMapper();
  private JsonNode entity;
  private CertificateService certificateService;
  
  @Before
  public void setUp() throws IOException {
    initMocks(this);
    certificateService = new CertificateService(certificateRepository, pdfGenerator, mapper);
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    JsonParser jsonParser = factory.createParser("{\"certificate\":\"aName\"}");
    entity = mapper.readTree(jsonParser);
  }
  
  @Test
  public void certificateServiceLoadsSchema() throws IOException {
    //Given
    CertificateService certificateServiceNew;
    
    //When
    certificateServiceNew = new CertificateService(certificateRepository, pdfGenerator, mapper);
    
    //Then
    assertEquals("Certificate schema", certificateServiceNew.schema.getDescription());
  }

  @Test (expected = ValidationException.class)
  public void createValidatesAgainstSchema() throws IOException {
    //Given
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    JsonParser jsonParser = factory.createParser("{\"k1\":\"v1\"}");
    JsonNode invalidEntity = mapper.readTree(jsonParser);
    
    //When
    certificateService.create(invalidEntity);
    
    //Then
    //Validation exception thrown
  }

  @Test
  public void createsSavesAsNewEntity() throws IOException {
    //Given
    UUID idSavedWith = UUID.randomUUID();
    Certificate certificate = new Certificate();
    certificate.setId(idSavedWith);
    when(certificateRepository.save(any())).thenReturn(certificate);
    
    //When
    UUID entityId = certificateService.create(entity);
    
    //Then
    verify(certificateRepository, times(1)).save(any());
    assertEquals(idSavedWith, entityId);
  }

  @Test
  public void createSavesPassedJsonObject() throws IOException {
    //Given
    UUID idSavedWith = UUID.randomUUID();
    Certificate certificate = new Certificate();
    certificate.setId(idSavedWith);
    ArgumentCaptor<Certificate> captorForDocument = ArgumentCaptor.forClass(Certificate.class);
    when(certificateRepository.save(captorForDocument.capture())).thenReturn(certificate);
    
    //When
    certificateService.create(entity);
    
    //Then
    assertEquals(null, captorForDocument.getValue().getId());
    assertEquals(entity.toString(), captorForDocument.getValue().getDocument());
  }

  @Test
  public void getCallsRepositoryWithId() throws IOException {
    //Given
    UUID id = UUID.randomUUID();
    Certificate record = new Certificate();
    record.setId(id);
    record.setDocument("");
    when(certificateRepository.findById(any())).thenReturn(Optional.of(record));
    
    //When
    certificateService.get(id);
    
    //Then
    verify(certificateRepository, times(1)).findById(id);
    verify(certificateRepository, times(1)).findById(any());
  }

  @Test
  public void getReturnsDocumentFromRepository() throws IOException {
    //Given
    UUID id = UUID.randomUUID();
    Certificate record = new Certificate();
    record.setId(id);
    record.setDocument(entity.toString());
    when(certificateRepository.findById(any())).thenReturn(Optional.of(record));
    
    //When
    JsonNode entityReturned = certificateService.get(id);
    
    //Then
    assertEquals("aName", entityReturned.get("certificate").textValue());
  }

  @Test
  public void updateGetsDocumentFromRepository() throws JsonPatchException, IOException {
    //Given
    UUID id = UUID.randomUUID();
    Certificate record = new Certificate();
    record.setId(id);
    record.setDocument(entity.toString());
    when(certificateRepository.findById(id)).thenReturn(Optional.of(record));
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    JsonParser jsonParser = factory.createParser("{\"certificate\":\"newName\"}");
    JsonNode mergePatch = mapper.readTree(jsonParser);
    
    //When
    certificateService.update(id, mergePatch, true);
    
    //Then
    verify(certificateRepository, times(1)).findById(id);
    verify(certificateRepository, times(1)).findById(any());
  }

  @Test
  public void updatePerformsMergePatchWhenSaving() throws IOException, JsonPatchException {
    //Given
    UUID id = UUID.randomUUID();
    Certificate record = new Certificate();
    record.setId(id);
    record.setDocument(entity.toString());
    when(certificateRepository.findById(id)).thenReturn(Optional.of(record));
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    JsonParser jsonParser = factory.createParser("{\"certificate\":\"patchedName\"}");
    JsonNode mergePatch = mapper.readTree(jsonParser);
    ArgumentCaptor<Certificate> captorForDocument = ArgumentCaptor.forClass(Certificate.class);
    when(certificateRepository.save(captorForDocument.capture())).thenReturn(null);
    
    //When
    certificateService.update(id, mergePatch, true);
    
    //Then
    assertEquals("{\"certificate\":\"patchedName\"}", captorForDocument.getValue().getDocument());
  }
  
  @Test
  public void updatePerformsCommandPatchWhenSaving() throws IOException, JsonPatchException {
    //Given
    UUID id = UUID.randomUUID();
    Certificate record = new Certificate();
    record.setId(id);
    record.setDocument(entity.toString());
    when(certificateRepository.findById(id)).thenReturn(Optional.of(record));
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    JsonParser jsonParser = factory.createParser("[{ \"op\": \"replace\", \"path\": \"/certificate\", \"value\": \"patchedName\"}]");
    JsonNode mergePatch = mapper.readTree(jsonParser);
    ArgumentCaptor<Certificate> captorForDocument = ArgumentCaptor.forClass(Certificate.class);
    when(certificateRepository.save(captorForDocument.capture())).thenReturn(null);
    
    //When
    certificateService.update(id, mergePatch, false);
    
    //Then
    assertEquals("{\"certificate\":\"patchedName\"}", captorForDocument.getValue().getDocument());
  }

  @Test (expected=ValidationException.class)
  public void updateValidatesPatchedEntityAgainstSchema() throws IOException, JsonPatchException {
    //Given
    UUID id = UUID.randomUUID();
    Certificate record = new Certificate();
    record.setId(id);
    record.setDocument(entity.toString());
    when(certificateRepository.findById(id)).thenReturn(Optional.of(record));
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    JsonParser jsonParser = factory.createParser("{\"extraName\":\"extraName\"}");
    JsonNode mergePatch = mapper.readTree(jsonParser);
    
    //When
    certificateService.update(id, mergePatch, true);
    
    //Then
    //Validation Exception should be thrown
  }

  @Test
  public void deleteCallsRepositoryOnceWithId() {
    //Given
    UUID id = UUID.randomUUID();
    
    //When
    certificateService.deleteData(id);
    
    //Then
    verify(certificateRepository, times(1)).deleteById(id);
    verify(certificateRepository, times(1)).deleteById(any());
  }

  @Test
  public void downloadCertificateReturnsExpectedByteArray() {
    //Given
    UUID id = UUID.randomUUID();
    Certificate certificate = new Certificate();
    certificate.setId(id);
    byte[] mockFile = new byte[20];
    new Random().nextBytes(mockFile);
    when(pdfGenerator.generate(any())).thenReturn(mockFile);

    //When
    byte[] actual = certificateService.downloadCertificate(id);

    //Then
    assertEquals(true, Arrays.equals(mockFile, actual));
  }

  @Test
  public void downloadCertificateCallsPdfGeneratorOnce() {
    //Given
    UUID id = UUID.randomUUID();
    Certificate certificate = new Certificate();
    certificate.setId(id);
    when(pdfGenerator.generate(any())).thenReturn(new byte[20]);

    //When
    certificateService.downloadCertificate(id);

    //Then
    verify(pdfGenerator, times(1)).generate(any());
  }
}
