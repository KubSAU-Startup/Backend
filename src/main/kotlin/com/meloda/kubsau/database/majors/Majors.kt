package com.meloda.kubsau.database.majors

import org.jetbrains.exposed.dao.id.IntIdTable

object Majors : IntIdTable() {
    val code = text("code")
    val title = text("title")
    val abbreviation = text("abbreviation")
}
