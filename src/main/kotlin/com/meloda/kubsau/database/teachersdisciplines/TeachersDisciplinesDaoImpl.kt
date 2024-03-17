package com.meloda.kubsau.database.teachersdisciplines

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.teachers.Teachers
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Teacher
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TeachersDisciplinesDaoImpl : TeachersDisciplinesDao {
    override suspend fun allItems(): List<Pair<Teacher, Discipline>> = dbQuery {
        TeachersDisciplines.innerJoin(Teachers).innerJoin(Disciplines)
            .selectAll()
            .map(::mapBothResultRow)
    }

    override suspend fun addNewReference(teacherId: Int, disciplineId: Int): Boolean = dbQuery {
        TeachersDisciplines.insert {
            it[TeachersDisciplines.teacherId] = teacherId
            it[TeachersDisciplines.disciplineId] = disciplineId
        }.resultedValues?.size != 0
    }

    override suspend fun deleteReference(teacherId: Int?, disciplineId: Int?): Boolean = dbQuery {
        val query = when {
            teacherId != null && disciplineId != null -> {
                (TeachersDisciplines.teacherId eq teacherId) and (TeachersDisciplines.disciplineId eq disciplineId)
            }

            teacherId != null -> {
                TeachersDisciplines.teacherId eq teacherId
            }

            disciplineId != null -> {
                TeachersDisciplines.disciplineId eq disciplineId
            }

            else -> null
        }

        query?.let { TeachersDisciplines.deleteWhere { query } > 0 } ?: false
    }


    override fun mapFirstResultRow(row: ResultRow): Teacher = Teacher(
        id = row[Teachers.id].value,
        firstName = row[Teachers.firstName],
        lastName = row[Teachers.lastName],
        middleName = row[Teachers.middleName],
        departmentId = row[Teachers.departmentId]
    )

    override fun mapSecondResultRow(row: ResultRow): Discipline = Discipline(
        id = row[Disciplines.id].value,
        title = row[Disciplines.title]
    )
}
