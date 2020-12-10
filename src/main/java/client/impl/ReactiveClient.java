package client.impl;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import client.AbstractClient;
import client.RemoteServiceUnavailableException;
import common.Payload;
import common.PayloadHandler;
import common.impl.ReactiveConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import scala.concurrent.duration.Duration;

/**
 *
 * @author psuryan
 *
 */
public class ReactiveClient extends AbstractClient {

  private ActorRef local;

  public ReactiveClient(PayloadHandler payloadHandler) {
    super(payloadHandler);
  }

  @Override
  protected void doCall(String interfaceName, Payload req) {
    logger.fine("Going to invoke " + req.getMethod() + " on the implementer of  " + interfaceName
            + " by sending a message.");
    local.tell(req, ActorRef.noSender());
  }

  @Override
  public synchronized void init(Properties props) throws Exception {
    InputStream configFile = getClass().getClassLoader()
            .getResourceAsStream(ReactiveConfig.CONFIG_FILE_NAME);
    Config config = ConfigFactory.parseReader(new InputStreamReader(configFile));
    ActorSystem system = ActorSystem.create(
            ReactiveConfig.LOCAL_SYSTEM_NAME + "_" + ReactiveConfig.getHostName().replace(".", "-"),
            config);
    String listOfServers = props.getProperty(ReactiveConfig.LIST_OF_SERVERS);
    boolean watching = Boolean
            .parseBoolean((String) props.getOrDefault(ReactiveConfig.WATCHING_MODE, "false"));
    String watchOverride = System.getProperty(ReactiveConfig.WATCHING_MODE);
    if (watchOverride != null) {
      watching = Boolean.valueOf(watchOverride);
    }
    List<Pair<String, Integer>> serverList = ReactiveConfig.getServerList(listOfServers);
    List<String> routeePaths = serverList.stream()
            .map(x -> ReactiveConfig.getRemoteActorSelectionStr(x.getLeft(), x.getRight()))
            .collect(Collectors.toList());
    local = system.actorOf(Props.create(ClientActor.class, this, routeePaths, watching),
            ReactiveConfig.LOCAL_ACTOR_NAME);
    logger.info("Created the local " + local.path());
  }

  @Override
  protected void shutDown() throws Exception {
    // nothing to do
  }

}

class ClientActor extends AbstractLoggingActor {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  // Keeping the watching disabled, until I figure out a way deal with the quarantined state.
  private boolean watching = false;

  private ReactiveClient client;

  private Router router;

  private Map<String, Integer> inactive = new ConcurrentHashMap<>();

  private List<String> routes;

  public ClientActor(ReactiveClient cli, List<String> routes, boolean watchingMode) {
    client = cli;
    this.routes = routes;
    this.watching = watchingMode;
    for (String route : routes) {
      inactive.put(route, 0);
    }
    router = new Router(new RoundRobinRoutingLogic(), new ArrayList<Routee>());
  }

  public void preStart() throws Exception {
    log.info("Started local with watching mode " + watching);
    sendIdentifyRequest();
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(Object[].class, res -> {
      log.debug("Received payload from server ", res);
      Object[] response = res;
      Payload payload = client.getPayloadHandler().unmarshall(response);
      log.info("FROM_SERVER " + " REQ_ID " + payload.getUuid());
      client.onReturn(payload);
    }).match(Payload.class, payload -> {
      if (!allDead()) {
        log.debug("Sending payload to server " + payload);
        log.info("TO_SERVER " + " REQ_ID " + payload.getUuid());
        router.route(payload, self());
      } else {
        log.error("No remote service instances available. Returning error to the client.");
        payload.setResponse(new RemoteServiceUnavailableException());
        client.onReturn(payload);
      }
    }).match(Terminated.class, message -> {
      // We should never reach this block if watching is disabled.
      String pathOfDead = message.actor().path().toString();
      log.error("Remote service instance has shutdown " + pathOfDead + " existenceConfirmed "
              + message.getExistenceConfirmed() + " addressTerminated "
              + message.getAddressTerminated() + " watching " + watching);
      router = router.removeRoutee(message.actor());
      inactive.put(pathOfDead, 1);
    }).match(ActorIdentity.class, identity -> {
      Optional<ActorRef> poossibleRemote = identity.getActorRef();
      if (!poossibleRemote.isPresent()) {
        // remote actor is not there ActorRef is null. This could happen because our periodic
        // ping returned false as the remote actor did't respond
        // if the watching is enabled we would not be in this clause - we will be handling the
        // terminated message even before the ping happened.
        String pathOfAbsent = identity.correlationId().toString();
        log.error("Remote service instance specified in configuration failed to identify itself "
                + pathOfAbsent);
        if (inactive.containsKey(pathOfAbsent)) {
          // The remote actor was known to be down before this ping.
          inactive.put(pathOfAbsent, inactive.get(pathOfAbsent) + 1);
        } else {
          // The first time the remote actor is being reported as inactive
          inactive.put(pathOfAbsent, 0);
          router.removeRoutee(getContext().actorSelection(pathOfAbsent));
        }
      } else {
        // remote actor is there - ActorRef is not null.
        String pathOfPresent = identity.correlationId().toString();
        if (inactive.containsKey(pathOfPresent)) {
          // Re remote actor was previously marked as being unavailable. So we make it available...
          router = router.addRoutee(new ActorRefRoutee(poossibleRemote.get()));
          inactive.remove(pathOfPresent);
          log.info("Remote service instance identified itself " + poossibleRemote.get().path()
                  + " with identity " + pathOfPresent);
          if (watching) {
            // if watching is enabled, we being to watch the actor for the first time.
            getContext().watch(poossibleRemote.get());
            log.info("Beginning to watch the remote actor " + poossibleRemote.get().path()
                    + " with identity " + pathOfPresent);
          }
        } else {
          // the remote actor is already known to be present. There is nothing to do.
          log.debug("Remote service instance checked-in again " + poossibleRemote.get().path()
                  + " with identity " + pathOfPresent);
        }
      }
      log.debug("Good remote instances " + router.routees() + " Bad remote instances "
              + inactive.entrySet());
    }).match(ReceiveTimeout.class, x -> {
      sendIdentifyRequest();
    }).build();
  }

  private void sendIdentifyRequest() {
    for (String path : routes) {
      getContext().actorSelection(path).tell(new Identify(path), self());
    }
    getContext().system().scheduler().scheduleOnce(
            Duration.create(ReactiveConfig.RETRY_INTERVAL, SECONDS), self(),
            ReceiveTimeout.getInstance(), getContext().dispatcher(), self());
  }

  private boolean allDead() {
    return inactive.entrySet().size() == routes.size() ? true : false;
  }
}
