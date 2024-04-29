package com.meloda.kubsau.database.faculties

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Faculty

interface FacultiesDao : Dao<Faculty> {

    suspend fun allFaculties(): List<Faculty>
    suspend fun addNewFaculty(title: String): Faculty?
}
