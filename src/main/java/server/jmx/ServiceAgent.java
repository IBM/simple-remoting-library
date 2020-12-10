package server.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import common.impl.ReactiveConfig;
import server.impl.ServerLauncher;

/**
 *
 * @author psuryan
 *
 */
public class ServiceAgent {

  private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

  public static void createJmxConnectorServer() throws IOException, MalformedObjectNameException,
          InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    int port = ReactiveConfig.findNextAvailablePort(ReactiveConfig.JMX_PORT);
    LocateRegistry.createRegistry(port);
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    ObjectName objName = new ObjectName("com.ibm.watsonx.framework.remoting:name=ServiceStats");
    mbs.registerMBean(new ServiceStats(), objName);
    JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + ReactiveConfig.getHostName()
            + "/jndi/rmi://" + ReactiveConfig.getHostName() + ":" + port + "/jmxrmi");
    JMXConnectorServer svr = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
    logger.info("Started the JMX server with the URL " + url);
    svr.start();
  }
}
