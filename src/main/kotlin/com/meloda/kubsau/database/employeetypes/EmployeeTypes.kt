package com.meloda.kubsau.database.employeetypes

import org.jetbrains.exposed.dao.id.IntIdTable

object EmployeeTypes : IntIdTable() {
    val title = text("title")
}
