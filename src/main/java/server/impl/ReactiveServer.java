package server.impl;

import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;

import common.Payload;
import common.PayloadHandler;
import common.impl.ReactiveConfig;
import server.AbstractServer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.remote.AssociatedEvent;
import akka.remote.AssociationErrorEvent;
import akka.remote.DisassociatedEvent;
import akka.remote.RemotingErrorEvent;
import akka.remote.RemotingLifecycleEvent;
import akka.remote.RemotingListenEvent;
import akka.remote.RemotingShutdownEvent;
import akka.routing.RoundRobinPool;

/**
 *
 * @author psuryan
 *
 */
public class ReactiveServer extends AbstractServer {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private ActorRef listener;

  private ActorSystem system;

  public ReactiveServer(Object obj, PayloadHandler payloadHandler) {
    super(obj, payloadHandler);
    logger.info("Payloadhandler is " + payloadHandler);
  }

  @Override
  public synchronized void doInitialization(Properties props) throws Exception {
    Properties properties = new Properties();
    properties.setProperty("akka.remote.netty.tcp.port", props.getProperty("port"));
    properties.setProperty("akka.remote.netty.tcp.hostname", ReactiveConfig.getHostName());
    Config overrides = ConfigFactory.parseProperties(properties);
    logger.info("The overrides " + overrides);
    Config orig = ConfigFactory.parseReader(new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(ReactiveConfig.CONFIG_FILE_NAME)));
    logger.info("The orig " + orig);
    Config config = overrides.withFallback(orig);
    logger.info("The merged " + config);
    system = ActorSystem.create(ReactiveConfig.REMOTE_SYSTEM_NAME, config);
    int instances = Integer.parseInt(props.getProperty(ReactiveConfig.INSTANCES));
    listener = system.actorOf(Props.create(ServerActor.class, () -> new ServerActor(this))
            .withRouter(new RoundRobinPool(instances)), ReactiveConfig.REMOTE_ACTOR_NAME);
    logger.info(
            "Server running in " + ReactiveConfig.getHostName() + ":" + props.getProperty("port")
                    + " with the name " + listener.path() + " with instance count " + instances);
  }

  @Override
  public void shutDown() throws Exception {
    system.terminate();
  }
}

class ServerActor extends AbstractLoggingActor {
  private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

  private AbstractServer server;

  public ServerActor(AbstractServer server) {
    this.server = server;
  }

  public void preStart() throws Exception {
    getContext().getSystem().eventStream().subscribe(getSelf(), RemotingLifecycleEvent.class);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(Payload.class, message -> {
      Object[] response;
      Payload req = message;
      logger.debug("Received String message: {}", message);
      logger.info("FROM_CLIENT "+ getSender()+" REQ_ID "+ req.getUuid());
      try {
        Object ret = server.invoke(req);
        if (ret != null) {
          logger.debug("sending return for " + req);
          req.setResponse(ret);
        } else {
          logger.debug("return value" + " is null");
        }
      } catch (Throwable t) {
        req.setResponse(t);
      } finally {
        response = (Object[]) server.getPayloadHandler().marshall(req);
        getSender().tell(response, getSelf());
        logger.info("TO_CLIENT "+ getSender()+" REQ_ID "+ req.getUuid());
      }
    }).match(AssociatedEvent.class, message -> {
      logger.info(message.eventName()+" " + message.getRemoteAddress());
    }).match(DisassociatedEvent.class, message -> {
      logger.info(message.eventName()+" " + message.getRemoteAddress());
    }).match(RemotingShutdownEvent.class, message -> {
      logger.info("RemotingShutdownEvent happend. That's all we know.");
    }).match(RemotingErrorEvent.class, message -> {
      logger.info("RemotingErrorEvent "+ message.cause().getMessage());
    }).match(AssociationErrorEvent.class, message -> {
      if (message.getCause().getMessage().contains("quarantined this system")) {
        logger.error(message.eventName()+" Quarantined by "+ message.getRemoteAddress());
      }
      logger.info("AssociationErrorEvent "+ message.cause().getMessage());
      //Not listening for the QuarantinedEvent because that is not dispatched on the system being quarantined.
      //See: https://stackoverflow.com/questions/32471088/akka-cluster-detecting-quarantined-state
    }).match(RemotingListenEvent.class, message -> {
      logger.info("RemotingListenEvent "+ message.getListenAddresses());
    }).build();
  }
}
