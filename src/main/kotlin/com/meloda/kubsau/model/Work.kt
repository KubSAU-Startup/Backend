package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Work(
    val id: Int,
)

object Works : Table() {
    val id = integer("id").autoIncrement()
    val typeId = integer("typeId").entityId()
    val disciplineId = integer("disciplineId").entityId()
    val studentId = integer("studentId").entityId()
    val registrationDate = integer("registrationDate")
    val title = text("title")

    override val primaryKey = PrimaryKey(id)
}
