# Архитектура User Log Library

## Обзор

User Log Library — это библиотека для создания структурированных событий аудита (audit logs) в Kotlin/Java приложениях. Библиотека предоставляет типобезопасные модели данных и удобные API для построения событий.

## Структура проекта

```
src/main/kotlin/com/boomstream/userlog/
├── model/                          # Модели данных
│   ├── AuditEvent.kt              # Основная модель события
│   ├── Actor.kt                   # Актор (кто выполняет действие)
│   ├── ActorType.kt               # Типы акторов (USER, SYSTEM, etc.)
│   ├── Resource.kt                # Ресурс (над чем выполняется действие)
│   ├── Change.kt                  # Изменение поля ресурса
│   └── ActionCategory.kt          # Категории действий (CREATE, UPDATE, etc.)
├── dsl/                           # Kotlin DSL
│   └── AuditEventDsl.kt           # DSL для построения событий
├── builder/                       # Builder API
│   └── AuditEventBuilder.kt   # Builders для Java
├── factory/                       # Фабрики для типовых событий
│   └── AuditEventFactory.kt       # Готовые методы для распространённых сценариев
└── serialization/                 # Сериализация
    └── AuditEventSerializer.kt    # JSON сериализация через Jackson
```

## Компоненты

### 1. Модели данных (model/)

#### AuditEvent

Центральная модель события аудита. Содержит всю информацию о произошедшем действии.

**Поля:**
- `eventId`: UUID события (идемпотентность)
- `version`: Версия схемы события
- `timestamp`: Временная метка
- `source`: Источник события (название сервиса)
- `correlationId`: ID для связи цепочки событий
- `actor`: Актор, выполняющий действие
- `action`: Действие в формате `domain.entity.verb`
- `category`: Категория действия
- `resource`: Целевой ресурс
- `changes`: Список изменений
- `outcome`: Результат выполнения
- `metadata`: Дополнительные метаданные
- `tags`: Теги для фильтрации

**Методы:**
- `isSecurityCritical()`: Проверка критичности для безопасности
- `getSummary()`: Краткое описание события

#### Actor

Представляет субъект, выполняющий действие.

**Поля:**
- `actorType`: Тип актора (USER, SYSTEM, SCHEDULER, etc.)
- `userId`: ID пользователя/сервиса
- `sessionId`: ID сессии
- `roles`: Роли на момент действия
- `ipAddress`: IP адрес
- `userAgent`: User-Agent клиента
- `onBehalfOf`: Информация об импersonation
- `attributes`: Дополнительные атрибуты

#### Resource

Представляет целевую сущность, над которой выполняется действие.

**Поля:**
- `type`: Тип ресурса (например: "order", "document")
- `id`: Идентификатор ресурса
- `displayName`: Человекочитаемое имя
- `parentResource`: Родительский ресурс (для иерархий)
- `attributes`: Дополнительные атрибуты

**Методы:**
- `getFullPath()`: Возвращает полный путь в иерархии (`project:123/task:456/comment:789`)

#### Change

Представляет изменение одного поля ресурса.

**Поля:**
- `field`: Имя поля (поддерживает dot-notation: `address.city`)
- `oldValue`: Старое значение (null для создания)
- `newValue`: Новое значение (null для удаления)
- `changeType`: Тип изменения (ADDED, MODIFIED, REMOVED, UNCHANGED)

#### ActorType

Enum типов акторов:
- `USER`: Обычный пользователь
- `SYSTEM`: Системный процесс
- `SCHEDULER`: Scheduled job
- `SERVICE_ACCOUNT`: API token/service account
- `EXTERNAL_SERVICE`: Внешняя интеграция

#### ActionCategory

Enum категорий действий для агрегации:
- `CREATE`, `UPDATE`, `DELETE`
- `VIEW`, `EXPORT`, `IMPORT`
- `LOGIN`, `LOGOUT`
- `PERMISSION_CHANGE`, `CONFIGURATION_CHANGE`
- `EXECUTE`, `DOWNLOAD`, `UPLOAD`
- `SHARE`, `ARCHIVE`, `RESTORE`

### 2. Kotlin DSL (dsl/)

Предоставляет выразительный DSL для построения событий в Kotlin.

**Основные компоненты:**
- `AuditEventBuilder`: Билдер события
- `ActorBuilder`: Билдер актора
- `ResourceBuilder`: Билдер ресурса
- `ChangeBuilder`: Билдер изменения
- `OutcomeBuilder`: Билдер результата
- `auditEvent {}`: Точка входа в DSL

