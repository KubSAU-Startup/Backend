package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Discipline(
    val id: Int,
    val title: String
)

object Disciplines : Table() {
    val id = integer("id").autoIncrement()
    val title = text("title")

    override val primaryKey = PrimaryKey(id)
}
