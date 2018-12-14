package uk.gov.defra.tracesx.certificate.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

public class PreAuthorizeCheckerTest {

    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;

    @InjectMocks
    PreAuthorizeChecker testee;

    @Before
    public void setUp() {
        initMocks(this);
    }

    public void preAuthorizeCheckerMethod() {

    }
    @PreAuthorize("hasAuthority('certificate.create')")
    public void preAuthorizeCheckerMethodWithAnnotation() {

    }

    @Test(expected = RuntimeException.class)
    public void whenPreAuthorizeIsNotDefinedThenThrowError() throws Exception {

        HandlerMethod handlerMethod = new HandlerMethod(this, this.getClass().getMethod(
                "preAuthorizeCheckerMethod"));
        testee.preHandle(requestMock, responseMock, handlerMethod);
    }

    @Test
    public void whenPreAuthorizeIsDefinedThenReturnTrue() throws Exception {

        HandlerMethod handlerMethod = new HandlerMethod(this, this.getClass().getMethod(
                "preAuthorizeCheckerMethodWithAnnotation"));
        assertThat(testee.preHandle(requestMock, responseMock, handlerMethod)).isTrue();
    }
}
