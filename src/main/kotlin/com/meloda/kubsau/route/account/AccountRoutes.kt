package com.meloda.kubsau.route.account

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDao
import com.meloda.kubsau.database.employeesfaculties.EmployeesFacultiesDao
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.SessionExpiredException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.WrongCurrentPasswordException
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Faculty
import com.meloda.kubsau.plugins.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.accountRoutes() {
    authenticate {
        route("/account") {
            getAccountInfo()
            editAccountInfo()
        }
    }
}

private fun Route.getAccountInfo() {
    val employeesDao by inject<EmployeesDao>()
    val employeesDepartmentsDao by inject<EmployeesDepartmentsDao>()
    val employeesFacultiesDao by inject<EmployeesFacultiesDao>()

    get {
        val principal = call.principal<UserPrincipal>() ?: throw SessionExpiredException
        val user = principal.user

        val employee = employeesDao.singleEmployee(user.employeeId) ?: throw ContentNotFoundException

        val departments = employeesDepartmentsDao.allDepartmentsByEmployeeId(employee.id)

        val faculty = if (employee.isAdmin()) {
            employeesFacultiesDao.singleFacultyByEmployeeId(employee.id)
        } else {
            null
        }

        respondSuccess {
            AccountInfo(
                id = user.id,
                type = employee.type,
                login = user.login,
                faculty = faculty,
                selectedDepartmentId = principal.departmentId,
                departments = departments
            )
        }
    }
}

private data class AccountInfo(
    val id: Int,
    val type: Int,
    val login: String,
    val selectedDepartmentId: Int?,
    val faculty: Faculty?,
    val departments: List<Department>
)

private fun Route.editAccountInfo() {
    val usersDao by inject<UsersDao>()

    patch {
        val principal = call.principal<UserPrincipal>() ?: throw SessionExpiredException

        val parameters = call.receiveParameters()

        val currentUser = usersDao.singleUser(principal.user.id) ?: throw ContentNotFoundException

        val currentPassword = parameters.getStringOrThrow("currentPassword")

        if (currentPassword != currentUser.password) {
            throw WrongCurrentPasswordException
        }

        val newPassword = parameters.getStringOrThrow("newPassword")
        // TODO: 07/06/2024, Danil Nikolaev: validate password for security

        if (usersDao.updateUser(currentUser.id, currentUser.login, newPassword)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
