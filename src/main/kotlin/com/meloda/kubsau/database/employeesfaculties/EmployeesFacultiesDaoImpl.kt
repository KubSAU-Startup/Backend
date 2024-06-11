package com.meloda.kubsau.database.employeesfaculties

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.database.employees.Employees
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.faculties.Faculties
import com.meloda.kubsau.database.faculties.FacultiesDao
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.model.Faculty
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class EmployeesFacultiesDaoImpl(
    private val employeeDao: EmployeeDao,
    private val facultiesDao: FacultiesDao
) : EmployeesFacultiesDao {

    override suspend fun allReferences(): List<Pair<Employee, Faculty>> = dbQuery {
        EmployeesFaculties
            .innerJoin(Employees)
            .innerJoin(Faculties)
            .selectAll()
            .map(::mapBothResultRow)
    }

    override suspend fun singleFacultyByEmployeeId(employeeId: Int): Faculty? = dbQuery {
        EmployeesFaculties
            .innerJoin(Faculties)
            .select(Faculties.columns)
            .where { EmployeesFaculties.employeeId eq employeeId }
            .map(::mapSecondResultRow)
            .singleOrNull()
    }

    override suspend fun singleFacultyIdByEmployeeId(employeeId: Int): Int? = dbQuery {
        EmployeesFaculties
            .select(EmployeesFaculties.facultyId)
            .where { EmployeesFaculties.employeeId eq employeeId }
            .map { row -> row[EmployeesFaculties.facultyId] }
            .singleOrNull()
    }

    override suspend fun addNewReference(employeeId: Int, facultyId: Int): Boolean = dbQuery {
        EmployeesFaculties.insert {
            it[EmployeesFaculties.employeeId] = employeeId
            it[EmployeesFaculties.facultyId] = facultyId
        }.resultedValues?.size != 0
    }

    override fun mapFirstResultRow(row: ResultRow): Employee = employeeDao.mapResultRow(row)

    override fun mapSecondResultRow(row: ResultRow): Faculty = facultiesDao.mapResultRow(row)
}
