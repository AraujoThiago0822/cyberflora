package com.cyberflora.backend.repository

import com.cyberflora.backend.entity.GrowingArea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface GrowingAreaRepository : JpaRepository<GrowingArea, Long> {

    fun findAllByOwnerEmail(email: String): List<GrowingArea>

    fun findByIdAndOwnerEmail(id: Long, email: String): Optional<GrowingArea>

    @Query("SELECT a FROM GrowingArea a WHERE a.owner.email = :email AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY a.name")
    fun searchByName(@Param("name") name: String, @Param("email") email: String): List<GrowingArea>
}