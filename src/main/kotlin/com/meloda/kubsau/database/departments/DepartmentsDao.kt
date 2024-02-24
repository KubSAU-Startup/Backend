package com.meloda.kubsau.database.departments

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Department

interface DepartmentsDao : Dao<Department> {

    suspend fun allDepartments(): List<Department>
    suspend fun singleDepartment(id: Int): Department?
    suspend fun addNewDepartment(title: String, phone: String): Department?
    suspend fun deleteDepartment(id: Int): Boolean
}
