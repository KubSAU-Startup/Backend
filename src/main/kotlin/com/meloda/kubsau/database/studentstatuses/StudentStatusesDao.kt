package com.meloda.kubsau.database.studentstatuses

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.StudentStatus

interface StudentStatusesDao : Dao<StudentStatus> {

    suspend fun allStatuses(): List<StudentStatus>
    suspend fun allStatusesByIds(statusIds: List<Int>): List<StudentStatus>
    suspend fun singleStatus(statusId: Int): StudentStatus?
    suspend fun addNewStatus(title: String): StudentStatus?
}
