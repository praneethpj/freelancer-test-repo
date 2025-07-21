package com.respiroc.domain.chat.stock.repository


import com.respiroc.domain.chat.stock.model.ItemType

import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*


interface ItemTypeRepository : JpaRepository<ItemType, Int>{
    fun existsByName(name: String): Boolean
    override fun findById(id:Int): Optional<ItemType>
    fun findByNameContainingIgnoreCase(keyword: String): List<ItemType>
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<ItemType>

    fun findByType(type: String): List<ItemType>
    fun findByActivate(activate: Boolean): List<ItemType>


}