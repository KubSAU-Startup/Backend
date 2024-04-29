package com.meloda.kubsau.database.studentstatuses

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.StudentStatus

interface StudentStatusesDao : Dao<StudentStatus> {

    suspend fun allStatuses(): List<StudentStatus>
    suspend fun addNewStatus(title: String): StudentStatus?
}
