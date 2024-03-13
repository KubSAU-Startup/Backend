package com.meloda.kubsau.database.users

import com.meloda.kubsau.database.departments.Departments
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val login = text("login")
    val password = text("password")
    val type = integer("type")
    val departmentId = integer("departmentId").references(Departments.id)
}
