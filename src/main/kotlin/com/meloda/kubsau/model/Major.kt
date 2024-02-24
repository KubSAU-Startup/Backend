package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Major(
    val id: Int,
    val code: String,
    val title: String,
    val abbreviation: String
)

object Majors : IntIdTable() {
    val code = text("code")
    val title = text("title")
    val abbreviation = text("abbreviation")
}
