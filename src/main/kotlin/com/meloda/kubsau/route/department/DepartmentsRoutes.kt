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
            getAllDepartmentsRoute()
            getDepartmentByIdRoute()
            deleteDepartmentByIdRoute()
            addDepartment()
        }
    }
}

private fun Route.getAllDepartmentsRoute() {
    val departmentsDao by inject<DepartmentsDao>()

    get {
        val departments = departmentsDao.allDepartments()
        respondSuccess { departments }
    }
}

private fun Route.getDepartmentByIdRoute() {
    val departmentsDao by inject<DepartmentsDao>()

    get("{id}") {
        val departmentId = call.parameters["id"]?.toInt() ?: throw ValidationException("id is empty")
        val department = departmentsDao.singleDepartment(departmentId) ?: throw ContentNotFoundException

        respondSuccess { department }
    }
}

private fun Route.deleteDepartmentByIdRoute() {
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

private fun Route.addDepartment() {
    val departmentsDao by inject<DepartmentsDao>()

    post {
        val parameters = call.receiveParameters()
        val title = parameters["title"] ?: throw ValidationException("title is empty")
        val phone = parameters["phone"] ?: throw ValidationException("phone is empty")

        val created = departmentsDao.addNewDepartment(title, phone) != null
        if (created) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
