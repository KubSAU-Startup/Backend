package com.meloda.kubsau.database.heads

import com.meloda.kubsau.database.faculties.Faculties
import org.jetbrains.exposed.dao.id.IntIdTable

object Heads : IntIdTable() {
    val code = text("code")
    val abbreviation = text("abbreviation")
    val title = text("title")
    val facultyId = integer("facultyId").references(Faculties.id)
}
