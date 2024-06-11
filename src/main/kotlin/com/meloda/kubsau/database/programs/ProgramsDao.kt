package com.meloda.kubsau.database.programs

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.Directivity
import com.meloda.kubsau.model.Program
import com.meloda.kubsau.route.programs.SearchEntry

interface ProgramsDao : Dao<Program> {

    suspend fun allPrograms(offset: Int?, limit: Int?): List<Program>
    suspend fun allProgramsByIds(programIds: List<Int>): List<Program>
    suspend fun allProgramsBySemester(semester: Int): List<Program>

    suspend fun allProgramsBySearch(
        programIds: List<Int>?,
        offset: Int?,
        limit: Int?,
        semester: Int?,
        directivityId: Int?,
        query: String?
    ): List<SearchEntry>

    suspend fun allDirectivitiesByPrograms(programIds: List<Int>): List<Pair<Int, Directivity>>

    suspend fun singleProgram(programId: Int): Program?

    suspend fun addNewProgram(
        semester: Int,
        directivityId: Int
    ): Program?

    suspend fun updateProgram(
        programId: Int,
        semester: Int,
        directivityId: Int
    ): Boolean

    suspend fun deleteProgram(programId: Int): Boolean
    suspend fun deletePrograms(programIds: List<Int>): Boolean
}
