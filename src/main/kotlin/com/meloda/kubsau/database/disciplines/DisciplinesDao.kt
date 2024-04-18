package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Discipline

interface DisciplinesDao : Dao<Discipline> {

    suspend fun allDisciplines(): List<Discipline>
    suspend fun allDisciplinesByIds(disciplineIds: List<Int>): List<Discipline>
    suspend fun singleDiscipline(disciplineId: Int): Discipline?
    suspend fun addNewDiscipline(title: String, workTypeId: Int): Discipline?
    suspend fun updateDiscipline(disciplineId: Int, title: String, workTypeId: Int): Int
    suspend fun deleteDiscipline(disciplineId: Int): Boolean
    suspend fun deleteDisciplines(disciplineIds: List<Int>): Boolean
}
