package com.meloda.kubsau.database.employeesfaculties

import com.meloda.kubsau.database.employees.Employees
import com.meloda.kubsau.database.faculties.Faculties
import org.jetbrains.exposed.dao.id.IntIdTable

object EmployeesFaculties : IntIdTable() {
    val employeeId = integer("employeeId").references(Employees.id)
    val facultyId = integer("facultyId").references(Faculties.id)
}
