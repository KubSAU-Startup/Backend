package com.meloda.kubsau.database.departments

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Department
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class DepartmentsDaoImpl : DepartmentsDao {

    override suspend fun allDepartments(): List<Department> = dbQuery {
        Departments.selectAll().map(::mapResultRow)
    }

    override suspend fun allDepartmentsByIds(departmentIds: List<Int>): List<Department> = dbQuery {
        Departments
            .selectAll()
            .where { Departments.id inList departmentIds }
            .map(::mapResultRow)
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

    override suspend fun updateDepartment(departmentId: Int, title: String, phone: String): Int = dbQuery {
        Departments.update(where = { Departments.id eq departmentId }) {
            it[Departments.title] = title
            it[Departments.phone] = phone
        }
    }

    override suspend fun deleteDepartment(departmentId: Int): Boolean = dbQuery {
        Departments.deleteWhere { Departments.id eq departmentId } > 0
    }

    override suspend fun deleteDepartments(departmentIds: List<Int>): Boolean = dbQuery {
        Departments.deleteWhere { Departments.id inList departmentIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Department = Department.mapResultRow(row)
}
