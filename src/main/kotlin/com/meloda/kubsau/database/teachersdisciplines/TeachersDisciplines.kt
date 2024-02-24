package com.meloda.kubsau.database.teachersdisciplines

import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.teachers.Teachers
import org.jetbrains.exposed.dao.id.IntIdTable

object TeachersDisciplines : IntIdTable() {
    val disciplineId = integer("disciplineId").references(Disciplines.id)
    val teacherId = integer("teacherId").references(Teachers.id)
}
