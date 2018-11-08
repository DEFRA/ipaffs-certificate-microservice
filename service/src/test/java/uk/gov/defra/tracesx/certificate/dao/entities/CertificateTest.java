package uk.gov.defra.tracesx.certificate.dao.entities;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class CertificateTest {
  
  @Before
  public void setUp() {
  }
  
  @Test
  public void verifyIdProperty() {
    //Given
    Certificate instance = new Certificate();
    UUID id = UUID.randomUUID();
    
    //When
    instance.setId(id);

    //Then
    assertEquals(id, instance.getId());
  }

  @Test
  public void verifyDocumentProperty() {
    //Given
    Certificate instance = new Certificate();
    String jsonDocument = "{\"test\": 123}";
    
    //When
    instance.setDocument(jsonDocument);

    //Then
    assertEquals(jsonDocument, instance.getDocument());
    
  }
}
