package uk.gov.defra.tracesx.certificate.integration;

import java.util.function.Function;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

public class API {

  private static final String REFERENCE = "CVEDA.GB.2018.1234567";

  @FunctionalInterface
  interface MakeRequest<ResponseBodyType> {
    Response<ResponseBodyType> makeRequest(WebTestClient webTestClient);
  }

  static class Response<T> {
    private final ResponseSpec responseSpec;
    private final Function<ResponseSpec, FluxExchangeResult<T>> getBody;

    private Response(
        ResponseSpec responseSpec,
        Function<ResponseSpec, FluxExchangeResult<T>> getBody) {
      this.responseSpec = responseSpec;
      this.getBody = getBody;
    }

    private FluxExchangeResult<T> cachedFluxExchangeResult;

    FluxExchangeResult<T> response() {
      if (cachedFluxExchangeResult == null) {
        cachedFluxExchangeResult = getBody.apply(responseSpec);
      }
      return cachedFluxExchangeResult;
    }

    FluxExchangeResult<T> body() {
      return response();
    }
  }

  public static String createUrl() {
    return "/certificate/" + REFERENCE + "?url=http://somewebsite.com/certificate/001";
  }
}
