package com.meloda.kubsau.route.department

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getIntList
import com.meloda.kubsau.common.getIntListOrThrow
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.departmentsRoutes() {
    authenticate {
        route("/departments") {
            getAllDepartments()
            getDepartmentById()
            getEmployees()
            addDepartment()
            editDepartment()
            deleteDepartmentById()
            deleteDepartments()
        }
    }
}

private fun Route.getAllDepartments() {
    val departmentsDao by inject<DepartmentsDao>()

    get {
        val departmentIds = call.request.queryParameters.getIntList(
            key = "departmentIds",
            defaultValue = emptyList()
        )

        val departments = if (departmentIds.isEmpty()) {
            departmentsDao.allDepartments()
        } else {
            departmentsDao.allDepartmentsByIds(departmentIds)
        }
        respondSuccess { departments }
    }
}

private fun Route.getDepartmentById() {
    val departmentsDao by inject<DepartmentsDao>()

    get("{id}") {
        val departmentId = call.parameters.getIntOrThrow("id")
        val department = departmentsDao.singleDepartment(departmentId) ?: throw ContentNotFoundException

        respondSuccess { department }
    }
}

private fun Route.getEmployees() {
    val departmentsDao by inject<DepartmentsDao>()
    val employeesDepartmentsDao by inject<EmployeesDepartmentsDao>()

    get("{id}/teachers") {
        val departmentId = call.parameters.getIntOrThrow("id")
        if (!departmentsDao.isExist(departmentId)) throw ContentNotFoundException

        val teachers = employeesDepartmentsDao.allTeachersByDepartmentId(departmentId)
        respondSuccess { teachers }
    }
}

private fun Route.addDepartment() {
    val departmentsDao by inject<DepartmentsDao>()

    post {
        val parameters = call.receiveParameters()
        val title = parameters.getStringOrThrow("title")
        val phone = parameters.getStringOrThrow("phone")

        val created = departmentsDao.addNewDepartment(title, phone)

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editDepartment() {
    val departmentsDao by inject<DepartmentsDao>()

    patch("{id}") {
        val departmentId = call.parameters.getIntOrThrow("id")
        val currentDepartment = departmentsDao.singleDepartment(departmentId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim()
        val phone = parameters["phone"]?.trim()

        departmentsDao.updateDepartment(
            departmentId = departmentId,
            title = title ?: currentDepartment.title,
            phone = phone ?: currentDepartment.phone
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteDepartmentById() {
    val departmentsDao by inject<DepartmentsDao>()

    delete("{id}") {
        val departmentId = call.parameters.getIntOrThrow("id")
        departmentsDao.singleDepartment(departmentId) ?: throw ContentNotFoundException

        if (departmentsDao.deleteDepartment(departmentId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteDepartments() {
    val departmentsDao by inject<DepartmentsDao>()

    delete {
        val departmentIds = call.request.queryParameters.getIntListOrThrow(
            key = "departmentIds",
            requiredNotEmpty = true
        )

        val currentDepartments = departmentsDao.allDepartmentsByIds(departmentIds)
        if (currentDepartments.isEmpty()) {
            throw ContentNotFoundException
        }

        if (departmentsDao.deleteDepartments(departmentIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
