package config.data

import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * Загрузчик исходников из репозитория.
 * Поддерживает локальные файлы, прямые ссылки и GitHub-репозитории.
 */
object RepositoryFetcher {

    private const val HTTP_OK = 200
    private const val TIMEOUT_MS = 5000

    /**
     * Загружает содержимое Cargo.toml по указанному URL.
     *
     * @param url Адрес репозитория или файла.
     * @return Текстовое содержимое файла Cargo.toml.
     */
    fun fetchCargoToml(url: String): String {
        val uri = URI(url)

        if (uri.scheme == "file") {
            val file = File(uri)
            if (!file.exists()) {
                throw IllegalArgumentException("Local file not found: ${file.path}")
            }
            return file.readText()
        }

        if (uri.scheme == "http" || uri.scheme == "https") {
            if (isGitHubRepoUrl(url)) {
                return fetchFromGitHubRepo(url)
            }
            return httpGet(url)
        }

        throw IllegalArgumentException("Unsupported URL scheme: ${uri.scheme}")
    }

    private fun isGitHubRepoUrl(url: String): Boolean {
        val cleanUrl = url.trimEnd('/')
        val regex = Regex("^https?://github\\.com/[^/]+/[^/]+$")
        return regex.matches(cleanUrl)
    }

    private fun fetchFromGitHubRepo(repoUrl: String): String {
        val cleanUrl = repoUrl.trimEnd('/')
        val parts = cleanUrl.split("/")
        val user = parts[parts.size - 2]
        val repo = parts[parts.size - 1]

        val branches = listOf("main", "master")
        val paths = listOf("Cargo.toml", "app/Cargo.toml", "backend/Cargo.toml", "src/Cargo.toml")

        for (branch in branches) {
            for (path in paths) {
                val rawUrl = "https://raw.githubusercontent.com/$user/$repo/$branch/$path"
                try {
                    val content = httpGet(rawUrl)
                    println(">> Found Cargo.toml at: $rawUrl")
                    return content
                } catch (e: Exception) {
                }
            }
        }
        throw RuntimeException(
            "Could not find Cargo.toml in repository $repoUrl. " +
                    "Tried branches: $branches, paths: $paths"
        )
    }

    private fun httpGet(urlStr: String): String {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.setRequestProperty("User-Agent", "Kotlin-Dep-Visualizer")

        try {
            val code = connection.responseCode
            if (code != HTTP_OK) {
                throw RuntimeException("HTTP $code for $urlStr")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}