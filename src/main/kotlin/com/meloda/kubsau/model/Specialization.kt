package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Specialization(
    val id: Int
)

object Specializations : IntIdTable() {
    val title = text("title")
}
