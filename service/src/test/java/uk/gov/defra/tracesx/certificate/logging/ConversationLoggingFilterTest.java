package uk.gov.defra.tracesx.certificate.logging;

import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class ConversationLoggingFilterTest {

  private static final String CONVERSATION_ID_HEADER_NAME = "INS-ConversationId";
  private static final String CONVERSATION_ID = "ConversationId";

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;
  @Mock
  private FilterConfig filterConfig;
  @Mock
  private Appender mockAppender;

  @Captor
  private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

  @InjectMocks
  private ConversationLoggingFilter conversationLoggingFilter;

  private final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

  @Before
  public void setup() {
    when(request.getHeader(CONVERSATION_ID_HEADER_NAME)).thenReturn(CONVERSATION_ID);
    logger.addAppender(mockAppender);
  }

  @After
  public void teardown() {
    logger.detachAppender(mockAppender);
  }

  @Test
  public void initializingFilterMessageIsLogged() {
    conversationLoggingFilter.init(filterConfig);

    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(INFO);
    assertThat(loggingEvent.getFormattedMessage()).contains("Initializing filter");
  }

  @Test
  public void filterChecksHeaderAndContinuesChain() throws Exception {
    conversationLoggingFilter.doFilter(request, response, filterChain);

    verify(request).getHeader(CONVERSATION_ID_HEADER_NAME);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  public void destroyingFilterMessageIsLogged() {
    conversationLoggingFilter.destroy();

    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(WARN);
    assertThat(loggingEvent.getFormattedMessage()).contains("Destroying filter");
  }
}
