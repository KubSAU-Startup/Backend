package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Specialization(
    val id: Int
)

object Specializations : Table() {
    val id = integer("id").autoIncrement()
    val title = text("title")

    override val primaryKey = PrimaryKey(id)
}
