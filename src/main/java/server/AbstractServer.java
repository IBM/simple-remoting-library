package server;

import java.util.Properties;
import java.util.logging.Logger;

import util.StopWatchRegistry;
import common.Payload;
import common.PayloadHandler;
import common.ProtocolHandler;
import common.ReflectionUtils;

/**
 *
 * @author psuryan
 *
 */
public abstract class AbstractServer extends ProtocolHandler {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private Object obj;

  public AbstractServer(Object obj, PayloadHandler payloadHandler) {
    super(payloadHandler);
    this.obj = obj;
  }

  public void init(Properties props) throws Exception {
    doInitialization(props);
  }

  @SuppressWarnings("rawtypes")
  public Object invoke(Payload req) throws Throwable {
    StopWatchRegistry.getInstance().getWatch(req.getMethod()).tick();
    logger.fine(req.getUuid() + " " + req.getMethod() + " " + req.getParams().length);
    Object ret = ReflectionUtils.invokeObjectWithPayload(req, obj);
    logger.fine("Invoked " + req.getMethod() + " on " + obj.getClass().getName());
    StopWatchRegistry.getInstance().getWatch(req.getMethod()).tock();;
    return ret;
  }

  protected abstract void doInitialization(Properties props) throws Exception;

  public abstract void shutDown() throws Exception;

}
