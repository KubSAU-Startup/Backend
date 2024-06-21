package com.meloda.kubsau.route.worktypes

import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.worktypes.WorkTypeDao
import com.meloda.kubsau.model.ContentNotFoundException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.workTypesRoutes() {
    authenticate {
        route("/worktypes") {
            getWorkTypes()
            getWorkTypeById()
        }
    }
}

private fun Route.getWorkTypes() {
    val workTypeDao by inject<WorkTypeDao>()

    get {
        val workTypeIds = call.request.queryParameters.getIntList(
            key = "workTypeIds",
            defaultValue = emptyList()
        )

        val workTypes = if (workTypeIds.isEmpty()) {
            workTypeDao.allWorkTypes()
        } else {
            workTypeDao.allWorkTypesByIds(workTypeIds)
        }

        respondSuccess { workTypes }
    }
}

private fun Route.getWorkTypeById() {
    val workTypeDao by inject<WorkTypeDao>()

    get("{id}") {
        val workTypeId = call.parameters.getIntOrThrow("id")
        val workType = workTypeDao.singleWorkType(workTypeId) ?: throw ContentNotFoundException

        respondSuccess { workType }
    }
}
