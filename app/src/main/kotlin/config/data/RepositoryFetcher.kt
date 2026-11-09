
package config.data

import java.net.HttpURLConnection
import java.net.URI

object RepositoryFetcher {

    fun fetchCargoToml(repoUrl: String): String {
        val url = repoUrl.trim().removeSuffix("/")

        require(url.startsWith("https://github.com/")) {
            "Expected GitHub repository URL"
        }

        val path = url.removePrefix("https://github.com/")
            .removeSuffix(".git")

        val parts = path.split("/")

        require(parts.size == 2) {
            "Expected URL: https://github.com/owner/repository"
        }

        val owner = parts[0]
        val repo = parts[1]

        for (branch in listOf("master", "main")) {
            val rawUrl =
                "https://raw.githubusercontent.com/$owner/$repo/$branch/Cargo.toml"

            try {
                println("Trying: $rawUrl")
                return download(rawUrl)
            } catch (e: Exception) {
                println("Cannot download from $branch: ${e.message}")
            }
        }

        throw IllegalStateException(
            "Cargo.toml not found in master or main branch"
        )
    }

    private fun download(url: String): String {
        val connection = URI(url).toURL()
            .openConnection() as HttpURLConnection

        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            if (connection.responseCode != 200) {
                throw IllegalStateException(
                    "HTTP ${connection.responseCode}"
                )
            }

            return connection.inputStream.bufferedReader()
                .use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
