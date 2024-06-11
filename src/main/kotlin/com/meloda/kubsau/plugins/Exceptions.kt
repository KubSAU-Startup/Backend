package com.meloda.kubsau.plugins

import com.meloda.kubsau.model.*
import com.meloda.kubsau.route.auth.WrongCredentialsException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*

fun Application.configureExceptions() {
    install(StatusPages) {
        exception<Throwable> { call, throwable ->
            when (throwable) {
                is UnknownException -> {
                    respondError(call) {
                        ApiError(Errors.UNKNOWN_ERROR, "Unknown error")
                    }
                }

                is AccessDeniedException -> {
                    respondError(call) {
                        ApiError(Errors.ACCESS_DENIED, throwable.message)
                    }
                }

                is ValidationException -> {
                    respondError(call = call, status = HttpStatusCode.BadRequest) {
                        ApiError(Errors.BAD_REQUEST, "Bad request: ${throwable.message}")
                    }
                }

                is WrongCredentialsException -> {
                    respondError(call) {
                        ApiError(Errors.WRONG_CREDENTIALS, "Wrong credentials")
                    }
                }

                is ContentNotFoundException -> {
                    respondError(call) {
                        ApiError(Errors.CONTENT_NOT_FOUND, throwable.message ?: "Content not found")
                    }
                }

                is WrongCurrentPasswordException -> {
                    respondError(call) {
                        ApiError(Errors.WRONG_PASSWORD, throwable.message ?: "Wrong current password")
                    }
                }

                is WrongTokenFormatException -> {
                    respondError(call) {
                        ApiError(Errors.WRONG_TOKEN_FORMAT, throwable.message ?: "Wrong token format")
                    }
                }

                is UnknownTokenException -> {
                    respondError(call) {
                        ApiError(Errors.UNKNOWN_TOKEN_ERROR, throwable.message ?: "Unknown token error")
                    }
                }
            }
        }

        status(
            HttpStatusCode.InternalServerError
        ) { call, statusCode ->
            when (statusCode) {
                HttpStatusCode.InternalServerError -> {
                    respondError(call = call, status = statusCode) {
                        ApiError(code = Errors.UNKNOWN_ERROR, "Unknown error")
                    }
                }
            }
        }
    }
}
