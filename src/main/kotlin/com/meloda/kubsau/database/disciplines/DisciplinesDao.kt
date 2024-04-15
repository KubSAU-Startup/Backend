package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Discipline

interface DisciplinesDao : Dao<Discipline> {

    suspend fun allDisciplines(): List<Discipline>
    suspend fun singleDiscipline(disciplineId: Int): Discipline?
    suspend fun addNewDiscipline(title: String, workTypeId: Int): Discipline?
    suspend fun deleteDiscipline(disciplineId: Int): Boolean
}
