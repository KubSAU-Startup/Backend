package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Discipline
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class DisciplinesDaoImpl : DisciplinesDao {

    override suspend fun allDisciplines(): List<Discipline> = dbQuery {
        Disciplines.selectAll().map(::mapResultRow)
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

    override suspend fun addNewDiscipline(title: String, workTypeId: Int): Discipline? = dbQuery {
        Disciplines.insert {
            it[Disciplines.title] = title
            it[Disciplines.workTypeId] = workTypeId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateDiscipline(disciplineId: Int, title: String, workTypeId: Int): Int = dbQuery {
        Disciplines.update(where = { Disciplines.id eq disciplineId }) {
            it[Disciplines.title] = title
            it[Disciplines.workTypeId] = workTypeId
        }
    }

    override suspend fun deleteDiscipline(disciplineId: Int): Boolean = dbQuery {
        Disciplines.deleteWhere { Disciplines.id eq disciplineId } > 0
    }

    override suspend fun deleteDisciplines(disciplineIds: List<Int>): Boolean = dbQuery {
        Disciplines.deleteWhere { Disciplines.id inList disciplineIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Discipline = Discipline.mapResultRow(row)
}
