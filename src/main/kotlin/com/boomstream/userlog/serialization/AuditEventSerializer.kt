package com.boomstream.userlog.serialization

import com.boomstream.userlog.model.AuditEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Сериализатор для событий аудита
 */
class AuditEventSerializer(
    private val objectMapper: ObjectMapper = createDefaultObjectMapper()
) {
    /**
     * Сериализует событие в JSON строку
     */
    fun serialize(event: AuditEvent): String {
        return objectMapper.writeValueAsString(event)
    }

    /**
     * Сериализует событие в байтовый массив (для Kafka)
     */
    fun serializeToBytes(event: AuditEvent): ByteArray {
        return objectMapper.writeValueAsBytes(event)
    }

    /**
     * Десериализует событие из JSON строки
     */
    fun deserialize(json: String): AuditEvent {
        return objectMapper.readValue(json, AuditEvent::class.java)
    }

    /**
     * Десериализует событие из байтового массива
     */
    fun deserializeFromBytes(bytes: ByteArray): AuditEvent {
        return objectMapper.readValue(bytes, AuditEvent::class.java)
    }

    /**
     * Сериализует событие в pretty-printed JSON (для логов)
     */
    fun serializePretty(event: AuditEvent): String {
        return objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(event)
    }

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
