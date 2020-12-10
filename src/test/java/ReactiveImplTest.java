import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import util.StopWatchRegistry;
import client.AbstractClient;
import client.impl.ClientFactory;
import common.Protocol;
import common.ProtocolSpec;
import common.impl.ReactiveConfig;
import examples.ComplexObject;
import examples.ExampleApi;
import examples.ExampleImpl;
import server.AbstractServer;
import server.impl.ServerFactory;


/**
 *
 * @author psuryan
 *
 */
public class ReactiveImplTest {

  private static Logger logger = Logger.getLogger(ReactiveImplTest.class.getName());

  private static ExampleApi exampleApi;

  private static StopWatchRegistry registry = StopWatchRegistry.getInstance();

  private static AbstractServer server;

  private static final int iterations = 100;

  @BeforeClass
  public static void beforeClass() {
    try {
      Properties props = new Properties();
      props.setProperty("listOfServers", ReactiveConfig.getHostName() + ":8000");
      props.setProperty(ReactiveConfig.WATCHING_MODE, "false");
      props.setProperty("port", "8000"); // needed by server
      props.setProperty(ReactiveConfig.INSTANCES, "1"); // server needs it
      ProtocolSpec spec = new ProtocolSpec();
      spec.setProps(props);
      server = makeServer(spec);
      exampleApi = getApi(false, props);
      Thread.sleep(40000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @AfterClass
  public static void afterClass() throws Exception {
    server.shutDown();
  }

  @Test
  public void testComplexReturnType() throws IOException {
    for (int i = 0; i < iterations; i++) {
      registry.getWatch("testComplexReturnType").tick();
      ComplexObject d = exampleApi.getComplexObject(i);
      registry.getWatch("testComplexReturnType").tock();
      assertTrue(d != null);
    }
  }

  @Test
  public void testComplexArgumentType() throws IOException {
    for (int i = 0; i < iterations; i++) {
      registry.getWatch("testComplexArgumentType").tick();
      int ret = exampleApi.process(new ComplexObject(i));
      registry.getWatch("testComplexArgumentType").tock();
      assertEquals(i, ret);
    }
  }

  @Test
  public void testCollectionType() throws IOException {
    registry.getWatch("testCollectionType").tick();
    List<ComplexObject> inputs = new ArrayList<ComplexObject>();
    inputs.add(new ComplexObject(3));
    inputs.add(new ComplexObject(5));
    inputs.add(new ComplexObject(13));
    int ret = exampleApi.process(inputs);
    registry.getWatch("testCollectionType").tock();
    assertEquals(21, ret);
  }

  @Test
  public void testPrimitives() throws Exception {

    Random r = new Random();

    for (int count = 0; count < iterations; count++) {
      int type = r.nextInt(4);
      int a = r.nextInt(10000) + 1;
      int b = r.nextInt(10000) + 1;

      double result = -1;

      switch (type) {
        case 0:
          registry.getWatch("add").tick();
          result = exampleApi.add(a, b);
          registry.getWatch("add").tock();
          if ((a + b) != result) {
            error("add", result, (a + b), count, iterations);
          }
          break;
        case 1:
          registry.getWatch("subtract").tick();
          result = exampleApi.subtract(a, b);
          registry.getWatch("subtract").tock();
          if ((a - b) != result) {
            error("subtract", result, (a - b), count, iterations);
          }
          break;
        case 2:
          registry.getWatch("multiply").tick();
          result = exampleApi.multiply(a, b);
          registry.getWatch("multiply").tock();
          if ((a * b) != result) {
            error("multiply", result, (a * b), count, iterations);
          }
          break;
        case 3:
          registry.getWatch("divide").tick();
          result = exampleApi.divide(a, b);
          registry.getWatch("divide").tock();
          if ((1.0 * a / b) != result) {
            error("divide", result, (1.0 * a / b), count, iterations);
          }
          break;
      }
    }
  }

  // @Test
  public void dummyTest() throws IOException {
    try {
      Thread.sleep(60000000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
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

  private static AbstractServer makeServer(ProtocolSpec spec)
          throws IllegalAccessException, InstantiationException, Exception {
    return ServerFactory.getInstance().getServer(Protocol.REMOTE_ACTOR,
            ExampleImpl.class.newInstance(), spec.getProps());
  }

  private void error(String type, double result, double actual, int count, int iterations) {
    logger.info(Thread.currentThread().getName() + " Invalid results. Existing at " + count + " of "
            + iterations + " at " + type + " result " + result + " actual " + actual);
    System.exit(-1);
  }
}
