package com.meloda.kubsau.route.disciplines

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.disciplines() {
    authenticate {
        route("/disciplines") {
            getDisciplineById()
        }
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
