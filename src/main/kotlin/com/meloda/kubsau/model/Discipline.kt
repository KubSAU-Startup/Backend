package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Discipline(
    val id: Int,
    val title: String
)

object Disciplines : IntIdTable() {
    val title = text("title")
}
