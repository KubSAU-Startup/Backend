package com.meloda.kubsau.route.grades

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getInt
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getString
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.database.grades.GradesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
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

        val gradeIds = parameters.getString("gradeIds")
            ?.split(",")
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt("limit")

        val grades = if (gradeIds.isEmpty()) {
            gradesDao.allGrades(offset, limit)
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
        val gradeIds = call.request.queryParameters.getStringOrThrow("gradeIds")
            .split(",")
            .mapNotNull(String::toIntOrNull)

        if (gradeIds.isEmpty()) {
            throw ValidationException("gradeIds is invalid")
        }

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
