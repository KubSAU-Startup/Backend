package com.meloda.kubsau.route.worktypes

import com.meloda.kubsau.api.respondSuccess
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

        val title = parameters["title"]?.trim() ?: throw ValidationException("title is empty")
        val isEditable =
            parameters["isEditable"]?.toBooleanStrictOrNull() ?: throw ValidationException("isEditable is empty")

        val created = workTypesDao.addNewWorkType(
            title = title,
            isEditable = isEditable
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
        val workTypeId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val currentWorkType = workTypesDao.singleWorkType(workTypeId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim()
        val isEditable = parameters["isEditable"]?.toBooleanStrictOrNull()

        workTypesDao.updateWorkType(
            workTypeId = workTypeId,
            title = title ?: currentWorkType.title,
            isEditable = isEditable ?: currentWorkType.isEditable
        ).let { changedCount ->
            if (changedCount == 1) {
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
        val workTypeId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
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
        val workTypeIds = call.request.queryParameters["workTypeIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: throw ValidationException("workTypeIds is empty")

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
