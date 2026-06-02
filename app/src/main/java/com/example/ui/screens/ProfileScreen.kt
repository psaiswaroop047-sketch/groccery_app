package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.GroceryViewModel
import com.example.data.repository.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val savedFirstName by viewModel.firstName.collectAsState()
    val savedPhoneNumber by viewModel.phoneNumber.collectAsState()
    val firebaseSyncStatus by viewModel.syncStatus.collectAsState()

    var firstNameInput by remember { mutableStateOf(savedFirstName) }
    var phoneNumberInput by remember { mutableStateOf(savedPhoneNumber) }

    // Keep inputs synced if view model changes (just in case)
    LaunchedEffect(savedFirstName, savedPhoneNumber) {
        firstNameInput = savedFirstName
        phoneNumberInput = savedPhoneNumber
    }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }

    fun handleUpdate() {
        var isValid = true

        val trimmedName = firstNameInput.trim()
        if (trimmedName.isEmpty()) {
            firstNameError = "First Name cannot be empty"
            isValid = false
        } else if (trimmedName.length < 2) {
            firstNameError = "First Name must be at least 2 characters"
            isValid = false
        } else {
            firstNameError = null
        }

        val trimmedPhone = phoneNumberInput.trim()
        if (trimmedPhone.isEmpty()) {
            phoneNumberError = "Phone Number cannot be empty"
            isValid = false
        } else if (trimmedPhone.length != 10 || !trimmedPhone.all { it.isDigit() }) {
            phoneNumberError = "Phone Number must be exactly 10 digits"
            isValid = false
        } else {
            phoneNumberError = null
        }

        if (isValid) {
            viewModel.updateProfile(trimmedName, trimmedPhone)
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 550.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Profile Icon Header
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Form container Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Personal Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // First Name Input
                    Column {
                        OutlinedTextField(
                            value = firstNameInput,
                            onValueChange = {
                                firstNameInput = it
                                if (firstNameError != null) firstNameError = null
                            },
                            label = { Text("First Name") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            isError = firstNameError != null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input")
                        )
                        if (firstNameError != null) {
                            Text(
                                text = firstNameError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .padding(start = 12.dp, top = 4.dp)
                                    .testTag("profile_name_error")
                            )
                        }
                    }

                    // Phone Number Input
                    Column {
                        OutlinedTextField(
                            value = phoneNumberInput,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }.take(10)
                                phoneNumberInput = digits
                                if (phoneNumberError != null) phoneNumberError = null
                            },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            isError = phoneNumberError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_mobile_input")
                        )
                        if (phoneNumberError != null) {
                            Text(
                                text = phoneNumberError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .padding(start = 12.dp, top = 4.dp)
                                    .testTag("profile_mobile_error")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Save Button
                    Button(
                        onClick = { handleUpdate() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_save_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(text = "Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Firebase Cloud Synchronization Sync Status Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (firebaseSyncStatus) {
                        is SyncStatus.Success -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        is SyncStatus.Syncing -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        is SyncStatus.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        is SyncStatus.OfflineFallback -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().testTag("firebase_sync_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🔥 Firebase Cloud Integration",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (firebaseSyncStatus) {
                            is SyncStatus.Success -> MaterialTheme.colorScheme.primary
                            is SyncStatus.Syncing -> MaterialTheme.colorScheme.tertiary
                            is SyncStatus.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    val desc = when (val state = firebaseSyncStatus) {
                        is SyncStatus.Idle -> "Standing by. Log in or save profile changes to sync active collections."
                        is SyncStatus.Configured -> state.message
                        is SyncStatus.Syncing -> state.message
                        is SyncStatus.Success -> state.message
                        is SyncStatus.OfflineFallback -> state.message
                        is SyncStatus.Error -> state.message
                    }

                    Text(
                        text = desc,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "To configure your own secure live Firebase database node:\n" +
                               "1. Register an application in the Firebase Console.\n" +
                               "2. Copy your 'google-services.json' file into `/app/`.\n" +
                               "3. Enable Firestore & Anonymous Auth.\n" +
                               "Offline Local Room DB handles transactions seamlessly when unlinked.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Logout card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sign Out",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "This will clear your local checkout profile information and return you to the login screen.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("logout_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(text = "Logout", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}
