package config.data

/**
 * Парсер файла Cargo.toml.
 */
object CargoTomlParser {

    /**
     * Извлекает прямые зависимости из содержимого манифеста.
     * @param tomlContent Сырой текст файла Cargo.toml.
     * @return Список найденных прямых зависимостей.
     */
    fun parseDependencies(tomlContent: String): List<Dependency> {
        val dependencies = mutableListOf<Dependency>()
        var currentSection = ""
        val depSections = listOf("dependencies", "dev-dependencies", "build-dependencies")

        tomlContent.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEach

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length - 1).trim()
                return@forEach
            }

            if (currentSection in depSections) {
                val dep = parseDependencyLine(line, currentSection)
                if (dep != null) dependencies.add(dep)
            }
        }
        return dependencies
    }

    private fun parseDependencyLine(line: String, section: String): Dependency? {
        val eqIndex = line.indexOf('=')
        if (eqIndex == -1) return null

        val name = line.substring(0, eqIndex).trim().removeSurrounding("\"")
        val versionPart = line.substring(eqIndex + 1).trim()

        val version = extractVersion(versionPart)
        val type = when (section) {
            "dev-dependencies" -> "dev"
            "build-dependencies" -> "build"
            else -> "normal"
        }
        return Dependency(name, version, type)
    }

    private fun extractVersion(part: String): String {
        return if (part.startsWith("\"")) {
            part.removeSurrounding("\"").substringBefore("\"").trim()
        } else if (part.startsWith("{")) {
            val regex = Regex("version\\s*=\\s*\"([^\"]+)\"")
            regex.find(part)?.groupValues?.get(1) ?: "unknown"
        } else {
            "unknown"
        }
    }
}