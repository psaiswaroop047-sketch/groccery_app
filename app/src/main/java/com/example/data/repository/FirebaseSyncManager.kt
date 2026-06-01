package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderDetail
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseSyncManager(private val context: Context) {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var isFirebaseInit = false

    companion object {
        private const val TAG = "FirebaseSyncManager"
    }

    init {
        try {
            // Check if Firebase is already initialized
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }
            if (app != null) {
                firebaseAuth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                isFirebaseInit = true
                _syncStatus.value = SyncStatus.Configured("Firebase initialized with local dummy/default endpoints. Standing by.")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize Firebase SDK safely. Offline Room DB fallback active.", e)
            _syncStatus.value = SyncStatus.Error("Firebase setup failed: ${e.localizedMessage}. Using offline DB mode.")
        }
    }

    fun isEnabled(): Boolean = isFirebaseInit && firestore != null

    suspend fun authenticateUser(phoneNumber: String, firstName: String) {
        if (!isEnabled()) {
            _syncStatus.value = SyncStatus.OfflineFallback("Firebase is unconfigured. Profile changes saved locally.")
            return
        }
        try {
            _syncStatus.value = SyncStatus.Syncing("Signing into Firebase cloud securely...")
            val auth = firebaseAuth ?: return
            
            // We register/sign-in anonymously to create an active Firebase session
            val authResult = auth.signInAnonymously().await()
            val user = authResult.user
            if (user != null) {
                // Once authenticated, sync profile attributes to users collection
                val userRef = firestore!!.collection("users").document(phoneNumber)
                val profileMap = hashMapOf(
                    "uid" to user.uid,
                    "firstName" to firstName,
                    "phoneNumber" to phoneNumber,
                    "lastActive" to System.currentTimeMillis()
                )
                userRef.set(profileMap).await()
                _syncStatus.value = SyncStatus.Success("Cloud integration established! Logged in as: $firstName")
            } else {
                _syncStatus.value = SyncStatus.Error("Firebase authenticated user session is null.")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Firebase Authentication/Profile sync failed: ${e.localizedMessage}")
            _syncStatus.value = SyncStatus.Error("Cloud Sync Error: ${e.localizedMessage}")
        }
    }

    suspend fun syncCart(phoneNumber: String, items: List<CartItem>) {
        if (!isEnabled() || phoneNumber.isBlank()) return
        try {
            _syncStatus.value = SyncStatus.Syncing("Backing up grocery cart to cloud...")
            val db = firestore ?: return
            
            // Save active cart elements to user subcollection
            val cartRef = db.collection("users").document(phoneNumber).collection("cart")
            
            // Delete old cart entries in Firestore to represent current state
            val oldItems = cartRef.get().await()
            for (doc in oldItems.documents) {
                doc.reference.delete()
            }
            
            // Insert active entries
            for (item in items) {
                val itemMap = hashMapOf(
                    "productId" to item.productId,
                    "name" to item.name,
                    "price" to item.price,
                    "quantity" to item.quantity,
                    "category" to item.category,
                    "imageUrl" to item.imageUrl,
                    "stock" to item.stock,
                    "unit" to item.unit,
                    "subtotal" to item.subtotal
                )
                cartRef.document(item.productId).set(itemMap)
            }
            _syncStatus.value = SyncStatus.Success("Shopping cart backed up to Firebase Cloud Firestore successfully!")
        } catch (e: Throwable) {
            Log.e(TAG, "Cart cloud sync failed: ${e.localizedMessage}")
            _syncStatus.value = SyncStatus.Error("Cloud Cart Sync Failed: ${e.localizedMessage}")
        }
    }

    suspend fun syncOrder(phoneNumber: String, order: Order, details: List<OrderDetail>) {
        if (!isEnabled() || phoneNumber.isBlank()) return
        try {
            _syncStatus.value = SyncStatus.Syncing("Uploading transaction ORD-${order.orderId} to historical ledger...")
            val db = firestore ?: return
            
            val userDoc = db.collection("users").document(phoneNumber)
            val orderDoc = userDoc.collection("orders").document(order.orderId)
            
            // 1. Write core order variables
            val orderMap = hashMapOf(
                "orderId" to order.orderId,
                "timestamp" to order.timestamp,
                "totalAmount" to order.totalAmount,
                "totalItems" to order.totalItems,
                "totalQuantity" to order.totalQuantity,
                "status" to order.status,
                "customerName" to order.customerName,
                "mobileNumber" to order.mobileNumber,
                "address" to order.address,
                "pincode" to order.pincode
            )
            orderDoc.set(orderMap).await()
            
            // 2. Write order subsets (Details collection)
            val detailsCol = orderDoc.collection("items")
            for (item in details) {
                val detailMap = hashMapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "price" to item.price,
                    "quantity" to item.quantity,
                    "imageUrl" to item.imageUrl,
                    "unit" to item.unit
                )
                detailsCol.document(item.productId).set(detailMap)
            }
            
            // Also clean active cart backup from Cloud Firestore since the order completed
            val cartRef = userDoc.collection("cart")
            val oldCartItems = cartRef.get().await()
            for (doc in oldCartItems.documents) {
                doc.reference.delete()
            }

            _syncStatus.value = SyncStatus.Success("Transaction ledger ORD-${order.orderId} backed up to Cloud Firestore!")
        } catch (e: Throwable) {
            Log.e(TAG, "Order cloud sync failed: ${e.localizedMessage}")
            _syncStatus.value = SyncStatus.Error("Cloud Order Ledger Sync Failed: ${e.localizedMessage}")
        }
    }
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    class Configured(val message: String) : SyncStatus()
    class Syncing(val message: String) : SyncStatus()
    class Success(val message: String) : SyncStatus()
    class OfflineFallback(val message: String) : SyncStatus()
    class Error(val message: String) : SyncStatus()
}
