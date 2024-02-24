package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Group(
    val id: Int,
    val title: String
)

object Groups : Table() {
    val id = integer("id").autoIncrement()
    val title = text("title")
    val majorId = integer("majorId").entityId()

    override val primaryKey = PrimaryKey(id)
}
