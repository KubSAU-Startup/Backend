package com.meloda.kubsau.plugins

import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.route.account.account
import com.meloda.kubsau.route.auth.auth
import com.meloda.kubsau.route.department.departments
import com.meloda.kubsau.route.journal.journals
import com.meloda.kubsau.route.qr.qr
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.routing() {
    routing {
//        swaggerUI(path = "/docs", swaggerFile = "docs/openapi.yaml")

        get("/") {
            call.respondText {
                "Server is working.\nVersion: ${Constants.BACKEND_VERSION}"
            }
        }

        auth()
        account()
        journals()
        departments()
        qr()

    }
}
