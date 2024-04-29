package com.meloda.kubsau.route.works

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getInt
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getOrThrow
import com.meloda.kubsau.common.getString
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
            editWork()
            deleteWorkById()
            deleteWorksByIds()
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

        val disciplineId = parameters.getIntOrThrow("disciplineId")
        val studentId = parameters.getIntOrThrow("studentId")
        val registrationDate = parameters.getIntOrThrow("registrationDate")
        val title = parameters.getString("title")
        val workTypeId = parameters.getIntOrThrow("workTypeId")

        val created = worksDao.addNewWork(
            disciplineId = disciplineId,
            studentId = studentId,
            registrationDate = registrationDate * 1000L,
            title = title,
            workTypeId = workTypeId
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editWork() {
    val worksDao by inject<WorksDao>()

    patch("{id}") {
        val workId = call.parameters.getIntOrThrow("id")
        val currentWork = worksDao.singleWork(workId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val disciplineId = parameters.getInt("disciplineId")
        val studentId = parameters.getInt("studentId")
        val registrationDate = parameters.getInt("registrationDate")
        val title = parameters.getString("title")
        val workTypeId = parameters.getInt("workTypeId")

        worksDao.updateWork(
            workId = workId,
            disciplineId = disciplineId ?: currentWork.disciplineId,
            studentId = studentId ?: currentWork.studentId,
            registrationDate = registrationDate?.let { it * 1000L } ?: currentWork.registrationDate,
            title = if ("title" in parameters) title else currentWork.title,
            workTypeId = workTypeId ?: currentWork.workTypeId
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteWorkById() {
    val worksDao by inject<WorksDao>()

    delete("{id}") {
        val workId = call.parameters.getIntOrThrow("id")
        worksDao.singleWork(workId) ?: throw ContentNotFoundException

        if (worksDao.deleteWork(workId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteWorksByIds() {
    val worksDao by inject<WorksDao>()

    delete {
        val workIds = call.request.queryParameters.getOrThrow("workIds")
            .split(",")
            .map(String::trim)
            .mapNotNull(String::toIntOrNull)

        if (workIds.isEmpty()) {
            throw ValidationException("workIds is invalid")
        }

        val currentWorks = worksDao.allWorksByIds(workIds)
        if (currentWorks.isEmpty()) {
            throw ContentNotFoundException
        }

        if (worksDao.deleteWorks(workIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
