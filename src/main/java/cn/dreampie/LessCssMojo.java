package cn.dreampie;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;

/**
 * Created by ice on 14-11-17.
 * author Dreampie
 * phase generate-resources
 * goal compile
 */
// CHECKSTYLE_OFF: LineLength
@Mojo(name = "compile", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
// CHECKSTYLE_ON: LineLength
public class LessCssMojo extends AbstractLessCssMojo {

  private LessCssCompiler lessCssCompiler;

  public void execute() throws MojoExecutionException, MojoFailureException {
    LogKit.setLog(getLog());
    initCompiler();
    start();
  }

  private void initCompiler() {
    lessCssCompiler = new LessCssCompiler();
    lessCssCompiler.setBuildContext(buildContext);
    lessCssCompiler.setIncludes(includes);
    lessCssCompiler.setExcludes(excludes);
    lessCssCompiler.setLessJs(lessJs);
    lessCssCompiler.setSkip(skip);
    lessCssCompiler.setSourceDirectory(sourceDirectory);
    lessCssCompiler.setOutputDirectory(outputDirectory);
    lessCssCompiler.setForce(force);
    lessCssCompiler.setEncoding(encoding);
    lessCssCompiler.setCompress(compress);
    lessCssCompiler.setWatch(watch);
    lessCssCompiler.setWatchInterval(watchInterval);
    lessCssCompiler.setNodeExecutable(nodeExecutable);
    lessCssCompiler.setOutputFileFormat(outputFileFormat);
  }

  private void start() {
    if (watch) {
      LessExecuteThread thread = new LessExecuteThread(lessCssCompiler, restartInterval);
      LessExecuteListener listen = new LessExecuteListener(thread);
      thread.addObserver(listen);
      new Thread(thread).start();
    } else {
      lessCssCompiler.execute();
    }
  }

}
