package com.meloda.kubsau.database.students

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.database.directivities.Directivities
import com.meloda.kubsau.database.faculties.Faculties
import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.heads.Heads
import com.meloda.kubsau.model.Student
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class StudentDaoImpl : StudentDao {

    override suspend fun allStudents(
        facultyId: Int?,
        offset: Int?,
        limit: Int?
    ): List<Student> = dbQuery {
        val dbQuery = Students
            .innerJoin(Groups, { Students.groupId }, { Groups.id })
            .innerJoin(Directivities, { Groups.directivityId }, { Directivities.id })
            .innerJoin(Heads, { Directivities.headId }, { Heads.id })
            .innerJoin(Faculties, { Heads.facultyId }, { Faculties.id })
            .selectAll()
            .apply {
                if (limit != null) {
                    limit(limit, ((offset ?: 0).toLong()))
                }
            }
            .orderBy(
                column = Students.id,
                order = SortOrder.DESC
            )

        facultyId?.let { dbQuery.andWhere { Faculties.id eq facultyId } }

        dbQuery.map(::mapResultRow)
    }

    override suspend fun allStudentsByIds(studentIds: List<Int>): List<Student> = dbQuery {
        Students
            .selectAll()
            .where { Students.id inList studentIds }
            .orderBy(
                column = Students.id,
                order = SortOrder.DESC
            )
            .map(::mapResultRow)
    }

    override suspend fun allStudentsByGroupId(groupId: Int): List<Student> = dbQuery {
        Students
            .selectAll()
            .where { Students.groupId eq groupId }
            .orderBy(
                column = Students.id,
                order = SortOrder.DESC
            )
            .map(::mapResultRow)
    }

    override suspend fun allStudentsByGroupIds(groupIds: List<Int>): List<Student> = dbQuery {
        Students
            .selectAll()
            .where { Students.groupId inList groupIds }
            .orderBy(
                column = Students.id,
                order = SortOrder.DESC
            )
            .map(::mapResultRow)
    }

    override suspend fun allStudentsByGroupIdsAsMap(
        groupIds: List<Int>,
        learnersOnly: Boolean?
    ): HashMap<Int, List<Student>> = dbQuery {
        val studentsMap = hashMapOf<Int, List<Student>>()

        val dbQuery = Students
            .selectAll()
            .where { Students.groupId inList groupIds }
            .orderBy(
                column = Students.id,
                order = SortOrder.DESC
            )

        learnersOnly?.let { dbQuery.andWhere { Students.status eq Student.STATUS_LEARNING } }

        val allStudents = dbQuery.map(::mapResultRow)

        groupIds.forEach { groupId ->
            allStudents
                .filter { student -> student.groupId == groupId }
                .let { students -> studentsMap[groupId] = students }
        }

        studentsMap
    }

    override suspend fun allStudentsBySearch(
        facultyId: Int?,
        offset: Int?,
        limit: Int?,
        groupId: Int?,
        gradeId: Int?,
        status: Int?,
        query: String?
    ): List<Student> = dbQuery {
        val dbQuery = Students
            .innerJoin(Groups, { Students.groupId }, { Groups.id })
            .innerJoin(Directivities, { Groups.directivityId }, { Directivities.id })
            .innerJoin(Heads, { Directivities.headId }, { Heads.id })
            .innerJoin(Faculties, { Heads.facultyId }, { Faculties.id })
            .select(Students.columns)
            .apply {
                if (limit != null) {
                    limit(limit, ((offset ?: 0).toLong()))
                }
            }
            .orderBy(
                column = Students.id,
                order = SortOrder.DESC
            )

        facultyId?.let { dbQuery.andWhere { Faculties.id eq facultyId } }
        groupId?.let { dbQuery.andWhere { Students.groupId eq groupId } }
        gradeId?.let { dbQuery.andWhere { Directivities.gradeId eq gradeId } }
        status?.let { dbQuery.andWhere { Students.status eq status } }

        query?.let { q -> "%$q%" }?.let { q ->
            dbQuery.andWhere {
                (Students.lastName.lowerCase() like q) or
                        (Students.firstName.lowerCase() like q) or
                        (Students.middleName.lowerCase() like q)
            }
        }

        dbQuery.map(Student::mapFromDb)
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

    override suspend fun updateStudent(
        studentId: Int,
        firstName: String,
        lastName: String,
        middleName: String?,
        groupId: Int,
        status: Int
    ): Boolean = dbQuery {
        Students.update(where = { Students.id eq studentId }) {
            it[Students.firstName] = firstName
            it[Students.lastName] = lastName
            it[Students.middleName] = middleName
            it[Students.groupId] = groupId
            it[Students.status] = status
        } > 0
    }

    override suspend fun deleteStudent(studentId: Int): Boolean = dbQuery {
        Students.deleteWhere { Students.id eq studentId } > 0
    }

    override suspend fun deleteStudents(studentIds: List<Int>): Boolean = dbQuery {
        Students.deleteWhere { Students.id inList studentIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Student = Student.mapFromDb(row)
}
