package com.meloda.kubsau.route.students

import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.students.StudentDao
import com.meloda.kubsau.database.studentstatuses.StudentStatusDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.UnknownException
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
    val studentDao by inject<StudentDao>()
    val studentStatusDao by inject<StudentStatusDao>()

    get {
        val parameters = call.request.queryParameters

        val studentIds = parameters.getIntList(
            key = "studentIds",
            defaultValue = emptyList(),
            maxSize = MAX_ITEMS_SIZE
        )

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt("limit", range = LimitRange)
        val extended = parameters.getBoolean("extended", false)

        val students = if (studentIds.isEmpty()) {
            studentDao.allStudents(offset, limit ?: MAX_ITEMS_SIZE)
        } else {
            studentDao.allStudentsByIds(studentIds)
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
            val statuses = studentStatusDao.allStatusesByIds(statusIds)

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
    val studentDao by inject<StudentDao>()
    val studentStatusDao by inject<StudentStatusDao>()

    get("{id}") {
        val studentId = call.parameters.getIntOrThrow("id")
        val extended = call.request.queryParameters.getBoolean("extended", false)

        val student = studentDao.singleStudent(studentId) ?: throw ContentNotFoundException

        if (!extended) {
            respondSuccess { StudentResponse(student = student) }
        } else {
            val status = studentStatusDao.singleStatus(student.statusId) ?: throw ContentNotFoundException

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
    val studentDao by inject<StudentDao>()

    get("/search") {
        val parameters = call.request.queryParameters

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt(key = "limit", range = LimitRange)
        val groupId = parameters.getInt("groupId")
        val gradeId = parameters.getInt("gradeId")
        val statusId = parameters.getInt("statusId")
        val query = parameters.getString(key = "query", trim = true)?.lowercase()

        val studentsStatuses = studentDao.allStudentsBySearch(
            offset = offset,
            limit = limit,
            groupId = groupId,
            gradeId = gradeId,
            statusId = statusId,
            query = query
        )

        val students = studentsStatuses.keys.toList()
        val statuses = studentsStatuses.values.toList().distinctBy(StudentStatus::id)

        respondSuccess {
            FullStudentsResponse(
                offset = offset ?: 0,
                count = studentsStatuses.size,
                students = students,
                statuses = statuses
            )
        }
    }
}

private fun Route.addStudent() {
    val studentDao by inject<StudentDao>()

    post {
        val parameters = call.receiveParameters()

        val lastName = parameters.getStringOrThrow("lastName")
        val firstName = parameters.getStringOrThrow("firstName")
        val middleName = parameters.getString("middleName")
        val groupId = parameters.getIntOrThrow("groupId")
        val statusId = parameters.getIntOrThrow("statusId")

        val created = studentDao.addNewStudent(
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
    val studentDao by inject<StudentDao>()

    patch("{id}") {
        val studentId = call.parameters.getIntOrThrow("id")
        val currentStudent = studentDao.singleStudent(studentId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val lastName = parameters.getString("lastName")
        val firstName = parameters.getString("firstName")
        val middleName = parameters.getString("middleName")
        val groupId = parameters.getInt("groupId")
        val statusId = parameters.getInt("statusId")

        studentDao.updateStudent(
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
    val studentDao by inject<StudentDao>()

    delete("{id}") {
        val studentId = call.parameters.getIntOrThrow("id")
        studentDao.singleStudent(studentId) ?: throw ContentNotFoundException

        if (studentDao.deleteStudent(studentId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteStudents() {
    val studentDao by inject<StudentDao>()

    delete {
        val studentIds = call.request.queryParameters.getIntListOrThrow(
            key = "studentIds",
            requiredNotEmpty = true
        )

        val currentStudents = studentDao.allStudentsByIds(studentIds)
        if (currentStudents.isEmpty()) {
            throw ContentNotFoundException
        }

        if (studentDao.deleteStudents(studentIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
