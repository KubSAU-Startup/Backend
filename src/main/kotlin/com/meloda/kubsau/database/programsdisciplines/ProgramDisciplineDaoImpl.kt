package com.meloda.kubsau.database.programsdisciplines

import com.meloda.kubsau.model.IdTitle
import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.programs.Programs
import com.meloda.kubsau.database.worktypes.WorkTypes
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Program
import com.meloda.kubsau.model.WorkType
import com.meloda.kubsau.route.programs.FullDisciplineIds
import com.meloda.kubsau.route.programs.SearchDiscipline
import com.meloda.kubsau.route.programs.SearchDisciplineWithProgramId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProgramDisciplineDaoImpl : ProgramDisciplineDao {

    override suspend fun allReferences(
        offset: Int?,
        limit: Int?
    ): List<Triple<Program, Discipline, WorkType>> = dbQuery {
        ProgramsDisciplines
            .innerJoin(Programs)
            .innerJoin(Disciplines)
            .innerJoin(WorkTypes)
            .selectAll()
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }
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

    override suspend fun allDisciplinesByProgramIdShortened(programId: Int): List<IdTitle> = dbQuery {
        ProgramsDisciplines.innerJoin(Disciplines)
            .selectAll()
            .where { ProgramsDisciplines.programId eq programId }
            .map { row ->
                IdTitle(
                    id = row[Disciplines.id].value,
                    title = row[Disciplines.title]
                )
            }
    }

    override suspend fun allDisciplinesByProgramIds(programIds: List<Int>): List<Discipline> = dbQuery {
        ProgramsDisciplines.innerJoin(Disciplines)
            .selectAll()
            .where { ProgramsDisciplines.programId inList programIds }
            .map(::mapSecondResultRow)
    }

    override suspend fun allSearchDisciplinesByProgramIds(
        programIds: List<Int>
    ): List<SearchDisciplineWithProgramId> = dbQuery {
        ProgramsDisciplines.innerJoin(Disciplines)
            .select(
                Disciplines.id,
                Disciplines.title,
                Disciplines.departmentId,
                ProgramsDisciplines.workTypeId,
                ProgramsDisciplines.programId
            )
            .where { ProgramsDisciplines.programId inList programIds }
            .map { row ->
                SearchDisciplineWithProgramId(
                    programId = row[ProgramsDisciplines.programId],
                    discipline = SearchDiscipline(
                        id = row[Disciplines.id].value,
                        title = row[Disciplines.title],
                        workTypeId = row[ProgramsDisciplines.workTypeId],
                        departmentId = row[Disciplines.departmentId]
                    )
                )
            }
    }

    override suspend fun allDisciplineIdsByProgramId(programId: Int): List<Int> = dbQuery {
        ProgramsDisciplines
            .select(ProgramsDisciplines.disciplineId)
            .where { ProgramsDisciplines.programId eq programId }
            .map { row -> row[ProgramsDisciplines.disciplineId] }
    }

    override suspend fun allDisciplineIdsByProgramIdAsMap(programId: Int): List<FullDisciplineIds> = dbQuery {
        ProgramsDisciplines
            .innerJoin(Disciplines)
            .select(ProgramsDisciplines.columns.plus(listOf(Disciplines.departmentId, Disciplines.title)))
            .where { ProgramsDisciplines.programId eq programId }
            .map { row ->
                FullDisciplineIds(
                    disciplineId = row[ProgramsDisciplines.disciplineId],
                    programId = row[ProgramsDisciplines.programId],
                    workTypeId = row[ProgramsDisciplines.workTypeId],
                    departmentId = row[Disciplines.departmentId],
                    title = row[Disciplines.title]
                )
            }
    }

    override suspend fun allDisciplineIdsByProgramIdsAsMap(
        programIds: List<Int>
    ): Map<Int, List<FullDisciplineIds>> = dbQuery {
        val disciplinesMap = hashMapOf<Int, List<FullDisciplineIds>>()

        val allDisciplines = ProgramsDisciplines
            .innerJoin(Disciplines)
            .select(ProgramsDisciplines.columns.plus(listOf(Disciplines.departmentId, Disciplines.title)))
            .where { ProgramsDisciplines.programId inList programIds }
            .map { row ->
                FullDisciplineIds(
                    disciplineId = row[ProgramsDisciplines.disciplineId],
                    programId = row[ProgramsDisciplines.programId],
                    workTypeId = row[ProgramsDisciplines.workTypeId],
                    departmentId = row[Disciplines.departmentId],
                    title = row[Disciplines.title]
                )
            }

        programIds.forEach { programId ->
            allDisciplines
                .filter { discipline -> discipline.programId == programId }
                .let { disciplines -> disciplinesMap[programId] = disciplines }
        }

        disciplinesMap
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

    override suspend fun workTypeId(programId: Int, disciplineId: Int): Int? = dbQuery {
        ProgramsDisciplines.innerJoin(WorkTypes)
            .select(WorkTypes.id)
            .where { (ProgramsDisciplines.programId eq programId) and (ProgramsDisciplines.disciplineId eq disciplineId) }
            .map { row -> row[WorkTypes.id].value }
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

    override fun mapFirstResultRow(row: ResultRow): Program = Program.mapFromDb(row)

    override fun mapSecondResultRow(row: ResultRow): Discipline = Discipline.mapResultRow(row)
}
