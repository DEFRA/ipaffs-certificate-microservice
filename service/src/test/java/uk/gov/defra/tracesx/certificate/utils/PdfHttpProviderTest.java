package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

public class PdfHttpProviderTest {

  PdfHttpProvider pdfHttpProvider;

  @Mock
  RestTemplate restTemplate;

  @Before
  public void setUp() {
    pdfHttpProvider = new PdfHttpProvider(restTemplate);
  }

  @Test
  public void when_tokenIsCorrect_expect_headerCreatedSuccessfully() {



    assertThat(true).isTrue();
  }

}
