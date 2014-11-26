package cn.dreampie;

import cn.dreampie.LessCompiler;
import cn.dreampie.LessException;
import cn.dreampie.resource.LessSource;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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

  protected boolean followDelete = false;

  private static final WatchEvent.Kind<?>[] watchEvents = {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE};

  /**
   * Execute the MOJO.
   *
   * @throws LessException if something unexpected occurs.
   */
  public void execute() {
    log.info("sourceDirectory = " + sourceDirectory);
    log.info("outputDirectory = " + outputDirectory);
    log.debug("includes = " + Arrays.toString(includes));
    log.debug("excludes = " + Arrays.toString(excludes));
    log.debug("force = " + force);
    log.debug("lessJs = " + lessJs);
    log.debug("skip = " + skip);

    if (!skip) {
      String[] files = getIncludedFiles();

      if (files == null || files.length < 1) {
        log.info("Nothing to compile - no LESS sources found");
      } else {
        if (log.isDebugEnabled()) {
          log.debug("included files = " + Arrays.toString(files));
        }

        Object lessCompiler = initLessCompiler();
        compileIfChanged(files, lessCompiler);
        if (watch) {
          log.info("Watching " + sourceDirectory);
          if (force) {
            force = false;
            log.info("Disabled the 'force' flag in watch mode.");
          }
          startWatch(files, lessCompiler);
        }
      }
    } else {
      log.info("Skipping plugin execution per configuration");
    }
  }


  private void compileIfChanged(String[] files, Object lessCompiler) {
    for (String file : files) {
      compileIfChanged(lessCompiler, file);
    }
  }

  private void compileIfChanged(Object lessCompiler, String file) {
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
        log.info("Finished compilation to " + outputDirectory + " in " + (System.currentTimeMillis() - compilationStarted) + " ms");
      } else if (!watch) {
        log.info("Bypassing LESS source: " + file + " (not modified)");
      }
    } catch (IOException e) {
//                    buildContext.addMessage(input, 0, 0, "Error compiling LESS source", BuildContext.SEVERITY_ERROR, e);
      log.error("Error while compiling LESS source: " + file, e);
    } catch (LessException e) {
      log.error("Error while compiling LESS source: " + file, e);
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

  private void startWatch(String[] files, Object compiler) {
    Path sourcePath = sourceDirectory.toPath();
    Path outPath = outputDirectory.toPath();
    WatchService watchService = null;
    try {
      watchService = initWatch(sourcePath);
    } catch (IOException e) {
      throw new LessException("Error watch sourceDirectory: " + sourceDirectory.getAbsolutePath(), e);
    }
    boolean changed = true;
    while (true) {
      if (changed) {
        log.info("Waiting for changes...");
        changed = false;
      }

      WatchKey watchKey = null;
      try {
        watchKey = watchService.take();
      } catch (InterruptedException e) {
        throw new LessException("Error get watch key", e);
      }
      Path dir = (Path) watchKey.watchable();

      for (WatchEvent<?> event : watchKey.pollEvents()) {
        Path file = dir.resolve((Path) event.context());
        log.debug(String.format("watched %s - %s", event.kind().name(), file));

        if (Files.isDirectory(file)) {
          if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
            // watch created folder.
            try {
              file.register(watchService, watchEvents);
            } catch (IOException e) {
              throw new LessException("Error register new folder", e);
            }
            log.debug(String.format("watch %s", file));
          }
          continue;
        }

        String fileName = sourcePath.relativize(file).toString();

        String outName = "";

        for (String name : files) {
          if (name != null && name.equals(fileName)) {

            if (isCompress()) {
              outName = fileName.replace(".less", ".min.css");
            } else {
              outName = fileName.replace(".less", ".css");
            }

            if (Files.exists(sourcePath.resolve(fileName)) && Files.notExists(outPath.resolve(outName))) {
              compileIfChanged(compiler, fileName);
            }

            if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
              if (followDelete) {
                try {
                  if (Files.deleteIfExists(outPath.resolve(outName))) {
                    log.info(String.format("deleted %s with %s", outName, name));
                    changed = true;
                  }
                } catch (IOException e) {
                  throw new LessException("Error delete file:" + outName, e);
                }
              }
            } else if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name()) || event.kind().name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
              compileIfChanged(compiler, fileName);
              changed = true;
            }
          }
        }
      }
      watchKey.reset();
    }

  }

  private WatchService initWatch(Path sourceDirectory) throws IOException {
    final WatchService watchService = sourceDirectory.getFileSystem().newWatchService();

    Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        dir.register(watchService, watchEvents);
        log.debug(String.format("watch %s", dir));
        return FileVisitResult.CONTINUE;
      }
    });
    return watchService;
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

  public boolean isFollowDelete() {
    return followDelete;
  }

  public void setFollowDelete(boolean followDelete) {
    this.followDelete = followDelete;
  }
}