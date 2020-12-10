package common.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author psuryan
 *
 */
public class ReactiveConfig {

  public static final String CONFIG_FILE_NAME = "actor-system.conf";

  public static final String LOCAL_ACTOR_NAME = "local";

  public static final String LOCAL_SYSTEM_NAME = "LocalSystem";

  public static final String REMOTE_ACTOR_NAME = "remote";

  public static final String REMOTE_SYSTEM_NAME = "RemoteSystem";

  public static final String INSTANCES = "instances";

  public static final String LIST_OF_SERVERS = "listOfServers";

  public static final String WATCHING_MODE = "watching";

  public static final int JMX_PORT = 7828;

  public static final int RETRY_INTERVAL = 20; //Seconds

  private static final String COMMA = ",";

  private static final String COLON = "\\:";

  public static final String getRemoteActorSelectionStr(String host, int port) {
    return "akka.tcp://" + REMOTE_SYSTEM_NAME + "@" + host + ":" + port + "/user/"
            + REMOTE_ACTOR_NAME;
  }

  public static List<Pair<String, Integer>> getServerList(String listOfServers) {
    List<Pair<String, Integer>> serverList = new ArrayList<>();
    String[] tokens = listOfServers.split(COMMA);
    for (String token : tokens) {
      String[] pair = token.split(COLON);
      serverList.add(Pair.of(pair[0], Integer.parseInt(pair[1])));
    }
    return serverList;
  }

  public static final String getHostName() {
    try {
      // TODO: this is not the most reliable way to do this.
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      System.err.println("EXCEPTION IN GETTING HOST NAME " + e.getCause());
      return "127.0.0.1";
    }
  }

  public static int findNextAvailablePort(int basePort) {
    int port = basePort;
    while (!ReactiveConfig.available(port)) {
      port++;
    }
    return port;
  }

  private static boolean available(int port) {
    try (Socket ignored = new Socket(getHostName(), port)) {
      return false;
    } catch (IOException ignored) {
      return true;
    }
  }

  public static void main(String[] args) {
    List<Pair<String, Integer>> serverList = ReactiveConfig
            .getServerList("localhost:8090,localhost:9092,localhost:9999");
    serverList.forEach(x -> System.out.println(x.getLeft() + " " + x.getRight()));
    System.out.println(ReactiveConfig.getHostName());
    System.out.println(ReactiveConfig.available(12346));
  }
}
