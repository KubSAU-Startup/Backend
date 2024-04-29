package com.meloda.kubsau.route.disciplines

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.disciplinesRoutes() {
    authenticate {
        route("/disciplines") {
            getDisciplines()
            getDisciplineById()
            addDiscipline()
            editDiscipline()
            deleteDisciplineById()
            deleteDisciplinesByIds()
        }
    }
}

private fun Route.getDisciplines() {
    val disciplinesDao by inject<DisciplinesDao>()

    get {
        val disciplineIds = call.request.queryParameters["disciplineIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val disciplines = if (disciplineIds.isEmpty()) {
            disciplinesDao.allDisciplines()
        } else {
            disciplinesDao.allDisciplinesByIds(disciplineIds)
        }

        respondSuccess { disciplines }
    }
}

private fun Route.getDisciplineById() {
    val disciplinesDao by inject<DisciplinesDao>()

    get("{id}") {
        val disciplineId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val discipline = disciplinesDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

        respondSuccess { discipline }
    }
}

private fun Route.addDiscipline() {
    val disciplinesDao by inject<DisciplinesDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim() ?: throw ValidationException("title is empty")
        val workTypeId = parameters["workTypeId"]?.toIntOrNull() ?: throw ValidationException("workTypeId is empty")

        val created = disciplinesDao.addNewDiscipline(
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

private fun Route.editDiscipline() {
    val disciplinesDao by inject<DisciplinesDao>()

    patch("{id}") {
        val disciplineId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val currentDiscipline = disciplinesDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim()
        val workTypeId = parameters["workTypeId"]?.toIntOrNull()

        disciplinesDao.updateDiscipline(
            disciplineId = disciplineId,
            title = title ?: currentDiscipline.title,
            workTypeId = workTypeId ?: currentDiscipline.workTypeId
        ).let { changedCount ->
            if (changedCount == 1) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteDisciplineById() {
    val disciplinesDao by inject<DisciplinesDao>()

    delete("{id}") {
        val disciplineId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        disciplinesDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

        if (disciplinesDao.deleteDiscipline(disciplineId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteDisciplinesByIds() {
    val disciplinesDao by inject<DisciplinesDao>()

    delete {
        val disciplineIds = call.request.queryParameters["disciplineIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: throw ValidationException("disciplineIds is empty")

        val currentDisciplines = disciplinesDao.allDisciplinesByIds(disciplineIds)
        if (currentDisciplines.isEmpty()) {
            throw ContentNotFoundException
        }

        if (disciplinesDao.deleteDisciplines(disciplineIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
