package com.boomstream.userlog.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Ресурс - целевая сущность, над которой выполняется действие
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Resource(
    /** Тип ресурса (например: "order", "document", "user_profile") */
    val type: String,

    /** Идентификатор ресурса */
    val id: String,

    /** Человекочитаемое имя/название */
    val displayName: String? = null,

    /** Родительский ресурс (для вложенных сущностей) */
    val parentResource: Resource? = null,

    /** Дополнительные атрибуты ресурса */
    val attributes: Map<String, Any> = emptyMap()
) {
    /**
     * Возвращает полный путь ресурса в виде строки
     * Например: "project:123/task:456/comment:789"
     */
    @JsonIgnore
    fun getFullPath(): String {
        val parts = mutableListOf<String>()
        var current: Resource? = this

        while (current != null) {
            parts.add("${current.type}:${current.id}")
            current = current.parentResource
        }

        return parts.reversed().joinToString("/")
    }
}
