package com.meloda.kubsau.database.specializationsdisciplines

import com.meloda.kubsau.database.RefDao
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Specialization

interface SpecializationsDisciplinesDao : RefDao<Specialization, Discipline> {

    suspend fun allItems(): List<Pair<Specialization, Discipline>>

    suspend fun addNewReference(specializationId: Int, disciplineId: Int): Boolean

    suspend fun deleteReference(specializationId: Int?, disciplineId: Int?): Boolean
}
