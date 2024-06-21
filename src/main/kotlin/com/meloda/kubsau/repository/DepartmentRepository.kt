package com.meloda.kubsau.repository

import com.meloda.kubsau.database.departments.DepartmentDao
import com.meloda.kubsau.model.Department

interface DepartmentRepository {
    suspend fun isExist(departmentId: Int): Boolean
    suspend fun getAllDepartments(allowedDepartmentIds: List<Int>?): List<Department>
    suspend fun getDepartmentsByIds(departmentIds: List<Int>): List<Department>
    suspend fun getDepartmentById(departmentId: Int): Department?
    suspend fun addDepartment(title: String, phone: String): Department?
    suspend fun editDepartment(departmentId: Int, title: String, phone: String): Boolean
    suspend fun deleteDepartmentById(departmentId: Int): Boolean
    suspend fun deleteDepartmentsByIds(departmentIds: List<Int>): Boolean
}

class DepartmentRepositoryImpl(private val dao: DepartmentDao) : DepartmentRepository {
    override suspend fun isExist(departmentId: Int): Boolean = dao.isExist(departmentId)

    override suspend fun getAllDepartments(allowedDepartmentIds: List<Int>?): List<Department> =
        dao.allDepartments(allowedDepartmentIds)

    override suspend fun getDepartmentsByIds(departmentIds: List<Int>): List<Department> =
        dao.allDepartmentsByIds(departmentIds)

    override suspend fun getDepartmentById(departmentId: Int): Department? =
        dao.singleDepartment(departmentId)

    override suspend fun addDepartment(title: String, phone: String): Department? =
        dao.addNewDepartment(title, phone)

    override suspend fun editDepartment(departmentId: Int, title: String, phone: String): Boolean =
        dao.updateDepartment(departmentId, title, phone)

    override suspend fun deleteDepartmentById(departmentId: Int): Boolean =
        dao.deleteDepartment(departmentId)

    override suspend fun deleteDepartmentsByIds(departmentIds: List<Int>): Boolean =
        dao.deleteDepartments(departmentIds)
}
