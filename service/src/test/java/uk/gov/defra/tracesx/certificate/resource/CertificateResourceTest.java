
package uk.gov.defra.tracesx.certificate.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.defra.tracesx.certificate.service.CertificateService;

public class CertificateResourceTest {

  private static final String EXAMPLE_PARSER = "{\"k1\":\"v1\"}";
  private static final String MERGE_PATCH_TYPE = "application/merge-patch+json";
  private static final String COMMAND_PATCH_TYPE = "application/json-patch+json";

  private JsonNode node;

  @Mock
  CertificateService certificateService;
  
  @Before
  public void setUp() throws IOException {
    initMocks(this);
    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    JsonParser jsonParser = factory.createParser(EXAMPLE_PARSER);
    node = mapper.readTree(jsonParser);
  }

  @Test
  public void insertCallsServiceWithPostedJson() throws URISyntaxException {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    when(certificateService.create(any())).thenReturn(UUID.randomUUID());
    
    //When
    resource.insert(node);
    
    //Then
    verify(certificateService, times(1)).create(node);
  }

  @Test
  public void insertReturnsCreatedLocation() throws URISyntaxException {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();
    when(certificateService.create(any())).thenReturn(id);
    
    //When
    ResponseEntity responseEntity = resource.insert(null);
    
    //Then
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertEquals("/certificate/" + id.toString() ,responseEntity.getHeaders().getLocation().toString());
  }

  @Test
  public void patchCallsEntityServiceWithMergePatchType() throws IOException, JsonPatchException {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();
    
    //When
    resource.patch(id, MERGE_PATCH_TYPE, node);
    
    //Then
    verify(certificateService, times(1)).update(id, node, true);
  }

  @Test
  public void patchCallsEntityServiceWithCommandPatchType() throws IOException, JsonPatchException {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();
    
    //When
    resource.patch(id, COMMAND_PATCH_TYPE, node);
    
    //Then
    verify(certificateService, times(1)).update(id, node, false);
  }
  
  @Test
  public void patchCallsEntityServiceWithIdAndPatch() throws JsonPatchException, IOException {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();
    
    //When
    resource.patch(id, COMMAND_PATCH_TYPE, node);
    
    //Then
    verify(certificateService, times(1)).update(id, node, false);
  }
  
  @Test
  public void patchReturnsOkayStatus() throws IOException, JsonPatchException {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();
    
    //When
    ResponseEntity responseEntity = resource.patch(id, COMMAND_PATCH_TYPE, node);
    
    //Then
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void deleteCallsServiceWithId() {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();
    
    //When
    resource.delete(id);
    
    //Then
    verify(certificateService, times(1)).deleteData(id);
  }
  
  @Test
  public void deleteReturnsHttpStatusOkay() {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();
    
    //When
    ResponseEntity entity = resource.delete(id);
    
    //Then
    assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void downloadCertificateReturnsExpectedResponse() throws IOException {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();
    byte[] mockFile = new byte[20];
    new Random().nextBytes(mockFile);
    when(certificateService.downloadCertificate(any())).thenReturn(mockFile);

    //When
    ResponseEntity entity = resource.downloadCertificate(id);

    //Then
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(true, Arrays.equals((byte[]) entity.getBody(), mockFile));
  }

  @Test
  public void downloadCertificateServiceWithId() throws IOException {
    //Given
    CertificateResource resource = new CertificateResource(certificateService);
    UUID id = UUID.randomUUID();

    //When
    resource.downloadCertificate(id);

    //Then
    verify(certificateService, times(1)).downloadCertificate(id);
  }
}
