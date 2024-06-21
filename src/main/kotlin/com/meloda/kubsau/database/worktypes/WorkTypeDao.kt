package com.meloda.kubsau.database.worktypes

import com.meloda.kubsau.base.FilterableDao
import com.meloda.kubsau.controller.EntryFilter
import com.meloda.kubsau.model.WorkType

interface WorkTypeDao : FilterableDao<WorkType, EntryFilter> {

    suspend fun allWorkTypes(): List<WorkType>
    suspend fun allWorkTypesAsFilters(): List<EntryFilter>
    suspend fun allWorkTypesByIds(workTypeIds: List<Int>): List<WorkType>
    suspend fun singleWorkType(workTypeId: Int): WorkType?
    suspend fun singleWorkType(title: String): WorkType?
    suspend fun addNewWorkType(title: String, needTitle: Boolean): WorkType?
    suspend fun updateWorkType(workTypeId: Int, title: String, needTitle: Boolean): Boolean
    suspend fun deleteWorkType(workTypeId: Int): Boolean
    suspend fun deleteWorkType(title: String): Boolean
    suspend fun deleteWorkTypes(workTypeIds: List<Int>): Boolean
}
