package com.meloda.kubsau.controller

import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.students.StudentDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.Student
import com.meloda.kubsau.model.UnknownException
import com.meloda.kubsau.model.respondSuccess
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class StudentController : BaseController {

    override fun Route.routes() {
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

    private data class StudentsResponse(
        val count: Int,
        val offset: Int,
        val students: List<Student>
    )

    private fun Route.getStudents() {
        val studentDao by inject<StudentDao>()

        get {
            val principal = call.userPrincipal()
            val parameters = call.request.queryParameters

            val offset = parameters.getInt("offset")
            val limit = parameters.getInt(key = "limit", range = LimitRange)
            val groupId = parameters.getInt("groupId")
            val gradeId = parameters.getInt("gradeId")
            val status = parameters.getInt("status")
            val query = parameters.getString(key = "query", trim = true)?.lowercase()

            val studentIds = parameters.getIntList(
                key = "studentIds",
                maxSize = MAX_ITEMS_SIZE
            )

            val students = studentDao.allStudentsBySearch(
                facultyId = principal.facultyId,
                offset = offset,
                limit = limit,
                groupId = groupId,
                gradeId = gradeId,
                status = status,
                query = query,
                studentIds = studentIds
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

    private fun Route.getStudentById() {
        val studentDao by inject<StudentDao>()

        get("{id}") {
            val studentId = call.parameters.getIntOrThrow("id")
            val student = studentDao.singleStudent(studentId) ?: throw ContentNotFoundException

            respondSuccess { student }
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
            val status = parameters.getIntOrThrow("status")

            // TODO: 20/06/2024, Danil Nikolaev: check groupId
            val created = studentDao.addNewStudent(
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
        val studentDao by inject<StudentDao>()

        patch("{id}") {
            val studentId = call.parameters.getIntOrThrow("id")
            val currentStudent = studentDao.singleStudent(studentId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val lastName = parameters.getString("lastName")
            val firstName = parameters.getString("firstName")
            val middleName = parameters.getString("middleName")
            val groupId = parameters.getInt("groupId")
            val status = parameters.getInt("status")

            // TODO: 20/06/2024, Danil Nikolaev: check groupId

            studentDao.updateStudent(
                studentId = studentId,
                firstName = firstName ?: currentStudent.firstName,
                lastName = lastName ?: currentStudent.lastName,
                middleName = if ("middleName" in parameters) middleName else currentStudent.middleName,
                groupId = groupId ?: currentStudent.groupId,
                status = status ?: currentStudent.status
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

            // TODO: 20/06/2024, Danil Nikolaev: check access

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

            // TODO: 20/06/2024, Danil Nikolaev: check access

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
}
