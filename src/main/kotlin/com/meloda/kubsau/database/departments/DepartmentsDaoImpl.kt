package com.meloda.kubsau.database.departments

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Departments
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class DepartmentsDaoImpl : DepartmentsDao {
    override fun mapResultRow(row: ResultRow) = Department(
        id = row[Departments.id].value,
        title = row[Departments.title],
        phone = row[Departments.phone]
    )

    override suspend fun allDepartments(): List<Department> = dbQuery {
        Departments.selectAll().map(::mapResultRow)
    }

    override suspend fun singleDepartment(id: Int): Department? = dbQuery {
        Departments
            .selectAll()
            .where { Departments.id eq id }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewDepartment(title: String, phone: String): Department? = dbQuery {
        Departments.insert {
            it[Departments.title] = title
            it[Departments.phone] = phone
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun deleteDepartment(id: Int): Boolean = dbQuery {
        Departments.deleteWhere { Departments.id eq id } > 0
    }
}

val departmentsDao: DepartmentsDao = DepartmentsDaoImpl()
