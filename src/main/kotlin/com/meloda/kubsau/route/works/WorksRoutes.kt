package com.meloda.kubsau.route.works

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.works.WorksDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.worksRoutes() {
    authenticate {
        route("/works") {
            getWorks()
            getWorkById()
            addWork()
        }
    }
}

private fun Route.getWorks() {
    val worksDao by inject<WorksDao>()

    get {
        val workIds = call.request.queryParameters["workIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val works = if (workIds.isEmpty()) {
            worksDao.allWorks()
        } else {
            worksDao.allWorksByIds(workIds)
        }

        respondSuccess { works }
    }
}

private fun Route.getWorkById() {
    val worksDao by inject<WorksDao>()

    get("{id}") {
        val workId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val work = worksDao.singleWork(workId) ?: throw ContentNotFoundException

        respondSuccess { work }
    }
}

private fun Route.addWork() {
    val worksDao by inject<WorksDao>()

    post {
        val parameters = call.receiveParameters()

        val disciplineId =
            parameters["disciplineId"]?.toIntOrNull() ?: throw ValidationException("disciplineId is empty")
        val studentId =
            parameters["studentId"]?.toIntOrNull() ?: throw ValidationException("studentId is empty")
        val registrationDate =
            parameters["registrationDate"]?.toIntOrNull() ?: throw ValidationException("registrationDate is empty")
        val title = parameters["title"]?.trim()

        val created = worksDao.addNewWork(
            disciplineId = disciplineId,
            studentId = studentId,
            registrationDate = registrationDate * 1000L,
            title = title
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}
