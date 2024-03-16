package com.meloda.kubsau.database.worktypes

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.WorkType

interface WorkTypesDao : Dao<WorkType> {

    suspend fun allWorkTypes(): List<WorkType>
    suspend fun singleWorkType(workTypeId: Int): WorkType?
    suspend fun singleWorkType(title: String): WorkType?
    suspend fun addNewWorkType(title: String, isEditable: Boolean): WorkType?
    suspend fun deleteWorkType(workTypeId: Int): Boolean
    suspend fun deleteWorkType(title: String): Boolean
}
