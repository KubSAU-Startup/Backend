package com.meloda.kubsau.route.employees

import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.departmentfaculty.DepartmentsFacultiesDao
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.employeesdepartments.EmployeeDepartmentDao
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.employeesRoutes() {
    authenticate {
        route("/employees") {
            getEmployees()
            getEmployee()
            addEmployee()
            editEmployee()
            deleteEmployeeById()
            deleteEmployeesByIds()
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

        // TODO: 20/06/2024, Danil Nikolaev: filter by selectedDepartmentId
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

private fun Route.addEmployee() {
    val employeeDao by inject<EmployeeDao>()
    val employeeDepartmentDao by inject<EmployeeDepartmentDao>()

    post {
        val principal = call.userPrincipal()

        val parameters = call.receiveParameters()

        val lastName = parameters.getStringOrThrow("lastName")
        val firstName = parameters.getStringOrThrow("firstName")
        val middleName = parameters.getStringOrThrow("middleName")
        val email = parameters.getStringOrThrow("email")
        val type = parameters.getIntOrThrow("type")

        val departmentId = principal.selectedDepartmentId
            ?: principal.departmentIds.singleOrNull()
            ?: throw AccessDeniedException("There are multiple departmentIds. Pick one")

        val created = employeeDao.addNewEmployee(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            email = email,
            type = type
        )

        if (created != null && employeeDepartmentDao.addNewReference(created.id, departmentId)) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editEmployee() {
    val employeeDao by inject<EmployeeDao>()

    patch("{id}") {
        val employeeId = call.parameters.getIntOrThrow("id")
        val currentEmployee =
            employeeDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val lastName = parameters.getString("lastName")
        val firstName = parameters.getString("firstName")
        val middleName = parameters.getString("middleName")
        val email = parameters.getString("email")
        val type = parameters.getInt("type")

        employeeDao.updateEmployee(
            employeeId = employeeId,
            firstName = firstName ?: currentEmployee.firstName,
            lastName = lastName ?: currentEmployee.lastName,
            middleName = middleName ?: currentEmployee.middleName,
            email = email ?: currentEmployee.email,
            type = type ?: currentEmployee.type
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteEmployeeById() {
    val employeeDao by inject<EmployeeDao>()
    val employeeDepartmentDao by inject<EmployeeDepartmentDao>()

    delete("{id}") {
        val employeeId = call.parameters.getIntOrThrow("id")
        employeeDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

        val successDelete = employeeDao.deleteEmployee(employeeId)

        if (successDelete) {
            val departmentIds = employeeDepartmentDao.allDepartmentIdsByEmployeeId(employeeId)
            if (departmentIds.isNotEmpty()) {
                employeeDepartmentDao.deleteReferences(employeeId)
            }

            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteEmployeesByIds() {
    val employeeDao by inject<EmployeeDao>()
    val employeeDepartmentDao by inject<EmployeeDepartmentDao>()

    delete {
        val employeeIds = call.request.queryParameters.getIntListOrThrow(
            key = "employeeIds",
            requiredNotEmpty = true
        )

        val currentEmployees = employeeDao.allEmployeesByIds(employeeIds)
        if (currentEmployees.isEmpty()) {
            throw ContentNotFoundException
        }

        if (employeeDao.deleteEmployees(employeeIds)) {
            employeeDepartmentDao.deleteReferences(employeeIds)
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
