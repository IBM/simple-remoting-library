package server.jmx;

import java.util.ArrayList;
import java.util.List;

import util.StopWatchRegistry;

/**
 *
 * @author psuryan
 *
 */
public class ServiceStats implements ServiceStatsMBean {

  @Override
  public List<String> getMethods() {
    List<String> methods = new ArrayList<String>();
    for (String str: StopWatchRegistry.getInstance().getTags()) {
      methods.add(str);
    }
    return methods;
  }

  @Override
  public int getNumSamples(String method) {
    return StopWatchRegistry.getInstance().getWatch(method).getStat().getNumSamples();
  }

  @Override
  public double getMean(String method) {
    return StopWatchRegistry.getInstance().getWatch(method).getStat().getMean();
  }

  @Override
  public double getVariance(String method) {
    return StopWatchRegistry.getInstance().getWatch(method).getStat().getVariance();
  }

  @Override
  public double getStdDev(String method) {
    return StopWatchRegistry.getInstance().getWatch(method).getStat().getStdDev();
  }

  @Override
  public double getMin(String method) {
    return StopWatchRegistry.getInstance().getWatch(method).getStat().getMin();
  }

  @Override
  public double getMax(String method) {
    return StopWatchRegistry.getInstance().getWatch(method).getStat().getMax();
  }

  @Override
  public double getSum(String method) {
    return StopWatchRegistry.getInstance().getWatch(method).getStat().getSum();
  }

  @Override
  public int getNumSamplesAll() {
    List<String> methods = getMethods();
    int count = 0;
    for (String method: methods){
      count +=  StopWatchRegistry.getInstance().getWatch(method).getStat().getNumSamples();
    }
    return count;
  }
}
