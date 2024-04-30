package com.meloda.kubsau.database.programsdisciplines

import com.meloda.kubsau.database.RefDao
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Program
import com.meloda.kubsau.model.WorkType

interface ProgramsDisciplinesDao : RefDao<Program, Discipline> {

    suspend fun allReferences(): List<Triple<Program, Discipline, WorkType>>
    suspend fun allReferencesByIds(programId: Int, disciplineId: Int): List<Triple<Program, Discipline, WorkType>>
    suspend fun allDisciplinesByProgramId(programId: Int): List<Discipline>
    suspend fun allDisciplinesByProgramIds(programIds: List<Int>): List<Discipline>
    suspend fun programByDisciplineId(disciplineId: Int): Program?
    suspend fun workType(programId: Int, disciplineId: Int): WorkType?
    suspend fun addNewReference(programId: Int, disciplineId: Int, workTypeId: Int): Boolean
    suspend fun deleteReference(programId: Int?, disciplineId: Int?): Boolean
    suspend fun deleteReferencesByProgramId(programId: Int): Boolean
}
