package com.example.data.repository

import com.example.data.MockProducts
import com.example.data.db.CartDao
import com.example.data.db.OrderDao
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderDetail
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GroceryRepository(
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) {
    // 1. Product operations (Local Mock API)
    val allProducts: List<Product> = MockProducts.items

    fun getProductById(id: String): Product? {
        return MockProducts.items.find { it.id == id }
    }

    // 2. Cart Operations (Room Database)
    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems()

    suspend fun addToCart(product: Product) {
        val existing = cartDao.getCartItem(product.id)
        if (existing != null) {
            val newQty = existing.quantity + 1
            if (newQty <= product.stock) {
                cartDao.insertOrUpdate(existing.copy(quantity = newQty))
            }
        } else {
            if (product.stock > 0) {
                cartDao.insertOrUpdate(
                    CartItem(
                        productId = product.id,
                        name = product.name,
                        price = product.price,
                        quantity = 1,
                        category = product.category,
                        imageUrl = product.imageUrl,
                        stock = product.stock,
                        unit = product.unit
                    )
                )
            }
        }
    }

    suspend fun updateCartItemQuantity(productId: String, quantity: Int) {
        val existing = cartDao.getCartItem(productId) ?: return
        if (quantity <= 0) {
            cartDao.delete(existing)
        } else if (quantity <= existing.stock) {
            cartDao.insertOrUpdate(existing.copy(quantity = quantity))
        }
    }

    suspend fun removeCartItem(productId: String) {
        cartDao.deleteByProductId(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // 3. Order Operations (Room Database)
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    suspend fun getOrderById(orderId: String): Order? {
        return orderDao.getOrderById(orderId)
    }

    suspend fun getOrderDetails(orderId: String): List<OrderDetail> {
        return orderDao.getOrderDetailsByOrderId(orderId)
    }

    suspend fun placeOrder(order: Order, details: List<OrderDetail>) {
        orderDao.insertOrder(order)
        orderDao.insertOrderDetails(details)
        cartDao.clearCart() // clear cart after successful checkout
    }
}
