package com.meloda.kubsau.database.teachers

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Teacher
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class TeachersDaoImpl : TeachersDao {
    override suspend fun allTeachers(): List<Teacher> = dbQuery {
        Teachers.selectAll().map(::mapResultRow)
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

    override suspend fun deleteTeacher(teacherId: Int): Boolean = dbQuery {
        Teachers.deleteWhere { Teachers.id eq teacherId } > 0
    }

    override fun mapResultRow(row: ResultRow): Teacher = Teacher.mapResultRow(row)
}
