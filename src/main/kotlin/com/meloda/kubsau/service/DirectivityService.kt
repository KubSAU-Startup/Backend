package com.meloda.kubsau.service

import com.meloda.kubsau.model.Directivity
import com.meloda.kubsau.model.Group
import com.meloda.kubsau.repository.DirectivityRepository
import com.meloda.kubsau.repository.GroupRepository

interface DirectivityService {
    suspend fun getAllDirectivities(
        facultyId: Int?,
        offset: Int?,
        limit: Int?
    ): List<Directivity>

    suspend fun getDirectivitiesByIds(directivityIds: List<Int>): List<Directivity>
    suspend fun getDirectivityById(directivityId: Int): Directivity?
    suspend fun addDirectivity(title: String, headId: Int, gradeId: Int): Directivity?
    suspend fun editDirectivity(directivityId: Int, title: String, headId: Int, gradeId: Int): Boolean
    suspend fun deleteDirectivity(directivityId: Int): Boolean
    suspend fun deleteDirectivities(directivityIds: List<Int>): Boolean
    suspend fun getGroupsInDirectivity(directivityId: Int): List<Group>
}

class DirectivityServiceImpl(
    private val repository: DirectivityRepository,
    private val groupRepository: GroupRepository
) : DirectivityService {
    override suspend fun getAllDirectivities(
        facultyId: Int?,
        offset: Int?,
        limit: Int?
    ): List<Directivity> = repository.getAllDirectivities(facultyId, offset, limit)

    override suspend fun getDirectivitiesByIds(directivityIds: List<Int>): List<Directivity> =
        repository.getDirectivitiesByIds(directivityIds)

    override suspend fun getDirectivityById(directivityId: Int): Directivity? =
        repository.getDirectivityById(directivityId)

    override suspend fun addDirectivity(title: String, headId: Int, gradeId: Int): Directivity? =
        repository.addDirectivity(title, headId, gradeId)

    override suspend fun editDirectivity(directivityId: Int, title: String, headId: Int, gradeId: Int): Boolean =
        repository.editDirectivity(directivityId, title, headId, gradeId)

    override suspend fun deleteDirectivity(directivityId: Int): Boolean =
        repository.deleteDirectivity(directivityId)

    override suspend fun deleteDirectivities(directivityIds: List<Int>): Boolean =
        repository.deleteDirectivities(directivityIds)

    override suspend fun getGroupsInDirectivity(directivityId: Int): List<Group> =
        groupRepository.getGroupsInDirectivity(directivityId)
}
