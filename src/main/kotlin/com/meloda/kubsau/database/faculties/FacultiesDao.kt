package com.meloda.kubsau.database.faculties

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Faculty

interface FacultiesDao : Dao<Faculty> {

    suspend fun allFaculties(offset: Int?, limit: Int?): List<Faculty>
    suspend fun allFacultiesByIds(facultyIds: List<Int>): List<Faculty>
    suspend fun singleFaculty(facultyId: Int): Faculty?
    suspend fun addNewFaculty(title: String): Faculty?
}
