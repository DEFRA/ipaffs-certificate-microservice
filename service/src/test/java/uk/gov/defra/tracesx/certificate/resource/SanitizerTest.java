package uk.gov.defra.tracesx.certificate.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class SanitizerTest {

  @Test
  public void shouldRemoveScript() {
    validate("<p>hello</p><script>console.log('hello');</script>", "<p>hello</p>");
  }

  @Test
  public void shouldAllowClassNames() {
    validate("<p class='any'>hello</p>", "<p class=\"any\">hello</p>");
  }

  @Test
  public void shouldAllowTableElements() {
    validate("<table class='any'></table>", "<table class=\"any\"></table>");
  }

  @Test
  public void shouldAllowMain() {
    validate("<main class='any'>x</main>", "<main class=\"any\">x</main>");
  }

  @Test
  public void shouldAllowBody() {
    validateUnchanged("<body>x</body>");
  }

  @Test
  public void shouldAllowImgTags() {
    validateUnchanged("<img src=\"/public/logo.png\" class=\"header-logo\" />");
  }

  @Test
  public void shouldAllowColspanOnRows() {
    String html = ""
        + "<table>"
          + "<tbody>"
          + "<tr>"
            + "<th colspan=\"2\">y</th>"
          + "</tr>"
          + "</tbody>"
        + "</table>";
    validateUnchanged(html);
  }

  @Test
  public void shouldAllowHtmlAndHeadElement() {
    String html = ""
        + "<html>"
        + "<head>"
        + "</head>"
        + "</html>";
    validateUnchanged(html);
  }

  @Test
  public void shouldAllowLinkToStyleSheet() {
    String html = ""
        + "<html>"
        + "<head>"
          + "<link href=\"/public/stylesheets/certificate.css\" rel=\"stylesheet\" />"
        + "</head>"
        + "</html>";
    validateUnchanged(html);
  }

  @Test
  public void shouldAllowH1H2H3Tags() {
    String html = ""
        + "<html>"
        + "<head>"
        + "</head>"
        + "<body>"
        + "<h1>Test H1</h1>"
        + "<h2>Test H2</h2>"
        + "<h3>Test H3</h3>"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<head>"
        + "</head>"
        + "<body>"
        + "<h1>Test H1</h1>"
        + "<h2>Test H2</h2>"
        + "<h3>Test H3</h3>"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldAllowDivs() {
    String html = ""
        + "<html>"
        + "<head>"
        + "</head>"
        + "<body>"
        + "<div><p>Test Div</p></div>"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<head>"
        + "</head>"
        + "<body>"
        + "<div><p>Test Div</p></div>"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }


  @Test
  public void shouldRemoveScriptTags() {
    String html = ""
        + "<html>"
        + "<head>"
        + "<script>alert('bad')</script>"
        + "</head>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<head>"
        + "</head>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldRemoveOnMouseOver() {
    String html = ""
        + "<html>"
        + "<body>"
        + "<div onmouseover=\"myOverFunction()\">"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<body>"
        + "<div></div>"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldRemoveIframe() {
    String html = ""
        + "<html>"
        + "<body>"
        + "<iframe src=\"javascript:alert('XSS');\"></iframe>"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<body>"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldRemoveIframeEventBased() {
    String html = ""
        + "<html>"
        + "<body>"
        + "<iframe src=# onmouseover=\"alert(document.cookie)\"></iframe>"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<body>"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldRemoveJavscriptFromTable() {
    String html = ""
        + "<html>"
        + "<body>"
        + "<table background=\"javascript:alert('XSS')\">"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<body>"
        + "<table></table>"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldRemoveJavscriptFromTableData() {
    String html = ""
        + "<html>"
        + "<body>"
        + "<tables><td background=\"javascript:alert('XSS')\">"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<body>"
        + "<table>"
        + "<tbody><tr><td></td></tr></tbody>"
        + "</table>"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldEscapeJavscriptFromImgSrc() {
    String html = ""
        + "<html>"
        + "<body>"
        + "<img src= onmouseover=\"alert('xxs')\">"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<body>"
        + "<img src=\"onmouseover&#61;\" />"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldEscapeJavscriptFromImgWrappedInAnchor() {
    String html = ""
        + "<html>"
        + "<body>"
        + "<a href=\"page.html\"><img src= onmouseover=\"alert('xxs')\"></a>"
        + "</body>"
        + "</html>";

    String expected = ""
        + "<html>"
        + "<body>"
        + "<img src=\"onmouseover&#61;\" />"
        + "</body>"
        + "</html>";

    validate(html, expected);
  }

  @Test
  public void shouldRemoveJavascriptFromCertificateHtml() throws IOException {
    String unsafeHtml = getHtmlContentFromFile("certificateWithScript.html");
    assertThat(unsafeHtml).contains("<script>alert('test');</script>");
    String safeHtml = Sanitizer.sanitize(unsafeHtml);
    assertThat(safeHtml).doesNotContain("<script>alert('test');</script>");
  }

  @Test
  public void shouldRemoveJavascriptFromTableInCertificateHtml() throws IOException {
    String unsafeHtml = getHtmlContentFromFile("certificateWithTableScript.html");
    assertThat(unsafeHtml).contains("background=\"javascript:alert('XSS')");
    String safeHtml = Sanitizer.sanitize(unsafeHtml);
    assertThat(safeHtml).doesNotContain("background=\"javascript:alert('XSS')");
  }

  private String getHtmlContentFromFile(String htmlContent) throws IOException{
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