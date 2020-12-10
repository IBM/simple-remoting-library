package client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import common.Payload;
import common.PayloadHandler;
import common.ProtocolHandler;

/**
 *
 * @author psuryan
 *
 */
public abstract class AbstractClient extends ProtocolHandler {

  public AbstractClient(PayloadHandler payloadHandler) {
    super(payloadHandler);
  }

  protected final Logger logger = Logger.getLogger(getClass().getName());

  private static final ExecutorService threadPool = Executors.newCachedThreadPool();

  private Map<String, ResponseHandler> handlers = new ConcurrentHashMap<String, ResponseHandler>();

  @SuppressWarnings("unchecked")
  public <T extends Object> T get(Class<T> interfaceClass) throws ClassNotFoundException {
    logger.info("interfacClass " + interfaceClass.getCanonicalName());
    InvocationHandler handler = new Invoker(this);
    return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
            new Class[] { interfaceClass }, handler);
  }

  public Object call(String interfaceName, Payload req)
          throws InterruptedException, ExecutionException {
    ResponseHandler handler = new ResponseHandler();
    handlers.put(req.getUuid().toString(), handler);
    Future<Object> future = threadPool.submit(handler);
    doCall(interfaceName, req);
    return future.get();
  }

  public void done() throws Exception {
    threadPool.shutdown();
    shutDown();
  }

  protected abstract void doCall(String interfaceName, Payload req);

  public void onReturn(Payload payload) {
    String key = payload.getUuid().toString();
    logger.fine("Removing " + key);
    handlers.get(key).handle(payload.getResponse());
    handlers.remove(key);
  }

  public abstract void init(Properties props) throws Exception;

  protected abstract void shutDown() throws Exception;
}

class ResponseHandler implements Callable<Object> {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private final CountDownLatch replyLatch = new CountDownLatch(1);

  private Object callbackResults;

  public Object call() throws Exception {
    replyLatch.await();
    return callbackResults;
  }

  void handle(Object obj) {
    callbackResults = obj;
    replyLatch.countDown();
    logger.fine("Handling return.");
  }
}
