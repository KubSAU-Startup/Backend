package com.meloda.kubsau.database.grades

import org.jetbrains.exposed.dao.id.IntIdTable

object Grades : IntIdTable() {
    val title = text("title")
}
