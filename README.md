# Audit Event Library

Библиотека для создания структурированных событий аудита (audit events) в Kotlin/Java приложениях.

## Документация

- [Примеры использования](doc/EXAMPLES.md)
- [Архитектура](doc/ARCHITECTURE.md)
- [Сводка проекта](doc/SUMMARY.md)
- [Чеклист реализации](doc/CHECKLIST.md)
- [Пример JSON события](doc/example-event.json)

## Возможности

- ✅ **Полная модель события аудита** — actor, resource, changes, outcome, metadata
- ✅ **Kotlin DSL** — удобный и выразительный синтаксис для Kotlin
- ✅ **Builder API** — полная поддержка Java с fluent builders
- ✅ **Фабричные методы** — готовые шаблоны для типовых сценариев
- ✅ **Вложенные ресурсы** — поддержка иерархии (project → task → comment)
- ✅ **Сериализация** — JSON сериализация через Jackson
- ✅ **Версионирование схемы** — для эволюции формата событий
- ✅ **Отслеживание изменений** — детальная информация о старых и новых значениях

## Структура события

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "version": "1.0",
  "timestamp": "2026-02-13T14:30:00.123Z",
  "source": "order-service",
  "correlationId": "req-abc-123",

  "actor": {
    "actorType": "USER",
    "userId": "u-42",
    "sessionId": "sess-xyz",
    "roles": ["manager"],
    "ipAddress": "192.168.1.10",
    "userAgent": "Mozilla/5.0 ..."
  },

  "action": "sales.order.updated",
  "category": "UPDATE",

  "resource": {
    "type": "order",
    "id": "ord-1001",
    "displayName": "Заказ #1001",
    "parentResource": {
      "type": "customer",
      "id": "cust-500"
    }
  },

  "changes": [
    {
      "field": "status",
      "oldValue": "pending",
      "newValue": "confirmed"
    }
  ],

  "outcome": {
    "success": true,
    "durationMs": 150
  },

  "metadata": {
    "environment": "production"
  },

  "tags": ["important", "customer-facing"]
}
```

## Использование

### Kotlin DSL

```kotlin
import io.github.maxixcom.audit.event.dsl.auditEvent
import io.github.maxixcom.audit.event.model.*

val event = auditEvent {
    source = "order-service"
    action = "sales.order.updated"
    category = ActionCategory.UPDATE

    actor {
        actorType = ActorType.USER
        userId = "user-42"
        sessionId = "sess-xyz"
        role("manager", "sales")
        ipAddress = "192.168.1.10"
    }

    resource {
        type = "order"
        id = "ord-1001"
        displayName = "Заказ #1001"

        parent {
            type = "customer"
            id = "cust-500"
        }
    }

    change {
        field = "status"
        from = "pending"
        to = "confirmed"
    }

    change("deliveryAddress.city") {
        from = "Москва"
        to = "Казань"
    }

    outcome {
        success = true
        duration = 150
    }

    metadata("environment" to "production")
    tags("important", "customer-facing")
}
```

### Builder API

```java
import io.github.maxixcom.audit.event.builder.*;
import io.github.maxixcom.audit.event.model.*;

AuditEvent event = AuditEventBuilder.create()
    .source("order-service")
    .action("sales.order.updated")
    .category(ActionCategory.UPDATE)
    .actor(ActorBuilder.user("user-42")
        .sessionId("sess-xyz")
        .roles("manager", "sales")
        .ipAddress("192.168.1.10")
        .build())
    .resource(ResourceBuilder.create("order", "ord-1001")
        .displayName("Заказ #1001")
        .parent(ResourceBuilder.create("customer", "cust-500").build())
        .build())
    .addChange(ChangeBuilder.modified("status")
        .from("pending")
        .to("confirmed")
        .build())
    .outcome(OutcomeBuilder.success()
        .duration(150L)
        .build())
    .tags("important")
    .build();
```

### Фабричные методы

```kotlin
import io.github.maxixcom.audit.event.factory.AuditEventFactory

