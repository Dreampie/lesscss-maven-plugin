package cn.dreampie;

import org.apache.maven.plugin.logging.Log;

import java.util.Observable;

/**
 * Created by wangrenhui on 2014/7/22.
 */
public class LessExecuteThread extends Observable implements Runnable {
  private Log log = LogKit.getLog();
  private int restartInterval = 1000;

  private LessCssCompiler lessCssCompiler;

  public LessExecuteThread(LessCssCompiler lessCssCompiler, int restartInterval) {
    this.lessCssCompiler = lessCssCompiler;
    this.restartInterval = restartInterval;
  }

  // 此方法一经调用，等待restartInterval时间之后可以通知观察者，在本例中是监听线程
  public void doBusiness() {
    log.error("LessExecuteThread is dead");
    try {
      Thread.sleep(restartInterval);
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }

    if (true) {
      super.setChanged();
    }
    notifyObservers();
  }


  public void run() {
    try {
      lessCssCompiler.execute();
    } catch (LessException e) {
      log.error(e.getMessage(), e);
      doBusiness();
    }
  }
}
