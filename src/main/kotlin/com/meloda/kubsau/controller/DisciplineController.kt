package com.meloda.kubsau.controller

import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.getIntList
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.userPrincipal
import com.meloda.kubsau.database.departmentfaculty.DepartmentsFacultiesDao
import com.meloda.kubsau.database.disciplines.DisciplineDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.model.UnknownTokenException
import com.meloda.kubsau.model.respondSuccess
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class DisciplineController : BaseController {

    override fun Route.routes() {
        authenticate {
            route("/disciplines") {
                getDisciplines()
                getDisciplineById()
            }
        }
    }

    private fun Route.getDisciplines() {
        val disciplineDao by inject<DisciplineDao>()
        val departmentsFacultiesDao by inject<DepartmentsFacultiesDao>()

        get {
            val principal = call.userPrincipal()
            val disciplineIds = call.request.queryParameters.getIntList(
                key = "disciplineIds",
                defaultValue = emptyList()
            )

            val departmentIds: List<Int> = if (principal.type == Employee.TYPE_ADMIN) {
                val facultyId = principal.facultyId ?: throw UnknownTokenException
                departmentsFacultiesDao.getDepartmentIdsByFacultyId(facultyId)
            } else principal.selectedDepartmentId?.let(::listOf) ?: principal.departmentIds

            val disciplines = if (disciplineIds.isEmpty()) {
                disciplineDao.allDisciplines(departmentIds)
            } else {
                disciplineDao.allDisciplinesByIds(disciplineIds)
                // TODO: 20/06/2024, Danil Nikolaev: check access ^
            }

            respondSuccess { disciplines }
        }
    }

    private fun Route.getDisciplineById() {
        val disciplineDao by inject<DisciplineDao>()

        get("{id}") {
            val disciplineId = call.parameters.getIntOrThrow("id")
            // TODO: 21/06/2024, Danil Nikolaev: check access ^
            val discipline = disciplineDao.singleDiscipline(disciplineId) ?: throw ContentNotFoundException

            respondSuccess { discipline }
        }
    }
}
