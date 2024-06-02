package com.meloda.kubsau.route.directivities

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.directivities.DirectivitiesDao
import com.meloda.kubsau.database.grades.GradesDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.heads.HeadsDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.model.Directivity
import com.meloda.kubsau.model.Grade
import com.meloda.kubsau.model.Head
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.directivitiesRoutes() {
    authenticate {
        route("/directivities") {
            getDirectivities()
            getDirectivityById()
            getGroups()
            addDirectivity()
            editDirectivity()
            deleteDirectivity()
            deleteDirectivities()
        }
    }
}

private data class DirectivitiesResponse(
    val directivities: List<Directivity>
)

private data class FullDirectivitiesResponse(
    val directivities: List<Directivity>,
    val heads: List<Head>,
    val grades: List<Grade>
)

private fun Route.getDirectivities() {
    val directivitiesDao by inject<DirectivitiesDao>()
    val headsDao by inject<HeadsDao>()
    val gradesDao by inject<GradesDao>()

    get {
        val parameters = call.request.queryParameters

        val directivityIds = parameters.getIntList(
            key = "directivityIds",
            defaultValue = emptyList(),
            maxSize = MAX_ITEMS_SIZE
        )

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt(key = "limit", range = LimitRange)
        val extended = parameters.getBoolean("extended", false)

        val directivities = if (directivityIds.isEmpty()) {
            directivitiesDao.allDirectivities(offset, limit ?: MAX_ITEMS_SIZE)
        } else {
            directivitiesDao.allDirectivitiesByIds(directivityIds)
        }

        if (!extended) {
            respondSuccess { DirectivitiesResponse(directivities = directivities) }
        } else {
            val headIds = directivities.map(Directivity::headId)
            val heads = headsDao.allHeadsByIds(headIds)

            val gradeIds = directivities.map(Directivity::gradeId)
            val grades = gradesDao.allGradesByIds(gradeIds)

            respondSuccess {
                FullDirectivitiesResponse(
                    directivities = directivities,
                    heads = heads,
                    grades = grades
                )
            }
        }
    }
}

private data class DirectivityResponse(
    val directivity: Directivity
)

private data class FullDirectivityResponse(
    val directivity: Directivity,
    val head: Head,
    val grade: Grade
)

private fun Route.getDirectivityById() {
    val directivitiesDao by inject<DirectivitiesDao>()
    val headsDao by inject<HeadsDao>()
    val gradesDao by inject<GradesDao>()

    get("{id}") {
        val directivityId = call.parameters.getIntOrThrow("id")
        val extended = call.request.queryParameters.getBoolean("extended", false)

        val directivity = directivitiesDao.singleDirectivity(directivityId) ?: throw ContentNotFoundException

        if (!extended) {
            respondSuccess { DirectivityResponse(directivity = directivity) }
        } else {
            val head = headsDao.singleHead(directivity.headId)
            val grade = gradesDao.singleGradeById(directivity.gradeId)

            if (head == null || grade == null) {
                throw ContentNotFoundException
            }

            respondSuccess {
                FullDirectivityResponse(
                    directivity = directivity,
                    head = head,
                    grade = grade
                )
            }
        }
    }
}

private fun Route.getGroups() {
    val directivitiesDao by inject<DirectivitiesDao>()
    val groupsDao by inject<GroupsDao>()

    get("{id}/groups") {
        val directivityId = call.parameters.getIntOrThrow("id")
        directivitiesDao.singleDirectivity(directivityId) ?: throw ContentNotFoundException

        val groups = groupsDao.allGroupsByDirectivity(directivityId)
        respondSuccess { groups }
    }
}

private fun Route.addDirectivity() {
    val directivitiesDao by inject<DirectivitiesDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters.getStringOrThrow("title")
        val headId = parameters.getIntOrThrow("headId")
        val gradeId = parameters.getIntOrThrow("gradeId")

        val created = directivitiesDao.addNewDirectivity(
            title = title,
            headId = headId,
            gradeId = gradeId
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editDirectivity() {
    val directivitiesDao by inject<DirectivitiesDao>()

    patch("{id}") {
        val directivityId = call.parameters.getIntOrThrow("id")
        val currentDirectivity = directivitiesDao.singleDirectivity(directivityId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters.getString("title")
        val headId = parameters.getInt("headId")
        val gradeId = parameters.getInt("gradeId")

        directivitiesDao.updateDirectivity(
            directivityId = directivityId,
            title = title ?: currentDirectivity.title,
            headId = headId ?: currentDirectivity.headId,
            gradeId = gradeId ?: currentDirectivity.gradeId
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteDirectivity() {
    val directivitiesDao by inject<DirectivitiesDao>()

    delete("{id}") {
        val directivityId = call.parameters.getIntOrThrow("id")
        directivitiesDao.singleDirectivity(directivityId) ?: throw ContentNotFoundException

        if (directivitiesDao.deleteDirectivity(directivityId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteDirectivities() {
    val directivitiesDao by inject<DirectivitiesDao>()

    delete {
        val directivityIds = call.request.queryParameters.getIntListOrThrow(
            key = "directivityIds",
            requiredNotEmpty = true
        )

        val currentDirectivities = directivitiesDao.allDirectivitiesByIds(directivityIds)
        if (currentDirectivities.isEmpty()) {
            throw ContentNotFoundException
        }

        if (directivitiesDao.deleteDirectivities(directivityIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
