package com.meloda.kubsau.model

import com.meloda.kubsau.database.employees.Employees
import org.jetbrains.exposed.sql.ResultRow

data class Employee(
    val id: Int,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val email: String?,
    val employeeTypeId: Int
) {

    companion object {

        fun mapResultRow(row: ResultRow): Employee = Employee(
            id = row[Employees.id].value,
            lastName = row[Employees.lastName],
            firstName = row[Employees.firstName],
            middleName = row[Employees.middleName],
            email = row[Employees.email],
            employeeTypeId = row[Employees.employeeTypeId]
        )
    }
}
