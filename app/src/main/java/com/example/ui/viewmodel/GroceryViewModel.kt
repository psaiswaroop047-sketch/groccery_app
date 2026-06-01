package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderDetail
import com.example.data.model.Product
import android.content.Context
import com.example.data.repository.GroceryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroceryViewModel(
    private val repository: GroceryRepository,
    context: Context
) : ViewModel() {

    // --- USER PROFILE & LOGIN STATE ---
    private val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _firstName = MutableStateFlow(sharedPrefs.getString("first_name", "") ?: "")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _phoneNumber = MutableStateFlow(sharedPrefs.getString("phone_number", "") ?: "")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    fun login(firstNameValue: String, phoneNumberValue: String) {
        sharedPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("first_name", firstNameValue.trim())
            .putString("phone_number", phoneNumberValue.trim())
            .apply()

        _isLoggedIn.value = true
        _firstName.value = firstNameValue.trim()
        _phoneNumber.value = phoneNumberValue.trim()

        _checkoutName.value = firstNameValue.trim()
        _checkoutMobile.value = phoneNumberValue.trim()
    }

    fun updateProfile(newFirstName: String, newPhoneNumber: String) {
        sharedPrefs.edit()
            .putString("first_name", newFirstName.trim())
            .putString("phone_number", newPhoneNumber.trim())
            .apply()

        _firstName.value = newFirstName.trim()
        _phoneNumber.value = newPhoneNumber.trim()

        _checkoutName.value = newFirstName.trim()
        _checkoutMobile.value = newPhoneNumber.trim()
    }

    fun logout() {
        sharedPrefs.edit().clear().apply()
        _isLoggedIn.value = false
        _firstName.value = ""
        _phoneNumber.value = ""

        _checkoutName.value = ""
        _checkoutMobile.value = ""
        _checkoutAddress.value = ""
        _checkoutPincode.value = ""
    }

    // --- HOME SCREEN STATE (Search & Category Filter) ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _filteredProducts = MutableStateFlow<List<Product>>(repository.allProducts)
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

    init {
        // Combine search query and category filter to update displayed products
        combine(searchQuery, selectedCategory) { query, category ->
            var items = repository.allProducts
            if (category != "All") {
                items = items.filter { it.category.equals(category, ignoreCase = true) }
            }
            if (query.isNotBlank()) {
                items = items.filter { it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
            }
            items
        }.onEach {
            _filteredProducts.value = it
        }.launchIn(viewModelScope)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // --- PRODUCT DETAILS ---
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    fun selectProduct(productId: String) {
        _selectedProduct.value = repository.getProductById(productId)
    }

    // --- CART STATE ---
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartCount: StateFlow<Int> = cartItems
        .map { list -> list.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalQuantity: StateFlow<Int> = cartItems
        .map { list -> list.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCost: StateFlow<Double> = cartItems
        .map { list -> list.sumOf { it.subtotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(product: Product) {
        viewModelScope.launch {
            repository.addToCart(product)
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(item.productId, item.quantity + 1)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(item.productId, item.quantity - 1)
        }
    }

    fun removeCartItem(productId: String) {
        viewModelScope.launch {
            repository.removeCartItem(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // --- CHECKOUT & VALIDATION ---
    private val _checkoutName = MutableStateFlow(sharedPrefs.getString("first_name", "") ?: "")
    val checkoutName: StateFlow<String> = _checkoutName.asStateFlow()

    private val _checkoutMobile = MutableStateFlow(sharedPrefs.getString("phone_number", "") ?: "")
    val checkoutMobile: StateFlow<String> = _checkoutMobile.asStateFlow()

    private val _checkoutAddress = MutableStateFlow("")
    val checkoutAddress: StateFlow<String> = _checkoutAddress.asStateFlow()

    private val _checkoutPincode = MutableStateFlow("")
    val checkoutPincode: StateFlow<String> = _checkoutPincode.asStateFlow()

    // Error states
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _mobileError = MutableStateFlow<String?>(null)
    val mobileError: StateFlow<String?> = _mobileError.asStateFlow()

    private val _addressError = MutableStateFlow<String?>(null)
    val addressError: StateFlow<String?> = _addressError.asStateFlow()

    private val _pincodeError = MutableStateFlow<String?>(null)
    val pincodeError: StateFlow<String?> = _pincodeError.asStateFlow()

    fun updateCheckoutName(value: String) {
        _checkoutName.value = value
        _nameError.value = null
    }

    fun updateCheckoutMobile(value: String) {
        // limit to 10 digits numeric
        val filtered = value.filter { it.isDigit() }.take(10)
        _checkoutMobile.value = filtered
        _mobileError.value = null
    }

    fun updateCheckoutAddress(value: String) {
        _checkoutAddress.value = value
        _addressError.value = null
    }

    fun updateCheckoutPincode(value: String) {
        // limit to 6 digits numeric
        val filtered = value.filter { it.isDigit() }.take(6)
        _checkoutPincode.value = filtered
        _pincodeError.value = null
    }

    private fun validateCheckoutForm(): Boolean {
        var isValid = true

        if (_checkoutName.value.trim().isEmpty()) {
            _nameError.value = "Name cannot be empty"
            isValid = false
        } else {
            _nameError.value = null
        }

        val mobile = _checkoutMobile.value.trim()
        if (mobile.length != 10) {
            _mobileError.value = "Mobile number must be 10 digits"
            isValid = false
        } else {
            _mobileError.value = null
        }

        if (_checkoutAddress.value.trim().isEmpty()) {
            _addressError.value = "Address cannot be empty"
            isValid = false
        } else {
            _addressError.value = null
        }

        val pin = _checkoutPincode.value.trim()
        if (pin.length != 6) {
            _pincodeError.value = "Pincode must be 6 digits"
            isValid = false
        } else {
            _pincodeError.value = null
        }

        return isValid
    }

    // --- PLACE ORDER ---
    private val _lastPlacedOrderId = MutableStateFlow<String?>(null)
    val lastPlacedOrderId: StateFlow<String?> = _lastPlacedOrderId.asStateFlow()

    fun placeOrder(onSuccess: (String) -> Unit) {
        if (!validateCheckoutForm()) return

        val items = cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            val orderId = "ORD${(10000..99999).random()}"
            val totals = cartItems.value
            val totalAmt = totals.sumOf { it.subtotal }
            val totalQt = totals.sumOf { it.quantity }
            val totalIts = totals.size

            val order = Order(
                orderId = orderId,
                timestamp = System.currentTimeMillis(),
                totalAmount = totalAmt,
                totalItems = totalIts,
                totalQuantity = totalQt,
                status = "Pending", // Default initial status
                customerName = _checkoutName.value.trim(),
                mobileNumber = _checkoutMobile.value.trim(),
                address = _checkoutAddress.value.trim(),
                pincode = _checkoutPincode.value.trim()
            )

            val orderDetails = items.map {
                OrderDetail(
                    orderId = orderId,
                    productId = it.productId,
                    productName = it.name,
                    price = it.price,
                    quantity = it.quantity,
                    imageUrl = it.imageUrl,
                    unit = it.unit
                )
            }

            repository.placeOrder(order, orderDetails)
            _lastPlacedOrderId.value = orderId

            // Reset form details (retaining profile name/mobile details)
            _checkoutName.value = _firstName.value
            _checkoutMobile.value = _phoneNumber.value
            _checkoutAddress.value = ""
            _checkoutPincode.value = ""

            onSuccess(orderId)
        }
    }

    // --- ORDER HISTORY ---
    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expand details on-demand or inside historical viewer
    private val _loadedOrderDetails = MutableStateFlow<Map<String, List<OrderDetail>>>(emptyMap())
    val loadedOrderDetails: StateFlow<Map<String, List<OrderDetail>>> = _loadedOrderDetails.asStateFlow()

    fun loadOrderDetails(orderId: String) {
        if (_loadedOrderDetails.value.containsKey(orderId)) return
        viewModelScope.launch {
            val details = repository.getOrderDetails(orderId)
            val updatedMap = _loadedOrderDetails.value.toMutableMap()
            updatedMap[orderId] = details
            _loadedOrderDetails.value = updatedMap
        }
    }
}

class GroceryViewModelFactory(
    private val repository: GroceryRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroceryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroceryViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