**Особенности:**
- `@DslMarker` аннотация для предотвращения вложенности
- Валидация обязательных полей в `build()`
- Поддержка shorthand методов (`role()`, `tags()`, `metadata()`)

### 3. Builder API (builder/)

Fluent Builder API для использования из Java.

**Компоненты:**
- `AuditEventBuilder`: Основной билдер
- `ActorBuilder`: Билдер актора с фабричными методами
- `ResourceBuilder`: Билдер ресурса
- `ChangeBuilder`: Билдер изменения
- `OutcomeBuilder`: Билдер результата

**Фабричные методы:**
- `ActorBuilder.user(userId)`: Создание пользователя
- `ActorBuilder.system()`: Создание системного актора
- `ActorBuilder.scheduler()`: Создание scheduler актора
- `ChangeBuilder.added()`, `modified()`, `removed()`: Создание изменений
- `OutcomeBuilder.success()`, `failure()`: Создание результата

### 4. Фабрики (factory/)

Готовые методы для создания событий по распространённым паттернам.

**Методы:**
- `createLoginEvent()`: Событие входа
- `createLogoutEvent()`: Событие выхода
- `createResourceCreatedEvent()`: Создание ресурса
- `createResourceUpdatedEvent()`: Обновление ресурса
- `createResourceDeletedEvent()`: Удаление ресурса
- `createResourceViewedEvent()`: Просмотр ресурса
- `createPermissionChangedEvent()`: Изменение прав доступа
- `createDataExportEvent()`: Экспорт данных

**Преимущества:**
- Стандартизация событий
- Меньше boilerplate кода
- Автоматическое добавление правильных тегов и категорий

### 5. Сериализация (serialization/)

Jackson-based сериализация в JSON.

**Методы:**
- `serialize(event)`: Сериализация в JSON строку
- `serializeToBytes(event)`: Сериализация в байты (для Kafka)
- `deserialize(json)`: Десериализация из JSON
- `deserializeFromBytes(bytes)`: Десериализация из байтов
- `serializePretty(event)`: Pretty-printed JSON для логов

**Настройки ObjectMapper:**
- Kotlin module для data classes
- Java Time module для `Instant`
- Даты не сериализуются как timestamps
- `JsonInclude.NON_NULL` для пропуска null полей
- `@JsonIgnore` для методов (`getFullPath()`, `isSecurityCritical()`, `getSummary()`)

## Паттерны использования

### 1. Формат действия (action)

Используйте формат `domain.entity.verb`:

```
✅ sales.order.created
✅ iam.user.role_changed
✅ billing.invoice.updated
✅ project.comment.deleted

❌ create_order
❌ updateUser
❌ invoice-payment
```

### 2. Версионирование схемы

Поле `version` используется для эволюции формата:

```kotlin
// v1.0 - начальная версия
data class AuditEvent(val version: String = "1.0", ...)

// v1.1 - добавили новые поля
data class AuditEvent(val version: String = "1.1", ...)

// При десериализации проверяем версию:
when (event.version) {
    "1.0" -> handleV1(event)
    "1.1" -> handleV1_1(event)
}
```

### 3. Correlation ID для цепочек событий

Используйте один `correlationId` для связанных событий:

```kotlin
val correlationId = UUID.randomUUID().toString()

// Событие 1: Создание заказа
auditEvent {
    correlationId = correlationId
    action = "sales.order.created"
    // ...
}

// Событие 2: Резервирование товара
auditEvent {
    correlationId = correlationId
    action = "inventory.item.reserved"
    // ...
}

// Событие 3: Создание платежа
auditEvent {
    correlationId = correlationId
    action = "payment.transaction.created"
    // ...
}
```

### 4. Иерархия ресурсов

Для вложенных ресурсов используйте `parentResource`:

```kotlin
resource {
    type = "comment"
    id = "comment-789"
    parent {
        type = "task"
        id = "task-456"
        parent {
            type = "project"
            id = "project-123"
        }
    }
}

// Полный путь: "project:123/task:456/comment:789"
```

### 5. Dot-notation для вложенных полей

При изменении вложенных полей используйте точечную нотацию:

```kotlin
change("user.profile.email") {
    from("old@example.com")
    to("new@example.com")
}

change("deliveryAddress.city") {
    from("Москва")
    to("Казань")
}
```

### 6. Теги для классификации

Используйте теги для фильтрации и группировки:

