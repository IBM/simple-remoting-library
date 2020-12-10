package util;

/**
 * @author psuryan
 */

public class Stopwatch {

  private long start = 0;

  private RunningStat stat;

  public Stopwatch(String name){
    this.stat = new RunningStat(name);
  }

  public RunningStat getStat() {
    return stat;
  }

  public void tick() {
    start = System.currentTimeMillis();
  }

  public void tock() {
    stat.addSample(System.currentTimeMillis() - start);
    start = 0;
  }

  @Override
  public String toString() {
    return stat.toString();
  }
}
