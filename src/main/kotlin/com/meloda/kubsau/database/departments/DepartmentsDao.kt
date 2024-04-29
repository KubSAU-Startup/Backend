package com.meloda.kubsau.database.departments

import com.meloda.kubsau.database.FilterableDao
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.route.journal.JournalFilter

interface DepartmentsDao : FilterableDao<Department, JournalFilter> {

    suspend fun allDepartments(): List<Department>
    suspend fun allDepartmentsAsFilters(): List<JournalFilter>
    suspend fun allDepartmentsByIds(departmentIds: List<Int>): List<Department>
    suspend fun singleDepartment(id: Int): Department?
    suspend fun addNewDepartment(title: String, phone: String): Department?
    suspend fun updateDepartment(departmentId: Int, title: String, phone: String): Int
    suspend fun deleteDepartment(departmentId: Int): Boolean
    suspend fun deleteDepartments(departmentIds: List<Int>): Boolean
}
