package com.example.data.db

import androidx.room.*
import com.example.data.model.Order
import com.example.data.model.OrderDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): Order?

    @Query("SELECT * FROM order_details WHERE orderId = :orderId")
    suspend fun getOrderDetailsByOrderId(orderId: String): List<OrderDetail>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderDetails(details: List<OrderDetail>)
}
