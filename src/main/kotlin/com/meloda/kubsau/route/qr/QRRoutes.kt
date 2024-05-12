package com.meloda.kubsau.route.qr

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getIntListOrThrow
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.model.Student
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.qrRoutes() {
    authenticate {
        route("/qr") {
            students()
        }
    }
}

private fun Route.students() {
    val studentsDao by inject<StudentsDao>()

    get("/groups/{groupId}/students") {
        val groupId = call.parameters.getIntOrThrow("groupId")
        val students = studentsDao.allStudentsByGroupId(groupId)
        respondSuccess { students }
    }

    get("/groups/students") {
        val params = call.request.queryParameters
        val groupIds = params.getIntListOrThrow(
            key = "groupIds",
            requiredNotEmpty = true
        )

        val fullStudents = studentsDao.allStudentsByGroupIds(groupIds)

        val listOfStudents = mutableListOf<GroupIdWithStudents>()

        groupIds.forEach { id ->
            fullStudents
                .filter { student -> student.groupId == id }
                .map(Student::mapToShrankItem)
                .let { shrankStudents -> GroupIdWithStudents(id, shrankStudents) }
                .let(listOfStudents::add)
        }

        respondSuccess { listOfStudents }
    }

    route("/students") {
        get {
            val students = studentsDao.allStudents(null, null).map(Student::mapToShrankItem)
            respondSuccess { students }
        }
    }
}


private data class GroupIdWithStudents(
    val groupId: Int,
    val students: List<ShrankItem>
)

private data class ShrankItem(
    val id: Int,
    val title: String
)

private fun Student.mapToShrankItem(): ShrankItem =
    ShrankItem(id = id, title = fullName)
