package com.meloda.kubsau.api

import com.meloda.kubsau.api.model.ApiError
import com.meloda.kubsau.api.model.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend fun <T> PipelineContext<Unit, ApplicationCall>.respondSuccess(
    status: HttpStatusCode = HttpStatusCode.OK, response: () -> T
) = respondSuccess(
    call = call,
    status = status,
    response = response
)

suspend fun PipelineContext<Unit, ApplicationCall>.respondError(
    status: HttpStatusCode = HttpStatusCode.OK, error: () -> ApiError
) = respondError(
    call = call,
    status = status,
    error = error
)

suspend fun <T> respondSuccess(
    call: ApplicationCall,
    status: HttpStatusCode = HttpStatusCode.OK,
    response: () -> T
) {
    respond(
        call = call,
        status = status,
        message = ApiResponse.success(response.invoke())
    )
}

suspend fun respondError(
    call: ApplicationCall,
    status: HttpStatusCode = HttpStatusCode.OK,
    error: () -> ApiError
) {
    respond(
        call = call,
        status = status,
        message = ApiResponse.error(error.invoke())
    )
}

private suspend inline fun <reified T : Any> respond(
    call: ApplicationCall,
    status: HttpStatusCode = HttpStatusCode.OK,
    message: T
) {
    call.respond(
        status = status,
        message = message
    )
}
