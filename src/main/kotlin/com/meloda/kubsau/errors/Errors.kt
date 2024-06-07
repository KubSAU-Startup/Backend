package com.meloda.kubsau.errors

object Errors {
    const val UNKNOWN_ERROR = 1
    const val UNKNOWN_METHOD = 2
    const val BAD_REQUEST = 3
    const val ACCESS_DENIED = 4

    const val WRONG_CREDENTIALS = 101
    const val ACCESS_TOKEN_REQUIRED = 102
    const val SESSION_EXPIRED = 103
    const val WRONG_PASSWORD = 104
    const val UNAVAILABLE_DEPARTMENT_ID = 105

    const val CONTENT_NOT_FOUND = 1001
}

data object UnknownException : Throwable()
data object NoAccessTokenException : Throwable()
data object SessionExpiredException : Throwable()
data object WrongCurrentPasswordException : Throwable()
data object UnavailableDepartmentId : Throwable()
sealed class ValidationException(override val message: String) : Throwable() {
    data class InvalidException(override val message: String) : ValidationException(message)
    data class InvalidValueException(val key: String) : ValidationException("$key is invalid")
    data class EmptyItemException(val key: String) : ValidationException("$key is invalid. Must not be empty")

    data class EmptyOrBlankException(
        val key: String
    ) : ValidationException("$key is invalid. Must not be empty or blank")

    data class InvalidRangeException(
        val key: String,
        val min: Number,
        val max: Number,
        val number: Number
    ) : ValidationException("$key is invalid. Required range: $min...$max, current value: $number")

    data class InvalidSizeException(
        val key: String,
        val maxSize: Int,
        val actualSize: Int
    ) : ValidationException("$key is invalid. Max size: $maxSize, actual size: $actualSize")
}

data object ContentNotFoundException : Throwable()
