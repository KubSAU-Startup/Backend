package com.meloda.kubsau.base

import org.jetbrains.exposed.sql.ResultRow

interface Dao<T> {

    fun mapResultRow(row: ResultRow): T
}
