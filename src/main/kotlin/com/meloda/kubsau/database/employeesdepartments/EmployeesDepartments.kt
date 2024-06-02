package com.meloda.kubsau.database.employeesdepartments

import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.employees.Employees
import org.jetbrains.exposed.dao.id.IntIdTable

object EmployeesDepartments : IntIdTable() {
    val employeeId = integer("employeeId").references(Employees.id)
    val departmentId = integer("departmentId").references(Departments.id)
}
