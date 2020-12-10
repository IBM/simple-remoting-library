package common;

/**
 *
 * @author psuryan
 *
 */
public class ProtocolHandler {
  private PayloadHandler payloadHandler;

  public ProtocolHandler(PayloadHandler payloadHandler){
    this.payloadHandler = payloadHandler;
  }

  public PayloadHandler getPayloadHandler() {
    return payloadHandler;
  }
}
