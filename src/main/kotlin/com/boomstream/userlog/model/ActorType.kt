package com.boomstream.userlog.model

/**
 * Тип актора, выполняющего действие
 */
enum class ActorType {
    /** Обычный пользователь системы */
    USER,

    /** Системный процесс/сервис */
    SYSTEM,

    /** Scheduled job/cron task */
    SCHEDULER,

    /** API token/service account */
    SERVICE_ACCOUNT,

    /** Внешняя интеграция */
    EXTERNAL_SERVICE
}
