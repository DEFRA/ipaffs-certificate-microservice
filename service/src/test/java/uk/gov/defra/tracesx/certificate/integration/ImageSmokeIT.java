package uk.gov.defra.tracesx.certificate.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.mockserver.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Builds the service image from the real Dockerfile and starts it the way Kubernetes does
 * (UID/GID 1000), wired to the same OAuth and MockServer containers as the other integration
 * tests. Catches packaging problems (entrypoint, layers, file permissions, missing
 * environment variables) before the image is pushed.
 *
 * <p>The image is built with the {@code docker build} CLI (BuildKit), exactly as the pipeline
 * builds it, rather than Testcontainers' legacy-builder image support, which failed in CI with
 * "failed to export image ... layer does not exist". The CLI must use the same Docker daemon as
 * Testcontainers.
 *
 * <p>Requires {@code target/application.jar}, so it must run after {@code mvn package}, which
 * Failsafe does. Base images are pulled from {@code IMAGE_ACR} (default docker.io), as in the
 * pipeline's docker build.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageSmokeIT extends IntegrationBase {

  private static final int SERVICE_PORT = 8000;
  private static final String KUBERNETES_USER = "1000:1000";
  private static final Duration BUILD_TIMEOUT = Duration.ofMinutes(10);
  private static final String REFERENCE = "CHEDA.GB.2018.1234567";

  private final String imageTag = "certificate-smoke-test:" + UUID.randomUUID();
  private GenericContainer<?> service;

  @BeforeAll
  @SuppressWarnings("resource")
  void startServiceImage() throws IOException, InterruptedException {
    buildImage();

    service = new GenericContainer<>(DockerImageName.parse(imageTag))
        .withNetwork(NETWORK)
        .withExposedPorts(SERVICE_PORT)
        .withCreateContainerCmdModifier(cmd -> cmd.withUser(KUBERNETES_USER))
        .withEnv(serviceEnvironment())
        .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("service-image"))
        .waitingFor(Wait.forLogMessage(".*Started CertificateApplication.*", 1)
            .withStartupTimeout(Duration.ofMinutes(2)));
    service.start();
  }

  @AfterAll
  void stopServiceImage() throws IOException, InterruptedException {
    if (service != null) {
      service.stop();
    }
    docker("rmi", "--force", imageTag);
  }

  @Test
  void healthCheckReportsUp() {
    serviceClient()
        .get().uri("/admin/health-check")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.status").isEqualTo("UP");
  }

  // Renders the certificate fixture, fetching its stylesheet and image from the (mocked) frontend.
  // The stylesheet asks for the Times New Roman font bundled in the jar, which the PDF must embed,
  // so this checks font rendering works on the distroless image.
  @Test
  void rendersACertificatePdfWithTheBundledFontsForAnAuthorisedUser() throws IOException {
    // IntegrationBase doesn't reset MockServer between tests, and CertificateServiceIT stubs the
    // same stylesheet, so start from a clean slate
    usingStub().reset();
    givenUserHasPermissions("certificate.create");
    usingStub()
        .when(request().withMethod("GET").withPath("/public/stylesheets/certificate.css"))
        .respond(response().withHeader("Content-Type", "text/css")
            .withBody("body { font-family: \"Times New Roman\"; } b { font-weight: bold; }"));
    usingStub()
        .when(request().withMethod("GET").withPath("/public/images/statuses/draft.png"))
        .respond(response().withHeader("Content-Type", "image/png").withBody("draft.png"));
    String html = new String(resourceLoader.getResource("classpath:integration/certificate.html")
        .getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    byte[] pdf = serviceClient()
        .post().uri("/certificate/{reference}?url={url}", REFERENCE, frontendUrl())
        .header("Authorization", "Bearer " + getToken("AnaAdams"))
        .bodyValue(html)
        .exchange()
        .expectStatus().isOk()
        .expectBody(byte[].class)
        .returnResult()
        .getResponseBody();

    String content = new String(pdf, StandardCharsets.ISO_8859_1);
    assertThat(content).startsWith("%PDF-");
    // A subset of the bundled "Times New Roman.ttf", whose PostScript name is Times-Roman
    assertThat(content).contains("+Times-Roman");
    // Logged for every text run when the requested font family can't be resolved
    assertThat(service.getLogs()).doesNotContain("Font list is empty");
  }

  @Test
  void rejectsAnUnauthenticatedRequest() {
    serviceClient()
        .post().uri("/certificate/{reference}?url={url}", REFERENCE, frontendUrl())
        .bodyValue("<p>hello world</p>")
        .exchange()
        .expectStatus().isUnauthorized();
  }

  private void buildImage() throws IOException, InterruptedException {
    String imageAcr = System.getenv().getOrDefault("IMAGE_ACR", "docker.io");
    DockerResult build = docker("build", "--progress=plain",
        "--build-arg", "IMAGE_ACR=" + imageAcr, "--tag", imageTag, ".");
    if (build.exitCode() != 0) {
      throw new IllegalStateException(
          "docker build failed (exit %d):%n%s".formatted(build.exitCode(), build.output()));
    }
    log.debug("docker build output:\n{}", build.output());
  }

  private static DockerResult docker(String... args) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>(List.of("docker"));
    command.addAll(List.of(args));
    // Output goes to a file so the process can't block on a full pipe and the timeout still applies
    Path outputFile = Files.createTempFile("docker-", ".log");
    try {
      Process process = new ProcessBuilder(command)
          .directory(Path.of("").toAbsolutePath().toFile())
          .redirectErrorStream(true)
          .redirectOutput(outputFile.toFile())
          .start();
      if (!process.waitFor(BUILD_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
        process.destroyForcibly();
        throw new IllegalStateException("Timed out running " + command);
      }
      return new DockerResult(process.exitValue(), Files.readString(outputFile));
    } finally {
      Files.deleteIfExists(outputFile);
    }
  }

  private record DockerResult(int exitCode, String output) {
  }

  // The frontend the service fetches certificate stylesheets and images from
  private static String frontendUrl() {
    return "http://%s:%d/".formatted(MOCK_SERVER_ALIAS, MockServerContainer.PORT);
  }

  private Map<String, String> serviceEnvironment() {
    String oauth = "http://%s:%d".formatted(OAUTH_ALIAS, 8080);
    String mockServer = "http://%s:%d".formatted(MOCK_SERVER_ALIAS, MockServerContainer.PORT);

    return Map.of(
        "JWT_JWKS", oauth + "/default/jwks",
        // Tokens are issued to the test via the host-mapped port, so the issuer claim uses it
        "JWT_CERT_ISSUER", "http://%s:%d/default".formatted(
            OAUTH_CONTAINER.getHost(), OAUTH_CONTAINER.getMappedPort(8080)),
        "JWT_AUDIENCE", "integration-test",
        "PERMISSIONS_SERVICE_URL", mockServer + "/permissions/",
        "FRONTEND_NOTIFICATION_SERVICE_SCHEME", "http",
        "FRONTEND_NOTIFICATION_SERVICE_HOST", MOCK_SERVER_ALIAS,
        "FRONTEND_NOTIFICATION_SERVICE_PORT", String.valueOf(MockServerContainer.PORT),
        "MONITORING_TYPE", "log");
  }

  private WebTestClient serviceClient() {
    return WebTestClient.bindToServer()
        .baseUrl("http://%s:%d".formatted(service.getHost(), service.getMappedPort(SERVICE_PORT)))
        .defaultHeader("INS-ConversationId", UUID.randomUUID().toString())
        .build();
  }
}
