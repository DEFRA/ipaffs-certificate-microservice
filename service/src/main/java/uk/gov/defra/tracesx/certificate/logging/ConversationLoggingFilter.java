package uk.gov.defra.tracesx.certificate.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Order(2)
public class ConversationLoggingFilter implements Filter {

  private static final String CONVERSATION_ID_HEADER_NAME = "INS-ConversationId";
  private static final String MDC_CONVERSATION_ID_KEY = "ConversationId";

  private static final Logger logger = LoggerFactory.getLogger(ConversationLoggingFilter.class);

  @Override
  public void init(final FilterConfig filterConfig) {
    logger.info("Initializing filter :{}", this);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

    final HttpServletRequest req = (HttpServletRequest) request;

    final String conversationId = req.getHeader(CONVERSATION_ID_HEADER_NAME);

    if (conversationId != null) {
      MDC.put(MDC_CONVERSATION_ID_KEY, conversationId);
    }

    try {
      chain.doFilter(request, response);
    } finally {
      if (conversationId != null) {
        MDC.remove(MDC_CONVERSATION_ID_KEY);
      }
    }
  }

  @Override
  public void destroy() {
    logger.warn("Destroying filter :{}", this);
  }
}
