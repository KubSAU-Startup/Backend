package com.meloda.kubsau

data class ApiResponse<T>(
    val response: T?,
    val error: ApiError?,
    val success: Boolean
) {

    companion object {
        fun <T> success(response: T): ApiResponse<T> {
            return ApiResponse(
                response = response,
                error = null,
                success = true
            )
        }

        fun error(error: ApiError): ApiResponse<Nothing> {
            return ApiResponse(
                response = null,
                error = error,
                success = false
            )
        }
    }
}

data class ApiError(val code: Int, val message: String)
