# Публикация библиотеки

Эта документация описывает процесс публикации библиотеки audit-event в Maven Central и GitHub Packages.

## Настройка GitHub Secrets

Для автоматической публикации необходимо настроить следующие секреты в GitHub:

1. Перейдите в Settings → Secrets and variables → Actions
2. Добавьте следующие secrets:

### Maven Central (Sonatype Portal)

- `MAVEN_CENTRAL_USERNAME` - ваш username от central.sonatype.com
- `MAVEN_CENTRAL_TOKEN` - токен от Portal (получить на https://central.sonatype.com)

### GPG Подпись

Экспортируйте ваши GPG ключи в ASCII armor формате:

```bash
# Экспорт публичного ключа
gpg --armor --export your.email@example.com > public.key

# Экспорт приватного ключа
gpg --armor --export-secret-key your.email@example.com > private.key
```

Затем создайте секреты:
- `GPG_PUBLIC_KEY` - содержимое файла public.key (весь текст включая `-----BEGIN PGP PUBLIC KEY BLOCK-----`)
- `GPG_SECRET_KEY` - содержимое файла private.key (весь текст включая `-----BEGIN PGP PRIVATE KEY BLOCK-----`)
- `GPG_PASSPHRASE` - пароль от вашего GPG ключа

### GitHub Token

- `GITHUB_TOKEN` - создается автоматически GitHub Actions (не требует настройки)

## Публикация новой версии

### Автоматическая публикация (рекомендуется)

1. Обновите версию в `build.gradle.kts`:
```kotlin
version = "0.0.1"  // уберите -SNAPSHOT для релиза
```

2. Создайте и запушьте тег:
```bash
git tag -a v0.0.1 -m "Release version 0.0.1"
git push origin v0.0.1
```

3. GitHub Actions автоматически запустит публикацию:
   - Соберет проект и запустит тесты
   - Опубликует в staging репозиторий
   - Подпишет артефакты GPG ключом
   - Опубликует в Maven Central
   - Опубликует в GitHub Packages

### Ручная публикация

1. Перейдите в Actions → Manual Release
2. Нажмите "Run workflow"
3. Введите версию (например, `0.0.1`)
4. Запустите workflow

## Локальная публикация

Для тестирования публикации локально:

```bash
# Сборка и публикация в staging
./gradlew clean build publish

# Проверка конфигурации jReleaser
./gradlew jreleaserConfig

# Публикация (требует настройки ~/.jreleaser/config.properties)
./gradlew jreleaserFullRelease
```

Для локальной публикации создайте файл `~/.jreleaser/config.properties`:

```properties
jreleaser.mavencentral.username=your-username
jreleaser.mavencentral.token=your-token
jreleaser.gpg.public.key=-----BEGIN PGP PUBLIC KEY BLOCK-----\n...
jreleaser.gpg.secret.key=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...
jreleaser.gpg.passphrase=your-passphrase
```

## Проверка публикации

### Maven Central
- Поиск: https://central.sonatype.com/search?q=io.github.maxixcom.audit
- Обычно доступно через 10-30 минут после публикации
- Синхронизация с Maven Central Search может занять до 2 часов

### GitHub Packages
- Packages: https://github.com/maxixcom/audit-event/packages
- Доступно сразу после публикации

## Использование опубликованной библиотеки

### Из Maven Central

**Gradle (Kotlin DSL)**:
```kotlin
dependencies {
    implementation("io.github.maxixcom.audit:audit-event:0.0.1")
}
```

**Gradle (Groovy)**:
```groovy
dependencies {
    implementation 'io.github.maxixcom.audit:audit-event:0.0.1'
}
```

**Maven**:
```xml
<dependency>
    <groupId>io.github.maxixcom.audit</groupId>
    <artifactId>audit-event</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Из GitHub Packages

Добавьте репозиторий:

**Gradle (Kotlin DSL)**:
```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/maxixcom/audit-event")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
        }
    }
}
```

## Troubleshooting

### Ошибка подписи GPG

**Симптомы**: `Failed to sign artifacts` или `Invalid GPG key`

**Решение**:
- Проверьте, что GPG ключи экспортированы в ASCII armor формате (с флагом `--armor`)
- Убедитесь, что в секретах сохранён весь текст ключа, включая заголовки `-----BEGIN/END-----`
- Проверьте правильность passphrase
- Убедитесь, что ключ не истёк

### Ошибка авторизации Maven Central

**Симптомы**: `401 Unauthorized` или `403 Forbidden`

**Решение**:
- Проверьте токен на https://central.sonatype.com
- Убедитесь, что namespace `io.github.maxixcom.audit` зарегистрирован и подтверждён
- Проверьте срок действия токена

### Публикация не запускается

**Симптомы**: Workflow не запускается после создания тега

**Решение**:
- Проверьте, что тег начинается с `v` (например, `v0.0.1`, а не `0.0.1`)
- Убедитесь, что все секреты настроены в Settings → Secrets
- Проверьте permissions для GITHUB_TOKEN в Settings → Actions → General

### Тесты не проходят в CI

**Симптомы**: Build fails на этапе тестирования

**Решение**:
- Запустите тесты локально: `./gradlew clean test`
- Проверьте логи в GitHub Actions
- Убедитесь, что Java 21 используется

### Артефакты не найдены в staging

**Симптомы**: `No artifacts found in staging repository`

**Решение**:
- Проверьте, что `./gradlew publish` завершился успешно
- Проверьте наличие директории `build/staging-deploy`
- Убедитесь, что в `build.gradle.kts` правильно настроен publishing repository

## Важные заметки

⚠️ **SNAPSHOT версии** не публикуются в Maven Central (только в GitHub Packages)

⚠️ **Версия в build.gradle.kts** должна быть БЕЗ `-SNAPSHOT` для релиза

⚠️ **Namespace** `io.github.maxixcom.audit` должен быть зарегистрирован в Sonatype Portal

⚠️ **GPG ключи** должны быть в ASCII armor формате (с `--armor` флагом)

⚠️ **Первая публикация** может занять до 2 часов (синхронизация с Maven Central)

⚠️ **Теги нельзя удалять** после публикации - это может нарушить версионирование

## Дополнительные ресурсы

- [jReleaser Documentation](https://jreleaser.org/guide/latest/)
- [Maven Central Portal Guide](https://central.sonatype.org/publish/publish-portal-gradle/)
- [GitHub Packages Guide](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)
- [GPG Guide](https://central.sonatype.org/publish/requirements/gpg/)
