package common;

import java.util.Properties;

/**
 *
 * @author psuryan
 *
 */

public class ProtocolSpec {

  private Protocol protocol;

  private Properties props;

  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  public Properties getProps() {
    return props;
  }

  public void setProps(Properties props) {
    this.props = props;
  }
}
