package com.meloda.kubsau.route.programs

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.programs.ProgramsDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.programs() {
    authenticate {
        route("/programs") {
            getAllPrograms()
        }
    }
}

private fun Route.getAllPrograms() {
    val programsDao by inject<ProgramsDao>()

    get {
        val programs = programsDao.allPrograms()

        respondSuccess { programs }
    }
}
