package com.meloda.kubsau.database.programsdisciplines

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.programs.Programs
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Program
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProgramsDisciplinesDaoImpl : ProgramsDisciplinesDao {
    override suspend fun allItems(): List<Pair<Program, Discipline>> = dbQuery {
        ProgramsDisciplines.innerJoin(Programs).innerJoin(Disciplines)
            .selectAll()
            .map(::mapBothResultRow)
    }

    override suspend fun allDisciplinesByProgramId(programId: Int): List<Discipline> = dbQuery {
        ProgramsDisciplines.innerJoin(Disciplines)
            .selectAll()
            .where { ProgramsDisciplines.programId eq programId }
            .map(::mapSecondResultRow)
    }

    override suspend fun addNewReference(programId: Int, disciplineId: Int): Boolean = dbQuery {
        ProgramsDisciplines.insert {
            it[ProgramsDisciplines.programId] = programId
            it[ProgramsDisciplines.disciplineId] = disciplineId
        }.resultedValues?.size != 0
    }

    override suspend fun deleteReference(programId: Int?, disciplineId: Int?): Boolean = dbQuery {
        val query = when {
            programId != null && disciplineId != null -> {
                (ProgramsDisciplines.programId eq programId) and (ProgramsDisciplines.disciplineId eq disciplineId)
            }

            programId != null -> {
                ProgramsDisciplines.programId eq programId
            }

            disciplineId != null -> {
                ProgramsDisciplines.disciplineId eq disciplineId
            }

            else -> null
        }

        query?.let { ProgramsDisciplines.deleteWhere { query } > 0 } ?: false
    }

    override fun mapFirstResultRow(row: ResultRow): Program = Program.mapResultRow(row)

    override fun mapSecondResultRow(row: ResultRow): Discipline = Discipline.mapResultRow(row)
}