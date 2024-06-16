package com.meloda.kubsau.database.employees

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.Employee

interface EmployeeDao : Dao<Employee> {

    suspend fun allEmployees(facultyId: Int?, offset: Int?, limit: Int?): List<Employee>
    suspend fun allTeachers(offset: Int?, limit: Int?, departmentIds: List<Int>?): List<Employee>
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
