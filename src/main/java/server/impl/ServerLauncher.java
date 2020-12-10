package server.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Logger;

import common.Protocol;
import common.ProtocolSpec;
import common.impl.ReactiveConfig;
import server.AbstractServer;
import server.jmx.ServiceAgent;

/**
 *
 * @author psuryan
 *
 */
public class ServerLauncher {
  private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

  public static void main(String[] args) {
    ProtocolSpec pSpec = new ProtocolSpec();
    Properties props = new Properties();
    String instances = args.length > 2 ? args[2] : "1";
    props.setProperty(ReactiveConfig.INSTANCES, instances);
    pSpec.setProtocol(Protocol.REMOTE_ACTOR);
    int port = ReactiveConfig.findNextAvailablePort(Integer.parseInt(args[1]));
    props.setProperty("port", String.valueOf(port));
    pSpec.setProps(props);
    try {
      AbstractServer remoteObject = ServerFactory.getInstance().getServer(pSpec.getProtocol(),
              validateAndMake(args[0]), pSpec.getProps());
      logger.info("Starting JMX server.");
      //new ServiceAgent();
      ServiceAgent.createJmxConnectorServer();
      registerShutdownHook(remoteObject);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void registerShutdownHook(final AbstractServer remoteObject) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        logger.info("About to shutdown the server stack.");
        try {
          remoteObject.shutDown();
          logger.info("Shutdown the server stack.");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static Object validateAndMake(String className) {
    Class clazz;
    Object obj;
    try {
      clazz = Class.forName(className);
      Constructor c = clazz.getDeclaredConstructor();
      if (!c.isAccessible()) {
        System.out.println("Changing private constructor's visibility.");
        c.setAccessible(true);
      }
      obj = c.newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
            | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
      e.printStackTrace();
      throw new RuntimeException("Problems in instantiating the class." + e.getCause());
    }
    return obj;
  }
}
