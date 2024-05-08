package com.meloda.kubsau.database.departments

import com.meloda.kubsau.database.FilterableDao
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.route.works.EntryFilter

interface DepartmentsDao : FilterableDao<Department, EntryFilter> {

    suspend fun allDepartments(): List<Department>
    suspend fun allDepartmentsAsFilters(): List<EntryFilter>
    suspend fun allDepartmentsByIds(departmentIds: List<Int>): List<Department>
    suspend fun singleDepartment(departmentId: Int): Department?
    suspend fun addNewDepartment(title: String, phone: String): Department?
    suspend fun updateDepartment(departmentId: Int, title: String, phone: String): Boolean
    suspend fun deleteDepartment(departmentId: Int): Boolean
    suspend fun deleteDepartments(departmentIds: List<Int>): Boolean
}
