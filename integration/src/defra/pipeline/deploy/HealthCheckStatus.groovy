package defra.pipeline.deploy

public class HealthCheckStatus {
  public final static String UP = "UP"
  public final static String DOWN = "DOWN"
  public final static String NOT_DEPLOYED = "NOT_DEPLOYED"
  public final static String UNSUPPORTED_NO_ENDPOINTS = "UNSUPPORTED_NO_ENDPOINTS"
  public final static String ENDPOINTS_FAILED = "ENDPOINTS_FAILED"
  public final static String UNKNOWN_STATE = "UNKNOWN_STATE"
}
