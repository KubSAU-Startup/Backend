package com.meloda.kubsau.service

import com.meloda.kubsau.database.departmentfaculty.DepartmentsFacultiesDao
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.model.UnknownTokenException
import com.meloda.kubsau.plugins.UserPrincipal
import com.meloda.kubsau.repository.DepartmentRepository
import com.meloda.kubsau.repository.EmployeeDepartmentRepository

interface DepartmentService {
    suspend fun isExist(departmentId: Int): Boolean
    suspend fun getAllDepartments(principal: UserPrincipal): List<Department>
    suspend fun getDepartmentByIds(departmentIds: List<Int>): List<Department>
    suspend fun getDepartmentById(departmentId: Int): Department?
    suspend fun getTeachersInDepartment(departmentId: Int): List<Employee>
    suspend fun addDepartment(facultyId: Int, title: String, phone: String): Department?
    suspend fun editDepartment(departmentId: Int, title: String, phone: String): Boolean
    suspend fun deleteDepartment(facultyId: Int, departmentId: Int): Boolean
    suspend fun deleteDepartments(facultyId: Int, departmentIds: List<Int>): Boolean
}

class DepartmentServiceImpl(
    private val repository: DepartmentRepository,
    private val employeeDepartmentRepository: EmployeeDepartmentRepository,
    private val departmentsFacultiesDao: DepartmentsFacultiesDao
) : DepartmentService {
    override suspend fun isExist(departmentId: Int): Boolean = repository.isExist(departmentId)

    override suspend fun getAllDepartments(principal: UserPrincipal): List<Department> {
        return if (principal.type == Employee.TYPE_ADMIN) {
            val facultyId = principal.facultyId ?: throw UnknownTokenException
            departmentsFacultiesDao.getDepartmentsByFacultyId(facultyId)
        } else repository.getDepartmentsByIds(principal.departmentIds)
    }

    override suspend fun getDepartmentByIds(departmentIds: List<Int>): List<Department> =
        repository.getDepartmentsByIds(departmentIds)

    override suspend fun getDepartmentById(departmentId: Int): Department? =
        repository.getDepartmentById(departmentId)

    override suspend fun getTeachersInDepartment(departmentId: Int): List<Employee> =
        employeeDepartmentRepository.getTeachersByDepartmentId(departmentId)

    override suspend fun addDepartment(facultyId: Int, title: String, phone: String): Department? {
        val department = repository.addDepartment(title, phone) ?: return null

        departmentsFacultiesDao.addReference(facultyId, department.id)
        return department
    }

    override suspend fun editDepartment(departmentId: Int, title: String, phone: String): Boolean =
        repository.editDepartment(departmentId, title, phone)

    override suspend fun deleteDepartment(facultyId: Int, departmentId: Int): Boolean {
        val deleted = departmentsFacultiesDao.deleteReference(facultyId, departmentId)
        if (deleted) {
            return repository.deleteDepartmentById(departmentId)
        }

        return false
    }

    override suspend fun deleteDepartments(facultyId: Int, departmentIds: List<Int>): Boolean {
        val deleted = departmentsFacultiesDao.deleteReferences(facultyId, departmentIds)
        if (deleted) {
            return repository.deleteDepartmentsByIds(departmentIds)
        }

        return false
    }
}
