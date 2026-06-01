package com.example.data.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val price: Double,
    val unit: String,
    val stock: Int,
    val imageUrl: String
)
