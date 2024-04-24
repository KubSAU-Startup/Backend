package com.meloda.kubsau.database.journals

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.students.Students
import com.meloda.kubsau.database.teachers.Teachers
import com.meloda.kubsau.database.works.Works
import com.meloda.kubsau.model.Journal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class JournalsDaoImpl : JournalsDao {

    override suspend fun allJournals(): List<Journal> = dbQuery {
        Journals
            .innerJoin(Students, { studentId }, { Students.id })
            .innerJoin(Groups, { Journals.groupId }, { Groups.id })
            .innerJoin(Disciplines, { Journals.disciplineId }, { Disciplines.id })
            .innerJoin(Teachers, { Journals.teacherId }, { Teachers.id })
            .innerJoin(Works, { Journals.workId }, { Works.id })
            .innerJoin(Departments, { Teachers.departmentId }, { Departments.id })
            .selectAll()
            .map(::mapResultRow)
    }

    override suspend fun allJournals(
        journalId: Int?,
        studentId: Int?,
        groupId: Int?,
        disciplineId: Int?,
        teacherId: Int?,
        workId: Int?,
        departmentId: Int?,
        workTypeId: Int?
    ) = dbQuery {
        val query = if (journalId != null) Journals.id eq journalId
        else {
            (studentId?.let { Journals.studentId eq studentId } ?: Op.TRUE) andIfNotNull
                    (groupId?.let { Journals.groupId eq groupId }) andIfNotNull
                    (disciplineId?.let { Journals.disciplineId eq disciplineId }) andIfNotNull
                    (teacherId?.let { Journals.teacherId eq teacherId }) andIfNotNull
                    (workId?.let { Journals.workId eq workId }) andIfNotNull
                    (departmentId?.let { Teachers.departmentId eq departmentId }) andIfNotNull
                    (workTypeId?.let { Disciplines.workTypeId eq workTypeId })
        }

        Journals
            .innerJoin(Students, { Journals.studentId }, { Students.id })
            .innerJoin(Groups, { Journals.groupId }, { Groups.id })
            .innerJoin(Disciplines, { Journals.disciplineId }, { Disciplines.id })
            .innerJoin(Teachers, { Journals.teacherId }, { Teachers.id })
            .innerJoin(Works, { Journals.workId }, { Works.id })
            .innerJoin(Departments, { Teachers.departmentId }, { Departments.id })
            .selectAll()
            .where { query }
            .map(::mapResultRow)
    }

    override suspend fun allJournalsByIds(journalIds: List<Int>): List<Journal> = dbQuery {
        Journals
            .innerJoin(Students, { studentId }, { Students.id })
            .innerJoin(Groups, { Journals.groupId }, { Groups.id })
            .innerJoin(Disciplines, { Journals.disciplineId }, { Disciplines.id })
            .innerJoin(Teachers, { Journals.teacherId }, { Teachers.id })
            .innerJoin(Works, { Journals.workId }, { Works.id })
            .innerJoin(Departments, { Teachers.departmentId }, { Departments.id })
            .selectAll()
            .where { Journals.id inList journalIds }
            .map(::mapResultRow)
    }

    override suspend fun singleById(journalId: Int): Journal? = dbQuery {
        Journals
            .innerJoin(Students, { studentId }, { Students.id })
            .innerJoin(Groups, { Journals.groupId }, { Groups.id })
            .innerJoin(Disciplines, { Journals.disciplineId }, { Disciplines.id })
            .innerJoin(Teachers, { Journals.teacherId }, { Teachers.id })
            .innerJoin(Works, { Journals.workId }, { Works.id })
            .innerJoin(Departments, { Teachers.departmentId }, { Departments.id })
            .selectAll()
            .where { Journals.id eq journalId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewJournal(
        studentId: Int,
        groupId: Int,
        disciplineId: Int,
        teacherId: Int,
        workId: Int
    ): Journal? = dbQuery {
        Journals.insert {
            it[Journals.studentId] = studentId
            it[Journals.groupId] = groupId
            it[Journals.disciplineId] = disciplineId
            it[Journals.teacherId] = teacherId
            it[Journals.workId] = workId
        }.resultedValues
            ?.singleOrNull()
            ?.let { row -> row[Journals.id].value }
            ?.let { id -> singleById(id) }
    }

    override suspend fun updateJournal(
        journalId: Int,
        studentId: Int,
        groupId: Int,
        disciplineId: Int,
        teacherId: Int,
        workId: Int
    ): Int = dbQuery {
        Journals.update(where = { Journals.id eq journalId }) {
            it[Journals.studentId] = studentId
            it[Journals.groupId] = groupId
            it[Journals.disciplineId] = disciplineId
            it[Journals.teacherId] = teacherId
            it[Journals.workId] = workId
        }
    }

    override suspend fun deleteJournal(journalId: Int): Boolean = dbQuery {
        Journals.deleteWhere { Journals.id eq journalId } > 0
    }

    override suspend fun deleteJournals(journalIds: List<Int>): Boolean = dbQuery {
        Journals.deleteWhere { Journals.id inList journalIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Journal = Journal.mapResultRow(row)
}
