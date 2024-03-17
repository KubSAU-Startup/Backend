package com.meloda.kubsau.database.specializations

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Specialization

interface SpecializationsDao : Dao<Specialization> {

    suspend fun allSpecializations(): List<Specialization>
    suspend fun singleSpecialization(specializationId: Int): Specialization?
    suspend fun addNewSpecialization(title: String): Specialization?
    suspend fun deleteSpecialization(specializationId: Int): Boolean
}
