package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class User(
    val id: Int,
    val email: String,
    // TODO: 24/02/2024, Danil Nikolaev: SECURITY, FOR FUCK's SAKE
    val password: String
)

object Users : Table() {
    val id = integer("id").autoIncrement()
    val email = text("email")
    val password = text("password")
    val type = integer("type")
    val departmentId = integer("departmentId").entityId()

    override val primaryKey = PrimaryKey(id)
}
