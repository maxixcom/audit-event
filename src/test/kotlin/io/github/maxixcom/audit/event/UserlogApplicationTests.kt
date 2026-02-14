package io.github.maxixcom.audit.event

import io.github.maxixcom.audit.event.dsl.auditEvent
import io.github.maxixcom.audit.event.model.ActionCategory
import io.github.maxixcom.audit.event.model.ActorType
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * Базовая проверка доступности классов библиотеки
 */
class UserlogLibraryTest {

	@Test
	fun `library loads successfully`() {
		// Проверяем, что основные классы доступны
		assertNotNull(io.github.maxixcom.audit.event.model.AuditEvent::class)
		assertNotNull(io.github.maxixcom.audit.event.builder.AuditEventBuilder::class)
		assertNotNull(io.github.maxixcom.audit.event.factory.AuditEventFactory)
		assertNotNull(io.github.maxixcom.audit.event.serialization.AuditEventSerializer::class)
	}

	@Test
	fun `DSL creates event successfully`() {
		// Проверяем, что DSL работает
		val event = auditEvent {
			source = "test"
			action = "test.action"
			category = ActionCategory.CREATE

			actor {
				actorType = ActorType.USER
				userId = "test-user"
			}

			resource {
				type = "test"
				id = "1"
			}
		}

		assertNotNull(event)
		assertNotNull(event.eventId)
	}
}
