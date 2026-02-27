package io.github.maxixcom.audit.event.serialization

import io.github.maxixcom.audit.event.model.AuditEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Jackson-реализация сериализатора для событий аудита
 */
class AuditEventSerializerJson(
    private val objectMapper: ObjectMapper = createDefaultObjectMapper()
) : AuditEventSerializer {

    override fun serialize(event: AuditEvent): String =
        objectMapper.writeValueAsString(event)

    override fun serializeToBytes(event: AuditEvent): ByteArray =
        objectMapper.writeValueAsBytes(event)

    override fun deserialize(json: String): AuditEvent =
        objectMapper.readValue(json, AuditEvent::class.java)

    override fun deserializeFromBytes(bytes: ByteArray): AuditEvent =
        objectMapper.readValue(bytes, AuditEvent::class.java)

    override fun serializePretty(event: AuditEvent): String =
        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(event)

    companion object {
        /**
         * Создаёт ObjectMapper с настройками по умолчанию
         */
        fun createDefaultObjectMapper(): ObjectMapper {
            return ObjectMapper()
                .registerKotlinModule()
                .registerModule(JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.INDENT_OUTPUT, false)
        }
    }
}
