package com.meloda.kubsau.database.works

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.controller.Entry
import com.meloda.kubsau.model.Work

interface WorkDao : Dao<Work> {

    suspend fun allWorks(departmentIds: List<Int>?, offset: Int?, limit: Int?): List<Work>
    suspend fun allLatestWorks(departmentIds: List<Int>?, offset: Int?, limit: Int?): List<Entry>
    suspend fun allLatestWorksByQuery(departmentIds: List<Int>?, offset: Int?, limit: Int?, query: String): List<Entry>
    suspend fun allWorksBySearch(
        departmentIds: List<Int>?,
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

    suspend fun allWorksByIds(departmentIds: List<Int>?, workIds: List<Int>): List<Work>
    suspend fun singleWork(departmentIds: List<Int>?, workId: Int): Work?

    suspend fun addNewWork(
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?,
        workTypeId: Int,
        employeeId: Int,
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
