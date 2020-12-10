package client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Logger;

import common.Payload;

/**
 *
 * @author psuryan
 *
 */
public class Invoker implements InvocationHandler {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private AbstractClient client = null;

  public Invoker(AbstractClient remoteProxy) {
    this.client = remoteProxy;
  }

  @SuppressWarnings("rawtypes")
  public Object invoke(Object target, Method method, Object[] args) throws Throwable {
    Class proxyClass = target.getClass();
    Class[] interfaces = proxyClass.getInterfaces();
    String interfaceName = interfaces[0].getName();
    UUID id = UUID.randomUUID();
    logger.fine("Calling abstract client to have " + interfaceName + ":" + method.getName()
            + " serviced. Request id " + id);
    Object obj = client.call(interfaceName, new Payload(id, method.getName(), args));
    logger.fine("Returning results to the local caller.");
    return obj;
  }
}
