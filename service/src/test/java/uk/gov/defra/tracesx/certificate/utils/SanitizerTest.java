package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SanitizerTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("htmlProvider")
  void shouldValidateAllowedHtml(String html, String expected) {
    validate(html, expected);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("allowedHtmlProvider")
  void shouldValidateUnchangedHtml(String html) {
    validateUnchanged(html);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("changedHtmlProvider")
  void shouldValidateParticularHtmlChange(String html, String expected) {
    validate(html, expected);
  }

  static Stream<Arguments> htmlProvider() {
    return Stream.of(
        arguments(named("shouldAllowClassNames", "<p class='any'>hello</p>"),
            "<p class=\"any\">hello</p>"),
        arguments(named("shouldAllowTableElements", "<table class='any'></table>"),
            "<table class=\"any\"></table>"),
        arguments(named("shouldAllowMain", "<main class='any'>x</main>"),
            "<main class=\"any\">x</main>"));
  }

  static Stream<Arguments> allowedHtmlProvider() {
    return Stream.of(
        arguments("shouldAllowLinkToStyleSheet",
            "<html><head><link href=\"/public/stylesheets/certificate.css\" rel=\"stylesheet\" /></head></html>"),
        arguments("shouldAllowHtmlAndHeadElement",
            "<html><head></head></html>"),
        arguments("shouldAllowColspanOnRows",
            "<table><tbody><tr><th colspan=\"2\">y</th></tr></tbody></table>"),
        arguments("shouldAllowLanguageAttributeInHtml",
            "<html lang=\"en\"><head><link href=\"/public/stylesheets/certificate.css\" rel=\"stylesheet\" /></head></html>"),
        arguments("shouldAllowImgTags",
            "<img src=\"/public/logo.png\" class=\"header-logo\" />"),
        arguments("shouldAllowBody",
            "<body>x</body>")
    );
  }

  static Stream<Arguments> changedHtmlProvider() {
    return Stream.of(
        arguments(
            named("shouldRemoveScript", "<p>hello</p><script>console.log('hello');</script>"),
            "<p>hello</p>"),
        arguments(named("shouldAllowH1H2H3Tags",
                "<html><head></head><body><h1>Test H1</h1><h2>Test H2</h2><h3>Test H3</h3></body></html>"),
            "<html><head></head><body><h1>Test H1</h1><h2>Test H2</h2><h3>Test H3</h3></body></html>"),
        arguments(named("shouldAllowDivs",
                "<html><head></head><body><div><p>Test Div</p></div></body></html>"),
            "<html><head></head><body><div><p>Test Div</p></div></body></html>"),
        arguments(named("shouldRemoveScriptTags",
                "<html><head><script>alert('bad')</script></head></html>"),
            "<html><head></head></html>"),
        arguments(named("shouldRemoveOnMouseOver", "<html><body><div onmouseover=\"myOverFunction()\"></body></html>"),
            "<html><body><div></div></body></html>"),
        arguments(named("shouldRemoveIframe", "<html><body><iframe src=\"javascript:alert('XSS');\"></iframe></body></html>"),
            "<html><body></body></html>"),
        arguments(named("shouldRemoveIframeEventBased", "<html><body><iframe src=# onmouseover=\"alert(document.cookie)\"></iframe></body></html>"),
            "<html><body></body></html>"),
        arguments(named("shouldRemoveJavscriptFromTable", "<html><body><table background=\"javascript:alert('XSS')\"></body></html>"),
            "<html><body><table></table></body></html>"),
        arguments(named("shouldRemoveJavscriptFromTableData", "<html><body><tables><td background=\"javascript:alert('XSS')\"></body></html>"),
            "<html><body><table><tbody><tr><td></td></tr></tbody></table></body></html>"),
        arguments(named("shouldEscapeJavscriptFromImgSrc", "<html><body><img src= onmouseover=\"alert('xxs')\"></body></html>"),
            "<html><body><img src=\"onmouseover&#61;\" /></body></html>"),
        arguments(named("shouldEscapeJavscriptFromImgWrappedInAnchor", "<html><body><a href=\"page.html\"><img src= onmouseover=\"alert('xxs')\"></a></body></html>"),
            "<html><body><img src=\"onmouseover&#61;\" /></body></html>")
    );
  }

  @Test
  void shouldRemoveJavascriptFromCertificateHtml() throws IOException {
    String unsafeHtml = getHtmlContentFromFile("certificateWithScript.html");
    assertThat(unsafeHtml).contains("<script>alert('test');</script>");
    String safeHtml = Sanitizer.sanitize(unsafeHtml);
    assertThat(safeHtml).doesNotContain("<script>alert('test');</script>");
  }

  @Test
  void shouldRemoveJavascriptFromTableInCertificateHtml() throws IOException {
    String unsafeHtml = getHtmlContentFromFile("certificateWithTableScript.html");
    assertThat(unsafeHtml).contains("background=\"javascript:alert('XSS')");
    String safeHtml = Sanitizer.sanitize(unsafeHtml);
    assertThat(safeHtml).doesNotContain("background=\"javascript:alert('XSS')");
  }

  private String getHtmlContentFromFile(String htmlContent) throws IOException {
    URL fileUrl = Sanitizer.class.getClassLoader().getResource(htmlContent);
    File file = new File(fileUrl.getFile());
    return FileUtils.readFileToString(file, "UTF-8");
  }

  private void validateUnchanged(String html) {
    validate(html, html);
  }

  private void validate(String before, String expected) {
    String after = Sanitizer.sanitize(before);
    assertThat(after).isEqualTo(expected);
  }
}
