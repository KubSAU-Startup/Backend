package com.meloda.kubsau.database.works

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.Work
import com.meloda.kubsau.route.works.Entry

interface WorksDao : Dao<Work> {

    suspend fun allWorks(offset: Int?, limit: Int?): List<Work>

    suspend fun allLatestWorks(offset: Int?, limit: Int?): List<Entry>
    suspend fun allLatestWorksByQuery(offset: Int?, limit: Int?, query: String): List<Entry>
    suspend fun allWorksBySearch(
        offset: Int?,
        limit: Int?,
        disciplineId: Int?,
        studentId: Int?,
        groupId: Int?,
        employeeId: Int?,
        departmentId: Int?,
        workTypeId: Int?,
        query: String?
    ): List<Entry>

    suspend fun allWorksByIds(workIds: List<Int>): List<Work>
    suspend fun singleWork(workId: Int): Work?

    suspend fun addNewWork(
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?,
        workTypeId: Int,
        employeeId: Int
    ): Work?

    suspend fun updateWork(
        workId: Int,
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?,
        workTypeId: Int,
        employeeId: Int
    ): Boolean

    suspend fun deleteWork(workId: Int): Boolean
    suspend fun deleteWorks(workIds: List<Int>): Boolean
}
