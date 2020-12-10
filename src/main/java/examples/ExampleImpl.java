package examples;

import java.util.List;

/**
 *
 * @author psuryan
 *
 */
public class ExampleImpl implements ExampleApi {

  public String echo() {
    return "Echo ";
  }

  @Override
  public int add(int a, int b) {
    return a + b;
  }

  @Override
  public int subtract(int a, int b) {
    return a - b;
  }

  @Override
  public int multiply(int a, int b) {
    return a * b;
  }

  @Override
  public double divide(int a, int b) {
    return 1.0 * a / b;
  }

  public ComplexObject getComplexObject(int i) {
    return new ComplexObject(i + 1);
  }

  @Override
  public int process(ComplexObject ob) {
    return ob.getId();
  }

  @Override
  public int process(List<ComplexObject> inputs) {
    return inputs.stream().mapToInt(i -> i.getId()).sum();
  }
}
