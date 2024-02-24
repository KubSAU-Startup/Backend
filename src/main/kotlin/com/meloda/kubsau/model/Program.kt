package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Program(
    val id: Int
)

object Programs : Table() {
    val id = integer("id").autoIncrement()
    val title = text("title")
    val semester = integer("semester")

    override val primaryKey = PrimaryKey(id)
}
