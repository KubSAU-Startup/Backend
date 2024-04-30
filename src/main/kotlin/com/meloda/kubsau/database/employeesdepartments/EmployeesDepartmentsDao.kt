package com.meloda.kubsau.database.employeesdepartments

import com.meloda.kubsau.database.RefDao
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Employee

interface EmployeesDepartmentsDao : RefDao<Employee, Department> {

    suspend fun allReferences(): List<Pair<Employee, Department>>
    suspend fun allEmployees(): List<Employee>
    suspend fun allDepartments(): List<Department>
    suspend fun singleDepartmentByEmployeeId(employeeId: Int): Department?
    suspend fun addNewReference(employeeId: Int, departmentId: Int): Boolean
}
