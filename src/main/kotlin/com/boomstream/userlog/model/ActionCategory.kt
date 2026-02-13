package com.boomstream.userlog.model

/**
 * Категория действия для агрегации и фильтрации
 */
enum class ActionCategory {
    CREATE,
    UPDATE,
    DELETE,
    VIEW,
    EXPORT,
    IMPORT,
    LOGIN,
    LOGOUT,
    PERMISSION_CHANGE,
    CONFIGURATION_CHANGE,
    EXECUTE,
    DOWNLOAD,
    UPLOAD,
    SHARE,
    ARCHIVE,
    RESTORE
}
