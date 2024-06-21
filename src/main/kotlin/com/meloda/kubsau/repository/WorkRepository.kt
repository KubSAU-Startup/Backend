package com.meloda.kubsau.repository

import com.meloda.kubsau.database.works.WorkDao
import com.meloda.kubsau.model.Work

interface WorkRepository {
    suspend fun getAllWorks(departmentIds: List<Int>?, offset: Int?, limit: Int?): List<Work>
    suspend fun getWorksByIds(departmentIds: List<Int>?, workIds: List<Int>): List<Work>
    suspend fun getWorkById(departmentIds: List<Int>?, workId: Int): Work?
}

class WorkRepositoryImpl(private val workDao: WorkDao) : WorkRepository {
    override suspend fun getAllWorks(
        departmentIds: List<Int>?,
        offset: Int?,
        limit: Int?
    ): List<Work> = workDao.allWorks(departmentIds, offset, limit)

    override suspend fun getWorksByIds(
        departmentIds: List<Int>?,
        workIds: List<Int>
    ): List<Work> = workDao.allWorksByIds(workIds)

    override suspend fun getWorkById(
        departmentIds: List<Int>?,
        workId: Int
    ): Work? = workDao.singleWork(workId)
}
