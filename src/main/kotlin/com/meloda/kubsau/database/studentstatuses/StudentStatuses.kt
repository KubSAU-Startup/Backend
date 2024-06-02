package com.meloda.kubsau.database.studentstatuses

import org.jetbrains.exposed.dao.id.IntIdTable

object StudentStatuses : IntIdTable() {
    val title = text("title")
}
