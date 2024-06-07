package com.meloda.kubsau.route.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.SecretsController
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDao
import com.meloda.kubsau.database.employeesfaculties.EmployeesFacultiesDao
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.SessionExpiredException
import com.meloda.kubsau.errors.UnavailableDepartmentId
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.User
import com.meloda.kubsau.plugins.AUDIENCE
import com.meloda.kubsau.plugins.ISSUER
import com.meloda.kubsau.plugins.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    route("/auth") {
        addSession()

        authenticate {
            modifySession()
        }
    }
}


private fun Route.addSession() {
    val usersDao by inject<UsersDao>()
    val employeesDao by inject<EmployeesDao>()
    val employeesFacultiesDao by inject<EmployeesFacultiesDao>()
    val employeesDepartmentsDao by inject<EmployeesDepartmentsDao>()

    post {
        val parameters = call.receiveParameters()

        val login = parameters.getStringOrThrow("login")
        val password = parameters.getStringOrThrow("password")

        val users = usersDao.allUsers()

        val logins = users.map(User::login)
        val passwords = users.map(User::password)

        if (login !in logins) {
            throw WrongCredentialsException
        }

        val loginIndex = logins.indexOf(login)

        if (passwords[loginIndex] != password) {
            throw WrongCredentialsException
        }

        val user = users[loginIndex]

        val employee = employeesDao.singleEmployee(user.employeeId) ?: throw ContentNotFoundException
        val facultyId: Int? = if (employee.isAdmin()) {
            employeesFacultiesDao.singleFacultyIdByEmployeeId(employee.id)
        } else null
        val departmentIds: List<Int> = employeesDepartmentsDao.allDepartmentIdsByEmployeeId(employee.id)

        val accessToken = JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("id", user.id)
            .sign(Algorithm.HMAC256(SecretsController.jwtSecret))

        respondSuccess {
            AuthResponse(
                userId = user.id,
                accessToken = accessToken,
                facultyId = facultyId,
                departmentIds = departmentIds
            )
        }
    }
}

private fun Route.modifySession() {
    val usersDao by inject<UsersDao>()
    val employeesDepartmentsDao by inject<EmployeesDepartmentsDao>()

    patch {
        val principal = call.principal<UserPrincipal>() ?: throw SessionExpiredException
        val user = principal.user
        usersDao.singleUser(user.id) ?: throw ContentNotFoundException

        val availableDepartmentIds = employeesDepartmentsDao.allDepartmentsByEmployeeId(user.employeeId)
            .map(Department::id)

        val departmentId = call.request.queryParameters.getIntOrThrow("departmentId")
        if (departmentId !in availableDepartmentIds) {
            throw UnavailableDepartmentId
        }

        val modifiedToken = JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("id", user.id)
            .withClaim("departmentId", departmentId)
            .sign(Algorithm.HMAC256(SecretsController.jwtSecret))

        respondSuccess {
            ModifyTokenResponse(
                departmentId = departmentId,
                modifiedToken = modifiedToken
            )
        }
    }
}

private data class AuthResponse(
    val userId: Int,
    val accessToken: String,
    val facultyId: Int?,
    val departmentIds: List<Int>
)

private data class ModifyTokenResponse(
    val departmentId: Int,
    val modifiedToken: String
)

data object WrongCredentialsException : Throwable()
