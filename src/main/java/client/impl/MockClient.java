package client.impl;

import java.util.Properties;

import client.AbstractClient;
import common.Payload;
import common.PayloadHandler;

/**
 *
 * @author psuryan
 *
 */
public class MockClient extends AbstractClient {

  public MockClient(PayloadHandler payloadHandler) {
    super(payloadHandler);
  }

  @Override
  protected void doCall(String interfaceName, Payload req) {
    logger.info("Need to invoke " + req.getMethod() + " on the implementer of  " + interfaceName
            + " but I am a just a mock");
    req.setResponse(new RuntimeException("I am a mock"));
    onReturn(req);
  }

  @Override
  public void init(Properties props) throws Exception {
    // nothing to do
  }

  @Override
  protected void shutDown() throws Exception {
    // nothing to do
  }

}
