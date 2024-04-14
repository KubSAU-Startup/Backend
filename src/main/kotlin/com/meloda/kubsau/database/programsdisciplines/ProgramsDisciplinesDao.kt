package com.meloda.kubsau.database.programsdisciplines

import com.meloda.kubsau.database.RefDao
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Program

interface ProgramsDisciplinesDao : RefDao<Program, Discipline> {

    suspend fun allItems(): List<Pair<Program, Discipline>>

    suspend fun allDisciplinesByProgramId(programId: Int): List<Discipline>

    suspend fun addNewReference(programId: Int, disciplineId: Int): Boolean

    suspend fun deleteReference(programId: Int?, disciplineId: Int?): Boolean
}
