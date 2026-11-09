package config

/**
 * Перечисление (Enum) доступных ключей конфигурации.
 * Сопоставляет строковые имена ключей из CSV-файла
 * с внутренними константами. Содержит вспомогательный метод [fromCsvName]
 * для безопасного поиска ключа по строке.
 */

enum class ConfigKey(val csvName: String) {
    PACKAGE_NAME("packageName"),
    REPOSITORY_URL("repositoryUrl"),
    REPOSITORY_MODE("repositoryMode"),
    OUTPUT_FILE_NAME("outputFileName"),
    MAX_DEPTH("maxDepth");

    companion object {
        fun fromCsvName(name: String): ConfigKey? =
            entries.firstOrNull { it.csvName == name }
    }
}