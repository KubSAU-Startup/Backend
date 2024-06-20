package com.meloda.kubsau.database.employees

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartments
import com.meloda.kubsau.database.employeesfaculties.EmployeesFaculties
import com.meloda.kubsau.model.Employee
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class EmployeeDaoImpl : EmployeeDao {

    override suspend fun allEmployees(facultyId: Int?, offset: Int?, limit: Int?): List<Employee> = dbQuery {
        val dbQuery = Employees
            .run {
                facultyId?.run { innerJoin(EmployeesFaculties, { Employees.id }, { EmployeesFaculties.employeeId }) }
                    ?: this
            }
            .select(Employees.columns)
            .orderBy(Employees.id, order = SortOrder.DESC)
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }

        facultyId?.let { EmployeesFaculties.facultyId eq facultyId }

        dbQuery.map(::mapResultRow)
    }

    override suspend fun allTeachers(offset: Int?, limit: Int?, departmentIds: List<Int>?): List<Employee> = dbQuery {
        val dbQuery = Employees
            .run { departmentIds?.run { innerJoin(EmployeesDepartments) } ?: this }
            .select(Employees.columns)
            .orderBy(Employees.id, order = SortOrder.DESC)
            .where { Employees.type eq Employee.TYPE_TEACHER }
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }

        departmentIds?.let { dbQuery.andWhere { EmployeesDepartments.departmentId inList departmentIds } }

        dbQuery.map(::mapResultRow)
    }

    override suspend fun allEmployeesByIds(employeeIds: List<Int>): List<Employee> = dbQuery {
        val dbQuery = Employees
            .innerJoin(EmployeesFaculties)
            .select(Employees.columns)
            .orderBy(Employees.id, order = SortOrder.DESC)
            .where { Employees.id inList employeeIds }

        dbQuery.map(::mapResultRow)
    }

    override suspend fun singleEmployee(employeeId: Int): Employee? = dbQuery {
        Employees
            .select(Employees.columns)
            .where { Employees.id eq employeeId }
            .map(::mapResultRow).singleOrNull()
    }

    override suspend fun addNewEmployee(
        lastName: String,
        firstName: String,
        middleName: String?,
        email: String,
        type: Int
    ): Employee? = dbQuery {
        Employees.insert {
            it[Employees.lastName] = lastName
            it[Employees.firstName] = firstName
            it[Employees.middleName] = middleName
            it[Employees.email] = email
            it[Employees.type] = type
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateEmployee(
        employeeId: Int,
        lastName: String,
        firstName: String,
        middleName: String?,
        email: String,
        type: Int
    ): Boolean = dbQuery {
        Employees.update(where = { Employees.id eq employeeId }) {
            it[Employees.lastName] = lastName
            it[Employees.firstName] = firstName
            it[Employees.middleName] = middleName
            it[Employees.email] = email
            it[Employees.type] = type
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
