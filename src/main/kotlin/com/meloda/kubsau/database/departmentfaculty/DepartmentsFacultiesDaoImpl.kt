package com.meloda.kubsau.database.departmentfaculty

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Faculty
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class DepartmentsFacultiesDaoImpl : DepartmentsFacultiesDao {
    override suspend fun getAll(): List<Int> = dbQuery {
        DepartmentsFaculties.select(DepartmentsFaculties.id).map { row -> row[DepartmentsFaculties.id].value }
    }

    override suspend fun getDepartmentsByFacultyId(facultyId: Int): List<Department> = dbQuery {
        DepartmentsFaculties
            .innerJoin(Departments)
            .select(Departments.columns)
            .where { DepartmentsFaculties.facultyId eq facultyId }
            .map(::mapFirstResultRow)
    }

    override suspend fun getDepartmentIdsByFacultyId(facultyId: Int): List<Int> = dbQuery {
        DepartmentsFaculties
            .select(DepartmentsFaculties.departmentId)
            .where { DepartmentsFaculties.facultyId eq facultyId }
            .map { row -> row[DepartmentsFaculties.departmentId] }
    }

    override suspend fun addReference(facultyId: Int, departmentId: Int): Boolean = dbQuery {
        DepartmentsFaculties.insert {
            it[DepartmentsFaculties.facultyId] = facultyId
            it[DepartmentsFaculties.departmentId] = departmentId
        }.resultedValues?.size != 0
    }

    override suspend fun deleteReference(facultyId: Int, departmentId: Int): Boolean = dbQuery {
        DepartmentsFaculties.deleteWhere {
            (DepartmentsFaculties.facultyId eq facultyId) and (DepartmentsFaculties.departmentId eq departmentId)
        } > 0
    }

    override fun mapFirstResultRow(row: ResultRow): Department = Department.mapFromDb(row)

    override fun mapSecondResultRow(row: ResultRow): Faculty = Faculty.mapFromDb(row)
}
