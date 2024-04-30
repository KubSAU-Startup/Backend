package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.database.FilterableDao
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.route.works.JournalFilter

interface DisciplinesDao : FilterableDao<Discipline, JournalFilter> {

    suspend fun allDisciplines(): List<Discipline>
    suspend fun allDisciplinesAsFilters(): List<JournalFilter>
    suspend fun allDisciplinesByIds(disciplineIds: List<Int>): List<Discipline>
    suspend fun singleDiscipline(disciplineId: Int): Discipline?
    suspend fun addNewDiscipline(title: String, departmentId: Int): Discipline?
    suspend fun updateDiscipline(disciplineId: Int, title: String, departmentId: Int): Int
    suspend fun deleteDiscipline(disciplineId: Int): Boolean
    suspend fun deleteDisciplines(disciplineIds: List<Int>): Boolean
}
