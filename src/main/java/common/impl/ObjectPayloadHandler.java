package common.impl;

import java.util.UUID;

import common.Payload;
import common.PayloadHandler;

/**
 *
 * @author psuryan
 *
 */
public class ObjectPayloadHandler implements PayloadHandler {

  public Object marshall(Payload payload) {
    int paramLen = payload.getParams() == null ? 0 : payload.getParams().length;
    int len = paramLen + (payload.getResponse() != null ? 3 : 2);
    Object[] array = new Object[len];
    array[0] = payload.getUuid();
    array[1] = payload.getMethod();
    if (paramLen > 0) {
      System.arraycopy(payload.getParams(), 0, array, 2, payload.getParams().length);
    }
    if (payload.getResponse() != null) {
      array[array.length - 1] = payload.getResponse();
    }
    return array;
  }

  public Payload unmarshall(Object object) {
    Object[] array = (Object[]) object;
    Payload payload = new Payload();
    Object[] input = new Object[array.length - 2];
    payload.setUuid((UUID) array[0]);
    payload.setMethod((String) array[1]);
    for (int i = 2; i < array.length; i++) {
      input[i - 2] = array[i];
    }
    payload.setParams(input);
    payload.setResponse(array[array.length - 1]);
    return payload;
  }
}
