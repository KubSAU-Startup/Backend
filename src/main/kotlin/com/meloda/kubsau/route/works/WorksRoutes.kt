package com.meloda.kubsau.route.works

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.works.WorksDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.worksRoutes() {
    authenticate {
        route("/works") {
            getAllWorks()
        }
    }
}

private fun Route.getAllWorks() {
    val worksDao by inject<WorksDao>()

    get {
        val works = worksDao.allWorks()

        respondSuccess { works }
    }
}
