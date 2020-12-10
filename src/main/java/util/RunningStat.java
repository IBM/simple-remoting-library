package util;

/**
 * Streaming algorithm to compute stats See: Donald Knuth's Art of Computer Programming, Vol 2, page
 * 232, 3rd ed
 *
 * @author psuryan
 */

public class RunningStat {

  private int n;

  private double oldM;

  private double newM;

  private double oldS;

  private double newS;

  private double min = 0;

  private double max = 0;

  private double sum = 0;

  private String tag;

  private Object lock = new Object();

  public RunningStat(String tag) {
    n = 0;
    this.tag = tag;
  }

  public void clear() {
    synchronized (lock) {
      n = 0;
    }
  }

  public void addSample(double sample) {
    synchronized (lock) {
      n++;
      if (n == 1) {
        oldM = newM = sample;
        oldS = 0.0;
      } else {
        newM = oldM + (sample - oldM) / n;
        newS = oldS + (sample - oldM) * (sample - newM);

        oldM = newM;
        oldS = newS;
      }
      if (sample < min) {
        min = sample;
      }
      if (sample > max) {
        max = sample;
      }
      sum = sum + sample;
    }
  }

  public int getNumSamples() {
    synchronized (lock) {
      return n;
    }
  }

  public double getMean() {
    synchronized (lock) {
      return (n > 0) ? newM : 0.0;
    }
  }

  public double getVariance() {
    synchronized (lock) {
      return ((n > 1) ? newS / (n - 1) : 0.0);
    }
  }

  public double getStdDev() {
    synchronized (lock) {
      return Math.sqrt(getVariance());
    }
  }

  public double getMin() {
    synchronized (lock) {
      return min;
    }
  };

  public double getMax() {
    synchronized (lock) {
      return max;
    }
  };

  public double getSum() {
    synchronized (lock) {
      return sum;
    }
  };

  public String getTag() {
    return tag;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Stats for " + this.getTag());
    sb.append("\nCount: " + this.getNumSamples());
    sb.append("\nSum: " + this.getSum());
    sb.append("\nMax: " + this.getMax());
    sb.append("\nMin: " + this.getMin());
    sb.append("\nAverage: " + this.getMean());
    sb.append("\nVariance: " + this.getVariance());
    sb.append("\nSD: " + this.getStdDev());
    return sb.toString();
  }

}