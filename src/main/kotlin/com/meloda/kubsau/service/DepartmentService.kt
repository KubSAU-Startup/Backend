package com.meloda.kubsau.service

import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.repository.DepartmentRepository
import com.meloda.kubsau.repository.EmployeeDepartmentRepository

interface DepartmentService {
    suspend fun isExist(departmentId: Int): Boolean
    suspend fun getAllDepartments(allowedDepartmentIds: List<Int>?): List<Department>
    suspend fun getDepartmentByIds(departmentIds: List<Int>): List<Department>
    suspend fun getDepartmentById(departmentId: Int): Department?
    suspend fun getTeachersInDepartment(departmentId: Int): List<Employee>
    suspend fun addDepartment(title: String, phone: String): Department?
    suspend fun editDepartment(departmentId: Int, title: String, phone: String): Boolean
    suspend fun deleteDepartment(departmentId: Int): Boolean
    suspend fun deleteDepartments(departmentIds: List<Int>): Boolean
}

class DepartmentServiceImpl(
    private val repository: DepartmentRepository,
    private val employeeDepartmentRepository: EmployeeDepartmentRepository
) : DepartmentService {
    override suspend fun isExist(departmentId: Int): Boolean = repository.isExist(departmentId)

    override suspend fun getAllDepartments(allowedDepartmentIds: List<Int>?): List<Department> =
        repository.getAllDepartments(allowedDepartmentIds)

    override suspend fun getDepartmentByIds(departmentIds: List<Int>): List<Department> =
        repository.getDepartmentsByIds(departmentIds)

    override suspend fun getDepartmentById(departmentId: Int): Department? =
        repository.getDepartmentById(departmentId)

    override suspend fun getTeachersInDepartment(departmentId: Int): List<Employee> =
        employeeDepartmentRepository.getTeachersByDepartmentId(departmentId)

    override suspend fun addDepartment(title: String, phone: String): Department? =
        repository.addDepartment(title, phone)

    override suspend fun editDepartment(departmentId: Int, title: String, phone: String): Boolean =
        repository.editDepartment(departmentId, title, phone)

    override suspend fun deleteDepartment(departmentId: Int): Boolean =
        repository.deleteDepartmentById(departmentId)

    override suspend fun deleteDepartments(departmentIds: List<Int>): Boolean =
        repository.deleteDepartmentsByIds(departmentIds)
}
