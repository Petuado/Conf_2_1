package config

import java.io.File
import java.net.URI

/**
 * Загрузчик и валидатор конфигурации.
 * Чтение CSV-файла построчно (с поддержкой комментариев и пустых строк).
 * Парсинг пар "ключ,значение" с проверкой на дубликаты и отсутствие обязательных полей.
 * Строгая валидация каждого параметра:
 * При любой ошибке выбрасывает [ConfigException] с указанием номера строки или параметра.
 */

object ConfigLoader {

    fun load(path: String): AppConfig {
        val file = File(path)
        if (!file.exists()) throw ConfigException("Config file not found: $path")
        if (!file.isFile) throw ConfigException("Config path is not a file: $path")

        val raw = parseCsv(file)
        return buildConfig(raw)
    }

    private fun parseCsv(file: File): Map<ConfigKey, String> {
        val result = mutableMapOf<ConfigKey, String>()
        file.readLines().forEachIndexed { idx, line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            val commaIdx = trimmed.indexOf(',')
            if (commaIdx <= 0) {
                throw ConfigException("Line ${idx + 1}: expected 'key,value', got: '$line'")
            }
            val keyRaw = trimmed.substring(0, commaIdx).trim()
            val valueRaw = trimmed.substring(commaIdx + 1).trim()

            val key = ConfigKey.fromCsvName(keyRaw)
                ?: throw ConfigException("Line ${idx + 1}: unknown config key '$keyRaw'")

            if (result.containsKey(key)) {
                throw ConfigException("Line ${idx + 1}: duplicate key '$keyRaw'")
            }
            if (valueRaw.isEmpty()) {
                throw ConfigException("Line ${idx + 1}: empty value for key '$keyRaw'")
            }
            result[key] = valueRaw
        }

        val missing = ConfigKey.entries.filter { it !in result }
        if (missing.isNotEmpty()) {
            throw ConfigException("Missing required keys: ${missing.joinToString { it.csvName }}")
        }
        return result
    }

    private fun buildConfig(raw: Map<ConfigKey, String>): AppConfig {
        return AppConfig(
            packageName = validatePackageName(raw.getValue(ConfigKey.PACKAGE_NAME)),
            repositoryUrl = validateRepositoryUrl(raw.getValue(ConfigKey.REPOSITORY_URL)),
            repositoryMode = validateRepositoryMode(raw.getValue(ConfigKey.REPOSITORY_MODE)),
            outputFileName = validateOutputFileName(raw.getValue(ConfigKey.OUTPUT_FILE_NAME)),
            maxDepth = validateMaxDepth(raw.getValue(ConfigKey.MAX_DEPTH))
        )
    }

    private fun validatePackageName(value: String): String {
        val regex = Regex("^[A-Za-z0-9._-]+$")
        if (!regex.matches(value)) {
            throw ConfigException(
                "packageName '$value' is invalid: allowed chars are letters, digits, '.', '_', '-'"
            )
        }
        return value
    }

    private fun validateRepositoryUrl(value: String): String {
        val uri = try {
            URI(value)
        } catch (e: Exception) {
            throw ConfigException("repositoryUrl '$value' is not a valid URI: ${e.message}")
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme !in setOf("http", "https", "file")) {
            throw ConfigException(
                "repositoryUrl '$value' must use http, https or file scheme"
            )
        }
        if (scheme == "file" && uri.path.isNullOrBlank()) {
            throw ConfigException("repositoryUrl '$value' has empty file path")
        }
        return value
    }

    private fun validateRepositoryMode(value: String): RepositoryMode =
        when (value.lowercase()) {
            "local" -> RepositoryMode.LOCAL
            "remote" -> RepositoryMode.REMOTE
            else -> throw ConfigException(
                "repositoryMode '$value' is invalid: expected 'local' or 'remote'"
            )
        }

    private fun validateOutputFileName(value: String): String {
        if (value.contains('/') || value.contains('\\')) {
            throw ConfigException("outputFileName '$value' must not contain path separators")
        }
        val ext = value.substringAfterLast('.', "")
        if (ext.lowercase() !in setOf("png", "svg", "jpg", "jpeg", "gv", "dot")) {
            throw ConfigException(
                "outputFileName '$value' must have a supported extension (png, svg, jpg, jpeg, gv, dot)"
            )
        }
        return value
    }

    private fun validateMaxDepth(value: String): Int {
        val n = value.toIntOrNull()
            ?: throw ConfigException("maxDepth '$value' is not an integer")
        if (n < 1) throw ConfigException("maxDepth '$value' must be >= 1")
        if (n > 100) throw ConfigException("maxDepth '$value' is unreasonably large (max 100)")
        return n
    }
}