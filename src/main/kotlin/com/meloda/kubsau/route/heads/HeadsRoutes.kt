package com.meloda.kubsau.route.heads

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.heads.HeadsDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.majorsRoutes() {
    authenticate {
        route("/heads") {
            getAllHeads()
        }
    }
}

private fun Route.getAllHeads() {
    val headsDao by inject<HeadsDao>()

    get {
        val majors = headsDao.allHeads()

        respondSuccess { majors }
    }
}
