package com.meloda.kubsau.database.disciplines

import org.jetbrains.exposed.dao.id.IntIdTable

object Disciplines : IntIdTable() {
    val title = text("title")
}
