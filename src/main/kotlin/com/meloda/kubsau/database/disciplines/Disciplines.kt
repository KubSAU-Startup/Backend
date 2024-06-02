package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.database.departments.Departments
import org.jetbrains.exposed.dao.id.IntIdTable

object Disciplines : IntIdTable() {
    val title = text("title")
    val departmentId = integer("departmentId").references(Departments.id)
}
