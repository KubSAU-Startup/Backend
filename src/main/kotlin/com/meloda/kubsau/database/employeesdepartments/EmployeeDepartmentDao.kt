package com.meloda.kubsau.database.employeesdepartments

import com.meloda.kubsau.base.RefDao
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Employee

interface EmployeeDepartmentDao : RefDao<Employee, Department> {

    suspend fun allReferences(): List<Pair<Employee, Department>>
    suspend fun allTeachersByDepartmentId(departmentId: Int): List<Employee>
    suspend fun allDepartmentsByEmployeeId(employeeId: Int): List<Department>
    suspend fun allDepartmentIdsByEmployeeId(employeeId: Int): List<Int>
    suspend fun addNewReference(employeeId: Int, departmentId: Int): Boolean
}
