package com.meloda.kubsau.controller

import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.*
import com.meloda.kubsau.model.*
import com.meloda.kubsau.service.DepartmentService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

class DepartmentController(private val service: DepartmentService) : BaseController {

    override fun Route.routes() {
        authenticate {
            route("/departments") {
                getDepartments()
                getDepartmentById()
                getEmployees()
                addDepartment()
                editDepartment()
                deleteDepartmentById()
                deleteDepartments()
            }
        }
    }

    private fun Route.getDepartments() {
        get {
            val principal = call.userPrincipal()
            val departmentIds = call.request.queryParameters.getIntList(
                key = "departmentIds"
            )

            val departments = if (departmentIds == null) {
                service.getAllDepartments(principal)
            } else {
                service.getDepartmentByIds(departmentIds)
            }
            respondSuccess { departments }
        }
    }

    private fun Route.getDepartmentById() {
        get("{id}") {
            val principal = call.userPrincipal()
            val departmentId = call.parameters.getIntOrThrow("id")
            val department = service.getDepartmentById(departmentId) ?: throw ContentNotFoundException

            respondSuccess { department }
        }
    }

    private fun Route.getEmployees() {
        get("{id}/teachers") {
            val principal = call.userPrincipal()
            val departmentId = call.parameters.getIntOrThrow("id")
            if (!service.isExist(departmentId)) throw ContentNotFoundException

            val teachers = service.getTeachersInDepartment(departmentId)
            respondSuccess { teachers }
        }
    }

    private fun Route.addDepartment() {
        post {
            val principal = call.userPrincipal()

            if (principal.type != Employee.TYPE_ADMIN || principal.facultyId == null) {
                throw AccessDeniedException("Admin rights required")
            }

            val parameters = call.receiveParameters()
            val title = parameters.getStringOrThrow("title")
            val phone = parameters.getStringOrThrow("phone")

            val created = service.addDepartment(principal.facultyId, title, phone)

            if (created != null) {
                respondSuccess { created }
            } else {
                throw UnknownException
            }
        }
    }

    private fun Route.editDepartment() {
        patch("{id}") {
            val principal = call.userPrincipal()
            val departmentId = call.parameters.getIntOrThrow("id")

            if (principal.type != Employee.TYPE_ADMIN) {
                throw AccessDeniedException("Admin rights required")
            }

            val currentDepartment = service.getDepartmentById(departmentId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val title = parameters["title"]?.trim()
            val phone = parameters["phone"]?.trim()

            service.editDepartment(
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
        delete("{id}") {
            val principal = call.userPrincipal()

            if (principal.type != Employee.TYPE_ADMIN || principal.facultyId == null) {
                throw AccessDeniedException("Admin rights required")
            }

            val departmentId = call.parameters.getIntOrThrow("id")

            if (!service.isExist(departmentId)) {
                throw ContentNotFoundException
            }

            if (service.deleteDepartment(principal.facultyId, departmentId)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }

    private fun Route.deleteDepartments() {
        delete {
            val principal = call.userPrincipal()

            if (principal.type != Employee.TYPE_ADMIN || principal.facultyId == null) {
                throw AccessDeniedException("Admin rights required")
            }

            val departmentIds = call.request.queryParameters.getIntListOrThrow(
                key = "departmentIds",
                requiredNotEmpty = true
            )

            val currentDepartments = service.getDepartmentByIds(departmentIds)
            if (currentDepartments.isEmpty()) {
                throw ContentNotFoundException
            }

            if (service.deleteDepartments(principal.facultyId, departmentIds)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}
