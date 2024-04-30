package com.meloda.kubsau.database.employees

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Employee

interface EmployeesDao : Dao<Employee> {

    suspend fun allEmployees(): List<Employee>
    suspend fun allTeachers(): List<Employee>
    suspend fun allEmployeesByIds(employeeIds: List<Int>): List<Employee>
    suspend fun singleEmployee(employeeId: Int): Employee?

    suspend fun addNewEmployee(
        lastName: String,
        firstName: String,
        middleName: String?,
        email: String,
        type: Int
    ): Employee?

    suspend fun updateEmployee(
        employeeId: Int,
        lastName: String,
        firstName: String,
        middleName: String?,
        email: String,
        type: Int
    ): Boolean

    suspend fun deleteEmployee(employeeId: Int): Boolean
    suspend fun deleteEmployees(employeeIds: List<Int>): Boolean
}
