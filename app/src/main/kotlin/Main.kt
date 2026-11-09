import cli.ConfigPrinter
import config.ConfigException
import config.ConfigLoader
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: dependency-visualizer <path-to-config.csv>")
        exitProcess(2)
    }

    try {
        val config = ConfigLoader.load(args[0])
        ConfigPrinter.print(config)
    } catch (e: ConfigException) {
        System.err.println("Configuration error: ${e.message}")
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println("Unexpected error: ${e.message}")
        exitProcess(3)
    }
}