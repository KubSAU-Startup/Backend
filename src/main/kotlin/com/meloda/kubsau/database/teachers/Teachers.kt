package com.meloda.kubsau.database.teachers

import com.meloda.kubsau.database.departments.Departments
import org.jetbrains.exposed.dao.id.IntIdTable

object Teachers : IntIdTable() {
    val firstName = text("firstName")
    val lastName = text("lastName")
    val middleName = text("middleName")
    val departmentId = integer("departmentId").references(Departments.id)
}
