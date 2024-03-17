package com.meloda.kubsau.database.specializations

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Specialization
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class SpecializationsDaoImpl : SpecializationsDao {

    override suspend fun allSpecializations(): List<Specialization> = dbQuery {
        Specializations.selectAll().map(::mapResultRow)
    }

    override suspend fun singleSpecialization(specializationId: Int): Specialization? = dbQuery {
        Specializations
            .selectAll()
            .where { Specializations.id eq specializationId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewSpecialization(title: String): Specialization? = dbQuery {
        Specializations.insert {
            it[Specializations.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteSpecialization(specializationId: Int): Boolean = dbQuery {
        Specializations.deleteWhere { Specializations.id eq specializationId } > 0
    }

    override fun mapResultRow(row: ResultRow): Specialization = Specialization(
        id = row[Specializations.id].value,
        title = row[Specializations.title]
    )
}
