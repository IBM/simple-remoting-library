import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import util.StopWatchRegistry;
import client.AbstractClient;
import client.impl.ClientFactory;
import common.Protocol;
import common.ProtocolSpec;
import common.impl.ReactiveConfig;
import examples.ExampleApi;
import examples.ExampleImpl;

/**
 *
 * @author psuryan
 *
 */
public class ReactiveImplTestClient {

  private static ExampleApi exampleApi;

  private static StopWatchRegistry registry = StopWatchRegistry.getInstance();

  public static void init() {
    try {
      Properties props = new Properties();
      props.setProperty("listOfServers", ReactiveConfig.getHostName() + ":8000" + ","
              + ReactiveConfig.getHostName() + ":8001");
      // props.setProperty("listOfServers", ReactiveConfig.getHostName() + ":8000");
      props.setProperty(ReactiveConfig.WATCHING_MODE, "false");

      ProtocolSpec spec = new ProtocolSpec();
      spec.setProps(props);
      exampleApi = getApi(false, props);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static ExampleApi getApi(boolean isLocal, Properties props) throws Exception {
    if (isLocal) {
      return new ExampleImpl();
    } else {
      AbstractClient client = ClientFactory.getInstance().getClient(Protocol.REMOTE_ACTOR, props);
      return client.get(ExampleApi.class);
    }
  }

  public static void main(String[] args) {
    init();
    int numThreads = 8;
    int sleepDuration = 10;
    int iterations = 15;
    ExampleWork work = new ExampleWork(sleepDuration, registry, iterations, exampleApi);
    for (int i = 0; i < numThreads; i++) {
      new Thread(work).start();
    }
  }
}

class ExampleWork implements Runnable {

  private static Logger logger = Logger.getLogger(ExampleWork.class.getName());

  private int howMuchToSleep;

  private StopWatchRegistry registry;

  private int iterations;

  private ExampleApi exampleApi;

  public ExampleWork(int howMuchToSleep, StopWatchRegistry registry, int iterations,
          ExampleApi exampleApi) {
    this.howMuchToSleep = howMuchToSleep;
    this.registry = registry;
    this.iterations = iterations;
    this.exampleApi = exampleApi;
  }

  @Override
  public void run() {
    int round = 0;
    int errors = 0;
    while (true) {
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(howMuchToSleep));
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      errors = testPrimitives(round++, errors);
      logger.info("Round " + round + " total " + round * iterations + " errors " + errors);
    }
  }

  public int testPrimitives(int round, int initalErrors) {
    int errors = initalErrors;
    Random r = new Random();
    for (int count = 0; count < iterations; count++) {
      int type = r.nextInt(4);
      int a = r.nextInt(10000) + 1;
      int b = r.nextInt(10000) + 1;

      double result = -1;

      switch (type) {
        case 0:
          registry.getWatch("add").tick();
          try {
            result = exampleApi.add(a, b);
          } catch (Exception e) {
            errors++;
            continue;
          }
          registry.getWatch("add").tock();
          if ((a + b) != result) {
            error("add", result, (a + b), count, iterations);
          }
          break;
        case 1:
          registry.getWatch("subtract").tick();
          try {
            result = exampleApi.subtract(a, b);
          } catch (Exception e) {
            errors++;
            continue;
          }
          registry.getWatch("subtract").tock();
          if ((a - b) != result) {
            error("subtract", result, (a - b), count, iterations);
          }
          break;
        case 2:
          registry.getWatch("multiply").tick();
          try {
            result = exampleApi.multiply(a, b);
          } catch (Exception e) {
            errors++;
            continue;
          }
          registry.getWatch("multiply").tock();
          if ((a * b) != result) {
            error("multiply", result, (a * b), count, iterations);
          }
          break;
        case 3:
          registry.getWatch("divide").tick();
          try {
            result = exampleApi.divide(a, b);
            registry.getWatch("divide").tock();
          } catch (Exception e) {
            errors++;
            continue;
          }
          if ((1.0 * a / b) != result) {
            error("divide", result, (1.0 * a / b), count, iterations);
          }
          break;
      }
    }
    return errors;
  }

  private void error(String type, double result, double actual, int count, int iterations) {
    logger.info(Thread.currentThread().getName() + " Invalid results. Existing at " + count + " of "
            + iterations + " at " + type + " result " + result + " actual " + actual);
    System.exit(-1);
  }

}
