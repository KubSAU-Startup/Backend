package com.meloda.kubsau.errors

object Errors {
    const val UNKNOWN_ERROR = 1
    const val UNKNOWN_METHOD = 2
    const val BAD_REQUEST = 3
    const val ACCESS_DENIED = 4

    const val WRONG_CREDENTIALS = 101
    const val ACCESS_TOKEN_REQUIRED = 102
    const val SESSION_EXPIRED = 103
}

data object UnknownException : Throwable()
data object NoAccessTokenException : Throwable()
data object SessionExpiredException : Throwable()
data class ValidationException(override val message: String) : Throwable()
