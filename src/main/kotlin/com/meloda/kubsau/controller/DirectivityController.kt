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

    companion object {
        context(Route)
        fun routes() {
            val controller by inject<DirectivityController>()
            controller.routes()
        }
    }
}
