package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

data class Department(
    val id: Int,
    val title: String,
    val phone: String
)

object Departments : IntIdTable() {
    val title = text("title")
    val phone = text("phone")
}
