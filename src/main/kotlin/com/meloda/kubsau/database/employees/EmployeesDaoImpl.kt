package com.meloda.kubsau.database.employees

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.model.Employee
import com.meloda.kubsau.route.journal.JournalFilter
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class EmployeesDaoImpl : EmployeesDao {

    override suspend fun allEmployees(): List<Employee> = dbQuery {
        Employees.selectAll().map(::mapResultRow)
    }

    override suspend fun allEmployeesAsFilters(): List<JournalFilter> = dbQuery {
        allEmployees().map { employee ->
            JournalFilter(
                id = employee.id,
                title = employee.fullName
            )
        }
    }

    override suspend fun allEmployeesByIds(employeeIds: List<Int>): List<Employee> = dbQuery {
        Employees
            .selectAll()
            .where { Employees.id inList employeeIds }
            .map(::mapResultRow)
    }

    override suspend fun singleEmployee(employeeId: Int): Employee? = dbQuery {
        Employees
            .selectAll()
            .where { Employees.id eq employeeId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewEmployee(
        lastName: String,
        firstName: String,
        middleName: String?,
        email: String?,
        employeeTypeId: Int
    ): Employee? = dbQuery {
        Employees.insert {
            it[Employees.lastName] = lastName
            it[Employees.firstName] = firstName
            it[Employees.middleName] = middleName
            it[Employees.email] = email
            it[Employees.employeeTypeId] = employeeTypeId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateEmployee(
        employeeId: Int,
        lastName: String,
        firstName: String,
        middleName: String?,
        email: String?,
        employeeTypeId: Int
    ): Boolean = dbQuery {
        Employees.update(where = { Employees.id eq employeeId }) {
            it[Employees.lastName] = lastName
            it[Employees.firstName] = firstName
            it[Employees.middleName] = middleName
            it[Employees.email] = email
            it[Employees.employeeTypeId] = employeeTypeId
        } > 0
    }

    override suspend fun deleteEmployee(employeeId: Int): Boolean = dbQuery {
        Employees.deleteWhere { Employees.id eq employeeId } > 0
    }

    override suspend fun deleteEmployees(employeeIds: List<Int>): Boolean = dbQuery {
        Employees.deleteWhere { Employees.id inList employeeIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Employee = Employee.mapResultRow(row)
}
