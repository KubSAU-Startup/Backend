package com.meloda.kubsau.database.specializationsdisciplines

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.specializations.Specializations
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Specialization
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SpecializationsDisciplinesDaoImpl : SpecializationsDisciplinesDao {

    override suspend fun allItems(): List<Pair<Specialization, Discipline>> = dbQuery {
        SpecializationsDisciplines.innerJoin(Specializations).innerJoin(Disciplines)
            .selectAll()
            .map(::mapBothResultRow)
    }

    override suspend fun addNewReference(specializationId: Int, disciplineId: Int): Boolean = dbQuery {
        SpecializationsDisciplines.insert {
            it[SpecializationsDisciplines.specializationId] = specializationId
            it[SpecializationsDisciplines.disciplineId] = disciplineId
        }.resultedValues?.size != 0
    }

    override suspend fun deleteReference(specializationId: Int?, disciplineId: Int?): Boolean = dbQuery {
        val query = when {
            specializationId != null && disciplineId != null -> {
                (SpecializationsDisciplines.specializationId eq specializationId) and (SpecializationsDisciplines.disciplineId eq disciplineId)
            }

            specializationId != null -> {
                SpecializationsDisciplines.specializationId eq specializationId
            }

            disciplineId != null -> {
                SpecializationsDisciplines.disciplineId eq disciplineId
            }

            else -> null
        }

        query?.let { SpecializationsDisciplines.deleteWhere { query } > 0 } ?: false
    }

    override fun mapFirstResultRow(row: ResultRow): Specialization = Specialization.mapResultRow(row)

    override fun mapSecondResultRow(row: ResultRow): Discipline = Discipline.mapResultRow(row)
}
