package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String,
    val imageUrl: String,
    val stock: Int,
    val unit: String
) {
    val subtotal: Double
        get() = price * quantity
}
