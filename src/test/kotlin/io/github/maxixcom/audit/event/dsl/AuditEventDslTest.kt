package io.github.maxixcom.audit.event.dsl

import io.github.maxixcom.audit.event.model.ActionCategory
import io.github.maxixcom.audit.event.model.ActorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuditEventDslTest {

    @Test
    fun `should create audit event with DSL`() {
        // Given & When
        val event = auditEvent {
            source = "order-service"
            action = "sales.order.updated"
            category = ActionCategory.UPDATE
            correlationId = "req-abc-123"

            actor {
                actorType = ActorType.USER
                userId = "user-42"
                sessionId = "sess-xyz"
                role("manager", "sales")
                ipAddress = "192.168.1.10"
                userAgent = "Mozilla/5.0"
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
                from("pending")
                to("confirmed")
            }

            change("deliveryAddress.city") {
                from("Москва")
                to("Казань")
            }

            outcome {
                success = true
                duration = 150
            }

            metadata("environment" to "production")
            tags("important", "customer-facing")
        }

        // Then
        assertEquals("order-service", event.source)
        assertEquals("sales.order.updated", event.action)
        assertEquals(ActionCategory.UPDATE, event.category)
        assertEquals("req-abc-123", event.correlationId)

        assertEquals(ActorType.USER, event.actor.actorType)
        assertEquals("user-42", event.actor.userId)
        assertEquals("sess-xyz", event.actor.sessionId)
        assertEquals(listOf("manager", "sales"), event.actor.roles)
        assertEquals("192.168.1.10", event.actor.ipAddress)

        assertEquals("order", event.resource.type)
        assertEquals("ord-1001", event.resource.id)
        assertEquals("Заказ #1001", event.resource.displayName)
        assertNotNull(event.resource.parentResource)
        assertEquals("customer", event.resource.parentResource?.type)
        assertEquals("cust-500", event.resource.parentResource?.id)

        assertEquals(2, event.changes.size)
        assertEquals("status", event.changes[0].field)
        assertEquals("pending", event.changes[0].oldValue)
        assertEquals("confirmed", event.changes[0].newValue)

        assertEquals("deliveryAddress.city", event.changes[1].field)
        assertEquals("Москва", event.changes[1].oldValue)
        assertEquals("Казань", event.changes[1].newValue)

        assertNotNull(event.outcome)
        assertEquals(true, event.outcome?.success)
        assertEquals(150L, event.outcome?.durationMs)

        assertEquals("production", event.metadata["environment"])
        assertEquals(setOf("important", "customer-facing"), event.tags)
    }

    @Test
    fun `should create minimal audit event`() {
        // Given & When
        val event = auditEvent {
            source = "test-service"
            action = "test.action"
            category = ActionCategory.VIEW

            actor {
                actorType = ActorType.SYSTEM
            }

            resource {
                type = "test"
                id = "123"
            }
        }

        // Then
        assertEquals("test-service", event.source)
        assertEquals("test.action", event.action)
        assertEquals(ActionCategory.VIEW, event.category)
        assertEquals(ActorType.SYSTEM, event.actor.actorType)
        assertEquals("test", event.resource.type)
        assertEquals("123", event.resource.id)
        assertEquals(0, event.changes.size)
    }

    @Test
    fun `should create event with onBehalfOf`() {
        // Given & When
        val event = auditEvent {
            source = "admin-service"
            action = "admin.user.impersonate"
            category = ActionCategory.LOGIN

            actor {
                actorType = ActorType.USER
                userId = "admin-1"
                onBehalfOf(
                    userId = "user-42",
                    userName = "John Doe",
                    reason = "Customer support request #12345"
                )
            }

            resource {
                type = "session"
                id = "sess-123"
            }
        }

        // Then
        assertNotNull(event.actor.onBehalfOf)
        assertEquals("user-42", event.actor.onBehalfOf?.userId)
        assertEquals("John Doe", event.actor.onBehalfOf?.userName)
        assertEquals("Customer support request #12345", event.actor.onBehalfOf?.reason)
    }

    @Test
    fun `should build resource full path`() {
        // Given & When
        val event = auditEvent {
            source = "project-service"
            action = "project.comment.created"
            category = ActionCategory.CREATE

            actor {
                actorType = ActorType.USER
                userId = "user-1"
            }

            resource {
                type = "comment"
                id = "789"

                parent {
                    type = "task"
                    id = "456"

                    parent {
                        type = "project"
                        id = "123"
                    }
                }
            }
        }

        // Then
        assertEquals("project:123/task:456/comment:789", event.resource.getFullPath())
    }

    @Test
    fun `should detect security critical events`() {
        // Given & When
        val loginEvent = auditEvent {
            source = "auth"
            action = "auth.login"
            category = ActionCategory.LOGIN

            actor {
                actorType = ActorType.USER
                userId = "user-1"
            }

            resource {
                type = "session"
                id = "s1"
            }
        }

        val deleteEvent = auditEvent {
            source = "user-service"
            action = "user.delete"
            category = ActionCategory.DELETE

            actor {
                actorType = ActorType.USER
                userId = "user-1"
            }

            resource {
                type = "user"
                id = "user-2"
            }
        }

        val viewEvent = auditEvent {
            source = "doc-service"
            action = "doc.view"
            category = ActionCategory.VIEW

            actor {
                actorType = ActorType.USER
                userId = "user-1"
            }

            resource {
                type = "document"
                id = "doc-1"
            }
        }

        // Then
        assertEquals(true, loginEvent.isSecurityCritical())
        assertEquals(true, deleteEvent.isSecurityCritical())
        assertEquals(false, viewEvent.isSecurityCritical())
    }
}