```kotlin
tags(
    "security",      // События безопасности
    "critical",      // Критичные операции
    "gdpr",          // GDPR релевантные
    "pci-dss",       // PCI DSS compliance
    "financial",     // Финансовые операции
    "automated",     // Автоматические действия
    "bulk-operation" // Массовые операции
)
```

## Интеграция с Kafka

### Ключ партиционирования

Рекомендуется использовать `resource.type` как ключ для равномерного распределения:

```kotlin
val key = event.resource.type
val value = serializer.serializeToBytes(event)
val record = ProducerRecord(topic, key, value)
```

### Headers

Добавляйте метаданные в headers для быстрой фильтрации:

```kotlin
record.headers().apply {
    add("eventId", event.eventId.toByteArray())
    add("source", event.source.toByteArray())
    add("category", event.category.name.toByteArray())
    add("securityCritical", event.isSecurityCritical().toString().toByteArray())
}
```

### Topic naming

Рекомендуемые топики:
- `audit-events` - основной топик для всех событий
- `audit-events-security` - только критичные события безопасности
- `audit-events-{service}` - события конкретного сервиса

### Consumer groups

Разные consumer groups для разных целей:
- `audit-storage` - сохранение в долговременное хранилище
- `audit-analytics` - аналитика в реальном времени
- `audit-alerting` - мониторинг и алерты

## Performance

### Async logging

Отправка событий в Kafka должна быть асинхронной:

```kotlin
kafkaProducer.send(record) { metadata, exception ->
    if (exception != null) {
        logger.error("Failed to send audit event", exception)
    }
}
```

### Batching

Для массовых операций используйте batching:

```kotlin
val events = orderIds.map { createEvent(it) }
events.forEach { kafkaProducer.send(it) }
kafkaProducer.flush() // в конце batch
```

### Размер событий

Средний размер события: ~500-1000 байт в JSON.
Для 1000 событий/сек: ~1 MB/sec пропускная способность.

## Security

### Sensitive data

**НЕ логируйте:**
- Пароли
- Токены/API keys
- Номера кредитных карт
- Персональные данные (если не требуется по GDPR)

### GDPR compliance

Для GDPR-релевантных событий:
```kotlin
tags("gdpr", "personal-data")
metadata("dataSubjectId" to userId)
```

### Retention

Рекомендуемые сроки хранения:
- Security events: 3-5 лет
- Financial events: 7 лет
- Regular events: 1-2 года

## Тестирование

### Unit tests

Все основные компоненты покрыты тестами:
- `AuditEventDslTest`: Тесты Kotlin DSL
- `AuditEventBuilderTest`: Тесты Builder API
- `AuditEventFactoryTest`: Тесты фабричных методов
- `AuditEventSerializerTest`: Тесты сериализации

Запуск тестов:
```bash
./gradlew test
```

### Примеры

См. `UsageExamples.kt` для запускаемых примеров.

## Зависимости

```kotlin
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
```

## Расширение

### Добавление новых ActionCategory

```kotlin
enum class ActionCategory {
    // Существующие...
    CREATE, UPDATE, DELETE,

    // Новые категории
    CUSTOM_ACTION,
    ANOTHER_ACTION
}
```

### Добавление кастомных фабрик

```kotlin
object CustomAuditEventFactory {
    fun createCustomEvent(...): AuditEvent = auditEvent {
        // ...
    }
}
```

### Schema Registry интеграция

Для Avro схем:
```kotlin
// Зарегистрировать схему в Confluent Schema Registry
val schema = AvroSchemaUtils.generate(AuditEvent::class.java)
schemaRegistry.register("audit-event-value", schema)
```

## Мониторинг

### Метрики

Рекомендуется собирать метрики:
- Количество событий по категориям
- Количество failed events
- Средний размер события
- Latency отправки в Kafka
- Количество security critical events

### Алерты

Настроить алерты на:
- Spike в количестве failed events
- Spike в DELETE операциях
- Необычная активность (много LOGIN_FAILED)
- Импersonation события

## Best Practices

1. ✅ Используйте `correlationId` для связанных событий
2. ✅ Добавляйте осмысленные теги
3. ✅ Измеряйте продолжительность операций
4. ✅ Используйте фабричные методы для типовых сценариев
5. ✅ Валидируйте обязательные поля
6. ❌ Не логируйте sensitive data
7. ❌ Не создавайте события синхронно в критичных путях
8. ❌ Не используйте произвольный формат для `action`
