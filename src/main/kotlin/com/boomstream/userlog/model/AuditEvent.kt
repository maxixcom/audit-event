package com.boomstream.userlog.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant
import java.util.*

/**
 * Событие аудита - центральная модель для логирования действий пользователей
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuditEvent(
    /** Уникальный идентификатор события (для идемпотентности) */
    val eventId: String = UUID.randomUUID().toString(),

    /** Версия схемы события */
    val version: String = "1.0",

    /** Временная метка события */
    val timestamp: Instant = Instant.now(),

    /** Источник события (название сервиса/модуля) */
    val source: String,

    /** ID корреляции для связи цепочки действий */
    val correlationId: String? = null,

    /** Актор, выполняющий действие */
    val actor: Actor,

    /** Действие в формате domain.entity.verb */
    val action: String,

    /** Категория действия */
    val category: ActionCategory,

    /** Целевой ресурс */
    val resource: Resource,

    /** Список изменений */
    val changes: List<Change> = emptyList(),

    /** Результат выполнения действия */
    val outcome: Outcome? = null,

    /** Дополнительные метаданные */
    val metadata: Map<String, Any> = emptyMap(),

    /** Теги для фильтрации и группировки */
    val tags: Set<String> = emptySet()
) {
    /**
     * Результат выполнения действия
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Outcome(
        /** Успешность выполнения */
        val success: Boolean,

        /** Код ошибки (если не успешно) */
        val errorCode: String? = null,

        /** Сообщение об ошибке */
        val errorMessage: String? = null,

        /** Продолжительность выполнения в миллисекундах */
        val durationMs: Long? = null
    )

    /**
     * Проверяет, является ли событие критичным для безопасности
     */
    @JsonIgnore
    fun isSecurityCritical(): Boolean {
        return category in listOf(
            ActionCategory.PERMISSION_CHANGE,
            ActionCategory.LOGIN,
            ActionCategory.LOGOUT,
            ActionCategory.DELETE
        ) || tags.contains("security") || tags.contains("critical")
    }

    /**
     * Возвращает краткое описание события для логов
     */
    @JsonIgnore
    fun getSummary(): String {
        val result = if (outcome?.success != false) "успешно" else "с ошибкой"
        return "${actor.userId ?: actor.actorType} $result выполнил '$action' над ${resource.type}:${resource.id}"
    }
}
