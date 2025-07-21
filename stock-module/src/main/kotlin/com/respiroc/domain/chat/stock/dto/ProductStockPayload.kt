package com.respiroc.domain.chat.stock.dto

data class ProductStockPayload(

    var itemTypeId: Int,


    var name: String = "",

    var description: String = "",


    var count: Int = 0
)
