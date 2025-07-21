package com.respiroc.domain.chat.stock.service

import com.respiroc.domain.chat.stock.dto.ProductStockPayload
import com.respiroc.domain.chat.stock.model.ProductStock
import com.respiroc.domain.chat.stock.repository.ItemTypeRepository
import com.respiroc.domain.chat.stock.repository.ProductStockRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader

@Service
@Transactional
class ProductStockService(
    private val productStockRepository: ProductStockRepository,
    private val itemTypeRepository: ItemTypeRepository
) {

    fun createProductStock(payload: ProductStockPayload): ProductStock {
        val itemType = itemTypeRepository.findById(payload.itemTypeId!!)
            .orElseThrow { EntityNotFoundException("ItemType not found with ID ${payload.itemTypeId}") }


        val productStock = ProductStock(
            name = payload.name,
            description = payload.description,
            itemType = itemType,
            count = payload.count
        )
        return productStockRepository.save(productStock)
    }

    fun getProductStocksByPageAndKeyword(page: Int, size: Int, keyword: String?): Page<ProductStock> {
        val sort = Sort.by(Sort.Direction.ASC, "name")
        val pageable = PageRequest.of(page, size, sort)
        return if (keyword.isNullOrBlank()) {
            productStockRepository.findAll(pageable)
        } else {
            productStockRepository.findByNameContainingIgnoreCase(keyword, pageable)
        }
    }

    fun updateProductStock(id: Int, payload: ProductStockPayload): ProductStock {
        val productStock = productStockRepository.findById(id)
            .orElseThrow { EntityNotFoundException("ProductStock not found with ID $id") }

        val itemType = itemTypeRepository.findById(payload.itemTypeId)
            .orElseThrow { EntityNotFoundException("ItemType not found with ID ${payload.itemTypeId}") }

        productStock.name = payload.name
        productStock.description = payload.description
        productStock.itemType = itemType
        productStock.count = payload.count

        return productStockRepository.save(productStock)
    }

    fun getProductStockById(id: Int): ProductStock {
        return productStockRepository.findById(id)
            .orElseThrow { EntityNotFoundException("ProductStock not found with id $id") }
    }

    fun deleteProductStock(id: Int) {
        if (!productStockRepository.existsById(id)) {
            throw EntityNotFoundException("ProductStock not found with id $id")
        }
        productStockRepository.deleteById(id)
    }
    fun importProductStocksFromCsv(file: MultipartFile) {
        val reader = BufferedReader(InputStreamReader(file.inputStream))
        val lines = reader.readLines()

        if (lines.isEmpty()) throw IllegalArgumentException("CSV file is empty")

        val headerSkipped = lines.drop(1)

        for (line in headerSkipped) {
            val tokens = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (tokens.size >= 4) {
                val name = tokens[0]
                val description = tokens[1]
                val itemTypeId: Int = tokens[2].toIntOrNull() ?: 0
                    ?: throw IllegalArgumentException("Invalid itemTypeId value: '${tokens[2]}'")
                val count = tokens[3].toIntOrNull() ?: 0

                val optionalItemType = itemTypeRepository.findById(itemTypeId)
                if (optionalItemType.isEmpty) {
                    println("ItemType with ID $itemTypeId not found. Skipping line: $line")
                    continue
                }

                val itemType = optionalItemType.get()
                val exists = productStockRepository.existsByNameAndItemType(name, itemType)
                println("Checking for name=$name, itemTypeId=$itemTypeId, exists=$exists")

                if (!exists) {
                    val productStock = ProductStock(
                        name = name,
                        description = description,
                        itemType = itemType,
                        count = count
                    )
                    productStockRepository.save(productStock)
                    println("Saved: $productStock")
                } else {
                    println("Duplicate found. Skipping: $name")
                }
            }
        }
    }

    fun findByCountLessThan(threshold: Int): List<ProductStock> =
        productStockRepository.findByCountLessThan(threshold)

    fun findByNameContaining(name: String): List<ProductStock> =
        productStockRepository.findByNameContainingIgnoreCase(name)

    fun findByItemTypeName(typeName: String): List<ProductStock> =
        productStockRepository.findByItemType_Name(typeName)


    fun getTotalStockCount(): Int {
        return productStockRepository.sumCount() ?: 0
    }
    fun getAllProductStocks(): List<ProductStock> = productStockRepository.findAll()

}
