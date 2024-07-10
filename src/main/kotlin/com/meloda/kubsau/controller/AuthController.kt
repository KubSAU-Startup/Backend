package com.meloda.kubsau.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.common.userPrincipal
import com.meloda.kubsau.config.SecretsController
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.employeesdepartments.EmployeeDepartmentDao
import com.meloda.kubsau.database.employeesfaculties.EmployeeFacultyDao
import com.meloda.kubsau.database.users.UserDao
import com.meloda.kubsau.model.AccessDeniedException
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.plugins.AUDIENCE
import com.meloda.kubsau.plugins.ISSUER
import com.meloda.kubsau.util.PasswordUtil
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class AuthController : BaseController {

    override fun Route.routes() {
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
        val employeeFacultyDao by inject<EmployeeFacultyDao>()
        val employeeDepartmentDao by inject<EmployeeDepartmentDao>()

        post {
            val parameters = call.receiveParameters()

            val login = parameters.getStringOrThrow("login")
            val password = parameters.getStringOrThrow("password")

            val user = userDao.singleUser(login) ?: throw WrongCredentialsException
            if (!PasswordUtil.checkPassword(password, user.passwordHash)) {
                throw WrongCredentialsException
            }

            val employee = employeeDao.singleEmployee(user.employeeId) ?: throw ContentNotFoundException
            val facultyId: Int? = if (employee.isAdmin()) {
                employeeFacultyDao.singleFacultyIdByEmployeeId(employee.id)
            } else null
            val departmentIds: List<Int> = employeeDepartmentDao.allDepartmentIdsByEmployeeId(employee.id)

            val selectedDepartmentId = departmentIds.singleOrNull()

            val accessToken = JWT.create()
                .withAudience(AUDIENCE)
                .withIssuer(ISSUER)
                .withClaim("id", user.id)
                .withClaim("type", employee.type)
                .withClaim("facultyId", facultyId)
                .withClaim("departmentIds", departmentIds.joinToString())
                .withClaim("selectedDepartmentId", selectedDepartmentId)
                .sign(Algorithm.HMAC256(SecretsController.jwtSecret))

            respondSuccess {
                AuthResponse(
                    userId = user.id,
                    accessToken = accessToken,
                    facultyId = facultyId,
                    departmentIds = departmentIds,
                    selectedDepartmentId = selectedDepartmentId
                )
            }
        }
    }

    private fun Route.modifySession() {
        val userDao by inject<UserDao>()
        val employeeDepartmentDao by inject<EmployeeDepartmentDao>()

        patch {
            val principal = call.userPrincipal()
            val user = principal.user
            userDao.singleUser(user.id) ?: throw ContentNotFoundException

            val availableDepartmentIds = employeeDepartmentDao.allDepartmentsByEmployeeId(user.employeeId)
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
                .withClaim("departmentIds", principal.departmentIds.joinToString())
                .withClaim("selectedDepartmentId", departmentId)
                .sign(Algorithm.HMAC256(SecretsController.jwtSecret))

            respondSuccess {
                ModifyTokenResponse(
                    departmentId = departmentId,
                    modifiedToken = modifiedToken
                )
            }
        }
    }
}

data object WrongCredentialsException : Throwable()

private data class AuthResponse(
    val userId: Int,
    val accessToken: String,
    val facultyId: Int?,
    val departmentIds: List<Int>,
    val selectedDepartmentId: Int?
)

private data class ModifyTokenResponse(
    val departmentId: Int,
    val modifiedToken: String
)
