package cli

import config.AppConfig

/**
 * Вывод конфигурации в консоль.
 * Форматирует объект [AppConfig] в вид ключ = значение.
 * Используется на первом этапе для демонстрации успешной загрузки настроек.
 */

object ConfigPrinter {
    fun print(config: AppConfig) {
        println("=== Configuration ===")
        config.asKeyValue().forEach { (k, v) -> println("$k = $v") }
    }
}