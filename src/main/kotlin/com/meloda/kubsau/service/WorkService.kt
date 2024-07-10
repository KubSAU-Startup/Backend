package com.meloda.kubsau.service

import com.meloda.kubsau.repository.WorkRepository

interface WorkService {

}

class WorkServiceImpl(private val workRepository: WorkRepository) : WorkService {

}
