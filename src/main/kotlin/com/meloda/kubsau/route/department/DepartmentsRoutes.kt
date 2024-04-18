package com.meloda.kubsau.route.department

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
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
        val departmentIds = call.request.queryParameters["departmentIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

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
        val departmentId = call.parameters["id"]?.toInt() ?: throw ValidationException("id is empty")
        val department = departmentsDao.singleDepartment(departmentId) ?: throw ContentNotFoundException

        respondSuccess { department }
    }
}

private fun Route.addDepartment() {
    val departmentsDao by inject<DepartmentsDao>()

    post {
        val parameters = call.receiveParameters()
        val title = parameters["title"] ?: throw ValidationException("title is empty")
        val phone = parameters["phone"] ?: throw ValidationException("phone is empty")

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
        val departmentId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val currentDepartment = departmentsDao.singleDepartment(departmentId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim()
        val phone = parameters["phone"]?.trim()

        departmentsDao.updateDepartment(
            departmentId = departmentId,
            title = title ?: currentDepartment.title,
            phone = phone ?: currentDepartment.phone
        ).let { changedCount ->
            if (changedCount == 1) {
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
        val departmentId = call.parameters["id"]?.toInt() ?: throw ValidationException("id is empty")

        val deleted = departmentsDao.deleteDepartment(departmentId)
        if (deleted) {
            respondSuccess { 1 }
        } else {
            throw ContentNotFoundException
        }
    }
}

private fun Route.deleteDepartments() {
    val departmentsDao by inject<DepartmentsDao>()

    delete {
        val departmentIds = call.request.queryParameters["departmentIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: throw ValidationException("departmentIds is empty")

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
