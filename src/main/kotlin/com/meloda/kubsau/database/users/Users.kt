package com.meloda.kubsau.database.users

import com.meloda.kubsau.database.employees.Employees
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val login = text("login").uniqueIndex()
    val password = text("password")
    val employeeId = integer("employeeId").references(Employees.id)
}
