package com.meloda.kubsau.database.heads

import com.meloda.kubsau.base.Dao
import com.meloda.kubsau.model.Head

interface HeadDao : Dao<Head> {

    suspend fun allHeads(facultyId: Int?, offset: Int?, limit: Int?): List<Head>
    suspend fun allHeadsByIds(headIds: List<Int>): List<Head>
    suspend fun singleHead(headId: Int): Head?
    suspend fun addNewHead(code: String, abbreviation: String, title: String, facultyId: Int): Head?
    suspend fun updateHead(headId: Int, code: String, abbreviation: String, title: String, facultyId: Int): Boolean
    suspend fun deleteHead(headId: Int): Boolean
    suspend fun deleteHeads(headIds: List<Int>): Boolean
}
