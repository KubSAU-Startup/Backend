package com.meloda.kubsau.common

import org.slf4j.event.Level

enum class LogLevel(val value: Int) {
    TRACE(0),
    DEBUG(10),
    INFO(20),
    WARN(30),
    ERROR(40);

    fun toLevel(): Level = Level.intToLevel(value)

    companion object {
        fun parse(value: String): LogLevel? = when (value.lowercase()) {
            "trace" -> TRACE
            "debug" -> DEBUG
            "info" -> INFO
            "warn" -> WARN
            "error" -> ERROR
            else -> null
        }
    }
}
