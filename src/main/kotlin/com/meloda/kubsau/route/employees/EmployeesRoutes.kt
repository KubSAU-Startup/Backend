package com.meloda.kubsau.route.employees

import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.UnknownException
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

    get {
        val employeeIds = call.request.queryParameters.getIntList(
            key = "employeeIds",
            defaultValue = emptyList()
        )

        val employees = if (employeeIds.isEmpty()) {
            employeeDao.allEmployees()
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

    post {
        val parameters = call.receiveParameters()

        val lastName = parameters.getStringOrThrow("lastName")
        val firstName = parameters.getStringOrThrow("firstName")
        val middleName = parameters.getStringOrThrow("middleName")
        val email = parameters.getStringOrThrow("email")
        val type = parameters.getIntOrThrow("type")

        val created = employeeDao.addNewEmployee(
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
    val employeeDao by inject<EmployeeDao>()

    patch("{id}") {
        val employeeId = call.parameters.getIntOrThrow("id")
        val currentEmployee = employeeDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

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

    delete("{id}") {
        val employeeId = call.parameters.getIntOrThrow("id")
        employeeDao.singleEmployee(employeeId) ?: throw ContentNotFoundException

        if (employeeDao.deleteEmployee(employeeId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteEmployeesByIds() {
    val employeeDao by inject<EmployeeDao>()

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
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
