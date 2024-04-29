package com.meloda.kubsau.route.worktypes

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
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
    val workTypesDao by inject<WorkTypesDao>()

    get {
        val workTypeIds = call.request.queryParameters["workTypeIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val workTypes = if (workTypeIds.isEmpty()) {
            workTypesDao.allWorkTypes()
        } else {
            workTypesDao.allWorkTypesByIds(workTypeIds)
        }

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

private fun Route.addWorkType() {
    val workTypesDao by inject<WorkTypesDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters.getOrThrow("title")
        val needTitle = parameters.getBooleanOrThrow("needTitle")

        val created = workTypesDao.addNewWorkType(
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
    val workTypesDao by inject<WorkTypesDao>()

    patch("{id}") {
        val workTypeId = call.parameters.getIntOrThrow("id")
        val currentWorkType = workTypesDao.singleWorkType(workTypeId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters.getString("title")
        val needTitle = parameters.getBoolean("needTitle")

        workTypesDao.updateWorkType(
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
    val workTypesDao by inject<WorkTypesDao>()

    delete("{id}") {
        val workTypeId = call.parameters.getIntOrThrow("id")
        workTypesDao.singleWorkType(workTypeId) ?: throw ContentNotFoundException

        if (workTypesDao.deleteWorkType(workTypeId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteWorkTypesByIds() {
    val workTypesDao by inject<WorkTypesDao>()

    delete {
        val workTypeIds = call.request.queryParameters.getOrThrow("workTypeIds")
            .split(",")
            .map(String::trim)
            .mapNotNull(String::toIntOrNull)

        if (workTypeIds.isEmpty()) {
            throw ValidationException("workTypeIds is invalid")
        }

        val currentWorkTypes = workTypesDao.allWorkTypesByIds(workTypeIds)
        if (currentWorkTypes.isEmpty()) {
            throw ContentNotFoundException
        }

        if (workTypesDao.deleteWorkTypes(workTypeIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
