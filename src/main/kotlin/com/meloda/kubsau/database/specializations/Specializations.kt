package com.meloda.kubsau.database.specializations

import org.jetbrains.exposed.dao.id.IntIdTable

object Specializations : IntIdTable() {
    val title = text("title")
}
