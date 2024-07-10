package com.meloda.kubsau.database.employeesdepartments

import com.meloda.kubsau.config.DatabaseController.dbQuery
import com.meloda.kubsau.database.departments.DepartmentDao
import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.employees.Employees
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Employee
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class EmployeeDepartmentDaoImpl(
    private val employeeDao: EmployeeDao,
    private val departmentDao: DepartmentDao
) : EmployeeDepartmentDao {

    override suspend fun isReferenceExist(employeeId: Int, departmentId: Int): Boolean = dbQuery {
        EmployeesDepartments
            .select(EmployeesDepartments.id)
            .where {
                (EmployeesDepartments.employeeId eq employeeId) and (EmployeesDepartments.departmentId eq departmentId)
            }
            .map { row -> row[EmployeesDepartments.id].value }
            .isNotEmpty()
    }

    override suspend fun isReferencesExist(employeeId: Int): Boolean = dbQuery {
        EmployeesDepartments
            .select(EmployeesDepartments.id)
            .where { EmployeesDepartments.employeeId eq employeeId }
            .map { row -> row[EmployeesDepartments.id].value }
            .isNotEmpty()
    }

    override suspend fun allReferences(): List<Pair<Employee, Department>> = dbQuery {
        EmployeesDepartments
            .innerJoin(Employees)
            .innerJoin(Departments)
            .selectAll()
            .map(::mapBothResultRow)
    }

    override suspend fun allTeachersByDepartmentId(departmentId: Int): List<Employee> = dbQuery {
        EmployeesDepartments
            .innerJoin(Employees)
            .select(Employees.columns)
            .where { (EmployeesDepartments.departmentId eq departmentId) and (Employees.type eq Employee.TYPE_TEACHER) }
            .map(::mapFirstResultRow)
    }

    override suspend fun allDepartmentsByEmployeeId(employeeId: Int): List<Department> = dbQuery {
        EmployeesDepartments
            .innerJoin(Departments)
            .select(Departments.columns)
            .where { EmployeesDepartments.employeeId eq employeeId }
            .map(::mapSecondResultRow)
    }

    override suspend fun allDepartmentIdsByEmployeeId(employeeId: Int): List<Int> = dbQuery {
        EmployeesDepartments
            .select(EmployeesDepartments.departmentId)
            .where { EmployeesDepartments.employeeId eq employeeId }
            .map { row -> row[EmployeesDepartments.departmentId] }
    }

    override suspend fun addNewReference(employeeId: Int, departmentId: Int): Boolean = dbQuery {
        EmployeesDepartments.insert {
            it[EmployeesDepartments.employeeId] = employeeId
            it[EmployeesDepartments.departmentId] = departmentId
        }.resultedValues?.size != 0
    }

    override suspend fun deleteReferences(employeeId: Int): Boolean = dbQuery {
        EmployeesDepartments.deleteWhere { EmployeesDepartments.employeeId eq employeeId } > 0
    }

    override suspend fun deleteReferences(employeeIds: List<Int>): Boolean = dbQuery {
        EmployeesDepartments.deleteWhere { EmployeesDepartments.employeeId inList employeeIds } > 0
    }

    override fun mapFirstResultRow(row: ResultRow): Employee = employeeDao.mapResultRow(row)

    override fun mapSecondResultRow(row: ResultRow): Department = departmentDao.mapResultRow(row)
}
