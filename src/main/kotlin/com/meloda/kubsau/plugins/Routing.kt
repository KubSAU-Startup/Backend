package com.meloda.kubsau.plugins

import com.meloda.kubsau.base.Constants
import com.meloda.kubsau.route.account.account
import com.meloda.kubsau.route.auth.auth
import com.meloda.kubsau.route.department.departments
import com.meloda.kubsau.route.journal.journals
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val PORT = 8080

fun configureServer() {
    val server = embeddedServer(Netty, PORT) {
        install(AutoHeadResponse)
        install(CORS) {
            anyHost()

            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Delete)

            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Accept)
            allowHeader(HttpHeaders.Authorization)
        }

        configureExceptions()
        configureContentNegotiation()

        routing()
    }

    println("Server is working on port: $PORT")

    server.start(wait = true)
}

private fun Application.routing() {
    routing {
        get("/") {
            call.respondText {
                "Server is working.\nVersion: ${Constants.BACKEND_VERSION}"
            }
        }

        auth()
        account()
        journals()
        departments()
    }
}
