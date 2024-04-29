package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Work

interface WorksDao : Dao<Work> {

    suspend fun allWorks(): List<Work>
    suspend fun allWorksByIds(workIds: List<Int>): List<Work>
    suspend fun singleWork(workId: Int): Work?

    suspend fun addNewWork(
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?
    ): Work?

    suspend fun updateWork(
        workId: Int,
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?
    ): Int

    suspend fun deleteWork(workId: Int): Boolean
    suspend fun deleteWorks(workIds: List<Int>): Boolean
}
