package uk.gov.defra.tracesx.certificate.integration;

import org.junit.jupiter.api.Test;

class AdminEndpointsIT extends IntegrationBase {

  @Test
  void testAdminHealthEndpointWithNoAuthentication() {

    webClient("no_auth")
        .get()
        .uri("/admin/health-check")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json("""
              {
                  "status": "UP"
              }
            """);
  }

  @Test
  void testAdminInfoEndpointWithNoAuthentication() {

    webClient("no_auth")
        .get()
        .uri("/admin/info")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json("""
              {
                "app": {
                  "version":1.0,
                  "name":"Certificate"
                }
              }
            """);
  }

}
