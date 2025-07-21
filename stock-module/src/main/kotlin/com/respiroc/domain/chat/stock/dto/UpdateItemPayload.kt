package com.respiroc.domain.chat.stock.dto

data class UpdateItemPayload(
    val itemTypeId: Int?  ,
    val name: String  ,
    val type:String ,
    val activate:Boolean
)


