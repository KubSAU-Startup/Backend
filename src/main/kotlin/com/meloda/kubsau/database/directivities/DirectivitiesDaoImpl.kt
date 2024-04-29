package com.meloda.kubsau.database.directivities

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Directivity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class DirectivitiesDaoImpl : DirectivitiesDao {

    override suspend fun allDirectivities(): List<Directivity> = dbQuery {
        Directivities.selectAll().map(::mapResultRow)
    }

    override suspend fun addNewDirectivity(title: String, headId: Int): Directivity? = dbQuery {
        Directivities.insert {
            it[Directivities.title] = title
            it[Directivities.headId] = headId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override fun mapResultRow(row: ResultRow): Directivity = Directivity.mapResultRow(row)
}
