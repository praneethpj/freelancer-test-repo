package com.respiroc.domain.chat.stock.repository

import com.respiroc.domain.chat.stock.model.ItemType
import com.respiroc.domain.chat.stock.model.ProductStock
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductStockRepository : JpaRepository<ProductStock, Int>{
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<ProductStock>
    fun existsByNameAndItemType(name: String,itemType: ItemType): Boolean

    fun findByNameContainingIgnoreCase(name: String): List<ProductStock>
    fun findByItemType_Name(typeName: String): List<ProductStock>
    fun findByCountLessThan(threshold: Int): List<ProductStock>

    @Query("SELECT SUM(p.count) FROM product_stock p")
    fun sumCount(): Int?
}