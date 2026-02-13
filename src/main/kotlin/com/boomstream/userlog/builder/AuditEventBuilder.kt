package com.boomstream.userlog.builder

import com.boomstream.userlog.model.*
import java.time.Instant
import java.util.*

/**
 * Builder для построения события аудита
 *
 * Пример использования из Java:
 * ```java
 * AuditEvent event = AuditEventBuilder.create()
 *     .source("order-service")
 *     .action("sales.order.updated")
 *     .category(ActionCategory.UPDATE)
 *     .actor(ActorBuilder.user("user-123")
 *         .sessionId("sess-xyz")
 *         .roles("manager", "sales")
 *         .ipAddress("192.168.1.10")
 *         .build())
 *     .resource(ResourceBuilder.create("order", "ord-1001")
 *         .displayName("Заказ #1001")
 *         .parent(ResourceBuilder.create("customer", "cust-500").build())
 *         .build())
 *     .addChange(ChangeBuilder.modify("status")
 *         .from("pending")
 *         .to("confirmed")
 *         .build())
 *     .outcome(OutcomeBuilder.success()
 *         .duration(150L)
 *         .build())
 *     .build();
 * ```
 */
class AuditEventBuilder private constructor() {
    private var eventId: String? = null
    private var version: String = "1.0"
    private var timestamp: Instant = Instant.now()
    private var source: String? = null
    private var correlationId: String? = null
    private var action: String? = null
    private var category: ActionCategory? = null
    private var actor: Actor? = null
    private var resource: Resource? = null
    private val changes = mutableListOf<Change>()
    private var outcome: AuditEvent.Outcome? = null
    private val metadata = mutableMapOf<String, Any>()
    private val tags = mutableSetOf<String>()

    fun eventId(eventId: String): AuditEventBuilder = apply {
        this.eventId = eventId
    }

    fun version(version: String): AuditEventBuilder = apply {
        this.version = version
    }

    fun timestamp(timestamp: Instant): AuditEventBuilder = apply {
        this.timestamp = timestamp
    }

    fun source(source: String): AuditEventBuilder = apply {
        this.source = source
    }

    fun correlationId(correlationId: String): AuditEventBuilder = apply {
        this.correlationId = correlationId
    }

    fun action(action: String): AuditEventBuilder = apply {
        this.action = action
    }

    fun category(category: ActionCategory): AuditEventBuilder = apply {
        this.category = category
    }

    fun actor(actor: Actor): AuditEventBuilder = apply {
        this.actor = actor
    }

    fun resource(resource: Resource): AuditEventBuilder = apply {
        this.resource = resource
    }

    fun addChange(change: Change): AuditEventBuilder = apply {
        changes.add(change)
    }

    fun changes(vararg changes: Change): AuditEventBuilder = apply {
        this.changes.addAll(changes)
    }

    fun outcome(outcome: AuditEvent.Outcome): AuditEventBuilder = apply {
        this.outcome = outcome
    }

    fun addMetadata(key: String, value: Any): AuditEventBuilder = apply {
        metadata[key] = value
    }

    fun addTag(tag: String): AuditEventBuilder = apply {
        tags.add(tag)
    }

    fun tags(vararg tags: String): AuditEventBuilder = apply {
        this.tags.addAll(tags)
    }

