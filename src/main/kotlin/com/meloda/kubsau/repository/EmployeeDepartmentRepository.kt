package com.meloda.kubsau.repository

import com.meloda.kubsau.database.employeesdepartments.EmployeeDepartmentDao
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Employee

interface EmployeeDepartmentRepository {
    suspend fun getTeachersByDepartmentId(departmentId: Int): List<Employee>
    suspend fun getDepartmentIdsByEmployeeId(employeeId: Int): List<Int>
    suspend fun getDepartmentsByEmployeeId(employeeId: Int): List<Department>
}

class EmployeeDepartmentRepositoryImpl(private val dao: EmployeeDepartmentDao) : EmployeeDepartmentRepository {

    override suspend fun getTeachersByDepartmentId(departmentId: Int): List<Employee> =
        dao.allTeachersByDepartmentId(departmentId)

    override suspend fun getDepartmentIdsByEmployeeId(employeeId: Int): List<Int> =
        dao.allDepartmentIdsByEmployeeId(employeeId)

    override suspend fun getDepartmentsByEmployeeId(employeeId: Int): List<Department> =
        dao.allDepartmentsByEmployeeId(employeeId)
}
