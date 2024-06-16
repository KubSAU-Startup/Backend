package com.meloda.kubsau.controller

import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.grades.GradeDao
import com.meloda.kubsau.database.heads.HeadDao
import com.meloda.kubsau.model.*
import com.meloda.kubsau.service.DirectivityService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class DirectivityController(private val service: DirectivityService) {

    context(Route)
    private fun routes() {
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
        // TODO: 17/06/2024, Danil Nikolaev: stop using
        val headDao by inject<HeadDao>()
        val gradeDao by inject<GradeDao>()

        get {
            val principal = call.userPrincipal()
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
                service.getAllDirectivities(principal.facultyId, offset, limit ?: MAX_ITEMS_SIZE)
            } else {
                service.getDirectivitiesByIds(directivityIds)
            }

            if (!extended) {
                respondSuccess { DirectivitiesResponse(directivities = directivities) }
            } else {
                val headIds = directivities.map(Directivity::headId)
                val heads = headDao.allHeadsByIds(headIds)

                val gradeIds = directivities.map(Directivity::gradeId)
                val grades = gradeDao.allGradesByIds(gradeIds)

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
        // TODO: 17/06/2024, Danil Nikolaev: stop using
        val headDao by inject<HeadDao>()
        val gradeDao by inject<GradeDao>()

        get("{id}") {
            val directivityId = call.parameters.getIntOrThrow("id")
            val extended = call.request.queryParameters.getBoolean("extended", false)

            val directivity = service.getDirectivityById(directivityId) ?: throw ContentNotFoundException

            if (!extended) {
                respondSuccess { DirectivityResponse(directivity = directivity) }
            } else {
                val head = headDao.singleHead(directivity.headId)
                val grade = gradeDao.singleGradeById(directivity.gradeId)

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
        get("{id}/groups") {
            val directivityId = call.parameters.getIntOrThrow("id")

            // TODO: 17/06/2024, Danil Nikolaev: implement isExist()
            service.getDirectivityById(directivityId) ?: throw ContentNotFoundException

            val groups = service.getGroupsInDirectivity(directivityId)
            respondSuccess { groups }
        }
    }

    private fun Route.addDirectivity() {
        post {
            val parameters = call.receiveParameters()

            val title = parameters.getStringOrThrow("title")
            val headId = parameters.getIntOrThrow("headId")
            val gradeId = parameters.getIntOrThrow("gradeId")

            val created = service.addDirectivity(
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
        patch("{id}") {
            val directivityId = call.parameters.getIntOrThrow("id")
            val currentDirectivity = service.getDirectivityById(directivityId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val title = parameters.getString("title")
            val headId = parameters.getInt("headId")
            val gradeId = parameters.getInt("gradeId")

            service.editDirectivity(
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
        delete("{id}") {
            val directivityId = call.parameters.getIntOrThrow("id")

            // TODO: 17/06/2024, Danil Nikolaev: implement isExist()
            service.getDirectivityById(directivityId) ?: throw ContentNotFoundException

            if (service.deleteDirectivity(directivityId)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }

    private fun Route.deleteDirectivities() {
        delete {
            val directivityIds = call.request.queryParameters.getIntListOrThrow(
                key = "directivityIds",
                requiredNotEmpty = true
            )

            val currentDirectivities = service.getDirectivitiesByIds(directivityIds)
            if (currentDirectivities.isEmpty()) {
                throw ContentNotFoundException
            }

            if (service.deleteDirectivities(directivityIds)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }

    companion object {
        context(Route)
        fun routes() {
            val controller by inject<DirectivityController>()
            controller.routes()
        }
    }
}
