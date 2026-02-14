package io.github.maxixcom.audit.event.builder

import io.github.maxixcom.audit.event.model.ActionCategory
import io.github.maxixcom.audit.event.model.ActorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuditEventBuilderTest {

    @Test
    fun `should create audit event with Java builder`() {
        // Given & When
        val event = AuditEventBuilder.create()
            .source("order-service")
            .action("sales.order.updated")
            .category(ActionCategory.UPDATE)
            .correlationId("req-abc-123")
            .actor(
                ActorBuilder.user("user-42")
                    .sessionId("sess-xyz")
                    .roles("manager", "sales")
                    .ipAddress("192.168.1.10")
                    .userAgent("Mozilla/5.0")
                    .build()
            )
            .resource(
                ResourceBuilder.create("order", "ord-1001")
                    .displayName("Заказ #1001")
                    .parent(
                        ResourceBuilder.create("customer", "cust-500")
                            .build()
                    )
                    .build()
            )
            .addChange(
                ChangeBuilder.modified("status")
                    .from("pending")
                    .to("confirmed")
                    .build()
            )
            .addChange(
                ChangeBuilder.create("deliveryAddress.city")
                    .from("Москва")
                    .to("Казань")
                    .build()
            )
            .outcome(
                OutcomeBuilder.success()
                    .duration(150L)
                    .build()
            )
            .addMetadata("environment", "production")
            .tags("important", "customer-facing")
            .build()

        // Then
        assertEquals("order-service", event.source)
        assertEquals("sales.order.updated", event.action)
        assertEquals(ActionCategory.UPDATE, event.category)
        assertEquals("req-abc-123", event.correlationId)

        assertEquals(ActorType.USER, event.actor.actorType)
        assertEquals("user-42", event.actor.userId)
        assertEquals("sess-xyz", event.actor.sessionId)
        assertEquals(listOf("manager", "sales"), event.actor.roles)

        assertEquals("order", event.resource.type)
        assertEquals("ord-1001", event.resource.id)
        assertNotNull(event.resource.parentResource)

        assertEquals(2, event.changes.size)
        assertEquals("status", event.changes[0].field)

        assertNotNull(event.outcome)
        assertEquals(true, event.outcome?.success)
    }

    @Test
    fun `should create system actor`() {
        // Given & When
        val actor = ActorBuilder.system()
            .addAttribute("process", "scheduler")
            .build()

        // Then
        assertEquals(ActorType.SYSTEM, actor.actorType)
        assertEquals("scheduler", actor.attributes["process"])
    }

    @Test
    fun `should create service account actor`() {
        // Given & When
        val actor = ActorBuilder.serviceAccount("svc-account-123")
            .role("service")
            .build()

        // Then
        assertEquals(ActorType.SERVICE_ACCOUNT, actor.actorType)
        assertEquals("svc-account-123", actor.userId)
        assertEquals(listOf("service"), actor.roles)
    }

    @Test
    fun `should create failure outcome`() {
        // Given & When
        val outcome = OutcomeBuilder.failure("ERR_001", "Validation failed")
            .duration(50L)
            .build()

        // Then
        assertEquals(false, outcome.success)
        assertEquals("ERR_001", outcome.errorCode)
        assertEquals("Validation failed", outcome.errorMessage)
        assertEquals(50L, outcome.durationMs)
    }

    @Test
    fun `should create change with added type`() {
        // Given & When
        val change = ChangeBuilder.added("newField", "newValue")

        // Then
        assertEquals("newField", change.field)
        assertEquals(null, change.oldValue)
        assertEquals("newValue", change.newValue)
    }

    @Test
    fun `should create change with removed type`() {
        // Given & When
        val change = ChangeBuilder.removed("oldField", "oldValue")

        // Then
        assertEquals("oldField", change.field)
        assertEquals("oldValue", change.oldValue)
        assertEquals(null, change.newValue)
    }

    @Test
    fun `should create resource with attributes`() {
        // Given & When
        val resource = ResourceBuilder.create("document", "doc-123")
            .displayName("Important Document")
            .addAttribute("size", 1024)
            .addAttribute("format", "pdf")
            .build()

        // Then
        assertEquals("document", resource.type)
        assertEquals("doc-123", resource.id)
        assertEquals("Important Document", resource.displayName)
        assertEquals(1024, resource.attributes["size"])
        assertEquals("pdf", resource.attributes["format"])
    }

    @Test
    fun `should create actor with onBehalfOf`() {
        // Given & When
        val actor = ActorBuilder.user("admin-1")
            .onBehalfOf("user-42", "John Doe", "Support request")
            .build()

        // Then
        assertNotNull(actor.onBehalfOf)
        assertEquals("user-42", actor.onBehalfOf?.userId)
        assertEquals("John Doe", actor.onBehalfOf?.userName)
        assertEquals("Support request", actor.onBehalfOf?.reason)
    }
}
