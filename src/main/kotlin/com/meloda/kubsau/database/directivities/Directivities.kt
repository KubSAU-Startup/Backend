package com.meloda.kubsau.database.directivities

import com.meloda.kubsau.database.grades.Grades
import com.meloda.kubsau.database.heads.Heads
import org.jetbrains.exposed.dao.id.IntIdTable

object Directivities : IntIdTable() {
    val title = text("title")
    val headId = integer("headId").references(Heads.id)
    val gradeId = integer("gradeId").references(Grades.id)
}
