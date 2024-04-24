package com.meloda.kubsau.route.teachers

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.teachers.TeachersDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.teachersRoutes() {
    authenticate {
        route("/teachers") {
            getTeachers()
            getTeacherById()
            addTeacher()
            editTeacher()
            deleteTeacherById()
            deleteTeachersByIds()
        }
    }
}

private fun Route.getTeachers() {
    val teachersDao by inject<TeachersDao>()

    get {
        val teacherIds = call.request.queryParameters["teacherIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val teachers = if (teacherIds.isEmpty()) {
            teachersDao.allTeachers()
        } else {
            teachersDao.allTeachersByIds(teacherIds)
        }

        respondSuccess { teachers }
    }
}

private fun Route.getTeacherById() {
    val teachersDao by inject<TeachersDao>()

    get("{id}") {
        val teacherId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val teacher = teachersDao.singleTeacher(teacherId) ?: throw ContentNotFoundException

        respondSuccess { teacher }
    }
}

private fun Route.addTeacher() {
    val teachersDao by inject<TeachersDao>()

    post {
        val parameters = call.receiveParameters()

        val firstName = parameters["firstName"]?.trim() ?: throw ValidationException("firstName is empty")
        val lastName = parameters["lastName"]?.trim() ?: throw ValidationException("lastName is empty")
        val middleName = parameters["middleName"]?.trim() ?: throw ValidationException("middleName is empty")
        val departmentId =
            parameters["departmentId"]?.toIntOrNull() ?: throw ValidationException("departmentId is empty")

        val created = teachersDao.addNewTeacher(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            departmentId = departmentId
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editTeacher() {
    val teachersDao by inject<TeachersDao>()

    patch("{id}") {
        val teacherId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val currentTeacher = teachersDao.singleTeacher(teacherId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val firstName = parameters["firstName"]?.trim()
        val lastName = parameters["lastName"]?.trim()
        val middleName = parameters["middleName"]?.trim()
        val departmentId = parameters["departmentId"]?.toIntOrNull()

        teachersDao.updateTeacher(
            teacherId = teacherId,
            firstName = firstName ?: currentTeacher.firstName,
            lastName = lastName ?: currentTeacher.lastName,
            middleName = middleName ?: currentTeacher.middleName,
            departmentId = departmentId ?: currentTeacher.departmentId
        ).let { changedCount ->
            if (changedCount == 1) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteTeacherById() {
    val teachersDao by inject<TeachersDao>()

    delete("{id}") {
        val teacherId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        teachersDao.singleTeacher(teacherId) ?: throw ContentNotFoundException

        if (teachersDao.deleteTeacher(teacherId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteTeachersByIds() {
    val teachersDao by inject<TeachersDao>()

    delete {
        val teacherIds = call.request.queryParameters["teacherIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: throw ValidationException("teacherIds is empty")

        val currentTeachers = teachersDao.allTeachersByIds(teacherIds)
        if (currentTeachers.isEmpty()) {
            throw ContentNotFoundException
        }

        if (teachersDao.deleteTeachers(teacherIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
