package com.meloda.kubsau.route.employees

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getInt
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getOrThrow
import com.meloda.kubsau.common.getString
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
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
        val employeeIds = call.request.queryParameters["employeeIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

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

        val lastName = parameters.getOrThrow("lastName")
        val firstName = parameters.getOrThrow("firstName")
        val middleName = parameters.getOrThrow("middleName")
        val email = parameters.getString("email")
        val employeeTypeId = parameters.getIntOrThrow("employeeTypeId")

        val created = employeesDao.addNewEmployee(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            email = email,
            employeeTypeId = employeeTypeId
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
        val employeeTypeId = parameters.getInt("employeeTypeId")

        employeesDao.updateEmployee(
            employeeId = employeeId,
            firstName = firstName ?: currentEmployee.firstName,
            lastName = lastName ?: currentEmployee.lastName,
            middleName = middleName ?: currentEmployee.middleName,
            email = if ("email" in parameters) email else currentEmployee.email,
            employeeTypeId = employeeTypeId ?: currentEmployee.employeeTypeId
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
        val employeeIds = call.request.queryParameters.getOrThrow("employeeIds")
            .split(",")
            .map(String::trim)
            .mapNotNull(String::toIntOrNull)

        if (employeeIds.isEmpty()) {
            throw ValidationException("employeeIds is invalid")
        }

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
