package com.meloda.kubsau.database

import com.meloda.kubsau.common.isInDocker
import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.journals.Journals
import com.meloda.kubsau.database.majors.Majors
import com.meloda.kubsau.database.programs.Programs
import com.meloda.kubsau.database.sessions.Sessions
import com.meloda.kubsau.database.specializations.Specializations
import com.meloda.kubsau.database.students.Students
import com.meloda.kubsau.database.teachers.Teachers
import com.meloda.kubsau.database.teachersdisciplines.TeachersDisciplines
import com.meloda.kubsau.database.users.Users
import com.meloda.kubsau.database.works.Works
import com.meloda.kubsau.database.worktypes.WorkTypes
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseController {

    fun init() {
        val driverClassName = "org.h2.Driver"

        val filePath = if (isInDocker) {
            "/config/db/database.sql"
        } else {
            "${System.getProperty("user.dir")}/database.sql"
        }

        val jdbcURL = "jdbc:h2:file:$filePath"

        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(
                Departments, Disciplines, Groups,
                Journals, Majors, Programs,
                Sessions, Specializations,
                Students, Teachers,
                TeachersDisciplines, Users, Works,
                WorkTypes
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
