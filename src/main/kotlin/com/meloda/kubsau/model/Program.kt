package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Program(
    val id: Int,
    val title: String,
    val semester: Int
)

object Programs : IntIdTable() {
    val title = text("title")
    val semester = integer("semester")
}
