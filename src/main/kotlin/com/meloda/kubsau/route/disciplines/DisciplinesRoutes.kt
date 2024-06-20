package com.meloda.kubsau.route.disciplines

import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.disciplines.DisciplineDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.respondSuccess
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.disciplinesRoutes() {
    authenticate {
        route("/disciplines") {
            getDisciplines()
            getDisciplineById()
        }
    }
}

private fun Route.getDisciplines() {
    val disciplineDao by inject<DisciplineDao>()

    get {
        val principal = call.userPrincipal()
        val disciplineIds = call.request.queryParameters.getIntList(
            key = "disciplineIds",
            defaultValue = emptyList()
        )

        val disciplines = if (disciplineIds.isEmpty()) {
            disciplineDao.allDisciplines(principal.departmentIds)
        } else {
            disciplineDao.allDisciplinesByIds(disciplineIds)
            // TODO: 20/06/2024, Danil Nikolaev: check access
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
