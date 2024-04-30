package com.meloda.kubsau.database.programsdisciplines

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.programs.Programs
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.worktypes.WorkTypes
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Program
import com.meloda.kubsau.model.WorkType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProgramsDisciplinesDaoImpl(
    private val programsDao: ProgramsDao,
    private val disciplinesDao: DisciplinesDao
) : ProgramsDisciplinesDao {

    override suspend fun allReferences(): List<Triple<Program, Discipline, WorkType>> = dbQuery {
        ProgramsDisciplines
            .innerJoin(Programs)
            .innerJoin(Disciplines)
            .innerJoin(WorkTypes)
            .selectAll()
            .map { row ->
                val (program, discipline) = mapBothResultRow(row)
                Triple(program, discipline, WorkType.mapResultRow(row))
            }
    }

    override suspend fun allReferencesByIds(
        programId: Int,
        disciplineId: Int
    ): List<Triple<Program, Discipline, WorkType>> = dbQuery {
        ProgramsDisciplines
            .innerJoin(Programs)
            .innerJoin(Disciplines)
            .innerJoin(WorkTypes)
            .selectAll()
            .where { (ProgramsDisciplines.programId eq programId) and (ProgramsDisciplines.disciplineId eq disciplineId) }
            .map { row ->
                val (program, discipline) = mapBothResultRow(row)
                Triple(program, discipline, WorkType.mapResultRow(row))
            }
    }

    override suspend fun allDisciplinesByProgramId(programId: Int): List<Discipline> = dbQuery {
        ProgramsDisciplines.innerJoin(Disciplines)
            .selectAll()
            .where { ProgramsDisciplines.programId eq programId }
            .map(::mapSecondResultRow)
    }

    override suspend fun allDisciplinesByProgramIds(programIds: List<Int>): List<Discipline> = dbQuery {
        ProgramsDisciplines.innerJoin(Disciplines)
            .selectAll()
            .where { ProgramsDisciplines.programId inList programIds }
            .map(::mapSecondResultRow)
    }

    override suspend fun programByDisciplineId(disciplineId: Int): Program? = dbQuery {
        ProgramsDisciplines.innerJoin(Programs)
            .selectAll()
            .where { ProgramsDisciplines.disciplineId eq disciplineId }
            .map(::mapFirstResultRow)
            .singleOrNull()
    }

    override suspend fun workType(programId: Int, disciplineId: Int): WorkType? = dbQuery {
        ProgramsDisciplines.innerJoin(WorkTypes)
            .selectAll()
            .where { (ProgramsDisciplines.programId eq programId) and (ProgramsDisciplines.disciplineId eq disciplineId) }
            .map(WorkType.Companion::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewReference(programId: Int, disciplineId: Int, workTypeId: Int): Boolean = dbQuery {
        ProgramsDisciplines.insert {
            it[ProgramsDisciplines.programId] = programId
            it[ProgramsDisciplines.disciplineId] = disciplineId
            it[ProgramsDisciplines.workTypeId] = workTypeId
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

    override suspend fun deleteReferencesByProgramId(programId: Int): Boolean = dbQuery {
        ProgramsDisciplines.deleteWhere { ProgramsDisciplines.programId eq programId } > 0
    }

    override fun mapFirstResultRow(row: ResultRow): Program = Program.mapResultRow(row)

    override fun mapSecondResultRow(row: ResultRow): Discipline = Discipline.mapResultRow(row)
}
