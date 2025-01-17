package com.meloda.kubsau.controller

import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.faculties.FacultyDao
import com.meloda.kubsau.model.*
import com.meloda.kubsau.repository.HeadRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class HeadController(private val repository: HeadRepository) : BaseController {

    override fun Route.routes() {
        authenticate {
            route("/heads") {
                getHeads()
            }
        }
    }

    private data class HeadsResponse(val heads: List<Head>)

    private data class FullHeadsResponse(
        val heads: List<Head>,
        val faculties: List<Faculty>
    )

    private fun Route.getHeads() {
        val facultyDao by inject<FacultyDao>()

        get {
            val principal = call.userPrincipal()

            if (principal.type != Employee.TYPE_ADMIN || principal.facultyId == null) {
                throw AccessDeniedException("Admin rights required")
            }

            val parameters = call.request.queryParameters

            val headIds = parameters.getIntList(
                key = "headIds",
                defaultValue = emptyList(),
                maxSize = MAX_ITEMS_SIZE
            )

            val offset = parameters.getInt("offset")
            val limit = parameters.getInt(key = "limit", range = LimitRange)
            val extended = parameters.getBoolean("extended", false)

            val heads = if (headIds.isEmpty()) {
                repository.getAllHeads(
                    facultyId = principal.facultyId,
                    offset = offset,
                    limit = limit ?: MAX_ITEMS_SIZE
                )
            } else {
                repository.getHeadsByIds(headIds)
            }

            if (headIds.isNotEmpty() && heads.map(Head::facultyId).distinct().singleOrNull() != principal.facultyId) {
                val unavailableHeadIds =
                    heads.filter { head -> head.facultyId != principal.facultyId }.map(Head::id)
                throw AccessDeniedException("Unavailable headIds: ${unavailableHeadIds.joinToString()}")
            }

            if (!extended) {
                respondSuccess { HeadsResponse(heads = heads) }
            } else {
                val facultyIds = heads.map(Head::facultyId)
                val faculties = facultyDao.allFacultiesByIds(facultyIds)

                respondSuccess {
                    FullHeadsResponse(
                        heads = heads,
                        faculties = faculties
                    )
                }
            }
        }
    }
}
