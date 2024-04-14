package com.meloda.kubsau.route.students

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.students() {
    authenticate {
        route("/students") {
            getAllStudents()
            getStudentById()
        }
    }
}

private fun Route.getAllStudents() {
    val studentsDao by inject<StudentsDao>()

    get {
        val students = studentsDao.allStudents()

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
