package com.meloda.kubsau.common

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
