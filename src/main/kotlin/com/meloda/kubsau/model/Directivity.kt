package com.meloda.kubsau.model

import com.meloda.kubsau.database.directivities.Directivities
import org.jetbrains.exposed.sql.ResultRow

data class Directivity(
    val id: Int,
    val title: String,
    val headId: Int,
    val gradeId: Int
) {

    companion object {

        fun mapResultRow(row: ResultRow): Directivity = Directivity(
            id = row[Directivities.id].value,
            title = row[Directivities.title],
            headId = row[Directivities.headId],
            gradeId = row[Directivities.gradeId]
        )
    }
}
