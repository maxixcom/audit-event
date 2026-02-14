package io.github.maxixcom.audit.event.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Актор - субъект, выполняющий действие
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Actor(
    /** Тип актора */
    val actorType: ActorType,

    /** ID пользователя (может быть null для системных действий) */
    val userId: String? = null,

    /** ID сессии */
    val sessionId: String? = null,

    /** Роли пользователя на момент действия */
    val roles: List<String> = emptyList(),

    /** IP адрес */
    val ipAddress: String? = null,

    /** User-Agent браузера/клиента */
    val userAgent: String? = null,

    /**
     * Если действие выполняется от имени другого пользователя
     * (импersonation, делегирование прав)
     */
    val onBehalfOf: OnBehalfOf? = null,

    /** Дополнительные атрибуты актора */
    val attributes: Map<String, Any> = emptyMap()
) {
    /**
     * Информация о пользователе, от чьего имени выполняется действие
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class OnBehalfOf(
        val userId: String,
        val userName: String? = null,
        val reason: String? = null
    )
}
