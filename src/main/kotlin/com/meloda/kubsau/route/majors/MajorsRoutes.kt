package com.meloda.kubsau.route.majors

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.majors.MajorsDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.majors() {
    authenticate {
        route("/majors") {
            getAllMajors()
        }
    }
}

private fun Route.getAllMajors() {
    val majorsDao by inject<MajorsDao>()

    get {
        val majors = majorsDao.allMajors()

        respondSuccess { majors }
    }
}
