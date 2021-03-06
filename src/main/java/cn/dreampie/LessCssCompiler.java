package cn.dreampie;

import cn.dreampie.LessCompiler;
import cn.dreampie.LessException;
import cn.dreampie.resource.LessSource;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

/**
 * Created by wangrenhui on 2014/7/11.
 */
public class LessCssCompiler extends AbstractLessCss {

  private Log log = LogKit.getLog();

  private Object lessCompiler;
  /**
   * The directory for compiled CSS stylesheets.
   */
  protected File outputDirectory;

  /**
   * When <code>true</code> the LESS compiler will compress the CSS stylesheets.
   */
  private boolean compress;

  /**
   * When <code>true</code> the plugin will watch for changes in LESS files and compile if it detects one.
   */
  protected boolean watch = false;

  /**
   * When <code>true</code> the plugin will watch for changes in LESS files and compile if it detects one.
   */
  private int watchInterval = 1000;

  /**
   * The character encoding the LESS compiler will use for writing the CSS stylesheets.
   */
  private String encoding;

  /**
   * When <code>true</code> forces the LESS compiler to always compile the LESS sources. By default LESS sources are only compiled when modified (including imports) or the CSS stylesheet does not exists.
   */
  private boolean force;

  /**
   * The location of the LESS JavasSript file.
   */
  private File lessJs;

  /**
   * The format of the output file names.
   */
  private String outputFileFormat;

  private static final String FILE_NAME_FORMAT_PARAMETER_REGEX = "\\{fileName\\}";

  private long lastErrorModified = 0;

  /**
   * Execute the MOJO.
   *
   * @throws LessException if something unexpected occurs.
   */
  public void execute() throws LessException {
    log.info("sourceDirectory = " + sourceDirectory);
    log.info("outputDirectory = " + outputDirectory);
    log.debug("includes = " + Arrays.toString(includes));
    log.debug("excludes = " + Arrays.toString(excludes));
    log.debug("force = " + force);
    log.debug("lessJs = " + lessJs);
    log.debug("skip = " + skip);

    if (!skip) {
      if (watch) {
        log.info("Watching " + sourceDirectory);
        if (force) {
          force = false;
          log.info("Disabled the 'force' flag in watch mode.");
        }
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (watch && !Thread.currentThread().isInterrupted()) {
          executeInternal();
          try {
            Thread.sleep(watchInterval);
          } catch (InterruptedException e) {
            log.error("interrupted");
          }
        }
      } else {
        executeInternal();
      }
    } else {
      log.info("Skipping plugin execution per configuration");
    }
  }

  private void executeInternal() throws LessException {
    String[] files = getIncludedFiles();

    if (files == null || files.length < 1) {
      log.info("Nothing to compile - no LESS sources found");
    } else {
      if (log.isDebugEnabled()) {
        log.debug("included files = " + Arrays.toString(files));
      }

      Object lessCompiler = initLessCompiler();
      compileIfChanged(files, lessCompiler);
    }
  }

  private void compileIfChanged(String[] files, Object lessCompiler) {
    for (String file : files) {
      File input = new File(sourceDirectory, file);

      buildContext.removeMessages(input);

      if (outputFileFormat != null) {
        file = outputFileFormat.replaceAll(FILE_NAME_FORMAT_PARAMETER_REGEX, file.replace(".less", ""));
      }

      String outFile = null;
      if (isCompress()) {
        outFile = file.replace(".less", ".min.css");
      } else {
        outFile = file.replace(".less", ".css");
      }

      File output = new File(outputDirectory, outFile);

      if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
        log.error("Cannot create output directory " + output.getParentFile());
        return;
      }

      try {
        LessSource lessSource = new LessSource(input);
        long lessLastModified = lessSource.getLastModifiedIncludingImports();
        if (!output.exists() || (force || output.lastModified() < lessLastModified) && lastErrorModified < lessLastModified) {
          lastErrorModified = lessLastModified;
          long compilationStarted = System.currentTimeMillis();
          log.info("Compiling LESS source: " + file);
          if (lessCompiler instanceof LessCompiler) {
            ((LessCompiler) lessCompiler).compile(lessSource, output, force);
          }
          buildContext.refresh(output);
          log.info("Finished compilation to " + output + " in " + (System.currentTimeMillis() - compilationStarted) + " ms");
        } else if (!watch) {
          log.info("Bypassing LESS source: " + file + " (not modified)");
        }
      } catch (IOException e) {
        log.error("Error while compiling LESS source: " + file, e);
      } catch (LessException e) {
        log.error("Error while compiling LESS source: " + file, e);
      }
    }
  }

  private Object initLessCompiler() throws LessException {
    if (lessCompiler == null) {
      LessCompiler newLessCompiler = new LessCompiler();
      newLessCompiler.setCompress(compress);
      newLessCompiler.setEncoding(encoding);
      if (lessJs != null) {
        try {
          newLessCompiler.setLessJs(lessJs.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new LessException(
              "Error while loading LESS JavaScript: " + lessJs.getAbsolutePath(), e);
        }
      }
      lessCompiler = newLessCompiler;
    }
    return lessCompiler;
  }


  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public boolean isCompress() {
    return compress;
  }

  public void setCompress(boolean compress) {
    this.compress = compress;
  }

  public boolean isWatch() {
    return watch;
  }

  public void setWatch(boolean watch) {
    this.watch = watch;
  }

  public int getWatchInterval() {
    return watchInterval;
  }

  public void setWatchInterval(int watchInterval) {
    this.watchInterval = watchInterval;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public File getLessJs() {
    return lessJs;
  }

  public void setLessJs(File lessJs) {
    this.lessJs = lessJs;
  }

  public String getOutputFileFormat() {
    return outputFileFormat;
  }

  public void setOutputFileFormat(String outputFileFormat) {
    this.outputFileFormat = outputFileFormat;
  }

  public long getLastErrorModified() {
    return lastErrorModified;
  }

  public void setLastErrorModified(long lastErrorModified) {
    this.lastErrorModified = lastErrorModified;
  }
}