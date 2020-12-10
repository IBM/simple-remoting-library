package examples;

import java.io.Serializable;

/**
 *
 * @author psuryan
 *
 */
public class ComplexObject implements Serializable {

  private static final long serialVersionUID = 2956183242135348503L;

  private int id;

  public ComplexObject(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return "returnEntity_" + id;
  }

  @Override
  public String toString() {
    return "id " + id + " name " + getName();
  }
}
