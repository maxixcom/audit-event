package com.boomstream.userlog.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Изменение поля ресурса
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Change(
    /** Имя поля (может быть в dot-notation: "address.city") */
    val field: String,

    /** Старое значение (null для создания) */
    val oldValue: Any? = null,

    /** Новое значение (null для удаления) */
    val newValue: Any? = null,

    /** Тип изменения для семантической ясности */
    val changeType: ChangeType? = null
) {
    enum class ChangeType {
        /** Поле было добавлено */
        ADDED,

        /** Поле было изменено */
        MODIFIED,

        /** Поле было удалено */
        REMOVED,

        /** Значение не изменилось (для явного логирования неизменности) */
        UNCHANGED
    }

    init {
        // Автоматическое определение типа изменения, если не указан явно
        if (changeType == null) {
            when {
                oldValue == null && newValue != null -> ChangeType.ADDED
                oldValue != null && newValue == null -> ChangeType.REMOVED
                oldValue != newValue -> ChangeType.MODIFIED
                else -> ChangeType.UNCHANGED
            }
        }
    }
}
