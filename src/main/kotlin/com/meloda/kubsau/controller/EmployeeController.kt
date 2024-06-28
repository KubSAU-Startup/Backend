package com.meloda.kubsau.controller

import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.getIntList
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getString
import com.meloda.kubsau.common.userPrincipal
import com.meloda.kubsau.database.departmentfaculty.DepartmentsFacultiesDao
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class EmployeeController : BaseController {

    override fun Route.routes() {
        authenticate {
            route("/employees") {
                getEmployees()
                getEmployee()
                editEmployee()
            }
        }
    }

    private fun Route.getEmployees() {
        val employeeDao by inject<EmployeeDao>()
        val departmentsFacultiesDao by inject<DepartmentsFacultiesDao>()

        get {
            val principal = call.userPrincipal()
            val employeeIds = call.request.queryParameters.getIntList(
                key = "employeeIds",
                defaultValue = emptyList()
            )

            val departmentIds: List<Int> = if (principal.type == Employee.TYPE_ADMIN) {
                val facultyId = principal.facultyId ?: throw UnknownTokenException
                departmentsFacultiesDao.getDepartmentIdsByFacultyId(facultyId)
            } else principal.selectedDepartmentId?.let(::listOf) ?: principal.departmentIds

            val employees = if (employeeIds.isEmpty()) {
                employeeDao.allEmployees(departmentIds, null, null)
            } else {
                employeeDao.allEmployeesByIds(employeeIds)
            }

            respondSuccess { employees }
        }
    }

    private fun Route.getEmployee() {
        val employeeDao by inject<EmployeeDao>()

        get("{id}") {
            val employeeId = call.parameters.getIntOrThrow("id")
            val employee = employeeDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

            respondSuccess { employee }
        }
    }

    private fun Route.editEmployee() {
        val employeeDao by inject<EmployeeDao>()

        patch("{id}") {
            val principal = call.userPrincipal()
            val employeeId = call.parameters.getIntOrThrow("id")
            val currentEmployee =
                employeeDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val lastName = parameters.getString("lastName")
            val firstName = parameters.getString("firstName")
            val middleName = parameters.getString("middleName")
            val email = parameters.getString("email")

            if (principal.user.employeeId != employeeId) {
                throw AccessDeniedException("You cannot edit info of another user")
            }

            employeeDao.updateEmployee(
                employeeId = employeeId,
                firstName = firstName ?: currentEmployee.firstName,
                lastName = lastName ?: currentEmployee.lastName,
                middleName = middleName ?: currentEmployee.middleName,
                email = email ?: currentEmployee.email
            ).let { success ->
                if (success) {
                    respondSuccess { 1 }
                } else {
                    throw UnknownException
                }
            }
        }
    }
}
