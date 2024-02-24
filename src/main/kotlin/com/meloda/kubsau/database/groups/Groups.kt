package com.meloda.kubsau.database.groups

import com.meloda.kubsau.database.majors.Majors
import org.jetbrains.exposed.dao.id.IntIdTable

object Groups : IntIdTable() {
    val title = text("title")
    val majorId = integer("majorId").references(Majors.id)
}
