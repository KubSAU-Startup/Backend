package com.meloda.kubsau.route.directivities

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.directivities.DirectivitiesDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.directivitiesRoutes() {
    authenticate {
        route("/directivities") {
            getAllDirectivities()
        }
    }
}

private fun Route.getAllDirectivities() {
    val directivitiesDao by inject<DirectivitiesDao>()

    get {
        val specializations = directivitiesDao.allDirectivities()

        respondSuccess { specializations }
    }
}
