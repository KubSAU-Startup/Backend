package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class WorkType(
    val id: Int,
    val title: String,
    val editable: Boolean
)

object WorkTypes : IntIdTable() {
    val title = text("title")
    val editable = integer("editable").default(1)
}
