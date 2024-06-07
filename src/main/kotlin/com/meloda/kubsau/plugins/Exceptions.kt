package com.meloda.kubsau.plugins

import com.meloda.kubsau.api.model.ApiError
import com.meloda.kubsau.api.respondError
import com.meloda.kubsau.errors.*
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

                is NoAccessTokenException -> {
                    respondError(call = call, status = HttpStatusCode.Unauthorized) {
                        ApiError(Errors.ACCESS_TOKEN_REQUIRED, "Access token required")
                    }
                }

                is SessionExpiredException -> {
                    respondError(call) {
                        ApiError(Errors.SESSION_EXPIRED, "Session expired")
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

                is UnavailableDepartmentId -> {
                    respondError(call) {
                        ApiError(Errors.UNAVAILABLE_DEPARTMENT_ID, throwable.message ?: "Unavailable department id")
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
