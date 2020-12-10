package common;

import java.lang.reflect.Method;

import com.google.common.primitives.Primitives;

/**
 *
 * @author psuryan
 *
 */
public class ReflectionUtils {
  public static Object invokeObjectWithPayload(Payload req, Object obj) throws Throwable {
    String methodName = req.getMethod();
    Method method = getMethod(methodName, obj.getClass().getMethods(), req.getParams());
    try {
      Object ret = method.invoke(obj, req.getParams());
      return ret;
    } catch (Exception e) {
      e.printStackTrace(); // TODO: this is bad. Remove
      throw e.getCause();
    }
  }

  private static Method getMethod(String methodName, Method[] methods, Object[] params) {
    Method toInvoke = null;
    methodLoop: for (Method method : methods) {
      if (!methodName.equals(method.getName())) {
        continue;
      }
      Class<?>[] paramTypes = method.getParameterTypes();
      if (params == null && paramTypes == null) {
        toInvoke = method;
        break;
      } else if (params == null || paramTypes == null || paramTypes.length != params.length) {
        continue;
      }

      for (int i = 0; i < params.length; ++i) {
        @SuppressWarnings("rawtypes")
        Class paramClass = null;
        if (Primitives.isWrapperType(params[i].getClass())) {
          paramClass = Primitives.unwrap(params[i].getClass());
        } else {
          paramClass = params[i].getClass();
        }
        if (!paramTypes[i].isAssignableFrom(paramClass)) {
          continue methodLoop;
        }
      }
      toInvoke = method;
    }
    return toInvoke;
  }
}
