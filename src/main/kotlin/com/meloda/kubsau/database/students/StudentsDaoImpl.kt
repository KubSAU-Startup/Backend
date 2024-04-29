package com.meloda.kubsau.database.students

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Student
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class StudentsDaoImpl : StudentsDao {

    override suspend fun allStudents(): List<Student> = dbQuery {
        Students.selectAll().map(::mapResultRow)
    }

    override suspend fun allStudentsByIds(studentIds: List<Int>): List<Student> = dbQuery {
        Students
            .selectAll()
            .where { Students.id inList studentIds }
            .map(::mapResultRow)
    }

    override suspend fun allStudentsByGroupId(groupId: Int): List<Student> = dbQuery {
        Students
            .selectAll()
            .where { Students.groupId eq groupId }
            .map(::mapResultRow)
    }

    override suspend fun allStudentsByGroupIds(groupIds: List<Int>): List<Student> = dbQuery {
        Students
            .selectAll()
            .where { Students.groupId inList groupIds }
            .map(::mapResultRow)
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
        middleName: String?,
        groupId: Int,
        statusId: Int
    ): Student? = dbQuery {
        Students.insert {
            it[Students.firstName] = firstName
            it[Students.lastName] = lastName
            it[Students.middleName] = middleName
            it[Students.groupId] = groupId
            it[Students.statusId] = statusId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateStudent(
        studentId: Int,
        firstName: String,
        lastName: String,
        middleName: String?,
        groupId: Int,
        statusId: Int
    ): Boolean = dbQuery {
        Students.update(where = { Students.id eq studentId }) {
            it[Students.firstName] = firstName
            it[Students.lastName] = lastName
            it[Students.middleName] = middleName
            it[Students.groupId] = groupId
            it[Students.statusId] = statusId
        } > 0
    }

    override suspend fun deleteStudent(studentId: Int): Boolean = dbQuery {
        Students.deleteWhere { Students.id eq studentId } > 0
    }

    override suspend fun deleteStudents(studentIds: List<Int>): Boolean = dbQuery {
        Students.deleteWhere { Students.id inList studentIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Student = Student.mapResultRow(row)
}
