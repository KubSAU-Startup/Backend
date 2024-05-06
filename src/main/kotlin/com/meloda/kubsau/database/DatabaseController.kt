package com.meloda.kubsau.database

import com.meloda.kubsau.CONFIG_FOLDER
import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.directivities.Directivities
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.employees.Employees
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartments
import com.meloda.kubsau.database.employeesfaculties.EmployeesFaculties
import com.meloda.kubsau.database.faculties.Faculties
import com.meloda.kubsau.database.grades.Grades
import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.heads.Heads
import com.meloda.kubsau.database.programs.Programs
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplines
import com.meloda.kubsau.database.sessions.Sessions
import com.meloda.kubsau.database.students.Students
import com.meloda.kubsau.database.studentstatuses.StudentStatuses
import com.meloda.kubsau.database.users.Users
import com.meloda.kubsau.database.works.Works
import com.meloda.kubsau.database.worktypes.WorkTypes
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseController {

    fun init() {
        val driverClassName = "org.sqlite.JDBC"

        val folderPath = "$CONFIG_FOLDER/db"
        val filePath = "$folderPath/database.db"

        File(folderPath).apply {
            if (!exists()) mkdirs()
        }

        val jdbcURL = "jdbc:sqlite:$filePath"

        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            // TODO: 30/04/2024, Danil Nikolaev: enable/disable logger
            // addLogger(StdOutSqlLogger)

            SchemaUtils.create(
                Departments, Directivities, Disciplines, Employees,
                Faculties, Grades, Groups, Heads,
                Programs, Sessions, Students, StudentStatuses,
                Users, Works, WorkTypes
            )
            SchemaUtils.create(
                EmployeesDepartments, EmployeesFaculties, ProgramsDisciplines
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            // TODO: 30/04/2024, Danil Nikolaev: enable/disable logger
            // addLogger(StdOutSqlLogger)

            block()
        }
}
