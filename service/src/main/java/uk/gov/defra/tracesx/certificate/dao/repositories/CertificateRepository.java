package uk.gov.defra.tracesx.certificate.dao.repositories;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;

@Repository
public interface CertificateRepository extends CrudRepository<Certificate, UUID> {}
