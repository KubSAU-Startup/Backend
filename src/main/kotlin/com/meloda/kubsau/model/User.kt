package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class User(
    val id: Int,
    val email: String,
    // TODO: 24/02/2024, Danil Nikolaev: SECURITY, FOR FUCK's SAKE
    val password: String,
    val type: Int,
    val departmentId: Int
)

object Users : IntIdTable() {
    val email = text("email")
    val password = text("password")
    val type = integer("type")
    val departmentId = integer("departmentId").references(Departments.id)
}
