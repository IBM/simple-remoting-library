package examples;

import java.util.List;

/**
 *
 * @author psuryan
 *
 */
public interface ExampleApi {
  public String echo();
  public int add (int a, int b);
  public int subtract (int a, int b);
  public int multiply (int a, int b);
  public double divide (int a, int b);
  public ComplexObject getComplexObject(int i);
  public int process(ComplexObject ob);
  public int process(List<ComplexObject> inputs);
}
