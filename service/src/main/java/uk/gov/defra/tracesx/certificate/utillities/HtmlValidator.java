package uk.gov.defra.tracesx.certificate.utillities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HtmlValidator {

  public static final void validate(String html) throws ParserConfigurationException, IOException {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      InputSource input = new InputSource(new BufferedReader(new StringReader(html)));
      dBuilder.parse(input);
    } catch(SAXException e) {
      throw new InvalidHtmlException("Invalid html was provided", e.getCause());
    }
  }
}
