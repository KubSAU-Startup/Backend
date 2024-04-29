package com.meloda.kubsau.database.directivities

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Directivity

interface DirectivitiesDao : Dao<Directivity> {

    suspend fun allDirectivities(): List<Directivity>
    suspend fun addNewDirectivity(title: String, headId: Int): Directivity?
}
