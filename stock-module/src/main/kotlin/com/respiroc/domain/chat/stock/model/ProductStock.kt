package com.respiroc.domain.chat.stock.model


import com.respiroc.domain.chat.common.model.BaseAuditEntity
import jakarta.persistence.*

@Entity(name = "product_stock")
class ProductStock(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    var name: String = "",

    var description: String="",

    @ManyToOne
    @JoinColumn(name = "item_type_id")
    var itemType: ItemType? = null,

    var count: Int? = null
) : BaseAuditEntity()
