package io.github.maxixcom.audit.event.serialization

import io.github.maxixcom.audit.event.model.AuditEvent

/**
 * Интерфейс сериализатора для событий аудита
 */
interface AuditEventSerializer {
    /**
     * Сериализует событие в JSON строку
     */
    fun serialize(event: AuditEvent): String

    /**
     * Сериализует событие в байтовый массив (для Kafka)
     */
    fun serializeToBytes(event: AuditEvent): ByteArray

    /**
     * Десериализует событие из JSON строки
     */
    fun deserialize(json: String): AuditEvent

    /**
     * Десериализует событие из байтового массива
     */
    fun deserializeFromBytes(bytes: ByteArray): AuditEvent

    /**
     * Сериализует событие в pretty-printed JSON (для логов)
     */
    fun serializePretty(event: AuditEvent): String
}
