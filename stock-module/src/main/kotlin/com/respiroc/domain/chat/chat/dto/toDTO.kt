package com.respiroc.domain.chat.chat.dto


import com.respiroc.domain.chat.stock.model.ItemType
import com.respiroc.domain.chat.stock.model.ProductStock

fun ItemType.toDTO() = ItemTypeDTO(
    id = this.id,
    name = this.name,
    type = this.type,
    activate = this.activate
)

fun ProductStock.toDTO() = ProductStockDTO(
    id = this.id,
    name = this.name,
    description = this.description,
    count = this.count
)