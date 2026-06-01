package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val orderId: String, // ORD12345 style
    val timestamp: Long,
    val totalAmount: Double,
    val totalItems: Int,
    val totalQuantity: Int,
    val status: String, // "Pending", "Confirmed", "Delivered"
    val customerName: String,
    val mobileNumber: String,
    val address: String,
    val pincode: String
)
