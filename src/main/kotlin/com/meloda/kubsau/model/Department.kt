package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Department(
    val id: Int
)

object Departments : Table() {
    val id = integer("id").autoIncrement()
    val title = text("title")
    val phone = text("phone")

    override val primaryKey = PrimaryKey(id)
}
