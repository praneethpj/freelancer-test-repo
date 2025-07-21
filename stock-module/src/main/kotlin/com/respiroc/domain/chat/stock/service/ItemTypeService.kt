package com.respiroc.domain.chat.stock.service

import com.respiroc.domain.chat.stock.dto.ItemTypePayload
import com.respiroc.domain.chat.stock.model.ItemType
import com.respiroc.domain.chat.stock.repository.ItemTypeRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional

import org.springframework.stereotype.Service


import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader

@Service
@Transactional
class ItemTypeService(
    private val itemTypeRepository: ItemTypeRepository
) {



    fun createItemType(payload: ItemTypePayload): ItemType {
        if (itemTypeRepository.existsByName(payload.name)) {
            throw IllegalArgumentException("ItemType with name '${payload.name}' already exists.")
        }
        return itemTypeRepository.save(ItemType(name = payload.name, type = payload.type, activate = payload.activate))
    }



    fun getItemTypesByPageAndKeyword(page: Int, size: Int, keyword: String?): Page<ItemType> {
        val sort = Sort.by(Sort.Direction.ASC, "name")
        val pageable = PageRequest.of(page, size, sort)
        return if (keyword.isNullOrBlank()) {
            itemTypeRepository.findAll(pageable)
        } else {
            itemTypeRepository.findByNameContainingIgnoreCase(keyword, pageable)
        }
    }

    fun updateItemType(id: Int, payload: ItemTypePayload): ItemType {
        val itemType = itemTypeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("ItemType not found with ID $id") }

        itemType.name = payload.name
        itemType.type = payload.type
        itemType.activate = payload.activate

        return itemTypeRepository.save(itemType)
    }

    fun toggleStatus(id: Int) {
        val itemType = itemTypeRepository.findById(id).orElseThrow {
            EntityNotFoundException("ItemType not found with ID $id")
        }
        itemType.activate = !itemType.activate
        itemTypeRepository.save(itemType)
    }



    fun getItemTypeById(id: Int): ItemType {
        return itemTypeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("ItemType not found with id $id") }
    }

    fun importFromCsv(file: MultipartFile) {
        val reader = BufferedReader(InputStreamReader(file.inputStream))
        val lines = reader.readLines()

        if (lines.isEmpty()) throw IllegalArgumentException("CSV file is empty")

        val headerSkipped = lines.drop(1)

        for (line in headerSkipped) {
            val tokens = line.split(",").map { it.trim() }
            if (tokens.size >= 3) {
                if(itemTypeRepository.existsByName(name = tokens[0])==false){
                    val itemType = ItemType(
                        name = tokens[0],
                        type = tokens[1],
                        activate = tokens[2].toBooleanStrictOrNull() ?: true
                    )
                    itemTypeRepository.save(itemType)
                }

            }
        }
    }
    fun findByType(type: String): List<ItemType> =
        itemTypeRepository.findByType(type)

    fun findInactive(): List<ItemType> =
        itemTypeRepository.findByActivate(false)

    fun findByActivate(): List<ItemType> =
        itemTypeRepository.findByActivate(true)
}
