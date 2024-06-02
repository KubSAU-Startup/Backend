package com.meloda.kubsau.model

import com.meloda.kubsau.database.works.Works
import org.jetbrains.exposed.sql.ResultRow

data class Work(
    val id: Int,
    val disciplineId: Int,
    val studentId: Int,
    val registrationDate: Long,
    val title: String?,
    val workTypeId: Int,
    val employeeId: Int
) {

    companion object {

        fun mapFromDb(row: ResultRow): Work = Work(
            id = row[Works.id].value,
            disciplineId = row[Works.disciplineId],
            studentId = row[Works.studentId],
            registrationDate = row[Works.registrationDate],
            title = row[Works.title],
            workTypeId = row[Works.workTypeId],
            employeeId = row[Works.employeeId]
        )
    }
}
