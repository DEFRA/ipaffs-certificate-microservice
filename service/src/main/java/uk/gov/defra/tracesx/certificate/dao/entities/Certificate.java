package uk.gov.defra.tracesx.certificate.dao.entities;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "certificate")
public class Certificate {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String document;

  public Certificate() {}

  public Certificate(UUID id, String document) {
    this.id = id;
    this.document = document;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getDocument() {
    return document;
  }

  public void setDocument(String document) {
    this.document = document;
  }
}
