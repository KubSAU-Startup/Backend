package com.meloda.kubsau.database.journals

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.students.Students
import com.meloda.kubsau.database.teachers.Teachers
import com.meloda.kubsau.database.works.Works
import com.meloda.kubsau.model.Journal
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class JournalsDaoImpl : JournalsDao {
    override suspend fun allJournals(): List<Journal> = dbQuery {
        Journals
            .innerJoin(Groups)
            .crossJoin(Students)
            .innerJoin(Disciplines)
            .innerJoin(Teachers)
            .innerJoin(Works)
            .selectAll()
            .map(::mapResultRow)
    }

    override suspend fun singleJournal(journalId: Int): Journal? = dbQuery {
        Journals
            .innerJoin(Groups)
            .innerJoin(Students)
            .innerJoin(Disciplines)
            .innerJoin(Teachers)
            .innerJoin(Works)
            .selectAll()
            .where { Journals.id eq journalId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun singleJournal(
        journalId: Int?,
        studentId: Int?,
        groupId: Int?,
        disciplineId: Int?,
        teacherId: Int?,
        workId: Int?
    ): List<Journal> = dbQuery {
        // TODO: 17/03/2024, Danil Nikolaev: filter in db, not in code
        Journals
            .innerJoin(Groups)
            .innerJoin(Students)
            .innerJoin(Disciplines)
            .innerJoin(Teachers)
            .innerJoin(Works)
            .selectAll()
            .map(::mapResultRow)
            .filter { item ->
                if (journalId != null) {
                    item.id == journalId
                } else {
                    (item.work.id == workId || workId == null) &&
                            (item.discipline.id == disciplineId || disciplineId == null) &&
                            (item.teacher.id == teacherId || teacherId == null) &&
                            (item.group.id == groupId || groupId == null) &&
                            (item.student.id == studentId || studentId == null)
                }
            }
    }

    override suspend fun addNewJournal(
        studentId: Int,
        groupId: Int,
        disciplineId: Int,
        teacherId: Int,
        workId: Int
    ): Int? = dbQuery {
        Journals
            .insert {
                it[Journals.studentId] = studentId
                it[Journals.groupId] = groupId
                it[Journals.disciplineId] = disciplineId
                it[Journals.teacherId] = teacherId
                it[Journals.workId] = workId
            }.resultedValues?.singleOrNull()?.let { row -> row[Journals.id].value }
    }

    override suspend fun deleteJournal(journalId: Int): Boolean = dbQuery {
        Journals.deleteWhere { Journals.id eq journalId } > 0
    }

    override fun mapResultRow(row: ResultRow): Journal = Journal.mapResultRow(row)
}
