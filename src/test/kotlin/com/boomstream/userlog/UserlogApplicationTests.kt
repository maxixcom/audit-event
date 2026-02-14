package com.boomstream.userlog

import com.boomstream.userlog.dsl.auditEvent
import com.boomstream.userlog.model.ActionCategory
import com.boomstream.userlog.model.ActorType
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * Базовая проверка доступности классов библиотеки
 */
class UserlogLibraryTest {

	@Test
	fun `library loads successfully`() {
		// Проверяем, что основные классы доступны
		assertNotNull(com.boomstream.userlog.model.AuditEvent::class)
		assertNotNull(com.boomstream.userlog.builder.AuditEventBuilder::class)
		assertNotNull(com.boomstream.userlog.factory.AuditEventFactory)
		assertNotNull(com.boomstream.userlog.serialization.AuditEventSerializer::class)
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
