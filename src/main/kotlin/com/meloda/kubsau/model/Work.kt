package com.meloda.kubsau.model

import com.meloda.kubsau.database.works.Works
import org.jetbrains.exposed.sql.ResultRow

data class Work(
    val id: Int,
    val typeId: Int,
    val disciplineId: Int,
    val studentId: Int,
    val registrationDate: Long,
    val title: String
) {

    companion object {
        fun mapResultRow(row: ResultRow): Work = Work(
            id = row[Works.id].value,
            typeId = row[Works.typeId],
            disciplineId = row[Works.disciplineId],
            studentId = row[Works.studentId],
            registrationDate = row[Works.registrationDate],
            title = row[Works.title]
        )
    }
}
