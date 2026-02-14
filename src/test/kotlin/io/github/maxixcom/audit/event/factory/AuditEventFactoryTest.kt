package io.github.maxixcom.audit.event.factory

import io.github.maxixcom.audit.event.model.ActionCategory
import io.github.maxixcom.audit.event.model.Actor
import io.github.maxixcom.audit.event.model.ActorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuditEventFactoryTest {

    private val testActor = Actor(
        actorType = ActorType.USER,
        userId = "user-123",
        sessionId = "sess-xyz",
        roles = listOf("manager"),
        ipAddress = "192.168.1.10"
    )

    @Test
    fun `should create login event`() {
        // When
        val event = AuditEventFactory.createLoginEvent(
            source = "auth-service",
            userId = "user-42",
            sessionId = "sess-123",
            ipAddress = "127.0.0.1",
            userAgent = "Mozilla/5.0",
            success = true
        )

        // Then
        assertEquals("auth-service", event.source)
        assertEquals("auth.user.login", event.action)
        assertEquals(ActionCategory.LOGIN, event.category)
        assertEquals("user-42", event.actor.userId)
        assertEquals("sess-123", event.actor.sessionId)
        assertEquals("session", event.resource.type)
        assertEquals("sess-123", event.resource.id)
        assertEquals(true, event.outcome?.success)
        assertEquals(true, event.tags.contains("authentication"))
    }

    @Test
    fun `should create login event with failure`() {
        // When
        val event = AuditEventFactory.createLoginEvent(
            source = "auth-service",
            userId = "user-42",
            sessionId = "sess-123",
            ipAddress = "127.0.0.1",
            userAgent = "Mozilla/5.0",
            success = false,
            errorMessage = "Invalid credentials"
        )

        // Then
        assertEquals(false, event.outcome?.success)
        assertEquals("LOGIN_FAILED", event.outcome?.errorCode)
        assertEquals("Invalid credentials", event.outcome?.errorMessage)
    }

    @Test
    fun `should create logout event`() {
        // When
        val event = AuditEventFactory.createLogoutEvent(
            source = "auth-service",
            userId = "user-42",
            sessionId = "sess-123"
        )

        // Then
        assertEquals("auth-service", event.source)
        assertEquals("auth.user.logout", event.action)
        assertEquals(ActionCategory.LOGOUT, event.category)
        assertEquals("user-42", event.actor.userId)
    }

    @Test
    fun `should create resource created event`() {
        // When
        val event = AuditEventFactory.createResourceCreatedEvent(
            source = "order-service",
            actor = testActor,
            resourceType = "order",
            resourceId = "ord-1001",
            displayName = "Новый заказ",
            initialValues = mapOf(
                "status" to "pending",
                "amount" to 1500.0,
                "customerId" to "cust-500"
            )
        )

        // Then
        assertEquals("order-service", event.source)
        assertEquals("order-service.order.created", event.action)
        assertEquals(ActionCategory.CREATE, event.category)
        assertEquals("order", event.resource.type)
        assertEquals("ord-1001", event.resource.id)
        assertEquals(3, event.changes.size)

        val statusChange = event.changes.find { it.field == "status" }
        assertNotNull(statusChange)
        assertEquals(null, statusChange.oldValue)
        assertEquals("pending", statusChange.newValue)
    }

    @Test
    fun `should create resource updated event`() {
        // When
        val event = AuditEventFactory.createResourceUpdatedEvent(
            source = "order-service",
            actor = testActor,
            resourceType = "order",
            resourceId = "ord-1001",
            displayName = "Заказ #1001",
            changes = mapOf(
                "status" to ("pending" to "confirmed"),
                "amount" to (1500.0 to 1800.0)
            )
        )

        // Then
        assertEquals("order-service.order.updated", event.action)
        assertEquals(ActionCategory.UPDATE, event.category)
        assertEquals(2, event.changes.size)

        val statusChange = event.changes.find { it.field == "status" }
        assertNotNull(statusChange)
        assertEquals("pending", statusChange.oldValue)
        assertEquals("confirmed", statusChange.newValue)
    }

    @Test
    fun `should create resource deleted event`() {
        // When
        val event = AuditEventFactory.createResourceDeletedEvent(
            source = "order-service",
            actor = testActor,
            resourceType = "order",
            resourceId = "ord-1001",
            displayName = "Заказ #1001",
            finalValues = mapOf(
                "status" to "cancelled",
                "amount" to 1500.0
            )
        )

        // Then
        assertEquals("order-service.order.deleted", event.action)
        assertEquals(ActionCategory.DELETE, event.category)
        assertEquals(true, event.tags.contains("critical"))

        val statusChange = event.changes.find { it.field == "status" }
        assertNotNull(statusChange)
        assertEquals("cancelled", statusChange.oldValue)
        assertEquals(null, statusChange.newValue)
    }

    @Test
    fun `should create resource viewed event`() {
        // When
        val event = AuditEventFactory.createResourceViewedEvent(
            source = "document-service",
            actor = testActor,
            resourceType = "document",
            resourceId = "doc-123",
            displayName = "Конфиденциальный документ"
        )

        // Then
        assertEquals("document-service.document.viewed", event.action)
        assertEquals(ActionCategory.VIEW, event.category)
        assertEquals("document", event.resource.type)
        assertEquals(0, event.changes.size)
    }

    @Test
    fun `should create permission changed event`() {
        // When
        val event = AuditEventFactory.createPermissionChangedEvent(
            source = "iam-service",
            actor = testActor,
            targetUserId = "user-456",
            resourceType = "project",
            resourceId = "proj-789",
            oldPermissions = listOf("read"),
            newPermissions = listOf("read", "write", "admin")
        )

        // Then
        assertEquals("iam-service", event.source)
        assertEquals("iam.permission.changed", event.action)
        assertEquals(ActionCategory.PERMISSION_CHANGE, event.category)
        assertEquals("user-456", event.metadata["targetUserId"])
        assertEquals(true, event.tags.contains("security"))
        assertEquals(true, event.tags.contains("critical"))

        assertEquals(1, event.changes.size)
        assertEquals("permissions.user-456", event.changes[0].field)
    }

    @Test
    fun `should create data export event`() {
        // When
        val event = AuditEventFactory.createDataExportEvent(
            source = "export-service",
            actor = testActor,
            resourceType = "customer",
            resourceIds = listOf("cust-1", "cust-2", "cust-3"),
            format = "CSV"
        )

        // Then
        assertEquals("export-service.customer.exported", event.action)
        assertEquals(ActionCategory.EXPORT, event.category)
        assertEquals("CSV", event.metadata["exportFormat"])
        assertEquals(3, event.metadata["exportedCount"])
        assertEquals(true, event.tags.contains("export"))
    }
}