    fun build(): AuditEvent {
        require(source != null) { "source is required" }
        require(action != null) { "action is required" }
        require(category != null) { "category is required" }
        require(actor != null) { "actor is required" }
        require(resource != null) { "resource is required" }

        return AuditEvent(
            eventId = eventId ?: UUID.randomUUID().toString(),
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

    companion object {
        @JvmStatic
        fun create(): AuditEventBuilder = AuditEventBuilder()
    }
}

/**
 * Builder для Actor
 */
class ActorBuilder private constructor(private val actorType: ActorType) {
    private var userId: String? = null
    private var sessionId: String? = null
    private val roles = mutableListOf<String>()
    private var ipAddress: String? = null
    private var userAgent: String? = null
    private var onBehalfOf: Actor.OnBehalfOf? = null
    private val attributes = mutableMapOf<String, Any>()

    fun userId(userId: String): ActorBuilder = apply {
        this.userId = userId
    }

    fun sessionId(sessionId: String): ActorBuilder = apply {
        this.sessionId = sessionId
    }

    fun role(role: String): ActorBuilder = apply {
        roles.add(role)
    }

    fun roles(vararg roles: String): ActorBuilder = apply {
        this.roles.addAll(roles)
    }

    fun ipAddress(ipAddress: String): ActorBuilder = apply {
        this.ipAddress = ipAddress
    }

    fun userAgent(userAgent: String): ActorBuilder = apply {
        this.userAgent = userAgent
    }

    fun onBehalfOf(userId: String, userName: String?, reason: String?): ActorBuilder = apply {
        this.onBehalfOf = Actor.OnBehalfOf(userId, userName, reason)
    }

    fun onBehalfOf(userId: String): ActorBuilder = apply {
        this.onBehalfOf = Actor.OnBehalfOf(userId)
    }

    fun addAttribute(key: String, value: Any): ActorBuilder = apply {
        attributes[key] = value
    }

    fun build(): Actor {
        return Actor(
            actorType = actorType,
            userId = userId,
            sessionId = sessionId,
            roles = roles.toList(),
            ipAddress = ipAddress,
            userAgent = userAgent,
            onBehalfOf = onBehalfOf,
            attributes = attributes.toMap()
        )
    }

    companion object {
        @JvmStatic
        fun user(userId: String): ActorBuilder = ActorBuilder(ActorType.USER).apply {
            userId(userId)
        }

        @JvmStatic
        fun system(): ActorBuilder = ActorBuilder(ActorType.SYSTEM)

        @JvmStatic
        fun scheduler(): ActorBuilder = ActorBuilder(ActorType.SCHEDULER)

        @JvmStatic
        fun serviceAccount(accountId: String): ActorBuilder = ActorBuilder(ActorType.SERVICE_ACCOUNT).apply {
            userId(accountId)
        }

        @JvmStatic
        fun externalService(serviceId: String): ActorBuilder = ActorBuilder(ActorType.EXTERNAL_SERVICE).apply {
            userId(serviceId)
        }

        @JvmStatic
        fun create(actorType: ActorType): ActorBuilder = ActorBuilder(actorType)
    }
}

/**
 * Builder для Resource
 */
class ResourceBuilder private constructor(
    private val type: String,
    private val id: String
) {
    private var displayName: String? = null
    private var parentResource: Resource? = null
    private val attributes = mutableMapOf<String, Any>()

    fun displayName(displayName: String): ResourceBuilder = apply {
        this.displayName = displayName
    }

    fun parent(parent: Resource): ResourceBuilder = apply {
        this.parentResource = parent
    }

    fun addAttribute(key: String, value: Any): ResourceBuilder = apply {
        attributes[key] = value
    }

    fun build(): Resource {
        return Resource(
            type = type,
            id = id,
            displayName = displayName,
            parentResource = parentResource,
            attributes = attributes.toMap()
        )
    }

    companion object {
        @JvmStatic
        fun create(type: String, id: String): ResourceBuilder = ResourceBuilder(type, id)
    }
}

/**
 * Builder для Change
 */
class ChangeBuilder private constructor(private val field: String) {
    private var oldValue: Any? = null
    private var newValue: Any? = null
    private var changeType: Change.ChangeType? = null

    fun from(value: Any?): ChangeBuilder = apply {
        this.oldValue = value
    }

    fun to(value: Any?): ChangeBuilder = apply {
        this.newValue = value
    }

    fun changeType(changeType: Change.ChangeType): ChangeBuilder = apply {
        this.changeType = changeType
    }

    fun build(): Change {
        return Change(
            field = field,
            oldValue = oldValue,
            newValue = newValue,
            changeType = changeType
        )
    }

    companion object {
        @JvmStatic
        fun create(field: String): ChangeBuilder = ChangeBuilder(field)

        @JvmStatic
        fun added(field: String, value: Any): Change {
            return Change(field, null, value, Change.ChangeType.ADDED)
        }

        @JvmStatic
        fun modified(field: String): ChangeBuilder = ChangeBuilder(field).apply {
            changeType(Change.ChangeType.MODIFIED)
        }

        @JvmStatic
        fun removed(field: String, oldValue: Any): Change {
            return Change(field, oldValue, null, Change.ChangeType.REMOVED)
        }
    }
}

/**
 * Builder для Outcome
 */
class OutcomeBuilder private constructor() {
    private var success: Boolean = true
    private var errorCode: String? = null
    private var errorMessage: String? = null
    private var durationMs: Long? = null

    fun success(success: Boolean): OutcomeBuilder = apply {
        this.success = success
    }

    fun errorCode(errorCode: String): OutcomeBuilder = apply {
        this.errorCode = errorCode
    }

    fun errorMessage(errorMessage: String): OutcomeBuilder = apply {
        this.errorMessage = errorMessage
    }

    fun duration(durationMs: Long): OutcomeBuilder = apply {
        this.durationMs = durationMs
    }

    fun build(): AuditEvent.Outcome {
        return AuditEvent.Outcome(
            success = success,
            errorCode = errorCode,
            errorMessage = errorMessage,
            durationMs = durationMs
        )
    }

    companion object {
        @JvmStatic
        fun create(): OutcomeBuilder = OutcomeBuilder()

        @JvmStatic
        fun success(): OutcomeBuilder = OutcomeBuilder().apply {
            success(true)
        }

        @JvmStatic
        fun failure(errorCode: String, errorMessage: String): OutcomeBuilder = OutcomeBuilder().apply {
            success(false)
            errorCode(errorCode)
            errorMessage(errorMessage)
        }
    }
}
