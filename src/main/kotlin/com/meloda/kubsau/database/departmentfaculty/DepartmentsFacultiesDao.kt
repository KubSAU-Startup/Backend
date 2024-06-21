package com.meloda.kubsau.database.departmentfaculty

import com.meloda.kubsau.base.RefDao
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Faculty

interface DepartmentsFacultiesDao : RefDao<Department, Faculty> {

    suspend fun getAll(): List<Int>
    suspend fun getDepartmentsByFacultyId(facultyId: Int): List<Department>
    suspend fun getDepartmentIdsByFacultyId(facultyId: Int): List<Int>
    suspend fun addReference(facultyId: Int, departmentId: Int): Boolean
    suspend fun deleteReference(facultyId: Int, departmentId: Int): Boolean
}
