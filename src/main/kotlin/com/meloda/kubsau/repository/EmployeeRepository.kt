package com.meloda.kubsau.repository

import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.model.Employee

interface EmployeeRepository {
    suspend fun getAllEmployees(
        facultyId: Int?,
        offset: Int?,
        limit: Int?
    ): List<Employee>

    suspend fun getEmployeesByIds(employeeIds: List<Int>): List<Employee>
    suspend fun getEmployeeById(employeeId: Int): Employee?

    suspend fun addEmployee(
        lastName: String,
        firstName: String,
        middleName: String,
        email: String,
        type: Int
    ): Employee?

    suspend fun editEmployee(
        employeeId: Int,
        lastName: String,
        firstName: String,
        middleName: String,
        email: String,
        type: Int
    ): Boolean

    suspend fun deleteEmployee(employeeId: Int): Boolean
    suspend fun deleteEmployees(employeeIds: List<Int>): Boolean
}

class EmployeeRepositoryImpl(private val dao: EmployeeDao) : EmployeeRepository {
    override suspend fun getAllEmployees(facultyId: Int?, offset: Int?, limit: Int?): List<Employee> =
        dao.allEmployees(facultyId, offset, limit)

    override suspend fun getEmployeesByIds(employeeIds: List<Int>): List<Employee> =
        dao.allEmployeesByIds(employeeIds)

    override suspend fun getEmployeeById(employeeId: Int): Employee? =
        dao.singleEmployee(employeeId)

    override suspend fun addEmployee(
        lastName: String,
        firstName: String,
        middleName: String,
        email: String,
        type: Int
    ): Employee? = dao.addNewEmployee(lastName, firstName, middleName, email, type)

    override suspend fun editEmployee(
        employeeId: Int,
        lastName: String,
        firstName: String,
        middleName: String,
        email: String,
        type: Int
    ): Boolean = dao.updateEmployee(employeeId, lastName, firstName, middleName, email, type)

    override suspend fun deleteEmployee(employeeId: Int): Boolean = dao.deleteEmployee(employeeId)

    override suspend fun deleteEmployees(employeeIds: List<Int>): Boolean = dao.deleteEmployees(employeeIds)
}
