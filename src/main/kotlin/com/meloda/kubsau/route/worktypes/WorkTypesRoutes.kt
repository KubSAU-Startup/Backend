package com.meloda.kubsau.route.worktypes

import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.worktypes.WorkTypeDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.UnknownException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.workTypesRoutes() {
    authenticate {
        route("/worktypes") {
            getWorkTypes()
            getWorkTypeById()
            addWorkType()
            editWorkType()
            deleteWorkTypeById()
            deleteWorkTypesByIds()
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

private fun Route.addWorkType() {
    val workTypeDao by inject<WorkTypeDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters.getStringOrThrow("title")
        val needTitle = parameters.getBooleanOrThrow("needTitle")

        val created = workTypeDao.addNewWorkType(
            title = title,
            needTitle = needTitle
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editWorkType() {
    val workTypeDao by inject<WorkTypeDao>()

    patch("{id}") {
        val workTypeId = call.parameters.getIntOrThrow("id")
        val currentWorkType = workTypeDao.singleWorkType(workTypeId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters.getString("title")
        val needTitle = parameters.getBoolean("needTitle")

        workTypeDao.updateWorkType(
            workTypeId = workTypeId,
            title = title ?: currentWorkType.title,
            needTitle = needTitle ?: currentWorkType.needTitle
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteWorkTypeById() {
    val workTypeDao by inject<WorkTypeDao>()

    delete("{id}") {
        val workTypeId = call.parameters.getIntOrThrow("id")
        workTypeDao.singleWorkType(workTypeId) ?: throw ContentNotFoundException

        if (workTypeDao.deleteWorkType(workTypeId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteWorkTypesByIds() {
    val workTypeDao by inject<WorkTypeDao>()

    delete {
        val workTypeIds = call.request.queryParameters.getIntListOrThrow(
            key = "workTypeIds",
            requiredNotEmpty = true
        )

        val currentWorkTypes = workTypeDao.allWorkTypesByIds(workTypeIds)
        if (currentWorkTypes.isEmpty()) {
            throw ContentNotFoundException
        }

        if (workTypeDao.deleteWorkTypes(workTypeIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
