package com.meloda.kubsau.database.employeesfaculties

import com.meloda.kubsau.base.RefDao
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.model.Faculty

interface EmployeeFacultyDao : RefDao<Employee, Faculty> {

    suspend fun allReferences(): List<Pair<Employee, Faculty>>
    suspend fun singleFacultyByEmployeeId(employeeId: Int): Faculty?
    suspend fun singleFacultyIdByEmployeeId(employeeId: Int): Int?
    suspend fun addNewReference(employeeId: Int, facultyId: Int): Boolean
}
