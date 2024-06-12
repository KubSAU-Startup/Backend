package com.meloda.kubsau.repository

import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartmentsDao
import com.meloda.kubsau.database.employeesfaculties.EmployeesFacultiesDao
import com.meloda.kubsau.database.users.UserDao
import com.meloda.kubsau.model.AccountInfo
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.User
import com.meloda.kubsau.model.WrongCurrentPasswordException
import com.meloda.kubsau.plugins.UserPrincipal

interface UserRepository {
    suspend fun getAllUsers(): List<User>
    suspend fun getUsersByIds(userIds: List<Int>): List<User>
    suspend fun getUserById(userId: Int): User?
    suspend fun getAccountInfo(principal: UserPrincipal): AccountInfo?
    suspend fun updateAccountInfo(
        principal: UserPrincipal,
        currentPassword: String,
        newPassword: String
    ): Boolean
}

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val employeeDao: EmployeeDao,
    private val employeesDepartmentsDao: EmployeesDepartmentsDao,
    private val employeesFacultiesDao: EmployeesFacultiesDao
) : UserRepository {
    override suspend fun getAllUsers(): List<User> = userDao.allUsers()
    override suspend fun getUsersByIds(userIds: List<Int>): List<User> = userDao.allUsersByIds(userIds)
    override suspend fun getUserById(userId: Int): User? = userDao.singleUser(userId)

    override suspend fun getAccountInfo(principal: UserPrincipal): AccountInfo {
        val user = principal.user
        val employee = employeeDao.singleEmployee(user.employeeId) ?: throw ContentNotFoundException

        val departments = employeesDepartmentsDao.allDepartmentsByEmployeeId(employee.id)

        val faculty = if (employee.isAdmin()) {
            employeesFacultiesDao.singleFacultyByEmployeeId(employee.id)
        } else {
            null
        }

        return AccountInfo(
            id = user.id,
            type = employee.type,
            login = user.login,
            faculty = faculty,
            selectedDepartmentId = principal.selectedDepartmentId,
            departments = departments
        )
    }

    override suspend fun updateAccountInfo(
        principal: UserPrincipal,
        currentPassword: String,
        newPassword: String
    ): Boolean {
        val currentUser = userDao.singleUser(principal.user.id) ?: throw ContentNotFoundException

        if (currentPassword != currentUser.password) {
            throw WrongCurrentPasswordException
        }

        // TODO: 07/06/2024, Danil Nikolaev: validate password for security
        return userDao.updateUser(currentUser.id, currentUser.login, newPassword)
    }
}
