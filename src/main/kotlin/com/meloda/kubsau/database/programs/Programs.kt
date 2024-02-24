package com.meloda.kubsau.database.programs

import org.jetbrains.exposed.dao.id.IntIdTable

object Programs : IntIdTable() {
    val title = text("title")
    val semester = integer("semester")
}
