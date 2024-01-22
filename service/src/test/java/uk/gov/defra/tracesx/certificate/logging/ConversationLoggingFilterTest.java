package uk.gov.defra.tracesx.certificate.logging;

import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class ConversationLoggingFilterTest {

  private static final String CONVERSATION_ID_HEADER_NAME = "INS-ConversationId";

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

  @BeforeEach
  public void setup() {
    logger.addAppender(mockAppender);
  }

  @AfterEach
  public void teardown() {
    logger.detachAppender(mockAppender);
  }

  @Test
  void initializingFilterMessageIsLogged() {
    conversationLoggingFilter.init(filterConfig);

    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(INFO);
    assertThat(loggingEvent.getFormattedMessage()).contains("Initializing filter");
  }

  @Test
  void filterChecksHeaderAndContinuesChain() throws Exception {
    conversationLoggingFilter.doFilter(request, response, filterChain);

    verify(request).getHeader(CONVERSATION_ID_HEADER_NAME);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void destroyingFilterMessageIsLogged() {
    conversationLoggingFilter.destroy();

    verify(mockAppender).doAppend(captorLoggingEvent.capture());
    final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    assertThat(loggingEvent.getLevel()).isEqualTo(WARN);
    assertThat(loggingEvent.getFormattedMessage()).contains("Destroying filter");
  }
}
