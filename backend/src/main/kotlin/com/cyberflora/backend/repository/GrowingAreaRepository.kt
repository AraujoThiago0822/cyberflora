package com.cyberflora.backend.repository

import com.cyberflora.backend.entity.GrowingArea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GrowingAreaRepository : JpaRepository<GrowingArea, Long>