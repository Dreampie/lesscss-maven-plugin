package cn.dreampie;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.Scanner;
import org.lesscss.LessSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Createdby wangrenhui on 2014/7/11.
 */
// CHECKSTYLE_OFF: LineLength
@Mojo(name = "tree", threadSafe = true)
// CHECKSTYLE_ON: LineLength
public class LessCssTreeMojo extends AbstractLessCssMojo {

  private Log log = getLog();

  protected String[] getIncludedFiles() {
    Scanner scanner = buildContext.newScanner(sourceDirectory, true);
    scanner.setIncludes(includes);
    scanner.setExcludes(excludes);
    scanner.scan();
    return scanner.getIncludedFiles();
  }

  public void execute() {
    LogKit.setLog(log);
    log.info("sourceDirectory = " + sourceDirectory);
    log.debug("includes = " + Arrays.toString(includes));
    log.debug("excludes = " + Arrays.toString(excludes));

    String[] files = getIncludedFiles();

    if (files == null || files.length < 1) {
      log.info("No less sources found");
    } else {
      log.info("The following less sources have been resolved:");

      for (String file : files) {
        File lessFile = new File(sourceDirectory, file);
        try {
          LessSource lessSource = new LessSource(lessFile);
          listLessSource(lessSource, file, 0, false);
        } catch (FileNotFoundException e) {
          throw new LessCssException("Error while loading less source: " + lessFile.getAbsolutePath(), e);
        } catch (IOException e) {
          throw new LessCssException("Error while loading less source: " + lessFile.getAbsolutePath(), e);
        }
      }
    }
  }

  private void listLessSource(LessSource lessSource, String path, int level, boolean last) {
    String prefix = "";
    if (level > 0) {
      for (int i = 1; i <= level; i++) {
        if (i == level && last) {
          prefix = prefix + "`-- ";
        } else if (i == level) {
          prefix = prefix + "|-- ";
        } else {
          prefix = prefix + "|   ";
        }
      }
    }

    log.info(prefix + path);

    Iterator<Map.Entry<String, LessSource>> it = lessSource.getImports().entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, LessSource> entry = it.next();
      listLessSource(entry.getValue(), entry.getKey(), level + 1, !it.hasNext());
    }
  }
}
