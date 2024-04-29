package com.meloda.kubsau.route.students

import com.meloda.kubsau.api.respondSuccess
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
        val studentId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val student = studentsDao.singleStudent(studentId) ?: throw ContentNotFoundException

        respondSuccess { student }
    }
}

private fun Route.addStudent() {
    val studentsDao by inject<StudentsDao>()

    post {
        val parameters = call.receiveParameters()

        val firstName = parameters["firstName"]?.trim() ?: throw ValidationException("firstName is empty")
        val lastName = parameters["lastName"]?.trim() ?: throw ValidationException("lastName is empty")
        val middleName = parameters["middleName"]?.trim() ?: throw ValidationException("middleName is empty")
        val groupId = parameters["groupId"]?.toIntOrNull() ?: throw ValidationException("groupId is empty")
        val status = parameters["status"]?.toIntOrNull() ?: throw ValidationException("status is empty")

        val created = studentsDao.addNewStudent(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            groupId = groupId,
            status = status
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
        val studentId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val currentStudent = studentsDao.singleStudent(studentId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val firstName = parameters["firstName"]?.trim()
        val lastName = parameters["lastName"]?.trim()
        val middleName = parameters["middleName"]?.trim()
        val groupId = parameters["groupId"]?.toIntOrNull()
        val status = parameters["status"]?.toIntOrNull()

        studentsDao.updateStudent(
            studentId = studentId,
            firstName = firstName ?: currentStudent.firstName,
            lastName = lastName ?: currentStudent.lastName,
            middleName = middleName ?: currentStudent.middleName,
            groupId = groupId ?: currentStudent.groupId,
            status = status ?: currentStudent.status
        ).let { changedCount ->
            if (changedCount == 1) {
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
        val studentId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
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
        val studentIds = call.request.queryParameters["studentIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: throw ValidationException("studentIds is empty")

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
