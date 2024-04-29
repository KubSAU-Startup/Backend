package com.meloda.kubsau.route.students

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getInt
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getOrThrow
import com.meloda.kubsau.common.getString
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.studentsRoutes() {
    authenticate {
        route("/students") {
            getStudents()
            getStudentById()
            addStudent()
            editStudent()
            deleteStudent()
            deleteStudents()
        }
    }
}

private fun Route.getStudents() {
    val studentsDao by inject<StudentsDao>()

    get {
        val studentIds = call.request.queryParameters["studentIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val students = if (studentIds.isEmpty()) {
            studentsDao.allStudents()
        } else {
            studentsDao.allStudentsByIds(studentIds)
        }

        respondSuccess { students }
    }
}

private fun Route.getStudentById() {
    val studentsDao by inject<StudentsDao>()

    get("{id}") {
        val studentId = call.parameters.getIntOrThrow("id")
        val student = studentsDao.singleStudent(studentId) ?: throw ContentNotFoundException

        respondSuccess { student }
    }
}

private fun Route.addStudent() {
    val studentsDao by inject<StudentsDao>()

    post {
        val parameters = call.receiveParameters()

        val lastName = parameters.getOrThrow("lastName")
        val firstName = parameters.getOrThrow("firstName")
        val middleName = parameters.getString("middleName")
        val groupId = parameters.getIntOrThrow("groupId")
        val statusId = parameters.getIntOrThrow("statusId")

        val created = studentsDao.addNewStudent(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            groupId = groupId,
            statusId = statusId
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editStudent() {
    val studentsDao by inject<StudentsDao>()

    patch("{id}") {
        val studentId = call.parameters.getIntOrThrow("id")
        val currentStudent = studentsDao.singleStudent(studentId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val lastName = parameters.getString("lastName")
        val firstName = parameters.getString("firstName")
        val middleName = parameters.getString("middleName")
        val groupId = parameters.getInt("groupId")
        val statusId = parameters.getInt("statusId")

        studentsDao.updateStudent(
            studentId = studentId,
            firstName = firstName ?: currentStudent.firstName,
            lastName = lastName ?: currentStudent.lastName,
            middleName = if ("middleName" in parameters) middleName else currentStudent.middleName,
            groupId = groupId ?: currentStudent.groupId,
            statusId = statusId ?: currentStudent.statusId
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteStudent() {
    val studentsDao by inject<StudentsDao>()

    delete("{id}") {
        val studentId = call.parameters.getIntOrThrow("id")
        studentsDao.singleStudent(studentId) ?: throw ContentNotFoundException

        if (studentsDao.deleteStudent(studentId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteStudents() {
    val studentsDao by inject<StudentsDao>()

    delete {
        val studentIds = call.request.queryParameters.getOrThrow("studentIds")
            .split(",")
            .map(String::trim)
            .mapNotNull(String::toIntOrNull)

        if (studentIds.isEmpty()) {
            throw ValidationException("studentIds is invalid")
        }

        val currentStudents = studentsDao.allStudentsByIds(studentIds)
        if (currentStudents.isEmpty()) {
            throw ContentNotFoundException
        }

        if (studentsDao.deleteStudents(studentIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
