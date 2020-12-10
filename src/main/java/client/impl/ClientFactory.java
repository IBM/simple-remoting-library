package client.impl;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import client.AbstractClient;
import common.Protocol;
import common.impl.ObjectPayloadHandler;

/**
 *
 * @author psuryan
 *
 */
public class ClientFactory {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private Map<Protocol, AbstractClient> clients = new ConcurrentHashMap<>();

  private volatile static ClientFactory uniqueInstance;

  private ClientFactory() {

  }

  public static ClientFactory getInstance() {
    if (uniqueInstance == null) {
      synchronized (ClientFactory.class) {
        if (uniqueInstance == null) {
          uniqueInstance = new ClientFactory();
        }
      }
    }
    return uniqueInstance;
  }

  public synchronized AbstractClient getClient(Protocol type, Properties props) throws Exception {
    AbstractClient client = null;
    if (!clients.containsKey(type)) {
      switch (type) {
        case IN_PROCESS:
          client = new MockClient(new ObjectPayloadHandler());
          break;
        case REMOTE_ACTOR:
          client = new ReactiveClient(new ObjectPayloadHandler());
          break;
      }
      client.init(props);
      clients.put(type, client);
    }
    return client;
  }

  public void doneWith(AbstractClient proxy) throws Exception {
    logger.info("About to shutdown the client stack.");
    proxy.done();
  }
}
