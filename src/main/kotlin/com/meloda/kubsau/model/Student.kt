package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Student(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val groupId: Int,
    val status: Int
)

object Students : IntIdTable() {
    val firstName = text("firstName")
    val lastName = text("lastName")
    val middleName = text("middleName")
    val groupId = integer("groupId").references(Groups.id)
    val status = integer("status")
}
