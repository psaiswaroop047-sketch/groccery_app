package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.ui.viewmodel.GroceryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit,
    onOrderPlacedSuccessfully: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItemsState by viewModel.cartItems.collectAsState()
    val totalPriceState by viewModel.totalCost.collectAsState()

    // Cache the cart items and total cost to prevent displaying "0" or empty summaries during transitions
    var cachedCartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var cachedTotalPrice by remember { mutableStateOf(0.0) }

    LaunchedEffect(cartItemsState) {
        if (cartItemsState.isNotEmpty()) {
            cachedCartItems = cartItemsState
        }
    }

    LaunchedEffect(totalPriceState) {
        if (totalPriceState > 0.0) {
            cachedTotalPrice = totalPriceState
        }
    }

    // Fallbacks if the DB loaded but we have cached versions
    val cartItems = if (cartItemsState.isNotEmpty()) cartItemsState else cachedCartItems
    val totalPrice = if (totalPriceState > 0.0) totalPriceState else cachedTotalPrice

    // Form inputs
    val name by viewModel.checkoutName.collectAsState()
    val mobile by viewModel.checkoutMobile.collectAsState()
    val address by viewModel.checkoutAddress.collectAsState()
    val pincode by viewModel.checkoutPincode.collectAsState()

    // Errors
    val nameError by viewModel.nameError.collectAsState()
    val mobileError by viewModel.mobileError.collectAsState()
    val addressError by viewModel.addressError.collectAsState()
    val pincodeError by viewModel.pincodeError.collectAsState()

    val isOrdering by viewModel.isOrdering.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Review & Pay", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("checkout_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val windowSize = rememberWindowSizeClass()
        val isWideLayout = windowSize.width == WindowSizeClass.Expanded || windowSize.width == WindowSizeClass.Medium || windowSize.isLandscape

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isWideLayout) {
                // Tablet / Landscape Split screen layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1100.dp)
                        .padding(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left Column: Delivery Details Form
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Delivery Details",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            // --- Full Name Form Input (Read Only) ---
                            Column {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {},
                                    label = { Text("Full Name (Read Only)") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp),
                                    readOnly = true,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checkout_name_input")
                                )
                            }

                            // --- Mobile Number Form Input (Read Only) ---
                            Column {
                                OutlinedTextField(
                                    value = mobile,
                                    onValueChange = {},
                                    label = { Text("Mobile Number (Read Only)") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp),
                                    readOnly = true,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checkout_mobile_input")
                                )
                            }

                            // --- Address Form Input ---
                            Column {
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { viewModel.updateCheckoutAddress(it) },
                                    label = { Text("Delivery Address") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp),
                                    isError = addressError != null,
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checkout_address_input")
                                )
                                if (addressError != null) {
                                    Text(
                                        text = addressError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp).testTag("checkout_address_error")
                                    )
                                }
                            }

                            // --- Pincode Form Input ---
                            Column {
                                OutlinedTextField(
                                    value = pincode,
                                    onValueChange = { viewModel.updateCheckoutPincode(it) },
                                    label = { Text("Pincode (6 digits)") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp),
                                    isError = pincodeError != null,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checkout_pincode_input")
                                )
                                if (pincodeError != null) {
                                    Text(
                                        text = pincodeError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp).testTag("checkout_pincode_error")
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: Order Summary Info
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .wrapContentHeight()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Order Summary",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // List all items in summary
                            cartItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.7f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "${item.quantity} x ₹${item.price.toInt()} / ${item.unit}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                    }
                                    Text(
                                        text = "₹${item.subtotal.toInt()}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Grand total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grand Total",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${totalPrice.toInt()}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                // Mobile Portrait layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Order Summary Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Order Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // List all items in summary
                            cartItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.7f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "${item.quantity} x ₹${item.price.toInt()} / ${item.unit}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                    }
                                    Text(
                                        text = "₹${item.subtotal.toInt()}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grand total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grand Total",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${totalPrice.toInt()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 2. Customer Delivery Details Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Delivery Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            // --- Full Name Form Input (Read Only) ---
                            Column {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {},
                                    label = { Text("Full Name (Read Only)") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp),
                                    readOnly = true,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checkout_name_input")
                                )
                            }

                            // --- Mobile Number Form Input (Read Only) ---
                            Column {
                                OutlinedTextField(
                                    value = mobile,
                                    onValueChange = {},
                                    label = { Text("Mobile Number (Read Only)") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp),
                                    readOnly = true,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checkout_mobile_input")
                                )
                            }

                            // --- Address Form Input ---
                            Column {
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { viewModel.updateCheckoutAddress(it) },
                                    label = { Text("Delivery Address") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp),
                                    isError = addressError != null,
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checkout_address_input")
                                )
                                if (addressError != null) {
                                    Text(
                                        text = addressError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp).testTag("checkout_address_error")
                                    )
                                }
                            }

                            // --- Pincode Form Input ---
                            Column {
                                OutlinedTextField(
                                    value = pincode,
                                    onValueChange = { viewModel.updateCheckoutPincode(it) },
                                    label = { Text("Pincode (6 digits)") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null) },
                                    shape = RoundedCornerShape(12.dp),
                                    isError = pincodeError != null,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checkout_pincode_input")
                                )
                                if (pincodeError != null) {
                                    Text(
                                        text = pincodeError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 12.dp, top = 4.dp).testTag("checkout_pincode_error")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Pay & Place Order action row at bottom
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 1100.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (!isOrdering) {
                                viewModel.placeOrder(onSuccess = { createdOrderId ->
                                    onOrderPlacedSuccessfully(createdOrderId)
                                })
                            }
                        },
                        enabled = !isOrdering,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("place_order_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isOrdering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(text = "Processing...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
                                Text(text = "Place Order • ₹${totalPrice.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
