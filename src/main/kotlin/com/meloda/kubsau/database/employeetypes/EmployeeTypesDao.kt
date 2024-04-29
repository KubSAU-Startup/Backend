package com.meloda.kubsau.database.employeetypes

import com.meloda.kubsau.database.Dao
import com.meloda.kubsau.model.EmployeeType

interface EmployeeTypesDao : Dao<EmployeeType> {

    suspend fun allTypes(): List<EmployeeType>
    suspend fun addNewType(title: String): EmployeeType?
}
