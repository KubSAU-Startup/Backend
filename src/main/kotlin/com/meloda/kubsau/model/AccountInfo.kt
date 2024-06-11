package com.meloda.kubsau.model

data class AccountInfo(
    val id: Int,
    val type: Int,
    val login: String,
    val selectedDepartmentId: Int?,
    val faculty: Faculty?,
    val departments: List<Department>
)
