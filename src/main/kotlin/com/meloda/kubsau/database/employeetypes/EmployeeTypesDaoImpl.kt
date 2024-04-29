package com.meloda.kubsau.database.employeetypes

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.EmployeeType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class EmployeeTypesDaoImpl : EmployeeTypesDao {

    override suspend fun allTypes(): List<EmployeeType> = dbQuery {
        EmployeeTypes.selectAll().map(::mapResultRow)
    }

    override suspend fun addNewType(title: String): EmployeeType? = dbQuery {
        EmployeeTypes.insert {
            it[EmployeeTypes.title] = title
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override fun mapResultRow(row: ResultRow): EmployeeType = EmployeeType.mapResultRow(row)
}
