package common;

/**
 *
 * @author psuryan
 *
 */
public interface PayloadHandler {
  public Object marshall (Payload payload);
  public Payload unmarshall (Object object);
}
