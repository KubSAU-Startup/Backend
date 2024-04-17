package com.meloda.kubsau.route.teachers

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.teachers.TeachersDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.teachersRoutes() {
    authenticate {
        route("/teachers") {
            getAllTeachers()
        }
    }
}

private fun Route.getAllTeachers() {
    val teachersDao by inject<TeachersDao>()

    get {
        val teachers = teachersDao.allTeachers()

        respondSuccess { teachers }
    }
}
