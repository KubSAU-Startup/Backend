package com.meloda.kubsau.repository

import com.meloda.kubsau.database.disciplines.DisciplineDao
import com.meloda.kubsau.model.Discipline

interface DisciplineRepository {
    suspend fun getAllDisciplines(): List<Discipline>
    suspend fun getDisciplinesByIds(disciplineIds: List<Int>): List<Discipline>
    suspend fun getDisciplineById(disciplineId: Int): Discipline?
    suspend fun addDiscipline(title: String, departmentId: Int): Discipline?
    suspend fun editDiscipline(disciplineId: Int, title: String, departmentId: Int): Boolean
    suspend fun deleteDiscipline(disciplineId: Int): Boolean
    suspend fun deleteDisciplines(disciplineIds: List<Int>): Boolean
}

class DisciplineRepositoryImpl(private val dao: DisciplineDao) : DisciplineRepository {
    override suspend fun getAllDisciplines(): List<Discipline> = dao.allDisciplines()

    override suspend fun getDisciplinesByIds(disciplineIds: List<Int>): List<Discipline> =
        dao.allDisciplinesByIds(disciplineIds)

    override suspend fun getDisciplineById(disciplineId: Int): Discipline? =
        dao.singleDiscipline(disciplineId)

    override suspend fun addDiscipline(title: String, departmentId: Int): Discipline? =
        dao.addNewDiscipline(title, departmentId)

    override suspend fun editDiscipline(disciplineId: Int, title: String, departmentId: Int): Boolean =
        dao.updateDiscipline(disciplineId, title, departmentId)

    override suspend fun deleteDiscipline(disciplineId: Int): Boolean =
        dao.deleteDiscipline(disciplineId)

    override suspend fun deleteDisciplines(disciplineIds: List<Int>): Boolean =
        dao.deleteDisciplines(disciplineIds)
}
