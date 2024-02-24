package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Student(
    val id: Int
)

object Students : Table() {
    val id = integer("id").autoIncrement()
    val firstName = text("firstName")
    val lastName = text("lastName")
    val middleName = text("middleName")
    val groupId = integer("groupId").entityId()
    val status = integer("status")

    override val primaryKey = PrimaryKey(id)
}
