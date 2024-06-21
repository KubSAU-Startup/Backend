package com.meloda.kubsau.common

import com.meloda.kubsau.model.UnknownTokenException
import com.meloda.kubsau.model.ValidationException
import com.meloda.kubsau.plugins.UserPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.mindrot.jbcrypt.BCrypt

fun ApplicationCall.userPrincipal(): UserPrincipal = this.principal() ?: throw UnknownTokenException

fun ApplicationRequest.toLogString(): String {
    val headers = headers.entries().joinToString(", ") { (key, values) -> "$key: ${values.joinToString()}" }
    return "HTTP ${httpMethod.value} $uri Headers: $headers Body: ${this.receiveChannel()}"
}

fun ApplicationResponse.toLogString(): String {
    return "HTTP ${status()?.value} ${
        headers.allValues().entries().joinToString(", ") { (key, values) -> "$key: ${values.joinToString()}" }
    }"
}

fun Parameters.getString(
    key: String,
    defaultValue: String,
    trim: Boolean = true
): String = getString(key, trim) ?: defaultValue

fun Parameters.getString(
    key: String,
    trim: Boolean = true
): String? = try {
    getStringOrThrow(
        key = key,
        trim = trim
    )
} catch (ignored: ValidationException) {
    null
}

fun Parameters.getStringOrThrow(
    key: String,
    trim: Boolean = true,
    requiredNotEmpty: Boolean = false
): String = getOrThrow(key) {
    val string = if (trim) it.trim() else it

    if (requiredNotEmpty && (string.isEmpty() || string.isBlank())) {
        throw ValidationException.EmptyOrBlankException(key)
    }

    string
}

fun Parameters.getBoolean(key: String, defaultValue: Boolean): Boolean = getBoolean(key) ?: defaultValue

fun Parameters.getBoolean(key: String): Boolean? = try {
    getBooleanOrThrow(key)
} catch (ignored: ValidationException) {
    null
}

fun Parameters.getBooleanOrThrow(key: String): Boolean = getOrThrow(
    key = key,
    mapper = { it.toBooleanStrictOrNull() ?: throw ValidationException.InvalidValueException(key) }
)

fun Parameters.getIntList(
    key: String,
    defaultValue: List<Int>,
    maxSize: Int? = null,
    requiredNotEmpty: Boolean = false
): List<Int> = getIntList(
    key = key,
    maxSize = maxSize,
    requiredNotEmpty = requiredNotEmpty
) ?: defaultValue

fun Parameters.getIntList(
    key: String,
    maxSize: Int? = null,
    requiredNotEmpty: Boolean = false
): List<Int>? = runCatching {
    getIntListOrThrow(
        key = key,
        requiredNotEmpty = requiredNotEmpty
    )
}.fold(
    onSuccess = { list ->
        if (requiredNotEmpty && list.isEmpty()) {
            throw ValidationException.EmptyItemException(key)
        }

        maxSize?.let {
            if (list.size > maxSize) {
                throw ValidationException.InvalidSizeException(key, maxSize, list.size)
            }
        }

        list
    },
    onFailure = { exception ->
        if (exception is ValidationException.InvalidValueException) {
            throw exception
        }

        null
    }
)

fun Parameters.getIntListOrThrow(key: String, requiredNotEmpty: Boolean = false): List<Int> = getOrThrow(
    key = key,
    mapper = { value ->
        if (value.trim().isEmpty()) {
            if (requiredNotEmpty) {
                throw ValidationException.EmptyItemException(key)
            }

            emptyList()
        } else {
            val list = value.split(",").map(String::trim).mapNotNull(String::toIntOrNull)

            if (list.isEmpty()) {
                throw ValidationException.InvalidValueException(key)
            }

            list
        }
    }
)

fun Parameters.getLong(
    key: String,
    defaultValue: Long,
    desiredRange: LongRange? = null
): Long = getLong(key, desiredRange) ?: defaultValue

fun Parameters.getLong(key: String, range: LongRange? = null): Long? =
    runCatching {
        getLongOrThrow(key)
    }.fold(
        onSuccess = { number ->
            range?.let {
                if (number !in range) {
                    throw ValidationException.InvalidRangeException(
                        key = key,
                        min = range.first,
                        max = range.last,
                        number = number
                    )
                }
            }

            number
        },
        onFailure = { null }
    )

fun Parameters.getLongOrThrow(key: String): Long = getOrThrow(
    key = key,
    mapper = { it.toLongOrNull() ?: throw ValidationException.InvalidValueException(key) }
)

fun Parameters.getInt(
    key: String,
    defaultValue: Int,
    desiredRange: IntRange? = null
): Int = getInt(key, desiredRange) ?: defaultValue

fun Parameters.getInt(key: String, range: IntRange? = null): Int? =
    runCatching {
        getIntOrThrow(key)
    }.fold(
        onSuccess = { number ->
            range?.let {
                if (number !in range) {
                    throw ValidationException.InvalidRangeException(
                        key = key,
                        min = range.first,
                        max = range.last,
                        number = number
                    )
                }
            }

            number
        },
        onFailure = { null }
    )

fun Parameters.getIntOrThrow(key: String): Int = getOrThrow(
    key = key,
    mapper = { it.toIntOrNull() ?: throw ValidationException.InvalidValueException(key) }
)

fun <T> Parameters.getOrThrow(key: String, mapper: (String) -> T): T = getOrThrow(
    key = key,
    mapper = mapper,
    message = { "$key is empty" }
)

fun <T> Parameters.getOrThrow(key: String, mapper: (String) -> T, message: () -> String): T =
    mapper(this[key] ?: throw ValidationException.InvalidException(message()))


// TODO: 11/06/2024, Danil Nikolaev: когда-нибудь реализовать регистрацию и закинуть туда
fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
}

fun checkPassword(plaintext: String, hashed: String): Boolean {
    return runCatching {
        BCrypt.checkpw(plaintext, hashed)
    }.fold(
        onSuccess = { it },
        onFailure = { false }
    )
}
