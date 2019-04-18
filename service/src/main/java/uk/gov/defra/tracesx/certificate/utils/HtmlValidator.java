package uk.gov.defra.tracesx.certificate.utils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidHtmlException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class HtmlValidator {

  private HtmlValidator() {
    // no-arg constructor
  }

  public static final void validate(String html) throws ParserConfigurationException, IOException {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
      InputSource input = new InputSource(new BufferedReader(new StringReader(html)));
      documentBuilder.parse(input);
    } catch (SAXException exception) {
      throw new InvalidHtmlException("Invalid html was provided", exception.getCause());
    }
  }
}
