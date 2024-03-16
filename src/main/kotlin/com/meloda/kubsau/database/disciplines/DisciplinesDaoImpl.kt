package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Discipline
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class DisciplinesDaoImpl : DisciplinesDao {
    override suspend fun allDisciplines(): List<Discipline> = dbQuery {
        Disciplines.selectAll().map(::mapResultRow)
    }

    override suspend fun singleDiscipline(disciplineId: Int): Discipline? = dbQuery {
        Disciplines
            .selectAll()
            .where { Disciplines.id eq disciplineId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewDiscipline(title: String): Discipline? = dbQuery {
        Disciplines.insert {
            it[Disciplines.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteDiscipline(disciplineId: Int): Boolean = dbQuery {
        Disciplines.deleteWhere { Disciplines.id eq disciplineId } > 0
    }

    override fun mapResultRow(row: ResultRow): Discipline = Discipline(
        id = row[Disciplines.id].value,
        title = row[Disciplines.title]
    )
}
