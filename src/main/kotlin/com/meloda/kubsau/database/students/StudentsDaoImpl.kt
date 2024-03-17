package com.meloda.kubsau.database.students

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Student
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class StudentsDaoImpl : StudentsDao {
    override suspend fun allStudents(): List<Student> = dbQuery {
        Students.selectAll().map(::mapResultRow)
    }

    override suspend fun singleStudent(studentId: Int): Student? = dbQuery {
        Students
            .selectAll()
            .where { Students.id eq studentId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewStudent(
        firstName: String,
        lastName: String,
        middleName: String,
        groupId: Int,
        status: Int
    ): Student? = dbQuery {
        Students.insert {
            it[Students.firstName] = firstName
            it[Students.lastName] = lastName
            it[Students.middleName] = middleName
            it[Students.groupId] = groupId
            it[Students.status] = status
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteStudent(studentId: Int): Boolean = dbQuery {
        Students.deleteWhere { Students.id eq studentId } > 0
    }

    override fun mapResultRow(row: ResultRow): Student = Student.mapResultRow(row)
}
