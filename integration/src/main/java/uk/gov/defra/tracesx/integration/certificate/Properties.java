package uk.gov.defra.tracesx.integration.certificate;

public class Properties {

  public static final String SERVICE_BASE_URL = System
      .getProperty("service.base.url", "http://localhost:6060");

  public static final String FRONTEND_NOTIFICATION_URL = System
          .getProperty("frontend.notification.base.url", "http://localhost:8000");


}
