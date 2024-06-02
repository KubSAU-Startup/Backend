package com.meloda.kubsau.database.programsdisciplines

import com.meloda.kubsau.common.IdTitle
import com.meloda.kubsau.database.RefDao
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Program
import com.meloda.kubsau.model.WorkType
import com.meloda.kubsau.route.programs.FullDisciplineIds
import com.meloda.kubsau.route.programs.SearchDisciplineWithProgramId

interface ProgramsDisciplinesDao : RefDao<Program, Discipline> {

    suspend fun allReferences(offset: Int?, limit: Int?): List<Triple<Program, Discipline, WorkType>>
    suspend fun allReferencesByIds(programId: Int, disciplineId: Int): List<Triple<Program, Discipline, WorkType>>
    suspend fun allDisciplinesByProgramId(programId: Int): List<Discipline>
    suspend fun allDisciplinesByProgramIdShortened(programId: Int): List<IdTitle>
    suspend fun allDisciplinesByProgramIds(programIds: List<Int>): List<Discipline>
    suspend fun allSearchDisciplinesByProgramIds(programIds: List<Int>): List<SearchDisciplineWithProgramId>
    suspend fun allDisciplineIdsByProgramId(programId: Int): List<Int>
    suspend fun allDisciplineIdsByProgramIdAsMap(programId: Int): List<FullDisciplineIds>
    suspend fun allDisciplineIdsByProgramIdsAsMap(programIds: List<Int>): Map<Int, List<FullDisciplineIds>>
    suspend fun programByDisciplineId(disciplineId: Int): Program?
    suspend fun workType(programId: Int, disciplineId: Int): WorkType?
    suspend fun workTypeId(programId: Int, disciplineId: Int): Int?
    suspend fun addNewReference(programId: Int, disciplineId: Int, workTypeId: Int): Boolean
    suspend fun deleteReference(programId: Int?, disciplineId: Int?): Boolean
    suspend fun deleteReferencesByProgramId(programId: Int): Boolean
}
