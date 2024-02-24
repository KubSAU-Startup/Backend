package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class WorkType(
    val id: Int,
    val title: String
)

object WorkTypes : Table() {
    val id = integer("id").autoIncrement()
    val title = text("title")
    val editable = integer("editable").default(1)

    override val primaryKey = PrimaryKey(id)
}
