package fileparser

import org.apache.commons.cli.{CommandLine, CommandLineParser, DefaultParser, HelpFormatter => OldHelpFormatter, Options, Option => CLIOption}

import java.io.{PrintWriter, StringWriter}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Try
import scala.util.matching.Regex

object ConfigParser {
  val kv: Regex = """([^=]+)=(.+)""".r

  val conf: CLIOption = CLIOption.builder("c").longOpt("conf").hasArg(true).desc("Path to project configuration file").get()
  val name: CLIOption = CLIOption.builder("n").longOpt("name").hasArg(true).desc("Mod name").get()
//  val version: CLIOption = CLIOption.builder("V").longOpt("version").hasArg(true).desc("Mod version. Default is 1.0.0.0").get()
//  val author: CLIOption = CLIOption.builder("a").longOpt("author").hasArg(true).desc("Mod author. Used for mod initialization").get()
  val path: CLIOption = CLIOption.builder("p").longOpt("path").hasArg(true).desc("Path to mod sources").get()
  val textures: CLIOption = CLIOption.builder("t").longOpt("textures").hasArg(true).desc("Relative path to output TextureBank").get()
  val materials: CLIOption = CLIOption.builder("m").longOpt("materials").hasArg(true).desc("Relative path to output MaterialBank").get()
  val visuals: CLIOption = CLIOption.builder("v").longOpt("visuals").hasArg(true).desc("Relative path to output VisualBank").get()
//  val initialize: CLIOption = CLIOption.builder("i").longOpt("initialize").hasArg(false).desc("Flag to initialize new mod and directories structure inside sources directory").get()
  val ui: CLIOption = CLIOption.builder("ui").longOpt("ui").hasArg(false).desc("Flag to launch UI app. If cfg file is specified - new or existing project will be opened").get()

  val options: Options = new Options()
    .addOption(conf)
    .addOption(name)
//    .addOption(version)
//    .addOption(author)
    .addOption(path)
    .addOption(textures)
    .addOption(materials)
    .addOption(visuals)
    .addOption(ui)
//    .addOption(initialize)
  val parser: CommandLineParser = new DefaultParser()

  case class RequiredConfig(
    name: String,
    sources: String,
  )

  case class Config(
    name: Option[String] = None,
//    author: Option[String] = None,
//    version: Option[String] = None,
    sources: Option[String] = None,
    textures: Option[String] = None,
    materials: Option[String] = None,
    visuals: Option[String] = None,
    ui: Boolean = false,
//    initialize: Boolean = false,
  ) {
    def requiredConfig: Option[RequiredConfig] = for {
      name <- name
      if name.nonEmpty
      sources <- sources
      if sources.nonEmpty
    } yield RequiredConfig(name, sources)
  }

  def parse(path: Path): Option[Config] = Try {
    //last arg wins. and then overridden by cli
    val fileArgs: Map[String, String] = Files.readAllLines(path).asScala.toList.collect {
      case kv(k, v) => k.trim -> v.trim.replaceAll("\"", "")
    }.toMap
    Config(
      name = fileArgs.get(name.getLongOpt),
//      author = fileArgs.get(author.getLongOpt),
//      version = fileArgs.get(version.getLongOpt),
      sources = fileArgs.get(this.path.getLongOpt),
      textures = fileArgs.get(textures.getLongOpt),
      materials = fileArgs.get(materials.getLongOpt),
      visuals = fileArgs.get(visuals.getLongOpt),
//      initialize = fileArgs.get(initialize.getLongOpt).exists(_ equalsIgnoreCase "True"), //??
    )
  }.toOption

  def parse(args: Array[String]): Config = {
    val cmd: CommandLine = parser.parse(options, args)

    def getValue(opt: CLIOption): Option[String] = Option(cmd.getOptionValues(opt)).flatMap(_.headOption)

    val configFile: Option[String] = getValue(conf)
    val initialConfig: Config = configFile.map(Paths.get(_)).flatMap(parse).getOrElse(Config())
    initialConfig.copy(
      name = getValue(name).orElse(initialConfig.name),
//      author = getValue(author).orElse(initialConfig.author),
//      version = getValue(version).orElse(initialConfig.version),
      sources = getValue(path).orElse(initialConfig.sources),
      textures = getValue(textures).orElse(initialConfig.textures),
      materials = getValue(materials).orElse(initialConfig.materials),
      visuals = getValue(visuals).orElse(initialConfig.visuals),
      ui = cmd.hasOption(ui),
//      initialize = cmd.hasOption(initialize) || initialConfig.initialize, //??
    )
  }

  def helpMessage: String = {
    val formatter: OldHelpFormatter = new OldHelpFormatter()
    val stringWriter: StringWriter = new StringWriter()
    val printWriter: PrintWriter = new PrintWriter(stringWriter)
    formatter.printHelp(printWriter, 120, "java -jar lib/bg3-banks-gen.jar args...", "Configuration from config file will be overridden by CLI arguments.", options, 3, 5, "Mod name and sources path should be present in either configuration file or CLI arguments.")
    val helpString: String = stringWriter.toString
    printWriter.flush()
    helpString
  }
}
