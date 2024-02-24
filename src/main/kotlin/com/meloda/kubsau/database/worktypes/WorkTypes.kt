package com.meloda.kubsau.database.worktypes

import org.jetbrains.exposed.dao.id.IntIdTable

object WorkTypes : IntIdTable() {
    val title = text("title")
    val editable = integer("editable").default(1)
}