// Вход пользователя
val loginEvent = AuditEventFactory.createLoginEvent(
    source = "auth-service",
    userId = "user-123",
    sessionId = "sess-xyz",
    ipAddress = "192.168.1.10",
    userAgent = "Mozilla/5.0",
    success = true
)

// Создание ресурса
val createEvent = AuditEventFactory.createResourceCreatedEvent(
    source = "order-service",
    actor = currentActor,
    resourceType = "order",
    resourceId = "ord-2001",
    displayName = "Новый заказ",
    initialValues = mapOf(
        "status" to "pending",
        "amount" to 2500.0
    )
)

// Обновление ресурса
val updateEvent = AuditEventFactory.createResourceUpdatedEvent(
    source = "order-service",
    actor = currentActor,
    resourceType = "order",
    resourceId = "ord-2001",
    changes = mapOf(
        "status" to ("pending" to "confirmed"),
        "amount" to (2500.0 to 2800.0)
    )
)

// Удаление ресурса
val deleteEvent = AuditEventFactory.createResourceDeletedEvent(
    source = "order-service",
    actor = currentActor,
    resourceType = "order",
    resourceId = "ord-2001"
)

// Изменение прав доступа
val permissionEvent = AuditEventFactory.createPermissionChangedEvent(
    source = "iam-service",
    actor = currentActor,
    targetUserId = "user-456",
    resourceType = "project",
    resourceId = "proj-100",
    oldPermissions = listOf("read"),
    newPermissions = listOf("read", "write", "admin")
)

// Экспорт данных
val exportEvent = AuditEventFactory.createDataExportEvent(
    source = "export-service",
    actor = currentActor,
    resourceType = "customer",
    resourceIds = listOf("cust-1", "cust-2", "cust-3"),
    format = "CSV"
)
```

## Типы акторов

```kotlin
enum class ActorType {
    USER,              // Обычный пользователь
    SYSTEM,            // Системный процесс
    SCHEDULER,         // Scheduled job
    SERVICE_ACCOUNT,   // API token/service account
    EXTERNAL_SERVICE   // Внешняя интеграция
}
```

## Категории действий

```kotlin
enum class ActionCategory {
    CREATE,
    UPDATE,
    DELETE,
    VIEW,
    EXPORT,
    IMPORT,
    LOGIN,
    LOGOUT,
    PERMISSION_CHANGE,
    CONFIGURATION_CHANGE,
    EXECUTE,
    DOWNLOAD,
    UPLOAD,
    SHARE,
    ARCHIVE,
    RESTORE
}
```

## Специальные сценарии

### Impersonation (действие от имени другого пользователя)

```kotlin
val event = auditEvent {
    source = "admin-service"
    action = "support.user.data_access"
    category = ActionCategory.VIEW

    actor {
        actorType = ActorType.USER
        userId = "admin-1"

        onBehalfOf(
            userId = "user-42",
            userName = "John Doe",
            reason = "Customer support ticket #12345"
        )
    }

    resource {
        type = "user_profile"
        id = "user-42"
    }

    tags("impersonation", "support", "critical")
}
```

### Системное событие (scheduler)

```kotlin
val event = auditEvent {
    source = "billing-service"
    action = "billing.invoice.auto_generated"
    category = ActionCategory.CREATE

    actor {
        actorType = ActorType.SCHEDULER
        attribute("jobName", "monthly-invoice-generator")
        attribute("cronExpression", "0 0 1 * *")
    }

    resource {
        type = "invoice"
        id = "inv-3001"
    }
}
```

### Вложенные ресурсы

```kotlin
val event = auditEvent {
    source = "project-service"
    action = "project.comment.created"
    category = ActionCategory.CREATE

    actor {
        actorType = ActorType.USER
        userId = "user-123"
    }

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
}

