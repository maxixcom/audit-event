package com.boomstream.userlog.factory

import com.boomstream.userlog.dsl.auditEvent
import com.boomstream.userlog.model.*

/**
 * Фабрика для создания событий аудита по распространённым паттернам
 */
object AuditEventFactory {

    /**
     * Создаёт событие входа пользователя в систему
     */
    fun createLoginEvent(
        source: String,
        userId: String,
        sessionId: String,
        ipAddress: String?,
        userAgent: String?,
        success: Boolean,
        errorMessage: String? = null
    ): AuditEvent = auditEvent {
        this.source = source
        action = "auth.user.login"
        category = ActionCategory.LOGIN

        actor {
            actorType = ActorType.USER
            this.userId = userId
            this.sessionId = sessionId
            this.ipAddress = ipAddress
            this.userAgent = userAgent
        }

        resource {
            type = "session"
            id = sessionId
        }

        outcome {
            this.success = success
            if (!success && errorMessage != null) {
                this.errorCode = "LOGIN_FAILED"
                this.errorMessage = errorMessage
            }
        }

        tags("authentication", "security")
    }

    /**
     * Создаёт событие выхода пользователя из системы
     */
    fun createLogoutEvent(
        source: String,
        userId: String,
        sessionId: String
    ): AuditEvent = auditEvent {
        this.source = source
        action = "auth.user.logout"
        category = ActionCategory.LOGOUT

        actor {
            actorType = ActorType.USER
            this.userId = userId
            this.sessionId = sessionId
        }

        resource {
            type = "session"
            id = sessionId
        }

        outcome {
            success = true
        }

        tags("authentication")
    }

    /**
     * Создаёт событие создания ресурса
     */
    fun createResourceCreatedEvent(
        source: String,
        actor: Actor,
        resourceType: String,
        resourceId: String,
        displayName: String? = null,
        action: String? = null,
        initialValues: Map<String, Any> = emptyMap()
    ): AuditEvent = auditEvent {
        this.source = source
        this.action = action ?: "$source.$resourceType.created"
        category = ActionCategory.CREATE

        this.actor {
            actorType = actor.actorType
            userId = actor.userId
            sessionId = actor.sessionId
            roles = actor.roles
            ipAddress = actor.ipAddress
            userAgent = actor.userAgent
        }

        resource {
            type = resourceType
            id = resourceId
            this.displayName = displayName
        }

        initialValues.forEach { (field, value) ->
            change {
                this.field = field
                to(value)
            }
        }

        tags("creation")
    }

    /**
     * Создаёт событие обновления ресурса
     */
    fun createResourceUpdatedEvent(
        source: String,
        actor: Actor,
        resourceType: String,
        resourceId: String,
        displayName: String? = null,
        action: String? = null,
        changes: Map<String, Pair<Any?, Any?>>
    ): AuditEvent = auditEvent {
        this.source = source
        this.action = action ?: "$source.$resourceType.updated"
        category = ActionCategory.UPDATE

        this.actor {
            actorType = actor.actorType
            userId = actor.userId
            sessionId = actor.sessionId
            roles = actor.roles
            ipAddress = actor.ipAddress
            userAgent = actor.userAgent
        }

        resource {
            type = resourceType
            id = resourceId
            this.displayName = displayName
        }

        changes.forEach { (field, valuePair) ->
            change {
                this.field = field
                from(valuePair.first)
                to(valuePair.second)
            }
        }

        tags("modification")
    }

    /**
     * Создаёт событие удаления ресурса
     */
    fun createResourceDeletedEvent(
        source: String,
        actor: Actor,
        resourceType: String,
        resourceId: String,
        displayName: String? = null,
        action: String? = null,
        finalValues: Map<String, Any> = emptyMap()
    ): AuditEvent = auditEvent {
        this.source = source
        this.action = action ?: "$source.$resourceType.deleted"
        category = ActionCategory.DELETE

        this.actor {
            actorType = actor.actorType
            userId = actor.userId
            sessionId = actor.sessionId
            roles = actor.roles
            ipAddress = actor.ipAddress
            userAgent = actor.userAgent
        }

        resource {
            type = resourceType
            id = resourceId
            this.displayName = displayName
        }

        finalValues.forEach { (field, value) ->
            change {
                this.field = field
                from(value)
                to(null)
            }
        }

        tags("deletion", "critical")
    }

    /**
     * Создаёт событие просмотра ресурса
     */
    fun createResourceViewedEvent(
        source: String,
        actor: Actor,
        resourceType: String,
        resourceId: String,
        displayName: String? = null,
        action: String? = null
    ): AuditEvent = auditEvent {
        this.source = source
        this.action = action ?: "$source.$resourceType.viewed"
        category = ActionCategory.VIEW

        this.actor {
            actorType = actor.actorType
            userId = actor.userId
            sessionId = actor.sessionId
            roles = actor.roles
            ipAddress = actor.ipAddress
            userAgent = actor.userAgent
        }

        resource {
            type = resourceType
            id = resourceId
            this.displayName = displayName
        }

        tags("access")
    }

    /**
     * Создаёт событие изменения прав доступа
     */
    fun createPermissionChangedEvent(
        source: String,
        actor: Actor,
        targetUserId: String,
        resourceType: String,
        resourceId: String,
        oldPermissions: List<String>,
        newPermissions: List<String>
    ): AuditEvent = auditEvent {
        this.source = source
        action = "iam.permission.changed"
        category = ActionCategory.PERMISSION_CHANGE

        this.actor {
            actorType = actor.actorType
            userId = actor.userId
            sessionId = actor.sessionId
            roles = actor.roles
            ipAddress = actor.ipAddress
            userAgent = actor.userAgent
        }

        resource {
            type = resourceType
            id = resourceId
        }

        change {
            field = "permissions.$targetUserId"
            from(oldPermissions)
            to(newPermissions)
        }

        metadata("targetUserId" to targetUserId)
        tags("security", "critical", "permission")
    }

    /**
     * Создаёт событие экспорта данных
     */
    fun createDataExportEvent(
        source: String,
        actor: Actor,
        resourceType: String,
        resourceIds: List<String>,
        format: String
    ): AuditEvent = auditEvent {
        this.source = source
        action = "$source.$resourceType.exported"
        category = ActionCategory.EXPORT

        this.actor {
            actorType = actor.actorType
            userId = actor.userId
            sessionId = actor.sessionId
            roles = actor.roles
            ipAddress = actor.ipAddress
            userAgent = actor.userAgent
        }

        resource {
            type = resourceType
            id = resourceIds.joinToString(",")
            displayName = "${resourceIds.size} $resourceType(s)"
        }

        metadata(
            "exportFormat" to format,
            "exportedCount" to resourceIds.size,
            "resourceIds" to resourceIds
        )

        tags("export", "data-access")
    }
}
