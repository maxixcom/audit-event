package io.github.maxixcom.audit.event.dsl

import io.github.maxixcom.audit.event.model.*
import java.time.Instant

/**
 * DSL для построения события аудита
 *
 * Пример использования:
 * ```kotlin
 * val event = auditEvent {
 *     source = "order-service"
 *     action = "sales.order.updated"
 *     category = ActionCategory.UPDATE
 *
 *     actor {
 *         actorType = ActorType.USER
 *         userId = "user-123"
 *         roles = listOf("manager")
 *         ipAddress = "192.168.1.10"
 *     }
 *
 *     resource {
 *         type = "order"
 *         id = "ord-1001"
 *         displayName = "Заказ #1001"
 *         parent {
 *             type = "customer"
 *             id = "cust-500"
 *         }
 *     }
 *
 *     change {
 *         field = "status"
 *         from = "pending"
 *         to = "confirmed"
 *     }
 *
 *     change("deliveryAddress.city") {
 *         from = "Москва"
 *         to = "Казань"
 *     }
 *
 *     outcome {
 *         success = true
 *         duration = 150
 *     }
 *
 *     tags("important", "customer-facing")
 * }
 * ```
 */
@DslMarker
annotation class AuditEventDslMarker

@AuditEventDslMarker
class AuditEventBuilder {
    var eventId: String? = null
    var version: String = "1.0"
    var timestamp: Instant = Instant.now()
    var source: String? = null
    var correlationId: String? = null
    var action: String? = null
    var category: ActionCategory? = null

    private var actor: Actor? = null
    private var resource: Resource? = null
    private val changes = mutableListOf<Change>()
    private var outcome: AuditEvent.Outcome? = null
    private val metadata = mutableMapOf<String, Any>()
    private val tags = mutableSetOf<String>()

    fun actor(block: ActorBuilder.() -> Unit) {
        actor = ActorBuilder().apply(block).build()
    }

    fun resource(block: ResourceBuilder.() -> Unit) {
        resource = ResourceBuilder().apply(block).build()
    }

    fun change(block: ChangeBuilder.() -> Unit) {
        changes.add(ChangeBuilder().apply(block).build())
    }

    fun change(field: String, block: ChangeBuilder.() -> Unit) {
        changes.add(ChangeBuilder().apply {
            this.field = field
            block()
        }.build())
    }

    fun outcome(block: OutcomeBuilder.() -> Unit) {
        outcome = OutcomeBuilder().apply(block).build()
    }

    fun metadata(key: String, value: Any) {
        metadata[key] = value
    }

    fun metadata(vararg pairs: Pair<String, Any>) {
        metadata.putAll(pairs)
    }

    fun tags(vararg tags: String) {
        this.tags.addAll(tags)
    }

    fun build(): AuditEvent {
        require(source != null) { "source is required" }
        require(action != null) { "action is required" }
        require(category != null) { "category is required" }
        require(actor != null) { "actor is required" }
        require(resource != null) { "resource is required" }

        return AuditEvent(
            eventId = eventId ?: java.util.UUID.randomUUID().toString(),
            version = version,
            timestamp = timestamp,
            source = source!!,
            correlationId = correlationId,
            actor = actor!!,
            action = action!!,
            category = category!!,
            resource = resource!!,
            changes = changes.toList(),
            outcome = outcome,
            metadata = metadata.toMap(),
            tags = tags.toSet()
        )
    }
}

@AuditEventDslMarker
class ActorBuilder {
    var actorType: ActorType = ActorType.USER
    var userId: String? = null
    var sessionId: String? = null
    var roles: List<String> = emptyList()
    var ipAddress: String? = null
    var userAgent: String? = null
    private var onBehalfOf: Actor.OnBehalfOf? = null
    private val attributes = mutableMapOf<String, Any>()

    fun role(vararg roles: String) {
        this.roles = roles.toList()
    }

    fun onBehalfOf(userId: String, userName: String? = null, reason: String? = null) {
        onBehalfOf = Actor.OnBehalfOf(userId, userName, reason)
    }

    fun attribute(key: String, value: Any) {
        attributes[key] = value
    }

    fun build(): Actor {
        return Actor(
            actorType = actorType,
            userId = userId,
            sessionId = sessionId,
            roles = roles,
            ipAddress = ipAddress,
            userAgent = userAgent,
            onBehalfOf = onBehalfOf,
            attributes = attributes.toMap()
        )
    }
}

@AuditEventDslMarker
class ResourceBuilder {
    var type: String? = null
    var id: String? = null
    var displayName: String? = null
    private var parentResource: Resource? = null
    private val attributes = mutableMapOf<String, Any>()

    fun parent(block: ResourceBuilder.() -> Unit) {
        parentResource = ResourceBuilder().apply(block).build()
    }

    fun attribute(key: String, value: Any) {
        attributes[key] = value
    }

    fun build(): Resource {
        require(type != null) { "resource.type is required" }
        require(id != null) { "resource.id is required" }

        return Resource(
            type = type!!,
            id = id!!,
            displayName = displayName,
            parentResource = parentResource,
            attributes = attributes.toMap()
        )
    }
}

@AuditEventDslMarker
class ChangeBuilder {
    var field: String? = null
    var oldValue: Any? = null
    var newValue: Any? = null
    var changeType: Change.ChangeType? = null

    fun from(value: Any?) {
        oldValue = value
    }

    fun to(value: Any?) {
        newValue = value
    }

    fun build(): Change {
        require(field != null) { "change.field is required" }

        return Change(
            field = field!!,
            oldValue = oldValue,
            newValue = newValue,
            changeType = changeType
        )
    }
}

@AuditEventDslMarker
class OutcomeBuilder {
    var success: Boolean = true
    var errorCode: String? = null
    var errorMessage: String? = null
    var durationMs: Long? = null

    var duration: Long?
        get() = durationMs
        set(value) { durationMs = value }

    fun build(): AuditEvent.Outcome {
        return AuditEvent.Outcome(
            success = success,
            errorCode = errorCode,
            errorMessage = errorMessage,
            durationMs = durationMs
        )
    }
}

/**
 * Точка входа для создания события аудита через DSL
 */
fun auditEvent(block: AuditEventBuilder.() -> Unit): AuditEvent {
    return AuditEventBuilder().apply(block).build()
}
