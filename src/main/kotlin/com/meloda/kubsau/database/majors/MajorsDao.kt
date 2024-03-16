package com.meloda.kubsau.database.majors

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.Major

interface MajorsDao : Dao<Major> {

    suspend fun allMajors(): List<Major>
    suspend fun singleMajor(majorId: Int): Major?
    suspend fun addNewMajor(
        code: String,
        title: String,
        abbreviation: String
    ): Major?
    suspend fun deleteMajor(majorId: Int): Boolean
}