// Получить полный путь: "project:123/task:456/comment:789"
val path = event.resource.getFullPath()
```

### Событие с ошибкой

```kotlin
val event = auditEvent {
    source = "payment-service"
    action = "payment.transaction.process"
    category = ActionCategory.EXECUTE

    actor {
        actorType = ActorType.USER
        userId = "user-123"
    }

    resource {
        type = "payment"
        id = "pay-5001"
    }

    outcome {
        success = false
        errorCode = "INSUFFICIENT_FUNDS"
        errorMessage = "Payment declined: insufficient funds"
        duration = 1200
    }

    tags("payment", "failure")
}
```

## Сериализация

```kotlin
import io.github.maxixcom.audit.event.serialization.AuditEventSerializer

val serializer = AuditEventSerializer()

// В JSON строку
val json = serializer.serialize(event)

// В байты (для Kafka)
val bytes = serializer.serializeToBytes(event)

// Pretty-print для логов
val prettyJson = serializer.serializePretty(event)

// Десериализация
val event = serializer.deserialize(json)
val eventFromBytes = serializer.deserializeFromBytes(bytes)
```

## Утилиты

```kotlin
// Проверка критичности события для безопасности
if (event.isSecurityCritical()) {
    // Отправить алерт
}

// Краткое описание события
val summary = event.getSummary()
// "user-42 успешно выполнил 'sales.order.updated' над order:ord-1001"

// Полный путь вложенного ресурса
val path = event.resource.getFullPath()
// "project:123/task:456/comment:789"
```

## Best Practices

### 1. Формат action

Используйте формат `domain.entity.verb`:
- `sales.order.created`
- `iam.user.role_changed`
- `billing.invoice.updated`

### 2. Логирование изменений

Для создания используйте `oldValue: null`:
```kotlin
change {
    field = "status"
    from = null  // или просто не указывать
    to = "pending"
}
```

Для удаления используйте `newValue: null`:
```kotlin
change {
    field = "status"
    from = "cancelled"
    to = null
}
```

### 3. Dot-notation для вложенных полей

```kotlin
change {
    field = "address.city"
    from = "Москва"
    to = "Казань"
}
```

### 4. correlationId для цепочек событий

Используйте один `correlationId` для всех событий в рамках одной бизнес-операции:
```kotlin
val correlationId = UUID.randomUUID().toString()

// Событие 1
auditEvent {
    correlationId = correlationId
    // ...
}

// Событие 2 (связанное)
auditEvent {
    correlationId = correlationId
    // ...
}
```

### 5. Теги для классификации

```kotlin
tags(
    "security",      // Для событий безопасности
    "critical",      // Для критичных операций
    "gdpr",          // Для GDPR-релевантных событий
    "pci-dss",       // Для PCI DSS compliance
    "automated"      // Для автоматических действий
)
```

## Интеграция с Kafka

```kotlin
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

val serializer = AuditEventSerializer()

val event = auditEvent { /* ... */ }

val record = ProducerRecord(
    "audit-events",                    // topic
    event.resource.type,               // key (для партиционирования)
    serializer.serializeToBytes(event) // value
)

kafkaProducer.send(record)
```

## Тестирование

Запуск тестов:
```bash
./gradlew test
```

Запуск примеров:
```bash
./gradlew test --tests "*.UsageExamples"
```

## Установка

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.maxixcom.audit:audit-event:0.0.1-SNAPSHOT")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.maxixcom.audit:audit-event:0.0.1-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.maxixcom.audit</groupId>
    <artifactId>audit-event</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Публикация

Библиотека публикуется в Maven Central и GitHub Packages.

Последняя версия: ![Maven Central](https://img.shields.io/maven-central/v/io.github.maxixcom.audit/audit-event)

Подробнее о процессе публикации см. [doc/PUBLISHING.md](doc/PUBLISHING.md).

## Требования

- **Java 17+** — минимальная версия для использования библиотеки
- Kotlin 1.9+ (для Kotlin проектов)

## Зависимости

Библиотека имеет минимальные транзитивные зависимости:

```kotlin
dependencies {
    // Kotlin runtime (для Kotlin проектов уже включён)
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // JSON сериализация
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
}
```

**Размер библиотеки:** ~61KB

**Совместимость:**
- Java 21+
- Kotlin 2.2.21+
- Может использоваться в Spring Boot, Micronaut, Quarkus, plain Java/Kotlin и других проектах

## License

MIT
