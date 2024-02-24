package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Teacher(val id: Int, val fullName: String)

object Teachers : Table() {
    val id = integer("id").autoIncrement()
    val firstName = text("firstName")
    val lastName = text("lastName")
    val middleName = text("middleName")
    val departmentId = integer("departmentId").entityId()

    override val primaryKey = PrimaryKey(id)
}
