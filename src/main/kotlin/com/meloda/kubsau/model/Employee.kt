package com.meloda.kubsau.model

import com.meloda.kubsau.database.employees.Employees
import org.jetbrains.exposed.sql.ResultRow

data class Employee(
    val id: Int,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val email: String,
    val type: Int
) {
    val fullName: String
        get() = if (middleName == null) {
            "$firstName $lastName"
        } else {
            "$lastName $firstName $middleName"
        }

    fun isAdmin(): Boolean = type == TYPE_ADMIN
    fun isLaborant(): Boolean = type == TYPE_LABORANT
    fun isTeacher(): Boolean = type == TYPE_TEACHER

    companion object {

        const val TYPE_ADMIN = 1
        const val TYPE_LABORANT = 2
        const val TYPE_TEACHER = 3

        fun mapResultRow(row: ResultRow): Employee = Employee(
            id = row[Employees.id].value,
            lastName = row[Employees.lastName],
            firstName = row[Employees.firstName],
            middleName = row[Employees.middleName],
            email = row[Employees.email],
            type = row[Employees.type]
        )
    }
}
