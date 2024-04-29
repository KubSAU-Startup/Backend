package com.meloda.kubsau.route.disciplines

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getInt
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getOrThrow
import com.meloda.kubsau.common.getString
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
        val disciplineId = call.parameters.getIntOrThrow("id")
        val discipline = disciplinesDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

        respondSuccess { discipline }
    }
}

private fun Route.addDiscipline() {
    val disciplinesDao by inject<DisciplinesDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters.getOrThrow("title")
        val departmentId = parameters.getIntOrThrow("departmentId")

        val created = disciplinesDao.addNewDiscipline(
            title = title,
            departmentId = departmentId
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
        val disciplineId = call.parameters.getIntOrThrow("id")
        val currentDiscipline = disciplinesDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters.getString("title")
        val departmentId = parameters.getInt("departmentId")

        disciplinesDao.updateDiscipline(
            disciplineId = disciplineId,
            title = title ?: currentDiscipline.title,
            departmentId = departmentId ?: currentDiscipline.departmentId
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
        val disciplineId = call.parameters.getIntOrThrow("id")
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
        val disciplineIds = call.request.queryParameters.getOrThrow("disciplineIds")
            .split(",")
            .map(String::trim)
            .mapNotNull(String::toIntOrNull)

        if (disciplineIds.isEmpty()) {
            throw ValidationException("disciplineIds is invalid")
        }

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
