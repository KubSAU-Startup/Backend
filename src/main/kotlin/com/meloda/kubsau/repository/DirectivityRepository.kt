package com.meloda.kubsau.repository

import com.meloda.kubsau.database.directivities.DirectivityDao
import com.meloda.kubsau.model.Directivity

interface DirectivityRepository {
    suspend fun getAllDirectivities(facultyId: Int?, offset: Int?, limit: Int?): List<Directivity>
    suspend fun getDirectivitiesByIds(directivityIds: List<Int>): List<Directivity>
    suspend fun getDirectivityById(directivityId: Int): Directivity?
    suspend fun addDirectivity(title: String, headId: Int, gradeId: Int): Directivity?
    suspend fun editDirectivity(directivityId: Int, title: String, headId: Int, gradeId: Int): Boolean
    suspend fun deleteDirectivity(directivityId: Int): Boolean
    suspend fun deleteDirectivities(directivityIds: List<Int>): Boolean
}

class DirectivityRepositoryImpl(private val dao: DirectivityDao) : DirectivityRepository {
    override suspend fun getAllDirectivities(facultyId: Int?, offset: Int?, limit: Int?): List<Directivity> =
        dao.allDirectivities(facultyId, offset, limit)

    override suspend fun getDirectivitiesByIds(directivityIds: List<Int>): List<Directivity> =
        dao.allDirectivitiesByIds(directivityIds)

    override suspend fun getDirectivityById(directivityId: Int): Directivity? =
        dao.singleDirectivity(directivityId)

    override suspend fun addDirectivity(title: String, headId: Int, gradeId: Int): Directivity? =
        dao.addNewDirectivity(title, headId, gradeId)

    override suspend fun editDirectivity(directivityId: Int, title: String, headId: Int, gradeId: Int): Boolean =
        dao.updateDirectivity(directivityId, title, headId, gradeId)

    override suspend fun deleteDirectivity(directivityId: Int): Boolean =
        dao.deleteDirectivity(directivityId)

    override suspend fun deleteDirectivities(directivityIds: List<Int>): Boolean =
        dao.deleteDirectivities(directivityIds)
}
