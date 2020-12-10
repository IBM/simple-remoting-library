package server.impl;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import client.AbstractClient;
import common.Protocol;
import common.impl.ObjectPayloadHandler;
import server.AbstractServer;

/**
 *
 * @author psuryan
 *
 */
public class ServerFactory {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private volatile static ServerFactory uniqueInstance;

  private Map<Protocol, AbstractServer> servers = new ConcurrentHashMap<>();

  private ServerFactory() {

  }

  public static ServerFactory getInstance() {
    if (uniqueInstance == null) {
      synchronized (ServerFactory.class) {
        if (uniqueInstance == null) {
          uniqueInstance = new ServerFactory();
        }
      }
    }
    return uniqueInstance;
  }

  public AbstractServer getServer(Protocol type, Object obj, Properties props) throws Exception {
    AbstractServer remotserver = null;
    switch (type) {
      case REMOTE_ACTOR:
        if (!servers.containsKey(type)) {
          remotserver = new ReactiveServer(obj, new ObjectPayloadHandler());
          remotserver.init(props);
          servers.put(type, remotserver);
        }
        remotserver = servers.get(type);
        break;
    }
    return remotserver;
  }

  public void doneWith(AbstractClient proxy) throws Exception {
    logger.info("About to shutdown the client stack.");
    proxy.done();
  }
}
