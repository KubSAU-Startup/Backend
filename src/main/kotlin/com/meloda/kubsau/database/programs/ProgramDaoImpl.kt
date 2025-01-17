package com.meloda.kubsau.database.programs

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.controller.SearchEntry
import com.meloda.kubsau.controller.SearchProgram
import com.meloda.kubsau.database.directivities.Directivities
import com.meloda.kubsau.database.faculties.Faculties
import com.meloda.kubsau.database.grades.Grades
import com.meloda.kubsau.database.heads.Heads
import com.meloda.kubsau.model.IdTitle
import com.meloda.kubsau.model.Program
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class ProgramDaoImpl : ProgramDao {

    override suspend fun allPrograms(
        offset: Int?,
        limit: Int?
    ): List<Program> = dbQuery {
        Programs
            .selectAll()
            .apply { if (limit != null) limit(limit, (offset ?: 0).toLong()) }
            .map(::mapResultRow)
    }

    override suspend fun allProgramsByIds(programIds: List<Int>): List<Program> = dbQuery {
        Programs
            .selectAll()
            .where { Programs.id inList programIds }
            .map(::mapResultRow)
    }

    override suspend fun allProgramsBySearch(
        facultyId: Int?,
        programIds: List<Int>?,
        offset: Int?,
        limit: Int?,
        semester: Int?,
        directivityId: Int?,
        query: String?
    ): List<SearchEntry> = dbQuery {
        val dbQuery = Programs
            .innerJoin(Directivities, { Programs.directivityId }, { Directivities.id })
            .innerJoin(Grades, { Directivities.gradeId }, { Grades.id })
            .innerJoin(Heads, { Directivities.headId }, { Heads.id })
            .innerJoin(Faculties, { Heads.facultyId }, { Faculties.id })
            .select(
                Programs.id, Programs.semester,
                Directivities.id, Directivities.title,
                Grades.id, Grades.title
            )
            .apply { if (limit != null) limit(limit, (offset ?: 0).toLong()) }
            .orderBy(Programs.id, order = SortOrder.DESC)

        facultyId?.let { dbQuery.andWhere { Faculties.id eq facultyId } }
        programIds?.let { dbQuery.andWhere { Programs.id inList programIds } }
        semester?.let { dbQuery.andWhere { Programs.semester eq semester } }
        directivityId?.let { dbQuery.andWhere { Programs.directivityId eq directivityId } }
        query?.let { dbQuery.andWhere { Directivities.title.lowerCase() like "%$query%" } }

        dbQuery.map { row ->
            SearchEntry(
                program = SearchProgram(
                    id = row[Programs.id].value,
                    semester = row[Programs.semester]
                ),
                directivity = IdTitle(
                    id = row[Directivities.id].value,
                    title = row[Directivities.title]
                ),
                grade = IdTitle(
                    id = row[Grades.id].value,
                    title = row[Grades.title]
                ),
                disciplines = emptyList()
            )
        }
    }

    override suspend fun singleProgram(programId: Int): Program? = dbQuery {
        Programs
            .selectAll()
            .where { Programs.id eq programId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewProgram(semester: Int, directivityId: Int): Program? = dbQuery {
        Programs.insert {
            it[Programs.semester] = semester
            it[Programs.directivityId] = directivityId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateProgram(
        programId: Int,
        semester: Int,
        directivityId: Int
    ): Boolean = dbQuery {
        Programs.update(where = { Programs.id eq programId }) {
            it[Programs.semester] = semester
            it[Programs.directivityId] = directivityId
        } > 0
    }

    override suspend fun deleteProgram(programId: Int): Boolean = dbQuery {
        Programs.deleteWhere { Programs.id eq programId } > 0
    }

    override suspend fun deletePrograms(programIds: List<Int>): Boolean = dbQuery {
        Programs.deleteWhere { Programs.id inList programIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Program = Program.mapFromDb(row)
}
