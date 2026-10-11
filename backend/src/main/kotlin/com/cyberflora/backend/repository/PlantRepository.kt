package com.cyberflora.backend.repository

import com.cyberflora.backend.entity.Plant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PlantRepository : JpaRepository<Plant, Long> {

    fun findAllByOwnerEmail(email: String): List<Plant>

    fun findByIdAndOwnerEmail(id: Long, email: String): Optional<Plant>

    @Query("SELECT p FROM Plant p WHERE p.owner.email = :email AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY p.name")
    fun searchByName(@Param("name") name: String, @Param("email") email: String): List<Plant>
}