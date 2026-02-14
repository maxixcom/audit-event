# Примеры использования User Log Library

## Содержание

1. [Kotlin DSL](#kotlin-dsl)
2. [Builder API](#java-builder-api)
3. [Фабричные методы](#фабричные-методы)
4. [Специальные сценарии](#специальные-сценарии)
5. [Интеграция с Kafka](#интеграция-с-kafka)

---

## Kotlin DSL

### Базовый пример

```kotlin
import io.github.maxixcom.audit.event.dsl.auditEvent
import io.github.maxixcom.audit.event.model.*

val event = auditEvent {
    source = "order-service"
    action = "sales.order.created"
    category = ActionCategory.CREATE

    actor {
        actorType = ActorType.USER
        userId = "user-123"
        sessionId = "sess-xyz"
        role("manager")
        ipAddress = "192.168.1.10"
    }

    resource {
        type = "order"
        id = "ord-1001"
        displayName = "Заказ #1001"
    }

    change {
        field = "status"
        to("pending")
    }

    change {
        field = "amount"
        to(2500.0)
    }

    outcome {
        success = true
        duration = 120
    }

    tags("creation", "sales")
}
```

### Обновление ресурса

```kotlin
val event = auditEvent {
    source = "order-service"
    action = "sales.order.updated"
    category = ActionCategory.UPDATE
    correlationId = requestId

    actor {
        actorType = ActorType.USER
        userId = currentUser.id
        sessionId = currentSession.id
        roles = currentUser.roles
        ipAddress = request.remoteAddr
        userAgent = request.getHeader("User-Agent")
    }

    resource {
        type = "order"
        id = orderId
        displayName = "Заказ #$orderId"
    }

    // Изменение простого поля
    change {
        field = "status"
        from("pending")
        to("confirmed")
    }

    // Изменение вложенного поля (dot-notation)
    change("deliveryAddress.city") {
        from("Москва")
        to("Казань")
    }

    // Изменение с явным указанием типа
    change {
        field = "discount"
        from(0.0)
        to(10.0)
        changeType = Change.ChangeType.MODIFIED
    }

    outcome {
        success = true
        duration = System.currentTimeMillis() - startTime
    }

    metadata(
        "previousStatus" to oldOrder.status,
        "environment" to environment
    )

    tags("update", "confirmed")
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
        id = commentId

        parent {
            type = "task"
            id = taskId

            parent {
                type = "project"
                id = projectId
            }
        }
    }

    change {
        field = "text"
        to("This is a comment")
    }
}

// Получить полный путь: "project:123/task:456/comment:789"
println(event.resource.getFullPath())
```

---

## Builder API

### Базовый пример

```java
import io.github.maxixcom.audit.event.builder.*;
import io.github.maxixcom.audit.event.model.*;

AuditEvent event = AuditEventBuilder.create()
    .source("order-service")
    .action("sales.order.created")
    .category(ActionCategory.CREATE)
    .actor(ActorBuilder.user("user-123")
        .sessionId("sess-xyz")
        .role("manager")
        .ipAddress("192.168.1.10")
        .build())
    .resource(ResourceBuilder.create("order", "ord-1001")
        .displayName("Заказ #1001")
        .build())
    .addChange(ChangeBuilder.added("status", "pending"))
    .addChange(ChangeBuilder.added("amount", 2500.0))
    .outcome(OutcomeBuilder.success()
        .duration(120L)
        .build())
    .tags("creation", "sales")
    .build();
```

### Обработка ошибок

```java
AuditEvent event;
try {
    // Попытка выполнить операцию
    performPayment(paymentRequest);

    event = AuditEventBuilder.create()
        .source("payment-service")
        .action("payment.transaction.process")
        .category(ActionCategory.EXECUTE)
        .actor(createActor(currentUser))
        .resource(ResourceBuilder.create("payment", paymentId)
            .displayName("Payment #" + paymentId)
            .build())
        .outcome(OutcomeBuilder.success()
            .duration(duration)
            .build())
        .build();

} catch (InsufficientFundsException e) {
    event = AuditEventBuilder.create()
        .source("payment-service")
        .action("payment.transaction.process")
        .category(ActionCategory.EXECUTE)
        .actor(createActor(currentUser))
        .resource(ResourceBuilder.create("payment", paymentId).build())
        .outcome(OutcomeBuilder.failure("INSUFFICIENT_FUNDS", e.getMessage())
            .duration(duration)
            .build())
        .tags("payment", "failure")
        .build();
}

auditLogger.log(event);
```

### Различные типы акторов

```java
// Пользователь
Actor userActor = ActorBuilder.user("user-123")
    .sessionId("sess-xyz")
    .roles("manager", "sales")
    .build();

// Система
Actor systemActor = ActorBuilder.system()
    .addAttribute("processName", "data-sync")
    .build();

// Scheduler
Actor schedulerActor = ActorBuilder.scheduler()
    .addAttribute("jobName", "monthly-report")
    .addAttribute("cronExpression", "0 0 1 * *")
    .build();

// Service Account
Actor serviceActor = ActorBuilder.serviceAccount("svc-account-123")
    .role("api-integration")
    .build();

// External Service
Actor externalActor = ActorBuilder.externalService("stripe-webhook")
    .addAttribute("webhookId", "evt_123")
    .build();
```

---

## Фабричные методы

### Login/Logout

```kotlin
// Успешный вход
val loginEvent = AuditEventFactory.createLoginEvent(
    source = "auth-service",
    userId = "user-123",
    sessionId = "sess-xyz",
    ipAddress = "192.168.1.10",
    userAgent = "Mozilla/5.0",
    success = true
)

// Неудачный вход
val failedLoginEvent = AuditEventFactory.createLoginEvent(
    source = "auth-service",
    userId = "user-123",
    sessionId = "sess-xyz",
    ipAddress = "192.168.1.10",
    userAgent = "Mozilla/5.0",
    success = false,
    errorMessage = "Invalid credentials"
)

// Выход
val logoutEvent = AuditEventFactory.createLogoutEvent(
    source = "auth-service",
    userId = "user-123",
    sessionId = "sess-xyz"
)
```

### CRUD операции

```kotlin
val actor = Actor(
    actorType = ActorType.USER,
    userId = "user-123",
    sessionId = "sess-xyz",
    roles = listOf("manager")
)

// Create
val createEvent = AuditEventFactory.createResourceCreatedEvent(
    source = "order-service",
    actor = actor,
    resourceType = "order",
    resourceId = "ord-1001",
    displayName = "Заказ #1001",
    initialValues = mapOf(
        "status" to "pending",
        "amount" to 2500.0,
        "customerId" to "cust-500"
    )
)

// Update
val updateEvent = AuditEventFactory.createResourceUpdatedEvent(
    source = "order-service",
    actor = actor,
    resourceType = "order",
    resourceId = "ord-1001",
    changes = mapOf(
        "status" to ("pending" to "confirmed"),
        "amount" to (2500.0 to 2800.0)
    )
)

// Delete
val deleteEvent = AuditEventFactory.createResourceDeletedEvent(
    source = "order-service",
    actor = actor,
    resourceType = "order",
    resourceId = "ord-1001",
    finalValues = mapOf(
        "status" to "cancelled",
        "amount" to 2800.0
    )
)

// View
val viewEvent = AuditEventFactory.createResourceViewedEvent(
    source = "document-service",
    actor = actor,
    resourceType = "document",
    resourceId = "doc-123",
    displayName = "Конфиденциальный документ"
)
```

### Permission Changes

```kotlin
val permissionEvent = AuditEventFactory.createPermissionChangedEvent(
    source = "iam-service",
    actor = adminActor,
    targetUserId = "user-456",
    resourceType = "project",
    resourceId = "proj-100",
    oldPermissions = listOf("read"),
    newPermissions = listOf("read", "write", "admin")
)
```

### Data Export

```kotlin
val exportEvent = AuditEventFactory.createDataExportEvent(
    source = "export-service",
    actor = actor,
    resourceType = "customer",
    resourceIds = listOf("cust-1", "cust-2", "cust-3"),
    format = "CSV"
)
```

---

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
        sessionId = "admin-sess-123"
        role("admin", "support")

        // Указываем, от чьего имени выполняется действие
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

    metadata("ticketId" to "12345", "supportAgent" to "admin-1")
    tags("impersonation", "support", "critical")
}
```

### Массовые операции

```kotlin
// Для каждого ресурса создаём отдельное событие с общим correlationId
val correlationId = UUID.randomUUID().toString()

orderIds.forEach { orderId ->
    val event = auditEvent {
        this.correlationId = correlationId
        source = "order-service"
        action = "sales.order.bulk_confirmed"
        category = ActionCategory.UPDATE

        actor {
            actorType = ActorType.USER
            userId = currentUser.id
        }

        resource {
            type = "order"
            id = orderId
        }

        change {
            field = "status"
            from("pending")
            to("confirmed")
        }

        metadata("bulkOperationId" to correlationId, "totalCount" to orderIds.size)
        tags("bulk-operation")
    }

    auditLogger.log(event)
}
```

### Scheduled Job

```kotlin
val event = auditEvent {
    source = "billing-service"
    action = "billing.invoice.auto_generated"
    category = ActionCategory.CREATE

    actor {
        actorType = ActorType.SCHEDULER
        attribute("jobName", "monthly-invoice-generator")
        attribute("cronExpression", "0 0 1 * *")
        attribute("executionId", jobExecutionId)
    }

    resource {
        type = "invoice"
        id = invoiceId
    }

    change { field = "amount"; to(calculatedAmount) }
    change { field = "period"; to("2026-02") }

    outcome {
        success = true
        duration = executionTime
    }

    tags("automated", "billing", "scheduled")
}
```

---

## Интеграция с Kafka

### Producer

```kotlin
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import io.github.maxixcom.audit.event.serialization.AuditEventSerializer

class AuditEventKafkaProducer(
    private val kafkaProducer: KafkaProducer<String, ByteArray>,
    private val topic: String = "audit-events"
) {
    private val serializer = AuditEventSerializer()

    fun send(event: AuditEvent) {
        // Используем тип ресурса как ключ для партиционирования
        val key = event.resource.type
        val value = serializer.serializeToBytes(event)

        val record = ProducerRecord(topic, key, value)

        // Добавляем headers для метаданных
        record.headers().apply {
            add("eventId", event.eventId.toByteArray())
            add("source", event.source.toByteArray())
            add("category", event.category.name.toByteArray())
            event.correlationId?.let {
                add("correlationId", it.toByteArray())
            }
        }

        kafkaProducer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.error("Failed to send audit event ${event.eventId}", exception)
            } else {
                logger.debug("Audit event ${event.eventId} sent to ${metadata.topic()}-${metadata.partition()}")
            }
        }
    }
}
```

### Consumer

```kotlin
import org.apache.kafka.clients.consumer.KafkaConsumer
import io.github.maxixcom.audit.event.serialization.AuditEventSerializer

class AuditEventKafkaConsumer(
    private val kafkaConsumer: KafkaConsumer<String, ByteArray>
) {
    private val serializer = AuditEventSerializer()

    fun poll(timeout: Duration = Duration.ofSeconds(1)): List<AuditEvent> {
        val records = kafkaConsumer.poll(timeout)
        return records.map { record ->
            serializer.deserializeFromBytes(record.value())
        }
    }

    fun processEvents() {
        while (true) {
            val events = poll()
            events.forEach { event ->
                when {
                    event.isSecurityCritical() -> processSecurityCriticalEvent(event)
                    event.category == ActionCategory.DELETE -> processDeleteEvent(event)
                    else -> processRegularEvent(event)
                }
            }
        }
    }

    private fun processSecurityCriticalEvent(event: AuditEvent) {
        // Отправить алерт в monitoring
        alertService.sendAlert(
            severity = AlertSeverity.HIGH,
            message = event.getSummary(),
            metadata = mapOf("eventId" to event.eventId)
        )

        // Сохранить в отдельное хранилище
        securityAuditRepository.save(event)
    }
}
```

### Spring Boot Integration

```kotlin
@Configuration
class AuditEventConfig {

    @Bean
    fun auditEventSerializer(): AuditEventSerializer {
        return AuditEventSerializer()
    }

    @Bean
    fun kafkaTemplate(
        kafkaProducer: KafkaProducer<String, ByteArray>
    ): AuditEventKafkaProducer {
        return AuditEventKafkaProducer(kafkaProducer, topic = "audit-events")
    }
}

@Service
class AuditService(
    private val kafkaProducer: AuditEventKafkaProducer
) {
    fun logEvent(event: AuditEvent) {
        kafkaProducer.send(event)
    }

    fun logUserAction(
        action: String,
        category: ActionCategory,
        resourceType: String,
        resourceId: String,
        changes: Map<String, Pair<Any?, Any?>> = emptyMap()
    ) {
        val event = auditEvent {
            source = "my-service"
            this.action = action
            this.category = category

            actor {
                actorType = ActorType.USER
                userId = SecurityContextHolder.getContext().authentication.name
            }

            resource {
                type = resourceType
                id = resourceId
            }

            changes.forEach { (field, valuePair) ->
                change {
                    this.field = field
                    from(valuePair.first)
                    to(valuePair.second)
                }
            }

            outcome { success = true }
        }

        logEvent(event)
    }
}
```

### Использование в контроллере

```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val auditService: AuditService
) {

    @PutMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateStatusRequest
    ): OrderResponse {
        val oldOrder = orderService.getOrder(id)
        val updatedOrder = orderService.updateStatus(id, request.status)

        // Логируем событие
        auditService.logUserAction(
            action = "sales.order.status_updated",
            category = ActionCategory.UPDATE,
            resourceType = "order",
            resourceId = id,
            changes = mapOf(
                "status" to (oldOrder.status to updatedOrder.status)
            )
        )

        return updatedOrder.toResponse()
    }
}
```

---

## Фильтрация и поиск событий

### По критичности безопасности

```kotlin
val securityCriticalEvents = events.filter { it.isSecurityCritical() }
```

### По категории

```kotlin
val deleteEvents = events.filter { it.category == ActionCategory.DELETE }
val loginEvents = events.filter { it.category == ActionCategory.LOGIN }
```

### По тегам

```kotlin
val gdprEvents = events.filter { "gdpr" in it.tags }
val automatedEvents = events.filter { "automated" in it.tags }
```

### По результату

```kotlin
val failedEvents = events.filter { it.outcome?.success == false }
val slowEvents = events.filter { (it.outcome?.durationMs ?: 0) > 1000 }
```

### По пользователю

```kotlin
val userEvents = events.filter { it.actor.userId == "user-123" }
val adminActions = events.filter { "admin" in it.actor.roles }
```

---

## Best Practices

### 1. Используйте correlationId для связанных операций

```kotlin
val correlationId = MDC.get("requestId") ?: UUID.randomUUID().toString()

// Все события в рамках одного HTTP-запроса
val event1 = auditEvent {
    correlationId = correlationId
    // ...
}

val event2 = auditEvent {
    correlationId = correlationId
    // ...
}
```

### 2. Добавляйте контекстные теги

```kotlin
tags(
    "security",      // Для безопасности
    "critical",      // Для критичных операций
    "gdpr",          // Для GDPR compliance
    "pci-dss",       // Для PCI DSS
    "financial",     // Для финансовых операций
    "automated"      // Для автоматических действий
)
```

### 3. Используйте dot-notation для вложенных полей

```kotlin
change("user.profile.email") {
    from("old@example.com")
    to("new@example.com")
}
```

### 4. Измеряйте производительность

```kotlin
val startTime = System.currentTimeMillis()
try {
    performOperation()
    auditEvent {
        // ...
        outcome {
            success = true
            duration = System.currentTimeMillis() - startTime
        }
    }
} catch (e: Exception) {
    auditEvent {
        // ...
        outcome {
            success = false
            errorCode = e.javaClass.simpleName
            errorMessage = e.message
            duration = System.currentTimeMillis() - startTime
        }
    }
}
```
