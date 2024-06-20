package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.base.FilterableDao
import com.meloda.kubsau.controller.EntryFilter
import com.meloda.kubsau.model.Discipline

interface DisciplineDao : FilterableDao<Discipline, EntryFilter> {

    suspend fun allDisciplines(departmentIds: List<Int>?): List<Discipline>
    suspend fun allDisciplinesAsFilters(departmentIds: List<Int>?): List<EntryFilter>
    suspend fun allDisciplinesByIds(disciplineIds: List<Int>): List<Discipline>
    suspend fun singleDiscipline(disciplineId: Int): Discipline?
    suspend fun addNewDiscipline(title: String, departmentId: Int): Discipline?
    suspend fun updateDiscipline(disciplineId: Int, title: String, departmentId: Int): Boolean
    suspend fun deleteDiscipline(disciplineId: Int): Boolean
    suspend fun deleteDisciplines(disciplineIds: List<Int>): Boolean
}
