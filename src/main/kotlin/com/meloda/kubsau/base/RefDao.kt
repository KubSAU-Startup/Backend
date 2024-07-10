package com.meloda.kubsau.base

import org.jetbrains.exposed.sql.ResultRow

interface RefDao<T, K> {

    fun mapFirstResultRow(row: ResultRow): T
    fun mapSecondResultRow(row: ResultRow): K
    fun mapBothResultRow(row: ResultRow): Pair<T, K> {
        val first = mapFirstResultRow(row)
        val second = mapSecondResultRow(row)

        return first to second
    }
}
