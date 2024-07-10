package com.meloda.kubsau.database.directivities

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.Directivity

interface DirectivityDao : Dao<Directivity> {

    suspend fun allDirectivities(facultyId: Int?, offset: Int?, limit: Int?): List<Directivity>
    suspend fun allDirectivitiesByIds(directivityIds: List<Int>): List<Directivity>
    suspend fun singleDirectivity(directivityId: Int): Directivity?
    suspend fun addNewDirectivity(title: String, headId: Int, gradeId: Int): Directivity?
    suspend fun updateDirectivity(directivityId: Int, title: String, headId: Int, gradeId: Int): Boolean
    suspend fun deleteDirectivity(directivityId: Int): Boolean
    suspend fun deleteDirectivities(directivityIds: List<Int>): Boolean
}
