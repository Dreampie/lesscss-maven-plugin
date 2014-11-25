package cn.dreampie;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by ice on 14-11-17.
 * author Dreampie
 * phase generate-resources
 * goal compile
 */
// CHECKSTYLE_OFF: LineLength
@Mojo(name = "watch", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
// CHECKSTYLE_ON: LineLength
public class LessCssWatchMojo extends AbstractLessCssMojo {

  @Parameter(defaultValue = "false")
  protected boolean followDelete;

  public void execute() throws MojoExecutionException, MojoFailureException {
    LogKit.setLog(log);
    initCompiler();
    start();
  }

  private void start() {
    lessCssCompiler.setFollowDelete(followDelete);
    lessCssCompiler.setWatch(true);
    lessCssCompiler.execute();
  }

}
