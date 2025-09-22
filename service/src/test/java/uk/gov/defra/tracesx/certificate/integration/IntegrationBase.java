package uk.gov.defra.tracesx.certificate.integration;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.testcontainers.utility.DockerImageName.parse;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.mockserver.client.MockServerClient;
import org.mockserver.configuration.Configuration;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.socket.tls.KeyStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.lifecycle.Startables;
import uk.gov.defra.tracesx.common.permissions.PermissionsCache;

@Slf4j
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"classpath:application-integration-test.yml"})
abstract class IntegrationBase {

  @LocalServerPort
  int port;

  @Autowired
  PermissionsCache permissionsCache;

  @Autowired
  ResourceLoader resourceLoader;

  private MockServerClient mockServerClient;

  static final List<String> SERVICES_TO_MOCK =
      List.of(
          "permissions",
          "frontendNotification"
      );

  static final OAuthMockServerContainer OAUTH_CONTAINER = new OAuthMockServerContainer();
  static final MockServerContainer MOCK_SERVER_CONTAINER =
      new MockServerContainer(parse("mockserver/mockserver")
          .withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion()));

  static {
    HttpsURLConnection.setDefaultSSLSocketFactory(
        new KeyStoreFactory(new Configuration(), new MockServerLogger())
            .sslContext()
            .getSocketFactory());

    Startables.deepStart(
        OAUTH_CONTAINER,
        MOCK_SERVER_CONTAINER
    ).join();
  }

  @DynamicPropertySource
  static void setupContextValues(DynamicPropertyRegistry registry) {

    // Service API urls
    SERVICES_TO_MOCK.forEach(
        service ->
          registry.add("%s.service.url".formatted(service),
              () -> "%s/%s/".formatted(MOCK_SERVER_CONTAINER.getEndpoint(), service)));


    // Oauth Server container config
    registry.add("auth-api.url",
        () -> "http://%s:%d/oauth2/token".formatted(OAUTH_CONTAINER.getHost(),
            OAUTH_CONTAINER.getMappedPort(8080)));
    registry.add("auth-api.resource", () -> "http://%s:%d".formatted(OAUTH_CONTAINER.getHost(),
        OAUTH_CONTAINER.getMappedPort(8080)));
    registry.add("trade-auth-api.url",
        () -> "http://%s:%d/tenant/oauth2/v2.0/token".formatted(OAUTH_CONTAINER.getHost(),
            OAUTH_CONTAINER.getMappedPort(8080)));
    registry.add("spring.security.oauth2.client.provider.trade-platform.token-uri",
        () -> "http://%s:%d/trade-platform/token".formatted(OAUTH_CONTAINER.getHost(),
            OAUTH_CONTAINER.getMappedPort(8080)));
    registry.add("spring.security.jwt.iss",
        () -> "http://%s:%d/default".formatted(OAUTH_CONTAINER.getHost(),
            OAUTH_CONTAINER.getMappedPort(8080)));
    registry.add("spring.security.jwt.jwks",
        () -> "http://%s:%d/default/jwks".formatted(OAUTH_CONTAINER.getHost(),
            OAUTH_CONTAINER.getMappedPort(8080)));
    registry.add("spring.security.jwt.aud", () -> "integration-test");
    registry.add("spring.security.oauth2.client.provider.address-lookup.token-uri",
        () -> "http://%s:%d/address-lookup/token".formatted(OAUTH_CONTAINER.getHost(),
            OAUTH_CONTAINER.getMappedPort(8080)));
  }

  MockServerClient usingStub() {

    if (mockServerClient == null) {
      mockServerClient = new MockServerClient(
          MOCK_SERVER_CONTAINER.getHost(), MOCK_SERVER_CONTAINER.getServerPort());
      log.info("Dashboard url: http://{}:{}/dashboard/dashboard",
          MOCK_SERVER_CONTAINER.getHost(), MOCK_SERVER_CONTAINER.getServerPort());
    }

    return mockServerClient;
  }

  String getToken(String clientType) {

    Map<String, Object> overrides = getTokenOverrides();
    overrides.put("client_id", clientType);
    String reqBody = getTokenOverridesAsString(overrides);

    final RestClient restClient = RestClient.builder()
        .requestFactory(new HttpComponentsClientHttpRequestFactory())
        .baseUrl("http://%s:%d".formatted(
            OAUTH_CONTAINER.getHost(), OAUTH_CONTAINER.getMappedPort(8080)))
        .build();

    final ResponseEntity<JsonNode> response = restClient
        .post()
        .uri("/default/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(reqBody)
        .retrieve()
        .toEntity(JsonNode.class);

    return Optional.of(
            response.getBody())
        .map(body -> body.get("access_token"))
        .map(JsonNode::asText)
        .orElseThrow();
  }

  WebTestClient webClient(String clientType) {

    return WebTestClient.bindToServer()
        .baseUrl("http://localhost:%d".formatted(port))
        .defaultHeader("Authorization", "Bearer " + getToken(clientType))
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("INS-ConversationId", UUID.randomUUID().toString())
        .build();
  }

  void givenUserHasPermissions(final String... permissions) {
    final String jsonPermissions;

    if (permissions == null || permissions.length == 0) {
      jsonPermissions = "[]";
    } else {
      jsonPermissions = "[\"" + String.join("\",\"", permissions) + "\"]";
    }

    usingStub()
        .when(request().withPath("/permissions/roles/Inspector/permissions"))
        .withId("getUserPermissions")
        .respond(
            response().withBody(jsonPermissions, org.mockserver.model.MediaType.APPLICATION_JSON));
  }

  private Map<String, Object> getTokenOverrides() {

    Map<String, Object> overrides = new HashMap<>();
    overrides.put("grant_type", "authorization_code");
    overrides.put("code", "userid");

    return overrides;
  }

  private String getTokenOverridesAsString(Map<String, Object> overrides) {

    return overrides.entrySet().stream()
        .map(k -> k.getKey() + "=" + k.getValue())
        .collect(Collectors.joining("&"));
  }

  @AfterEach
  void tearDown() {
    permissionsCache.clearCache();
  }
}
