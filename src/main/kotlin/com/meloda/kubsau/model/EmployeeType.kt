package com.meloda.kubsau.model

import com.meloda.kubsau.database.employeetypes.EmployeeTypes
import org.jetbrains.exposed.sql.ResultRow

data class EmployeeType(
    val id: Int,
    val title: String
) {

    companion object {

        fun mapResultRow(row: ResultRow): EmployeeType = EmployeeType(
            id = row[EmployeeTypes.id].value,
            title = row[EmployeeTypes.title]
        )
    }
}
