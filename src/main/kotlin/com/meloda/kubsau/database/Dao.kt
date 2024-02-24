package com.meloda.kubsau.database

import org.jetbrains.exposed.sql.ResultRow

interface Dao<T> {

    fun mapResultRow(row: ResultRow): T
}
