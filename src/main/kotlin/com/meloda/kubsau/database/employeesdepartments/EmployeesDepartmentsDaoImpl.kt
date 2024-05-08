package com.meloda.kubsau.database.employeesdepartments

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.employees.Employees
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.model.Department
import com.meloda.kubsau.model.Employee
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class EmployeesDepartmentsDaoImpl(
    private val employeesDao: EmployeesDao,
    private val departmentsDao: DepartmentsDao
) : EmployeesDepartmentsDao {

    override suspend fun allReferences(): List<Pair<Employee, Department>> = dbQuery {
        EmployeesDepartments
            .innerJoin(Employees)
            .innerJoin(Departments)
            .selectAll()
            .map(::mapBothResultRow)
    }

    override suspend fun allEmployees(): List<Employee> = employeesDao.allEmployees()

    override suspend fun allDepartments(): List<Department> = departmentsDao.allDepartments()

    override suspend fun allDepartmentsByEmployee(employeeId: Int): List<Department> = dbQuery {
        EmployeesDepartments
            .innerJoin(Departments)
            .selectAll()
            .where { EmployeesDepartments.employeeId eq employeeId }
            .map(::mapSecondResultRow)
    }

    override suspend fun addNewReference(employeeId: Int, departmentId: Int): Boolean = dbQuery {
        EmployeesDepartments.insert {
            it[EmployeesDepartments.employeeId] = employeeId
            it[EmployeesDepartments.departmentId] = departmentId
        }.resultedValues?.size != 0
    }

    override fun mapFirstResultRow(row: ResultRow): Employee = employeesDao.mapResultRow(row)

    override fun mapSecondResultRow(row: ResultRow): Department = departmentsDao.mapResultRow(row)
}
