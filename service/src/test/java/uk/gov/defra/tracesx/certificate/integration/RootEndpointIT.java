package uk.gov.defra.tracesx.certificate.integration;

import org.junit.jupiter.api.Test;

class RootEndpointIT extends IntegrationBase {

  @Test
  void testRootEndpointWithNoAuthentication() {

    webClient("no_auth")
        .get()
        .uri("/")
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty();
  }
}
