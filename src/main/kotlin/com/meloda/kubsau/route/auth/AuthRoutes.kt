package com.meloda.kubsau.route.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.common.checkPassword
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.config.SecretsController
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDao
import com.meloda.kubsau.database.employeesfaculties.EmployeesFacultiesDao
import com.meloda.kubsau.database.users.UserDao
import com.meloda.kubsau.model.*
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
    val userDao by inject<UserDao>()
    val employeeDao by inject<EmployeeDao>()
    val employeesFacultiesDao by inject<EmployeesFacultiesDao>()
    val employeesDepartmentsDao by inject<EmployeesDepartmentsDao>()

    post {
        val parameters = call.receiveParameters()

        val login = parameters.getStringOrThrow("login")
        val password = parameters.getStringOrThrow("password")

        val users = userDao.allUsers()

        val logins = users.map(User::login)
        val passwords = users.map(User::password)

        if (login !in logins) {
            throw WrongCredentialsException
        }

        val loginIndex = logins.indexOf(login)

        if (!checkPassword(password, passwords[loginIndex])) {
            throw WrongCredentialsException
        }

        val user = users[loginIndex]

        val employee = employeeDao.singleEmployee(user.employeeId) ?: throw ContentNotFoundException
        val facultyId: Int? = if (employee.isAdmin()) {
            employeesFacultiesDao.singleFacultyIdByEmployeeId(employee.id)
        } else null
        val departmentIds: List<Int> = employeesDepartmentsDao.allDepartmentIdsByEmployeeId(employee.id)

        val accessToken = JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("id", user.id)
            .withClaim("type", employee.type)
            .withClaim("facultyId", facultyId)
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
    val userDao by inject<UserDao>()
    val employeesDepartmentsDao by inject<EmployeesDepartmentsDao>()

    patch {
        val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException
        val user = principal.user
        userDao.singleUser(user.id) ?: throw ContentNotFoundException

        val availableDepartmentIds = employeesDepartmentsDao.allDepartmentsByEmployeeId(user.employeeId)
            .map(Department::id)

        val departmentId = call.request.queryParameters.getIntOrThrow("departmentId")
        if (departmentId !in availableDepartmentIds) {
            throw AccessDeniedException("Unavailable departmentId value: $departmentId")
        }

        val modifiedToken = JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("id", user.id)
            .withClaim("type", principal.type)
            .withClaim("facultyId", principal.facultyId)
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
