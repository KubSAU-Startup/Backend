package com.meloda.kubsau.database.heads

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Head

interface HeadsDao : Dao<Head> {

    suspend fun allHeads(): List<Head>

    suspend fun addNewHead(
        code: String,
        abbreviation: String,
        title: String,
        facultyId: Int
    ): Head?
}
