package com.meloda.kubsau.database

import org.jetbrains.exposed.sql.ResultRow

interface FilterableDao<T, F> {

    fun mapResultRow(row: ResultRow): T
    fun mapFilterResultRow(row: ResultRow): F
}
