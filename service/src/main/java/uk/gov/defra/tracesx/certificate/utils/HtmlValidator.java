package uk.gov.defra.tracesx.certificate.utils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidHtmlException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class HtmlValidator {

  private HtmlValidator() {
    // no-arg constructor
  }

  public static final void validate(String html) throws ParserConfigurationException, IOException {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newDefaultInstance();
      dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      dbFactory.setXIncludeAware(false);
      dbFactory.setExpandEntityReferences(false);
      DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
      InputSource input = new InputSource(new BufferedReader(new StringReader(html)));
      documentBuilder.parse(input);
    } catch (SAXException exception) {
      throw new InvalidHtmlException("Invalid html was provided", exception.getCause());
    }
  }
}
