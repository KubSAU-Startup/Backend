package com.meloda.kubsau.route.grades

import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.grades.GradeDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.UnknownException
import com.meloda.kubsau.model.Grade
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.gradesRoutes() {
    authenticate {
        route("/grades") {
            getGrades()
            getGradeById()
            addGrade()
            editGrade()
            deleteGradeById()
            deleteGradesByIds()
        }
    }
}

private data class GradesResponse(
    val count: Int,
    val offset: Int,
    val grades: List<Grade>
)

private fun Route.getGrades() {
    val gradeDao by inject<GradeDao>()

    get {
        val parameters = call.request.queryParameters

        val gradeIds = parameters.getIntList(
            key = "gradeIds",
            defaultValue = emptyList(),
            maxSize = MAX_ITEMS_SIZE
        )

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt(key = "limit", range = LimitRange)

        val grades = if (gradeIds.isEmpty()) {
            gradeDao.allGrades(offset, limit ?: MAX_ITEMS_SIZE)
        } else {
            gradeDao.allGradesByIds(gradeIds)
        }.sortedBy(Grade::id)

        respondSuccess {
            GradesResponse(
                count = grades.size,
                offset = offset ?: 0,
                grades = grades
            )
        }
    }
}

private fun Route.getGradeById() {
    val gradeDao by inject<GradeDao>()

    get("{id}") {
        val gradeId = call.parameters.getIntOrThrow("id")
        val grade = gradeDao.singleGradeById(gradeId) ?: throw ContentNotFoundException

        respondSuccess { grade }
    }
}

private fun Route.addGrade() {
    val gradeDao by inject<GradeDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters.getStringOrThrow("title")

        val created = gradeDao.addNewGrade(title)

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editGrade() {
    val gradeDao by inject<GradeDao>()

    patch("{id}") {
        val gradeId = call.parameters.getIntOrThrow("id")
        val currentGrade = gradeDao.singleGradeById(gradeId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters.getString("title")

        if (gradeDao.updateGrade(gradeId, title ?: currentGrade.title)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteGradeById() {
    val gradeDao by inject<GradeDao>()

    delete("{id}") {
        val gradeId = call.parameters.getIntOrThrow("id")
        gradeDao.singleGradeById(gradeId) ?: throw ContentNotFoundException

        if (gradeDao.deleteGrade(gradeId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteGradesByIds() {
    val gradeDao by inject<GradeDao>()

    delete {
        val gradeIds = call.request.queryParameters.getIntListOrThrow(
            key = "gradeIds",
            requiredNotEmpty = true
        )

        val currentGrades = gradeDao.allGradesByIds(gradeIds)
        if (currentGrades.isEmpty()) {
            throw ContentNotFoundException
        }

        if (gradeDao.deleteGrades(gradeIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
