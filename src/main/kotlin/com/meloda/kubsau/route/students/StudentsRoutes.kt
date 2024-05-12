package com.meloda.kubsau.route.students

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.studentstatuses.StudentStatusesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import com.meloda.kubsau.model.Student
import com.meloda.kubsau.model.StudentStatus
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
            searchStudents()
            addStudent()
            editStudent()
            deleteStudent()
            deleteStudents()
        }
    }
}

private data class StudentsResponse(
    val count: Int,
    val offset: Int,
    val students: List<Student>
)

private data class FullStudentsResponse(
    val count: Int,
    val offset: Int,
    val students: List<Student>,
    val statuses: List<StudentStatus>
)

private fun Route.getStudents() {
    val studentsDao by inject<StudentsDao>()
    val studentStatusesDao by inject<StudentStatusesDao>()

    get {
        val parameters = call.request.queryParameters

        val studentIds = parameters.getString("studentIds")
            ?.split(",")
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt("limit")
        val extended = parameters.getBoolean("extended", false)

        val students = if (studentIds.isEmpty()) {
            studentsDao.allStudents(offset, limit)
        } else {
            studentsDao.allStudentsByIds(studentIds)
        }

        if (!extended) {
            respondSuccess {
                StudentsResponse(
                    count = students.size,
                    offset = offset ?: 0,
                    students = students
                )
            }
        } else {
            val statusIds = students.map(Student::statusId)
            val statuses = studentStatusesDao.allStatusesByIds(statusIds)

            respondSuccess {
                FullStudentsResponse(
                    count = students.size,
                    offset = offset ?: 0,
                    students = students,
                    statuses = statuses
                )
            }
        }
    }
}

private data class StudentResponse(val student: Student)

private data class FullStudentResponse(val student: Student, val status: StudentStatus)

private fun Route.getStudentById() {
    val studentsDao by inject<StudentsDao>()
    val studentStatusesDao by inject<StudentStatusesDao>()

    get("{id}") {
        val studentId = call.parameters.getIntOrThrow("id")
        val extended = call.request.queryParameters.getBoolean("extended", false)

        val student = studentsDao.singleStudent(studentId) ?: throw ContentNotFoundException

        if (!extended) {
            respondSuccess { StudentResponse(student = student) }
        } else {
            val status = studentStatusesDao.singleStatus(student.statusId) ?: throw ContentNotFoundException

            respondSuccess {
                FullStudentResponse(
                    student = student,
                    status = status
                )
            }
        }
    }
}

private fun Route.searchStudents() {
    val studentsDao by inject<StudentsDao>()

    get("/search") {
        val parameters = call.request.queryParameters

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt("limit")
        val groupId = parameters.getInt("groupId")
        val gradeId = parameters.getInt("gradeId")
        val statusId = parameters.getInt("statusId")
        val query = parameters.getString("query")
            ?.lowercase()
            ?.trim()
            ?.ifEmpty { null }
            ?.ifBlank { null }

        val students = studentsDao.allStudentsBySearch(
            offset = offset,
            limit = limit,
            groupId = groupId,
            gradeId = gradeId,
            statusId = statusId,
            query = query
        )

        respondSuccess {
            StudentsResponse(
                count = students.size,
                offset = offset ?: 0,
                students = students
            )
        }
    }
}

private fun Route.addStudent() {
    val studentsDao by inject<StudentsDao>()

    post {
        val parameters = call.receiveParameters()

        val lastName = parameters.getStringOrThrow("lastName")
        val firstName = parameters.getStringOrThrow("firstName")
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
        val studentIds = call.request.queryParameters.getStringOrThrow("studentIds")
            .split(",")
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
