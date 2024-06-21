package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.controller.EntryFilter
import com.meloda.kubsau.model.Discipline
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class DisciplineDaoImpl : DisciplineDao {

    override suspend fun allDisciplines(departmentIds: List<Int>?): List<Discipline> = dbQuery {
        val dbQuery = Disciplines
            .selectAll()

        departmentIds?.let { dbQuery.andWhere { Disciplines.departmentId inList departmentIds } }

        dbQuery.map(::mapResultRow)
    }

    override suspend fun allDisciplinesAsFilters(departmentIds: List<Int>?): List<EntryFilter> = dbQuery {
        val dbQuery = Disciplines.select(Disciplines.id, Disciplines.title)

        departmentIds?.let { dbQuery.andWhere { Disciplines.departmentId inList departmentIds } }

        dbQuery.map(::mapFilterResultRow)
    }

    override suspend fun allDisciplinesByIds(disciplineIds: List<Int>): List<Discipline> = dbQuery {
        Disciplines
            .selectAll()
            .where { Disciplines.id inList disciplineIds }
            .map(::mapResultRow)
    }

    override suspend fun singleDiscipline(disciplineId: Int): Discipline? = dbQuery {
        Disciplines
            .selectAll()
            .where { Disciplines.id eq disciplineId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewDiscipline(title: String, departmentId: Int): Discipline? = dbQuery {
        Disciplines.insert {
            it[Disciplines.title] = title
            it[Disciplines.departmentId] = departmentId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateDiscipline(disciplineId: Int, title: String, departmentId: Int): Boolean = dbQuery {
        Disciplines.update(where = { Disciplines.id eq disciplineId }) {
            it[Disciplines.title] = title
            it[Disciplines.departmentId] = departmentId
        } > 0
    }

    override suspend fun deleteDiscipline(disciplineId: Int): Boolean = dbQuery {
        Disciplines.deleteWhere { Disciplines.id eq disciplineId } > 0
    }

    override suspend fun deleteDisciplines(disciplineIds: List<Int>): Boolean = dbQuery {
        Disciplines.deleteWhere { Disciplines.id inList disciplineIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Discipline = Discipline.mapResultRow(row)

    override fun mapFilterResultRow(row: ResultRow): EntryFilter = EntryFilter(
        id = row[Disciplines.id].value,
        title = row[Disciplines.title]
    )
}
