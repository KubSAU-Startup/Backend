package com.meloda.kubsau.database.departments

import org.jetbrains.exposed.dao.id.IntIdTable

object Departments : IntIdTable() {
    val title = text("title")
    val phone = text("phone")
}
