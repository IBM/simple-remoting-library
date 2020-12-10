package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author psuryan
 */

public class StopWatchRegistry {

  private Map<String, Stopwatch> watches = new ConcurrentHashMap<String, Stopwatch>();

  private volatile static StopWatchRegistry uniqueInstance;

  public static StopWatchRegistry getInstance() {
    if (uniqueInstance == null) {
      synchronized (StopWatchRegistry.class) {
        if (uniqueInstance == null) {
          uniqueInstance = new StopWatchRegistry();
        }
      }
    }
    return uniqueInstance;
  }

  public Stopwatch getWatch(String name){
    Stopwatch watch = watches.get(name);
    if (watch == null) {
      watch = new Stopwatch(name);
      watches.put(name, watch);
    }
    return watch;
  }

  public Set<String> getTags(){
    return watches.keySet();
  }

  public List<Stopwatch> getWatches(){
    List<Stopwatch> watchList = new ArrayList<Stopwatch>();
    for (Map.Entry<String, Stopwatch> entry: watches.entrySet()){
      watchList.add(entry.getValue());
    }
    return watchList;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (Map.Entry<String, Stopwatch> entry : watches.entrySet()) {
      sb.append(entry.getKey() + " : " + entry.getValue().toString());
      sb.append("\n");
    }
    return sb.toString();
  }
}
