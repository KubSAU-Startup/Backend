package com.meloda.kubsau.database

import com.meloda.kubsau.base.isInDocker
import com.meloda.kubsau.model.Departments
import com.meloda.kubsau.model.Sessions
import com.meloda.kubsau.model.Users
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
            SchemaUtils.create(Users, Sessions, Departments)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
