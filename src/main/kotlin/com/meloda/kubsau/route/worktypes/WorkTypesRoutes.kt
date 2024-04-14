package com.meloda.kubsau.route.worktypes

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.workTypes() {
    authenticate {
        route("/worktypes") {
            getAllWorkTypes()
            getWorkTypeById()
        }
    }
}

private fun Route.getAllWorkTypes() {
    val workTypesDao by inject<WorkTypesDao>()

    get {
        val workTypes = workTypesDao.allWorkTypes()

        respondSuccess { workTypes }
    }
}

private fun Route.getWorkTypeById() {
    val workTypesDao by inject<WorkTypesDao>()

    get("{id}") {
        val workTypeId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val workType = workTypesDao.singleWorkType(workTypeId) ?: throw ContentNotFoundException

        respondSuccess { workType }
    }
}
