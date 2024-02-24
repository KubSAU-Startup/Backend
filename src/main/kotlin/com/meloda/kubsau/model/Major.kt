package com.meloda.kubsau.model

import org.jetbrains.exposed.sql.Table

data class Major(val id: Int)

object Majors : Table() {
    val id = integer("id").autoIncrement()
    val code = text("code")
    val title = text("title")
    val abbreviation = text("abbreviation")

    override val primaryKey = PrimaryKey(id)
}
