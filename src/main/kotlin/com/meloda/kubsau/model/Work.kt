package com.meloda.kubsau.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Work(
    val id: Int,
    val typeId: Int,
    val disciplineId: Int,
    val studentId: Int,
    val registrationDate: Int,
    val title: String
)

object Works : IntIdTable() {
    val typeId = integer("typeId").references(WorkTypes.id)
    val disciplineId = integer("disciplineId").references(Disciplines.id)
    val studentId = integer("studentId").references(Students.id)
    val registrationDate = integer("registrationDate")
    val title = text("title")
}
