package common;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author psuryan
 *
 */
public class Payload implements Serializable {

  private static final long serialVersionUID = -5883463224535103338L;

  private UUID uuid;

  private String method;

  private Object[] params;

  private Object response;

  public Payload() {

  }

  public Payload(UUID uuid, String method, Object[] params) {
    this.uuid = uuid;
    this.method = method;
    this.params = params;
  }

  public Object[] getParams() {
    return params;
  }

  public String getMethod() {
    return method;
  }

  public UUID getUuid() {
    return uuid;
  }

  public Object getResponse() {
    return response;
  }

  public void setResponse(Object response) {
    this.response = response;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setParams(Object[] params) {
    this.params = params;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("UUID : " + this.getUuid());
    sb.append(" Method : " + this.getMethod());
    sb.append(" params : ");
    for (Object param : this.getParams()) {
      sb.append(param);
    }
    sb.append("Response : " + this.getResponse() != null ? this.getResponse() : "");
    return sb.toString();
  }
}
