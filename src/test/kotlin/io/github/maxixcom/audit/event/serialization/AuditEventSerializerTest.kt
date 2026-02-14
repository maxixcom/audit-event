package io.github.maxixcom.audit.event.serialization

import io.github.maxixcom.audit.event.dsl.auditEvent
import io.github.maxixcom.audit.event.model.ActionCategory
import io.github.maxixcom.audit.event.model.ActorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuditEventSerializerTest {

    private val serializer = AuditEventSerializer()

    @Test
    fun `should serialize and deserialize audit event`() {
        // Given
        val originalEvent = auditEvent {
            source = "test-service"
            action = "test.action"
            category = ActionCategory.UPDATE

            actor {
                actorType = ActorType.USER
                userId = "user-123"
                sessionId = "sess-xyz"
                role("admin")
                ipAddress = "127.0.0.1"
            }

            resource {
                type = "order"
                id = "ord-456"
                displayName = "Test Order"
            }

            change {
                field = "status"
                from("pending")
                to("confirmed")
            }

            outcome {
                success = true
                duration = 100
            }

            metadata("key" to "value")
            tags("test")
        }

        // When
        val json = serializer.serialize(originalEvent)
        val deserializedEvent = serializer.deserialize(json)

        // Then
        assertEquals(originalEvent.eventId, deserializedEvent.eventId)
        assertEquals(originalEvent.source, deserializedEvent.source)
        assertEquals(originalEvent.action, deserializedEvent.action)
        assertEquals(originalEvent.category, deserializedEvent.category)
        assertEquals(originalEvent.actor.userId, deserializedEvent.actor.userId)
        assertEquals(originalEvent.resource.id, deserializedEvent.resource.id)
        assertEquals(originalEvent.changes.size, deserializedEvent.changes.size)
        assertEquals(originalEvent.outcome?.success, deserializedEvent.outcome?.success)
    }

    @Test
    fun `should serialize to bytes and deserialize`() {
        // Given
        val event = auditEvent {
            source = "test-service"
            action = "test.action"
            category = ActionCategory.CREATE

            actor {
                actorType = ActorType.SYSTEM
            }

            resource {
                type = "test"
                id = "123"
            }
        }

        // When
        val bytes = serializer.serializeToBytes(event)
        val deserialized = serializer.deserializeFromBytes(bytes)

        // Then
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
        assertEquals(event.eventId, deserialized.eventId)
        assertEquals(event.source, deserialized.source)
    }

    @Test
    fun `should serialize pretty JSON`() {
        // Given
        val event = auditEvent {
            source = "test-service"
            action = "test.action"
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

        // When
        val prettyJson = serializer.serializePretty(event)

        // Then
        assertTrue(prettyJson.contains("\n"))
        assertTrue(prettyJson.contains("\"source\" : \"test-service\""))
    }

    @Test
    fun `should handle nested resources in serialization`() {
        // Given
        val event = auditEvent {
            source = "test-service"
            action = "test.action"
            category = ActionCategory.UPDATE

            actor {
                actorType = ActorType.USER
                userId = "user-1"
            }

            resource {
                type = "comment"
                id = "c-789"

                parent {
                    type = "task"
                    id = "t-456"

                    parent {
                        type = "project"
                        id = "p-123"
                    }
                }
            }
        }

        // When
        val json = serializer.serialize(event)
        val deserialized = serializer.deserialize(json)

        // Then
        assertEquals("comment", deserialized.resource.type)
        assertNotNull(deserialized.resource.parentResource)
        assertEquals("task", deserialized.resource.parentResource?.type)
        assertNotNull(deserialized.resource.parentResource?.parentResource)
        assertEquals("project", deserialized.resource.parentResource?.parentResource?.type)
    }
}
