package com.respiroc.domain.chat.stock.model

import jakarta.persistence.*

@Entity
class ItemType(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    var name: String = "",

    var type:String="normal",

    var activate: Boolean = true,

    @OneToMany(mappedBy = "itemType", cascade = [CascadeType.ALL], orphanRemoval = true)
    var productStocks: MutableList<ProductStock> = mutableListOf()
)

