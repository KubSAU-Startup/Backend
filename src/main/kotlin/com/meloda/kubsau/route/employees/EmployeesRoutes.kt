package com.meloda.kubsau.route.employees

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
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
    val employeesDao by inject<EmployeesDao>()

    get {
        val employeeIds = call.request.queryParameters.getIntList(
            key = "employeeIds",
            defaultValue = emptyList()
        )

        val employees = if (employeeIds.isEmpty()) {
            employeesDao.allEmployees()
        } else {
            employeesDao.allEmployeesByIds(employeeIds)
        }

        respondSuccess { employees }
    }
}

private fun Route.getEmployee() {
    val employeesDao by inject<EmployeesDao>()

    get("{id}") {
        val employeeId = call.parameters.getIntOrThrow("id")
        val employee = employeesDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

        respondSuccess { employee }
    }
}

private fun Route.addEmployee() {
    val employeesDao by inject<EmployeesDao>()

    post {
        val parameters = call.receiveParameters()

        val lastName = parameters.getStringOrThrow("lastName")
        val firstName = parameters.getStringOrThrow("firstName")
        val middleName = parameters.getStringOrThrow("middleName")
        val email = parameters.getStringOrThrow("email")
        val type = parameters.getIntOrThrow("type")

        val created = employeesDao.addNewEmployee(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            email = email,
            type = type
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editEmployee() {
    val employeesDao by inject<EmployeesDao>()

    patch("{id}") {
        val employeeId = call.parameters.getIntOrThrow("id")
        val currentEmployee = employeesDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val lastName = parameters.getString("lastName")
        val firstName = parameters.getString("firstName")
        val middleName = parameters.getString("middleName")
        val email = parameters.getString("email")
        val type = parameters.getInt("type")

        employeesDao.updateEmployee(
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
    val employeesDao by inject<EmployeesDao>()

    delete("{id}") {
        val employeeId = call.parameters.getIntOrThrow("id")
        employeesDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

        if (employeesDao.deleteEmployee(employeeId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteEmployeesByIds() {
    val employeesDao by inject<EmployeesDao>()

    delete {
        val employeeIds = call.request.queryParameters.getIntListOrThrow(
            key = "employeeIds",
            requiredNotEmpty = true
        )

        val currentEmployees = employeesDao.allEmployeesByIds(employeeIds)
        if (currentEmployees.isEmpty()) {
            throw ContentNotFoundException
        }

        if (employeesDao.deleteEmployees(employeeIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
