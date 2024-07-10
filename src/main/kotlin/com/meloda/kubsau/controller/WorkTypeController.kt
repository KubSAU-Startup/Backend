package com.meloda.kubsau.controller

import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.getIntList
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.database.worktypes.WorkTypeDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.respondSuccess
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class WorkTypeController : BaseController {

    override fun Route.routes() {
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
}
