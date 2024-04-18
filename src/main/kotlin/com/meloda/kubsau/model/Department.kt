package com.meloda.kubsau.model

import com.meloda.kubsau.database.departments.Departments
import org.jetbrains.exposed.sql.ResultRow

data class Department(
    override val id: Int,
    override val title: String,
    val phone: String
) : Filterable {

    companion object {
        fun mapResultRow(row: ResultRow): Department = Department(
            id = row[Departments.id].value,
            title = row[Departments.title],
            phone = row[Departments.phone]
        )
    }
}
