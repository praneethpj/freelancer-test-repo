package com.respiroc.domain.chat.stock.dto

data class UpdateProductStockPayload(

    var id:Int?,
    var itemTypeId: Int,


    var name: String = "",

    var description: String = "",


    var count: Int = 0
)
