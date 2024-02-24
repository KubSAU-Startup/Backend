package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Teacher(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val departmentId: Int
)

object Teachers : IntIdTable() {
    val firstName = text("firstName")
    val lastName = text("lastName")
    val middleName = text("middleName")
    val departmentId = integer("departmentId").references(Departments.id)
}
