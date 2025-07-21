package com.respiroc.domain.chat.stock.dto

data class NewStockPayload(
    val items: List<UpdateItemPayload>
)