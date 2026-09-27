package com.cyberflora.backend.repository

import com.cyberflora.backend.entity.Plant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlantRepository : JpaRepository<Plant, Long>