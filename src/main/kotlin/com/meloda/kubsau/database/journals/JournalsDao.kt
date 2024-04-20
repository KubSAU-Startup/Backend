package com.meloda.kubsau.database.journals

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Journal

interface JournalsDao : Dao<Journal> {

    suspend fun allJournals(): List<Journal>

    suspend fun allJournals(
        journalId: Int?,
        studentId: Int?,
        groupId: Int?,
        disciplineId: Int?,
        teacherId: Int?,
        workId: Int?,
        departmentId: Int?,
        workTypeId: Int?
    ): List<Journal>

    suspend fun addNewJournal(
        studentId: Int,
        groupId: Int,
        disciplineId: Int,
        teacherId: Int,
        workId: Int
    ): Int?

    suspend fun deleteJournal(journalId: Int): Boolean
}
