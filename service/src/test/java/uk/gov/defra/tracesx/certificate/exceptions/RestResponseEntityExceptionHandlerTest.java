package uk.gov.defra.tracesx.certificate.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.ArrayList;
import java.util.List;
import org.everit.json.schema.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;


@RunWith(MockitoJUnitRunner.class)
public class RestResponseEntityExceptionHandlerTest {

  @Mock
  WebRequest mockWebRequest;
  @Mock
  private Appender mockAppender;
  @Captor
  private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

  @Before
  public void setUp() {
    Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);
  }

  @After
  public void teardown() {
    Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.detachAppender(mockAppender);
  }

  @Test
  public void logsNotFoundException() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    Exception ex = new NotFoundException();
    when(mockWebRequest.getDescription(false)).thenReturn("some description");

    //When
    exceptionHandler.handleNotFound(ex, mockWebRequest);

    //Then
    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    assertThat(loggingEvent.getFormattedMessage()).isEqualTo(
        "org.springframework.web.servlet.PageNotFound : some description");
  }

  @Test
  public void notFoundExceptionReturnsCorrectHttpResponse() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    Exception ex = new Exception();

    //When
    ResponseEntity<Object> response = exceptionHandler.handleNotFound(ex, mockWebRequest);

    //Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isEqualTo("");
  }

  @Test
  public void logsNotImplementedException() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    Exception ex = new NotImplementedException("Not Implemented");

    //When
    exceptionHandler.handleMethodNotImplemented(ex, mockWebRequest);

    //Then
    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Method Not Implemented");
  }


  @Test
  public void handleNotImplementedReturnsCorrectHttpResponse() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    Exception ex = new Exception();

    //When
    ResponseEntity<Object> response = exceptionHandler.handleMethodNotImplemented(ex,
        mockWebRequest);

    //Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
    assertThat(response.getBody()).isEqualTo("");
  }

  @Test
  public void logsBadJson() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    Exception ex = new Exception();

    //When
    exceptionHandler.handleBadJson(ex, mockWebRequest);

    //Then
    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Invalid Json");
  }

  @Test
  public void handleBadJsonReturnsCorrectHttpResponse() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    Exception ex = new Exception();

    //When
    ResponseEntity<Object> response = exceptionHandler.handleBadJson(ex, mockWebRequest);

    //Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("");
  }

  @Test
  public void logsNestedSchemaValidationFailure() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    ValidationException exception = mock(ValidationException.class);
    ValidationException exceptionNested = mock(ValidationException.class);
    when(exceptionNested.getPointerToViolation()).thenReturn("pointer");
    when(exceptionNested.getErrorMessage()).thenReturn("error message");
    List<ValidationException> nestedExceptions = new ArrayList();
    nestedExceptions.add(exceptionNested);
    when(exception.getCausingExceptions()).thenReturn(nestedExceptions);
    //When
    exceptionHandler.handleInvalidSchema(exception, mockWebRequest);

    //Then
    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Schema validation failed");
  }

  @Test
  public void logsSchemaValidationFailure() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    ValidationException exception = mock(ValidationException.class);
    when(exception.getPointerToViolation()).thenReturn("pointer");
    when(exception.getErrorMessage()).thenReturn("error message");
    List<ValidationException> nestedExceptions = new ArrayList();
    when(exception.getCausingExceptions()).thenReturn(nestedExceptions);

    //When
    exceptionHandler.handleInvalidSchema(exception, mockWebRequest);

    //Then
    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
    assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Schema validation failed");
  }

  @Test
  public void handleInvalidSchemaCorrectHttpResponse() {
    //Given
    RestResponseEntityExceptionHandler exceptionHandler = new RestResponseEntityExceptionHandler();
    ValidationException exception = mock(ValidationException.class);

    //When
    ResponseEntity<Object> response = exceptionHandler.handleInvalidSchema(exception,
        mockWebRequest);

    //Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("Schema or model error: null : null");
  }
}
