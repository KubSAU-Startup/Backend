package com.meloda.kubsau.database.grades

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Grade
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class GradesDaoImpl : GradesDao {

    override suspend fun allGrades(
        offset: Int?,
        limit: Int?
    ): List<Grade> = dbQuery {
        Grades
            .selectAll()
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }
            .map(::mapResultRow)
    }

    override suspend fun allGradesByIds(gradeIds: List<Int>): List<Grade> = dbQuery {
        Grades
            .selectAll()
            .where { Grades.id inList gradeIds }
            .map(::mapResultRow)
    }

    override suspend fun singleGradeById(gradeId: Int): Grade? = dbQuery {
        Grades
            .selectAll()
            .where { Grades.id eq gradeId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewGrade(title: String): Grade? = dbQuery {
        Grades.insert {
            it[Grades.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateGrade(gradeId: Int, title: String): Boolean = dbQuery {
        Grades.update(where = { Grades.id eq gradeId }) {
            it[Grades.title] = title
        } > 0
    }

    override suspend fun deleteGrade(gradeId: Int): Boolean = dbQuery {
        Grades.deleteWhere { Grades.id eq gradeId } > 0
    }

    override suspend fun deleteGrades(gradeIds: List<Int>): Boolean = dbQuery {
        Grades.deleteWhere { Grades.id inList gradeIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Grade = Grade.mapResultRow(row)
}
