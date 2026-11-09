import cli.ConfigPrinter
import config.ConfigException
import config.ConfigLoader
import config.data.CargoTomlParser
import config.data.RepositoryFetcher
import kotlin.system.exitProcess

/**
 * Главная точка входа в приложение.
 */
fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: dependency-visualizer <path-to-config.csv>")
        exitProcess(2)
    }

    try {
        val config = ConfigLoader.load(args[0])
        ConfigPrinter.print(config)

        println("\n--- Fetching dependencies ---")

        val tomlContent = RepositoryFetcher.fetchCargoToml(config.repositoryUrl)
        val dependencies = CargoTomlParser.parseDependencies(tomlContent)

        if (dependencies.isEmpty()) {
            println("No dependencies found in Cargo.toml.")
        } else {
            println("Direct dependencies of package '${config.packageName}':")
            dependencies.forEach { dep ->
                println(" - ${dep.name} (v${dep.version}) [${dep.type}]")
            }
        }

    } catch (e: ConfigException) {
        System.err.println("Configuration error: ${e.message}")
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        exitProcess(3)
    }
}