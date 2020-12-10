package server.jmx;

import java.util.List;

/**
 *
 * @author psuryan
 *
 */
public interface ServiceStatsMBean {

  public List<String> getMethods();

  public int getNumSamples(String method);

  public int getNumSamplesAll();

  public double getMean(String method);

  public double getVariance(String method);

  public double getStdDev(String method);

  public double getMin(String method);

  public double getMax(String method);

  public double getSum(String method);
}
