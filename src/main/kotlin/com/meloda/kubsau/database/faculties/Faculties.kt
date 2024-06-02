package com.meloda.kubsau.database.faculties

import org.jetbrains.exposed.dao.id.IntIdTable

object Faculties : IntIdTable() {
    val title = text("title")
}
