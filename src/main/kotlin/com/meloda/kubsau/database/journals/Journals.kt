package com.meloda.kubsau.database.journals

import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.students.Students
import com.meloda.kubsau.database.teachers.Teachers
import com.meloda.kubsau.database.works.Works
import org.jetbrains.exposed.dao.id.IntIdTable

object Journals : IntIdTable() {
    val studentId = integer("studentId").references(Students.id)
    val groupId = integer("groupId").references(Groups.id)
    val disciplineId = integer("disciplineId").references(Disciplines.id)
    val teacherId = integer("teacherId").references(Teachers.id)
    val workId = integer("workId").references(Works.id)
}
