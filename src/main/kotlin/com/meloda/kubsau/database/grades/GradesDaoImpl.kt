package com.meloda.kubsau.database.grades

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Grade
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class GradesDaoImpl : GradesDao {

    override suspend fun allGrades(): List<Grade> = dbQuery {
        Grades.selectAll().map(::mapResultRow)
    }

    override suspend fun addNewGrade(title: String): Grade? = dbQuery {
        Grades.insert {
            it[Grades.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override fun mapResultRow(row: ResultRow): Grade = Grade.mapResultRow(row)
}
