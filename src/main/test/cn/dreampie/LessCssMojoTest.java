package cn.dreampie;

import org.junit.Test;
import org.sonatype.plexus.build.incremental.ThreadBuildContext;

import java.io.File;

import static org.junit.Assert.*;

public class LessCssMojoTest {

  @Test
  public void testExecute() throws Exception {
    LessCssCompiler lessCssCompiler = new LessCssCompiler();
    lessCssCompiler.setBuildContext(ThreadBuildContext.getContext());
    File dir = new File(getClass().getResource("/").getPath());
    lessCssCompiler.setSourceDirectory(dir);
    lessCssCompiler.setOutputDirectory(dir);
    lessCssCompiler.setCompress(true);
    lessCssCompiler.setWatch(true);

    lessCssCompiler.execute();
  }
}