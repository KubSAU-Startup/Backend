package com.meloda.kubsau.database.directivities

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.model.Directivity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class DirectivitiesDaoImpl : DirectivitiesDao {

    override suspend fun allDirectivities(
        offset: Int?,
        limit: Int?
    ): List<Directivity> = dbQuery {
        Directivities
            .selectAll()
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }
            .map(::mapResultRow)
    }

    override suspend fun allDirectivitiesByIds(directivityIds: List<Int>): List<Directivity> = dbQuery {
        Directivities
            .selectAll()
            .where { Directivities.id inList directivityIds }
            .map(::mapResultRow)
    }

    override suspend fun singleDirectivity(directivityId: Int): Directivity? = dbQuery {
        Directivities
            .selectAll()
            .where { Directivities.id eq directivityId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewDirectivity(title: String, headId: Int, gradeId: Int): Directivity? = dbQuery {
        Directivities.insert {
            it[Directivities.title] = title
            it[Directivities.headId] = headId
            it[Directivities.gradeId] = gradeId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateDirectivity(
        directivityId: Int,
        title: String,
        headId: Int,
        gradeId: Int
    ): Boolean = dbQuery {
        Directivities.update(where = { Directivities.id eq directivityId }) {
            it[Directivities.title] = title
            it[Directivities.headId] = headId
            it[Directivities.gradeId] = gradeId
        } > 0
    }

    override suspend fun deleteDirectivity(directivityId: Int): Boolean = dbQuery {
        Directivities.deleteWhere { Directivities.id eq directivityId } > 0
    }

    override suspend fun deleteDirectivities(directivityIds: List<Int>): Boolean = dbQuery {
        Directivities.deleteWhere { Directivities.id inList directivityIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Directivity = Directivity.mapFromDb(row)
}
