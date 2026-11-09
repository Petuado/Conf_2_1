package config

enum class RepositoryMode { LOCAL, REMOTE }

/**
 * Храненит всю конфигурацию приложения.
 * Содержит строго типизированные поля, прошедшие валидацию.
 * Метод [asKeyValue] преобразует объект обратно в список пар ключ-значение
 */

data class AppConfig(
    val packageName: String,
    val repositoryUrl: String,
    val repositoryMode: RepositoryMode,
    val outputFileName: String,
    val maxDepth: Int
) {
    fun asKeyValue(): List<Pair<String, String>> = listOf(
        ConfigKey.PACKAGE_NAME.csvName to packageName,
        ConfigKey.REPOSITORY_URL.csvName to repositoryUrl,
        ConfigKey.REPOSITORY_MODE.csvName to repositoryMode.name.lowercase(),
        ConfigKey.OUTPUT_FILE_NAME.csvName to outputFileName,
        ConfigKey.MAX_DEPTH.csvName to maxDepth.toString()
    )
}