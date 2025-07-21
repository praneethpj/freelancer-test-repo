package com.respiroc.domain.chat.chat.service

import com.respiroc.domain.chat.stock.model.ItemType
import com.respiroc.domain.chat.stock.model.ProductStock
import com.respiroc.domain.chat.stock.repository.ItemTypeRepository
import com.respiroc.domain.chat.stock.repository.ProductStockRepository
import org.springframework.stereotype.Service

@Service
class StockQueryService(
    private val itemTypeRepo: ItemTypeRepository,
    private val productStockRepo: ProductStockRepository
) {

    fun searchByItemType(type: String): List<ItemType> =
        itemTypeRepo.findByType(type)

    fun inactiveItemTypes(): List<ItemType> =
        itemTypeRepo.findByActivate(false)

    fun lowStockProducts(threshold: Int = 10): List<ProductStock> =
        productStockRepo.findByCountLessThan(threshold)

    fun searchProductsByItemTypeName(typeName: String): List<ProductStock> =
        productStockRepo.findByItemType_Name(typeName)

    fun searchProductByName(query: String): List<ProductStock> =
        productStockRepo.findByNameContainingIgnoreCase(query)
}
