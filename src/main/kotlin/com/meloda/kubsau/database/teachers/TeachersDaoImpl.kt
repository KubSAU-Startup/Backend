package com.meloda.kubsau.database.teachers

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Teacher
import com.meloda.kubsau.route.journal.JournalFilter
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class TeachersDaoImpl : TeachersDao {

    override suspend fun allTeachers(): List<Teacher> = dbQuery {
        Teachers.selectAll().map(::mapResultRow)
    }

    override suspend fun allTeachersAsFilters(): List<JournalFilter> = dbQuery {
        Teachers
            .select(
                Teachers.id,
                Teachers.lastName,
                Teachers.firstName,
                Teachers.middleName
            )
            .map(::mapFilterResultRow)
    }

    override suspend fun allTeachersByIds(teacherIds: List<Int>): List<Teacher> = dbQuery {
        Teachers
            .selectAll()
            .where { Teachers.id inList teacherIds }
            .map(::mapResultRow)
    }

    override suspend fun singleTeacher(teacherId: Int): Teacher? = dbQuery {
        Teachers
            .selectAll()
            .where { Teachers.id eq teacherId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewTeacher(
        firstName: String,
        lastName: String,
        middleName: String,
        departmentId: Int
    ): Teacher? = dbQuery {
        Teachers.insert {
            it[Teachers.firstName] = firstName
            it[Teachers.lastName] = lastName
            it[Teachers.middleName] = middleName
            it[Teachers.departmentId] = departmentId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateTeacher(
        teacherId: Int,
        firstName: String,
        lastName: String,
        middleName: String,
        departmentId: Int
    ): Int = dbQuery {
        Teachers.update(where = { Teachers.id eq teacherId }) {
            it[Teachers.firstName] = firstName
            it[Teachers.lastName] = lastName
            it[Teachers.middleName] = middleName
            it[Teachers.departmentId] = departmentId
        }
    }

    override suspend fun deleteTeacher(teacherId: Int): Boolean = dbQuery {
        Teachers.deleteWhere { Teachers.id eq teacherId } > 0
    }

    override suspend fun deleteTeachers(teacherIds: List<Int>): Boolean = dbQuery {
        Teachers.deleteWhere { Teachers.id inList teacherIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Teacher = Teacher.mapResultRow(row)

    override fun mapFilterResultRow(row: ResultRow): JournalFilter = JournalFilter(
        id = row[Teachers.id].value,
        title = "%s %s %s".format(
            row[Teachers.lastName],
            row[Teachers.firstName],
            row[Teachers.middleName]
        )
    )
}
