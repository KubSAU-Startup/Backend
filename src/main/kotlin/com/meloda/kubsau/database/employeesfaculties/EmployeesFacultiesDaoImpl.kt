package com.meloda.kubsau.database.employeesfaculties

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.employees.Employees
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.database.faculties.Faculties
import com.meloda.kubsau.database.faculties.FacultiesDao
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.model.Faculty
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class EmployeesFacultiesDaoImpl(
    private val employeesDao: EmployeesDao,
    private val facultiesDao: FacultiesDao
) : EmployeesFacultiesDao {

    override suspend fun allReferences(): List<Pair<Employee, Faculty>> = dbQuery {
        EmployeesFaculties
            .innerJoin(Employees)
            .innerJoin(Faculties)
            .selectAll()
            .map(::mapBothResultRow)
    }

    override suspend fun singleFacultyByEmplyeeId(employeeId: Int): Faculty? = dbQuery {
        EmployeesFaculties
            .innerJoin(Faculties)
            .selectAll()
            .where { EmployeesFaculties.employeeId eq employeeId }
            .map(::mapSecondResultRow)
            .singleOrNull()
    }

    override suspend fun addNewReference(employeeId: Int, facultyId: Int): Boolean = dbQuery {
        EmployeesFaculties.insert {
            it[EmployeesFaculties.employeeId] = employeeId
            it[EmployeesFaculties.facultyId] = facultyId
        }.resultedValues?.size != 0
    }

    override fun mapFirstResultRow(row: ResultRow): Employee = employeesDao.mapResultRow(row)

    override fun mapSecondResultRow(row: ResultRow): Faculty = facultiesDao.mapResultRow(row)
}
