package com.meloda.kubsau.database.employeesfaculties

import com.meloda.kubsau.database.RefDao
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.model.Faculty

interface EmployeesFacultiesDao : RefDao<Employee, Faculty> {

    suspend fun allReferences(): List<Pair<Employee, Faculty>>
    suspend fun allEmployees(): List<Employee>
    suspend fun allFaculties(): List<Faculty>
    suspend fun addNewReference(employeeId: Int, facultyId: Int): Boolean
}
