package cn.dreampie;

import org.apache.maven.plugin.logging.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by wangrenhui on 2014/7/22.
 */
public class LessExecuteListener implements Observer {

  private Log log = LogKit.getLog();

  private LessExecuteThread lessExecuteThread;

  public LessExecuteListener(LessExecuteThread lessExecuteThread) {
    this.lessExecuteThread = lessExecuteThread;
  }


  public void update(Observable o, Object arg) {
    lessExecuteThread.addObserver(this);
    new Thread(lessExecuteThread).start();
    log.error("LessExecuteThread is start");
  }
}
