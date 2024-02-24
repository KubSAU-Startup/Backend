package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Group(
    val id: Int,
    val title: String,
    val majorId: Int
)

object Groups : IntIdTable() {
    val title = text("title")
    val majorId = integer("majorId").references(Majors.id)
}
