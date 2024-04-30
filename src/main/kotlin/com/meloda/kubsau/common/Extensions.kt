package com.meloda.kubsau.common

import com.meloda.kubsau.errors.ValidationException
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun ApplicationRequest.toLogString(): String {
    val headers = headers.entries().joinToString(", ") { (key, values) -> "$key: ${values.joinToString()}" }
    return "HTTP ${httpMethod.value} $uri Headers: $headers Body: ${this.receiveChannel()}"
}

fun ApplicationResponse.toLogString(): String {
    return "HTTP ${status()?.value} ${
        headers.allValues().entries().joinToString(", ") { (key, values) -> "$key: ${values.joinToString()}" }
    }"
}

fun Parameters.getString(key: String, trim: Boolean = true): String? = try {
    getOrThrow(key, trim)
} catch (ignored: ValidationException) {
    null
}

fun Parameters.getOrThrow(key: String, trim: Boolean = true): String = getOrThrow(key) { if (trim) it.trim() else it }

fun Parameters.getBoolean(key: String): Boolean? = try {
    getBooleanOrThrow(key)
} catch (ignored: ValidationException) {
    null
}

fun Parameters.getBooleanOrThrow(key: String): Boolean =
    getOrThrow(
        key = key,
        mapper = { it.toBooleanStrictOrNull() ?: throw ValidationException("$key is invalid") }
    )

fun Parameters.getInt(key: String): Int? = try {
    getIntOrThrow(key)
} catch (ignored: ValidationException) {
    null
}

fun Parameters.getIntOrThrow(key: String): Int = getOrThrow(
    key = key,
    mapper = { it.toIntOrNull() ?: throw ValidationException("$key is invalid") }
)

fun <T> Parameters.getOrThrow(key: String, mapper: (String) -> T): T = getOrThrow(
    key = key,
    mapper = mapper,
    message = { "$key is empty" }
)

fun <T> Parameters.getOrThrow(key: String, mapper: (String) -> T, message: () -> String): T =
    mapper(this[key] ?: throw ValidationException(message()))
