package com.meloda.kubsau.route.account

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getBoolean
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDao
import com.meloda.kubsau.database.employeesfaculties.EmployeesFacultiesDao
import com.meloda.kubsau.database.sessions.SessionsDao
import com.meloda.kubsau.database.users.UsersDao
import com.meloda.kubsau.errors.SessionExpiredException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Faculty
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.accountRoutes() {
    authenticate {
        route("/account") {
            getAccountInfoRoute()
        }
    }
}

private fun Route.getAccountInfoRoute() {
    val usersDao by inject<UsersDao>()
    val sessionsDao by inject<SessionsDao>()
    val employeesDao by inject<EmployeesDao>()
    val employeesDepartmentsDao by inject<EmployeesDepartmentsDao>()
    val employeesFacultiesDao by inject<EmployeesFacultiesDao>()

    get {
        val principal = call.principal<JWTPrincipal>()

        val login = principal?.payload?.getClaim("login")?.asString() ?: throw UnknownException
        val userId = usersDao.singleUser(login = login)?.id ?: throw UnknownException
        val session = sessionsDao.singleSession(userId = userId) ?: throw SessionExpiredException
        val user = usersDao.singleUser(userId = session.userId) ?: throw UnknownException
        val employee = employeesDao.singleEmployee(user.employeeId) ?: throw UnknownException
        val extended = call.request.queryParameters.getBoolean("extended", false)

        val departments = employeesDepartmentsDao.allDepartmentsByEmployee(employee.id)

        val faculty = if (employee.isAdmin()) {
            employeesFacultiesDao.singleFacultyByEmplyeeId(employee.id)
        } else {
            null
        }

        respondSuccess {
            val accountInfo = if (employee.isAdmin()) null else {
                AccountInfo(
                    id = user.id,
                    type = employee.type,
                    login = user.login,
                    departmentIds = departments.map(Department::id)
                )
            }

            val accountAdminInfo = if (employee.isAdmin()) {
                AccountAdminInfo(
                    id = user.id,
                    type = employee.type,
                    login = user.login,
                    facultyId = faculty?.id ?: -1,
                    departmentIds = departments.map(Department::id)
                )
            } else null

            if (extended) {
                if (employee.isAdmin()) {
                    FullAccountAdminInfo(
                        info = requireNotNull(accountAdminInfo),
                        faculty = faculty,
                        departments = departments
                    )
                } else {
                    FullAccountInfo(
                        info = requireNotNull(accountInfo),
                        departments = departments
                    )
                }
            } else {
                if (employee.isAdmin()) {
                    accountAdminInfo
                } else {
                    accountInfo
                }
            }
        }
    }
}

private data class AccountInfo(
    val id: Int,
    val type: Int,
    val login: String,
    val departmentIds: List<Int>
)

private data class AccountAdminInfo(
    val id: Int,
    val type: Int,
    val login: String,
    val facultyId: Int,
    val departmentIds: List<Int>
)

private data class FullAccountInfo(
    val info: AccountInfo,
    val departments: List<Department>
)

private data class FullAccountAdminInfo(
    val info: AccountAdminInfo,
    val faculty: Faculty?,
    val departments: List<Department>
)
