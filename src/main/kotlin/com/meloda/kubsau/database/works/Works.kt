package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.students.Students
import com.meloda.kubsau.database.worktypes.WorkTypes
import org.jetbrains.exposed.dao.id.IntIdTable

object Works : IntIdTable() {
    val typeId = integer("typeId").references(WorkTypes.id)
    val disciplineId = integer("disciplineId").references(Disciplines.id)
    val studentId = integer("studentId").references(Students.id)
    val registrationDate = integer("registrationDate")
    val title = text("title")
}
