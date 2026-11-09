# Dependency Graph Visualizer

Инструмент визуализации графа зависимостей для менеджера пакетов.

**Текущий статус:** Этап 1 — минимальный прототип с конфигурацией.

---

## Содержание

- [О проекте](#о-проекте)
- [Требования](#требования)
- [Структура проекта](#структура-проекта)
- [Конфигурация](#конфигурация)
- [Запуск](#запуск)
- [Обработка ошибок](#обработка-ошибок)
- [Пример работы](#пример-работы)
- [Разработка](#разработка)

---

## О проекте

Учебный проект: CLI-инструмент, который строит граф зависимостей пакетов
из репозитория и сохраняет его в виде изображения.

Готовые библиотеки и менеджеры пакетов для получения зависимостей **не используются** —
парсинг манифестов и построение графа реализуются самостоятельно.

Проект развивается поэтапно; каждый этап фиксируется тегом в Git.

---

### Что реализовано в этапе 1

- Чтение параметров из конфигурационного файла формата CSV.
- Строгая валидация всех пяти параметров с понятными сообщениями об ошибках.
- Вывод загруженной конфигурации в формате `ключ = значение`.
- Коды возврата процесса для интеграции с внешними скриптами.
- Запуск без сети и без Gradle wrapper (через локальный `kotlinc`).

### Что реализовано в этапе 2

- Загрузка манифеста `Cargo.toml` напрямую из публичного GitHub-репозитория
  (без использования `git` CLI и сторонних библиотек).
- Собственный построчный парсер TOML для извлечения прямых зависимостей
  из секций `[dependencies]`, `[dev-dependencies]`, `[build-dependencies]`.
- Вывод списка прямых зависимостей (имя, версия, тип) в консоль.


---

## Требования

- **JDK** 17 или выше (проверено на JDK 27, OpenJDK от Homebrew).
- Один из вариантов сборки:
    - **Kotlin compiler** 2.x (`kotlinc`) — рекомендуемый способ для запуска;
    - либо **Gradle** 9.x, установленный локально (wrapper не используется,
      т.к. окружение может быть без сети).

Установка Kotlin-компилятора (macOS, Homebrew):

```bash
brew install kotlin
kotlinc -version   # ожидается: info: kotlinc-jvm 2.x.x
```

---

## Структура проекта

```
Conf_2_1/
├── app/
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── Main.kt                    # точка входа, обработка кодов возврата
│       ├── config/
│       │   ├── AppConfig.kt           # модель конфигурации
│       │   ├── ConfigKey.kt           # перечисление ключей CSV
│       │   ├── ConfigException.kt     # доменное исключение
│       │   └── ConfigLoader.kt        # парсинг + валидация
│       ├── cli/
│       │   └── ConfigPrinter.kt       # вывод конфигурации
│       └── data/                      # Логика сбора данных (Этап 2)
│           ├── Dependency.kt          # модель зависимости
│           ├── RepositoryFetcher.kt   # загрузка Cargo.toml по URL
│           └── CargoTomlParser.kt     # парсер TOML без библиотек
├── config.csv                         # конфигурация по умолчанию
├── Cargo.toml                         # тестовый манифест для проверки
├── run.sh                             # сборка + запуск через kotlinc
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## Конфигурация

Пользовательские параметры задаются в CSV-файле `key,value` — по одной паре на строку.

### Формат

- Одна пара `ключ,значение` на строку.
- Значение может содержать запятые — разделителем считается **первая** запятая.
- Пустые строки и строки, начинающиеся с `#`, игнорируются.
- Все пять ключей обязательны; дубликаты запрещены.

### Параметры

| Ключ             | Тип                   | Ограничения                                                                 | Пример                              |
|------------------|-----------------------|-----------------------------------------------------------------------------|-------------------------------------|
| `packageName`    | строка                | только `A-Z a-z 0-9 . _ -`                                                  | `my-app`                            |
| `repositoryUrl`  | URI                   | схема `http`, `https` или `file`; для `file` путь не пустой                  | `https://github.com/example/repo`   |
| `repositoryMode` | enum                  | `local` или `remote`                                                        | `remote`                            |
| `outputFileName` | имя файла             | без разделителей пути; расширение из `png, svg, jpg, jpeg, gv, dot`         | `dependency-graph.png`              |
| `maxDepth`       | целое число           | `1 ≤ maxDepth ≤ 100`                                                        | `3`                                 |

### Пример `config.csv`

```csv
packageName,my-app
repositoryUrl,https://github.com/example/repo
repositoryMode,remote
outputFileName,dependency-graph.png
maxDepth,3
```

---

## Запуск

### Вариант 1 — через `run.sh` (рекомендуется)

```bash
chmod +x run.sh
./run.sh                  # использует config.csv в корне проекта
./run.sh path/to/my.csv   # или собственный конфиг
```

Скрипт:

1. Находит директорию с исходниками (`app/src/main/kotlin` либо `src/main/kotlin`).
2. Проверяет наличие `kotlinc` и `config.csv`.
3. Компилирует в `build/app.jar`.
4. Запускает `java -jar` с указанным конфигом.

### Вариант 2 — вручную (без скрипта)

```bash
kotlinc app/src/main/kotlin -include-runtime -d build/app.jar
java -jar build/app.jar config.csv
```

## Обработка ошибок

Все ошибки конфигурации перехватываются и выводятся в `stderr` с осмысленным
сообщением. Приложение не падает со стектрейсом.

### Коды возврата

| Код | Значение                              |
|-----|---------------------------------------|
| 0   | Успех                                 |
| 1   | Ошибка конфигурации                   |
| 2   | Неверные аргументы CLI                |
| 3   | Непредвиденная ошибка                 |

При сборе данных (Этап 2) возможны следующие сообщения:

Could not find Cargo.toml in repository ... — файл отсутствует или репозиторий приватный.
HTTP 404 for ... — неверный URL или удалённый файл.
Unsupported URL scheme — поддерживаются только http, https и file.
---

## Пример работы

### Успешный запуск

```bash
$ ./run.sh
>> Source dir : /Users/petya/Downloads/Conf_2_1/app/src/main/kotlin
>> Output jar : /Users/petya/Downloads/Conf_2_1/build/app.jar
>> Compiling...
>> Running with config: /Users/petya/Downloads/Conf_2_1/config.csv
=== Configuration ===
packageName = my-app
repositoryUrl = https://github.com/Petuado/Conf_2_1
repositoryMode = remote
outputFileName = dependency-graph.png
maxDepth = 3

--- Fetching dependencies ---
Direct dependencies of package 'my-app':
 - serde (v1.0) [normal]
 - tokio (v1.28) [normal]
 - reqwest (v0.11) [normal]
 - mockito (v0.31) [dev]
```

### Ошибочный конфиг

```bash
$ cat bad.csv
packageName,my app
repositoryUrl,ftp://x
repositoryMode,hybrid
outputFileName,graph.txt
maxDepth,abc

$ ./run.sh bad.csv
Configuration error: packageName 'my app' is invalid:
allowed chars are letters, digits, '.', '_', '-'
$ echo $?
1
```

### Ошибка в CLI

```bash
$ java -jar build/app.jar
Usage: dependency-visualizer <path-to-config.csv>
$ echo $?
2
```

---

## Разработка

### Ветки и теги

- `main` — стабильное состояние.
- Каждый этап фиксируется аннотированным тегом:
    - `stage-1` — минимальный прототип с конфигурацией.
    - `stage-2` — сбор данных (парсинг Cargo.toml).
