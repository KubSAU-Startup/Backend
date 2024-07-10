package com.meloda.kubsau.database.departments

import com.meloda.kubsau.base.FilterableDao
import com.meloda.kubsau.controller.EntryFilter
import com.meloda.kubsau.model.Department

interface DepartmentDao : FilterableDao<Department, EntryFilter> {

    suspend fun isExist(departmentId: Int): Boolean
    suspend fun allDepartments(allowedDepartmentIds: List<Int>?): List<Department>
    suspend fun allDepartmentsAsFilters(departmentIds: List<Int>?): List<EntryFilter>
    suspend fun allDepartmentsByIds(departmentIds: List<Int>): List<Department>
    suspend fun singleDepartment(departmentId: Int): Department?
    suspend fun addNewDepartment(title: String, phone: String): Department?
    suspend fun updateDepartment(departmentId: Int, title: String, phone: String): Boolean
    suspend fun deleteDepartment(departmentId: Int): Boolean
    suspend fun deleteDepartments(departmentIds: List<Int>): Boolean
}
