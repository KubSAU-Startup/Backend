package com.meloda.kubsau.database.programs

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Program

interface ProgramsDao : Dao<Program> {

    suspend fun allPrograms(): List<Program>
    suspend fun allProgramsByIds(programIds: List<Int>): List<Program>
    suspend fun programsBySemester(semester: Int): List<Program>
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
