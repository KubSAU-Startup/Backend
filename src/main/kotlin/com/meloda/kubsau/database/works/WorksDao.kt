package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Work
import com.meloda.kubsau.route.works.JournalItem

interface WorksDao : Dao<Work> {

    suspend fun allWorks(): List<Work>

    suspend fun allWorksByFilters(
        disciplineId: Int?,
        studentId: Int?,
        groupId: Int?,
        employeeId: Int?,
        departmentId: Int?,
        workTypeId: Int?
    ): List<JournalItem>

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
