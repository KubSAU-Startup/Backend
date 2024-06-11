package com.meloda.kubsau.route.grades

import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.grades.GradesDao
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
    val gradesDao by inject<GradesDao>()

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
            gradesDao.allGrades(offset, limit ?: MAX_ITEMS_SIZE)
        } else {
            gradesDao.allGradesByIds(gradeIds)
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
    val gradesDao by inject<GradesDao>()

    get("{id}") {
        val gradeId = call.parameters.getIntOrThrow("id")
        val grade = gradesDao.singleGradeById(gradeId) ?: throw ContentNotFoundException

        respondSuccess { grade }
    }
}

private fun Route.addGrade() {
    val gradesDao by inject<GradesDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters.getStringOrThrow("title")

        val created = gradesDao.addNewGrade(title)

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editGrade() {
    val gradesDao by inject<GradesDao>()

    patch("{id}") {
        val gradeId = call.parameters.getIntOrThrow("id")
        val currentGrade = gradesDao.singleGradeById(gradeId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters.getString("title")

        if (gradesDao.updateGrade(gradeId, title ?: currentGrade.title)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteGradeById() {
    val gradesDao by inject<GradesDao>()

    delete("{id}") {
        val gradeId = call.parameters.getIntOrThrow("id")
        gradesDao.singleGradeById(gradeId) ?: throw ContentNotFoundException

        if (gradesDao.deleteGrade(gradeId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteGradesByIds() {
    val gradesDao by inject<GradesDao>()

    delete {
        val gradeIds = call.request.queryParameters.getIntListOrThrow(
            key = "gradeIds",
            requiredNotEmpty = true
        )

        val currentGrades = gradesDao.allGradesByIds(gradeIds)
        if (currentGrades.isEmpty()) {
            throw ContentNotFoundException
        }

        if (gradesDao.deleteGrades(gradeIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
