package com.meloda.kubsau.repository

import com.meloda.kubsau.database.heads.HeadDao
import com.meloda.kubsau.model.Head

interface HeadRepository {
    suspend fun getAllHeads(
        facultyId: Int?,
        offset: Int?,
        limit: Int?
    ): List<Head>

    suspend fun getHeadsByIds(headIds: List<Int>): List<Head>
    suspend fun getHeadById(headId: Int): Head?

    suspend fun addHead(
        code: String,
        abbreviation: String,
        title: String,
        facultyId: Int
    ): Head?

    suspend fun editHead(
        headId: Int,
        code: String,
        abbreviation: String,
        title: String,
        facultyId: Int
    ): Boolean

    suspend fun deleteHead(headId: Int): Boolean
    suspend fun deleteHeads(headIds: List<Int>): Boolean
}

class HeadRepositoryImpl(private val dao: HeadDao) : HeadRepository {
    override suspend fun getAllHeads(facultyId: Int?, offset: Int?, limit: Int?): List<Head> =
        dao.allHeads(facultyId, offset, limit)

    override suspend fun getHeadsByIds(headIds: List<Int>): List<Head> = dao.allHeadsByIds(headIds)

    override suspend fun getHeadById(headId: Int): Head? = dao.singleHead(headId)

    override suspend fun addHead(code: String, abbreviation: String, title: String, facultyId: Int): Head? =
        dao.addNewHead(code, abbreviation, title, facultyId)

    override suspend fun editHead(
        headId: Int,
        code: String,
        abbreviation: String,
        title: String,
        facultyId: Int
    ): Boolean = dao.updateHead(headId, code, abbreviation, title, facultyId)

    override suspend fun deleteHead(headId: Int): Boolean = dao.deleteHead(headId)

    override suspend fun deleteHeads(headIds: List<Int>): Boolean = dao.deleteHeads(headIds)
}
