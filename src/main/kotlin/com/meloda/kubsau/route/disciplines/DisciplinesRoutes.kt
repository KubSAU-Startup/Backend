package com.meloda.kubsau.route.disciplines

import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.disciplines.DisciplineDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.UnknownException
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
    val disciplineDao by inject<DisciplineDao>()

    get {
        val disciplineIds = call.request.queryParameters.getIntList(
            key = "disciplineIds",
            defaultValue = emptyList()
        )

        val disciplines = if (disciplineIds.isEmpty()) {
            disciplineDao.allDisciplines()
        } else {
            disciplineDao.allDisciplinesByIds(disciplineIds)
        }

        respondSuccess { disciplines }
    }
}

private fun Route.getDisciplineById() {
    val disciplineDao by inject<DisciplineDao>()

    get("{id}") {
        val disciplineId = call.parameters.getIntOrThrow("id")
        val discipline = disciplineDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

        respondSuccess { discipline }
    }
}

private fun Route.addDiscipline() {
    val disciplineDao by inject<DisciplineDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters.getStringOrThrow("title")
        val departmentId = parameters.getIntOrThrow("departmentId")

        val created = disciplineDao.addNewDiscipline(
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
    val disciplineDao by inject<DisciplineDao>()

    patch("{id}") {
        val disciplineId = call.parameters.getIntOrThrow("id")
        val currentDiscipline = disciplineDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters.getString("title")
        val departmentId = parameters.getInt("departmentId")

        disciplineDao.updateDiscipline(
            disciplineId = disciplineId,
            title = title ?: currentDiscipline.title,
            departmentId = departmentId ?: currentDiscipline.departmentId
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteDisciplineById() {
    val disciplineDao by inject<DisciplineDao>()

    delete("{id}") {
        val disciplineId = call.parameters.getIntOrThrow("id")
        disciplineDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

        if (disciplineDao.deleteDiscipline(disciplineId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteDisciplinesByIds() {
    val disciplineDao by inject<DisciplineDao>()

    delete {
        val disciplineIds = call.request.queryParameters.getIntListOrThrow(
            key = "disciplineIds",
            requiredNotEmpty = true
        )

        val currentDisciplines = disciplineDao.allDisciplinesByIds(disciplineIds)
        if (currentDisciplines.isEmpty()) {
            throw ContentNotFoundException
        }

        if (disciplineDao.deleteDisciplines(disciplineIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
