package cn.dreampie;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;

/**
 * Created by ice on 14-11-17.
 * author Dreampie
 */
public abstract class AbstractLessCssMojo extends AbstractMojo {

  protected Log log = getLog();

  protected LessCssCompiler lessCssCompiler;
  /**
   * component
   */
  @Component
  protected BuildContext buildContext;

  /**
   * The source directory containing the LESS sources.
   */
  @Parameter(defaultValue = "${project.basedir}/src/main/lesscss")
  protected File sourceDirectory;

  /**
   * List of files to include. Specified as fileset patterns which are relative to the source directory. Default value is: { "**\/*.less" }
   */
  @Parameter
  protected String[] includes = new String[]{"**/*.less"};

  /**
   * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
   */
  @Parameter
  protected String[] excludes = new String[]{};

  /**
   * Whether to skip plugin execution.
   * This makes the build more controllable from profiles.
   */
  @Parameter(defaultValue = "false")
  protected boolean skip;

  /**
   * The directory for compiled css.
   */
  @Parameter(defaultValue = "${project.build.directory}/style", required = true)
  protected File outputDirectory;

  /**
   * When <code>true</code> the less compiler will compress the css.
   */
  @Parameter(defaultValue = "false")
  protected boolean compress;

  /**
   * The character encoding the less compiler will use for writing the css.
   */
  @Parameter(defaultValue = "${project.build.sourceEncoding}")
  protected String encoding;

  /**
   * When <code>true</code> forces the less compiler to always compile the less sources. By default less sources are only compiled when modified (including imports) or the CSS stylesheet does not exists.
   */
  @Parameter(defaultValue = "false")
  protected boolean force;

  /**
   * The location of the less css file.
   */
  @Parameter
  protected File lessJs;

  /**
   * The location of the NodeJS executable.
   */
  @Parameter
  protected String nodeExecutable;

  /**
   * The format of the output file names.
   */
  @Parameter
  protected String outputFileFormat;

  /**
   * The compile args.
   */
  @Parameter
  protected String[] args;

  /**
   * The restart thread time.
   */
  @Parameter(defaultValue = "1000")
  protected int restartInterval;

}
