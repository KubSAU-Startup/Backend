package com.meloda.kubsau.controller

import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.students.StudentDao
import com.meloda.kubsau.model.*
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

    private data class FullName(
        val lastName: String,
        val firstName: String,
        val middleName: String?
    )

    private fun Route.addStudent() {
        val studentDao by inject<StudentDao>()

        post {
            val parameters = call.receiveParameters()

            val lastName = parameters.getString("lastName")
            val firstName = parameters.getString("firstName")
            val middleName = parameters.getString("middleName")
            val names = parameters.getString("names")
            val groupId = parameters.getIntOrThrow("groupId")
            val status = parameters.getIntOrThrow("status")

            val response: Any? = when {
                names != null -> {
                    names
                        .split("\n")
                        .map { line ->
                            val fullName = line.split(" ").mapNotNull { it.trim().ifEmpty { null } }
                            when (fullName.size) {
                                !in 2..3 -> {
                                    throw ValidationException.InvalidValueException("names")
                                }

                                2 -> {
                                    val (last, first) = fullName
                                    FullName(
                                        lastName = last,
                                        firstName = first,
                                        middleName = null
                                    )
                                }

                                else -> {
                                    val (last, first, middle) = fullName
                                    FullName(
                                        lastName = last,
                                        firstName = first,
                                        middleName = middle
                                    )
                                }
                            }
                        }
                        .map { fullName ->
                            studentDao.addNewStudent(
                                firstName = fullName.firstName,
                                lastName = fullName.lastName,
                                middleName = fullName.middleName,
                                groupId = groupId,
                                status = status
                            )
                        }
                }

                firstName != null && lastName != null -> {
                    studentDao.addNewStudent(
                        firstName = firstName,
                        lastName = lastName,
                        middleName = middleName,
                        groupId = groupId,
                        status = status
                    )
                }

                else -> {
                    val keys = mutableListOf<String>()
                    if (firstName == null) keys += "firstName"
                    if (lastName == null) keys += "lastName"

                    throw ValidationException.InvalidException("Either names or ${keys.joinToString(" & ")} must not be empty")
                }
            }

            if (response != null) {
                respondSuccess { response }
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
}
