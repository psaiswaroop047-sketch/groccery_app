# Grocery Shopping Application - Architecture & Core Documentation

Welcome to the comprehensive technical and developer manual for the **Grocery Shopping** Android application. This is a production-grade, offline-first commerce application engineered using cutting-edge Android development practices: **Kotlin**, **Jetpack Compose (Material 3)**, **Jetpack Navigation**, **MVVM Architecture**, and a local state persistence engine powered by **Room Database (SQLite)** and **SharedPreferences**.

This document is designed to guide a developer of any level—from an absolute beginner with zero prior experience to a senior Android architect—through the overall design, modules, source code structure, workflows, testing suites, local database operations, deployment procedures, and troubleshooting guidelines of this application.

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Business & User Requirements](#2-business--user-requirements)
3. [Technology Stack](#3-technology-stack)
4. [System Architecture](#4-system-architecture)
5. [Project Directory & File Structure](#5-project-directory--file-structure)
6. [Database Design (SQLite Schema & Room ORM)](#6-database-design-sqlite-schema--room-orm)
7. [Detailed Code Walkthrough & Module Walkthrough](#7-detailed-code-walkthrough--module-walkthrough)
   - [Data & Storage Tier (Local Entities & DAOs)](#data--storage-tier-local-entities--daos)
   - [Business Logic & ViewModel Tier (State Machine Engine)](#business-logic--viewmodel-tier-state-machine-engine)
   - [UI Tier (Jetpack Compose Screen Components)](#ui-tier-jetpack-compose-screen-components)
8. [Data Flow, Workflows & Sequence Diagrams](#8-data-flow-workflows--sequence-diagrams)
9. [Installation & Setup Manual](#9-installation--setup-manual)
10. [Configuration Guide](#10-configuration-guide)
11. [Testing & Verification Strategy](#11-testing--verification-strategy)
12. [Troubleshooting & Debugging Guide](#12-troubleshooting--debugging-guide)
13. [Feature Modification & Development Playbook](#13-feature-modification--development-playbook)
14. [Deployment & Release Guide](#14-deployment--release-guide)
15. [Frequently Asked Questions (FAQ)](#15-frequently-asked-questions-faq)
16. [Maintenance & Software Lifecycle Support](#16-maintenance--software-lifecycle-support)

---

## 1. Project Overview

The **Grocery Shopping** app represents a highly polished, responsive shopfront where users can log in instantly with their phone numbers, search and filter organic farm-fresh groceries, select weight units, modify product carts dynamically, and checkout products through a highly secured local transaction engine.

### Key Capabilities
- **🔒 Instant Verification Profile**: Prompts standard login credentials upon startup, verifying the first name and mobile numbers using standard regular expressions.
- **🔍 Active Combined Index Filtering**: Allows typing partial text with real-time combinations of category selectors (e.g. Fruits, Veggies, Dairy, Bakery) to immediately subset products.
- **🗃️ Triple Engine Hybrid Persistence System**: 
  - **SharedPreferences**: Stores offline session locks, personal profile attributes, and login scopes locally.
  - **Room SQLite Relational Database**: Locally persists active Cart items, calculates subtotals matching inventory bounds safely, and logs permanent Order / OrderDetail tables.
  - **Firebase Cloud Firestore Backup**: Transmit profiles, shopping cart state, and order historical ledgers to Firebase Cloud Firestore databases in real-time when online, falling back safely to offline modes on network drops.
- **🛡️ State-Hardened Secure Checkout**: Pulls user credentials from the master profile as readonly parameters to accelerate form submission. Features defensive state caching to prevent empty value flashes during rapid navigation, and double-submission protection during order placement.
- **🌟 Interactive Performance Analytics**: Tracks historic receipts, counts totals, records items, and processes real-time quantity modifiers while checking physical stock bounds.

---

## 2. Business & User Requirements

Below are the business workflows translating direct user experience into physical features:

| User Story | Component | Expected Outcome | Visual Style |
| :--- | :--- | :--- | :--- |
| **New User Signup** | `LoginScreen` | Accepts name (Min 2 chars) and phone number (Must match 10 digits). Locks credentials. | Full primary colors gradient with sleek elevated forms containing custom vector leading icons. |
| **Welcome Greet** | `HomeScreen` | Reads SharedPrefs to parse profile and displays name with standard waving hand emoji. | Generous negative space, large headings, bold typography, and dynamic product cards. |
| **Category Selection**| `HomeScreen` | Refines product listing according to chip selection: "All", "Fruits", "Vegetables", etc. | Scrollable horizontal pill row with contrasting active selection indicator colors. |
| **Keyword Search** | `HomeScreen` | Live filtering on typing without requiring an explicit submit action. | Clean fill-styled top search-bar with responsive search-cancel triggers. |
| **Item Review** | `ProductDetailsScreen` | Detailed inspection of items, images, packaging weight unit, and current warehouse stock. | Large Coil image container, premium card grouping, stock indicators, and primary additive FAB controls. |
| **Adjust Quantity** | `CartScreen` | Inline `+`/`-` buttons with direct check on available inventory limits. | Custom list layout with clear visual dividers and a bottom total calculation sheet. |
| **Read-Only Checkout** | `CheckoutScreen` | Autofills matching profile records. Validates delivery address length and standard postal codes. | Professional step form layout. Read-Only profile panels containing secondary locked border styles. |
| **Visual Invoicing** | `OrderSuccessScreen` | Displays a generated unique ID (e.g., "ORD84920") and provides action maps. | Aesthetic green checklist emblem, bold order summary text, paired primary and secondary buttons. |
| **Historical Logs** | `OrderHistoryScreen` | Stores detailed complete orders, dynamic item counters, invoice totals, and expanding product tables. | Multi-card ledger display with custom expandable headers toggling individual product subsets. |

---

## 3. Technology Stack

The application relies on modern native Android tooling to reduce performance overhead and enable offline execution:

- **Kotlin 1.9+**: Strong typing, type-safe navigation, and memory-safe coroutine features.
- **Jetpack Compose**: Native declarative UI framework that eliminates heavy XML rendering configurations.
- **Material Design 3 (M3)**: Centralized theming (`Theme.kt`), unified material colors, dynamic typography scale, tactile ripple interactions, and modern Material icon sets.
- **Room SQLite ORM**: Local relational data persistence featuring transactional safety, automated entity-to-table schemas, and reactive Flow tracking queries.
- **Kotlin Coroutines & Flow**: High-performance asynchronous non-blocking thread scheduling and state management models.
- **Jetpack Navigation (Compose-Navigation)**: Type-safe route parameters passing matching serialized and structural arguments across screens.
- **Coil (Coroutine Image Loader)**: Fast, memory-cached, and asynchronous loading of grocery assets and remote thumbnails.
- **Moshi & Retrofit (Available for Expansion)**: Fully configured networking and JSON codegen engines, prepared to convert mock engines into remote server setups instantly.
- **Robolectric & Roborazzi**: Complete JVM testing workspace enabling visual regression captures (Screenshot testing) and view model validation without slowing down with thick emulator instrumentation.

---

## 4. System Architecture

The application implements a robust **MVVM (Model-View-ViewModel)** architectural pattern. Communication strictly operates unidirectionally, safeguarding state changes and ensuring modularity.

```
+----------------------------------------------------------------------------------------+
|                                     UI LAYER (View)                                    |
|   - Jetpack Compose Screen Components (HomeScreen, CartScreen, CheckoutScreen, etc.)   |
|   - Observes Jetpack UI States via Reactive flows (.collectAsStateWithLifecycle())     |
|   - Relies purely on State Injections from the ViewModel (Dynamic UI state bindings)   |
+------------------------------------+---------------------------------------------------+
                                     | Passes user interactions (Events)
                                     v
+------------------------------------+---------------------------------------------------+
|                            VIEWMODEL LAYER (GroceryViewModel)                         |
|   - Manages UI states through MutableStateFlow and combines queries on sub-scopes.      |
|   - Coordinates inputs, state-hardening variables, and async operations.               |
|   - Launches coroutines in viewModelScope bound tightly to the lifecycle.              |
+-------------------+-----------------+-----------------------------------+--------------+
                    |                 |                                   |
                    | Calls repository| Triggers Background Cloud Sync    | SharedPrefs Session
                    v                 v                                   v
+-------------------+---+      +------+--------------------------+      +-+--------------+
|     REPOSITORY        |      |       FIREBASE SYNC MANAGER      |      | SHARED PREFS   |
| (GroceryRepository)   |      |  - FirebaseSyncManager.kt        |      | - Offline Keys |
| - Decouples views     |   ┌--►  Authenticates users & syncs     |      | - Sessions     |
| - Handles Room writes |   │  │  carts/orders synchronously.     |      +----------------+
+---------+-------------+   │  +------------------+---------------+
          |                 │                     |
          v (Relational DB) │                     v (Secure HTTPS to Web SDK)
+---------+-------------+   │  +------------------+---------------+
|     ROOM DATABASE     |   │  |    FIREBASE CLOUD FIRESTORE      |
| - AppDatabase (SQLite)|   │  | - User and order historical ledgers |
| - CartDao / OrderDao  |   │  | - Secure real-time cloud database   |
+-----------------------+   │  +----------------------------------+
                            │
+---------------------------+------------------+
|           MOCK/IN-MEMORY INVENTORY API       |
| - MockProducts.kt (Catalog definitions)      |
+----------------------------------------------+
```

---

## 5. Project Directory & File Structure

Below is the directory map illustrating how files are organized logically by function:

```
/ (Root Workspace)
│
├── metadata.json                       # Android build configuration properties of AI Studio (Platform Sync)
├── build.gradle.kts                    # Root-level Gradle Build configuration
├── settings.gradle.kts                 # Master dependency repository inclusions
├── gradle.properties                   # General build variables and optimization flags
│
├── app/                                # Primary Android module container
│   ├── build.gradle.kts                # App-level SDK compile versions, signing credentials, dependencies
│   ├── google-services.json            # Firebase client config & API credentials
│   ├── proguard-rules.pro              # Code shrinking configuration for deployment
│   └── src/
│       ├── test/                       # Unified local unit and screen verification workspace
│       │   └── java/com/example/       # Local JUnit tests, Robolectric engines, and Roborazzi visual checks
│       │
│       └── main/                       # Main application sources
│           ├── AndroidManifest.xml     # Master Application configuration, permissions, and initial activity
│           │
│           ├── res/                    # Raw XML layout properties, static drawables, values
│           │   ├── values/
│           │   │   ├── strings.xml     # String values (Application Name, UI parameters)
│           │   │   └── colors.xml      # Color value palettes
│           │   └── mipmap-anydpi-v26/  # Adaptive launcher app icons
│           │
│           └── java/com/example/       # Primary package container
│               ├── MainActivity.kt     # Core Gateway, Edge-to-Edge window control, and NavHost Router
│               │
│               ├── data/               # Unified Local State & Network Engine
│               │   ├── MockProducts.kt # Hardcoded catalogue (Product prices, packaging units, and description sets)
│               │   │
│               │   ├── db/             # SQLite Data Storage Interfaces (Room Core)
│               │   │   ├── AppDatabase.kt   # Room Database class (Singleton generator & structural context)
│               │   │   ├── CartDao.kt       # Interface describing queries for managing the user's cart
│               │   │   └── OrderDao.kt      # Interface modeling queries for recording finalized purchases
│               │   │
│               │   ├── model/          # Strongly-typed relational data records
│               │   │   ├── CartItem.kt      # Stores active items, subtotals, and warehouse limit maps
│               │   │   ├── Order.kt         # Stores master metadata for placed orders
│               │   │   ├── OrderDetail.kt   # Relational sub-table storing item copies placed per order ID
│               │   │   └── Product.kt       # Base catalog blueprints
│               │   │
│               │   └── repository/     # Domain data access points
│               │       ├── FirebaseSyncManager.kt # Synchronizes profiles and transactional data to Firebase FireStore
│               │       └── GroceryRepository.kt # decodes raw Room records into domain states
│               │
│               └── ui/                 # Visual Layout Tier
│                   ├── theme/          # Material Design 3 palettes
│                   │   ├── Color.kt    # Base color codes
│                   │   ├── Theme.kt    # Dynamic Light / Dark ColorScheme builders
│                   │   └── Type.kt     # Text styles, sizing scales, and custom tracking
│                   │
│                   ├── viewmodel/      # Core State Coordinator
│                   │   └── GroceryViewModel.kt # Exposes reactive flows and handles calculations and form validation
│                   │
│                   └── screens/        # Modular Jetpack Compose Declarative Interfaces
│                       ├── SplashScreen.kt       # Splash landing page with responsive entry animation
│                       ├── LoginScreen.kt        # Name and phone validation lock
│                       ├── HomeScreen.kt         # Product lists categorized and searchable
│                       ├── ProductDetailsScreen.kt# Catalog detail viewer showing unit weight and stock
│                       ├── CartScreen.kt         # Master shopping list with reactive qty counters
│                       ├── CheckoutScreen.kt     # Validates postal code and shipping details
│                       ├── OrderSuccessScreen.kt # Displays the generated order ID and handles receipt actions
│                       ├── OrderHistoryScreen.kt # History log with expandable rows showing purchased item summaries
│                       ├── ProfileScreen.kt      # Personal profile manager containing signout safety checks
│                       └── CommonComponents.kt   # Standard UI elements (Dividers, loaders, custom buttons)
```

---

## 6. Database Design (SQLite Schema & Room ORM)

The application maintains data locally utilizing **SQLite tables** modeled cleanly with Kotlin annotations mapping fields into Room entities. Below is the detailed relational structure:

### Entity Relationships Diagram

```
   [CartItem Table]                         [Order Table]
*-------------------*                     *------------------*
|  productId (PK)   |                     |   orderId (PK)   |
|  name             |                     |   timestamp      |
|  price            |                     |   totalAmount    |
|  quantity         |                     |   totalItems     |
|  category         |                     |   totalQuantity  |
|  imageUrl         |                     |   status         |
|  stock------------+--Check bounds       |   customerName   |
|  unit             |                     |   mobileNumber   |
*-------------------*                     |   address        |
                                          |   pincode        |
                                          *--------+---------*
                                                   |
                                                   | 1
                                                   |
                                                   | N
                                          *--------v---------*
                                          |  OrderDetail     |
                                          |------------------|
                                          |  detailId (PK)   |
                                          |  orderId (FK)----+---> Relation Mapping
                                          |  productId       |
                                          |  productName     |
                                          |  price           |
                                          |  quantity        |
                                          |  imageUrl        |
                                          |  unit            |
                                          *------------------*
```

### Table Definitions

#### 1. CartItem Table (`cart_items`)
This table holds the user's active shopping sessions. If the app is killed or restarted, the cart retains its state, ensuring no data loss.
- `productId` (Text, Primary Key): Links to catalog items.
- `name` (Text): Name of product.
- `price` (Real): Cost of a single unit.
- `quantity` (Integer): Selected item count.
- `category` (Text): Product category.
- `imageUrl` (Text): Coil asset location.
- `stock` (Integer): Current inventory boundary check limit.
- `unit` (Text): Sizing indicator (e.g. "500g", "1 kg").

#### 2. Order Table (`orders`)
Records master info of success checkpoints.
- `orderId` (Text, Primary Key): Unique purchase token prefix (e.g., "ORD12345").
- `timestamp` (Integer): Date of order placement in UNIX epochs.
- `totalAmount` (Real): Order subtotal summing all products.
- `totalItems` (Integer): Count of unique items included in the order.
- `totalQuantity` (Integer): Accumulator of physical packing items.
- `status` (Text): State of package processing (e.g. "Pending", "Preparing").
- `customerName` (Text): Form user checkout parameter.
- `mobileNumber` (Text): Form user mobile identifier.
- `address` (Text): Delivery destination.
- `pincode` (Text): 6-digit postal code mapping.

#### 3. OrderDetail Table (`order_details`)
Stores dynamic copies of products checked out per invoice, protecting records from downstream stock updates.
- `detailId` (Integer, Primary Key & Auto-Increment): Relational row identifier.
- `orderId` (Text, Foreign Key): Binds transaction record into `/orders/` records.
- `productId` (Text)
- `productName` (Text)
- `price` (Real)
- `quantity` (Integer)
- `imageUrl` (Text)
- `unit` (Text)

---

## 7. Detailed Code Walkthrough & Module Walkthrough

### Data & Storage Tier (Local Entities & DAOs)

#### Master Setup (`AppDatabase.kt`)
Initializes standard instances while ensuring that double-instantiation does not cause memory leaks during async multi-threaded writes:
```kotlin
@Database(entities = [CartItem::class, Order::class, OrderDetail::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grocery_database"
                )
                .fallbackToDestructiveMigration() // Drops existing schema if developers update version without legacy migration code
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

#### Shopping Queries (`CartDao.kt`)
Uses standard SQLite constructs:
```kotlin
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>> // Real-time active subscription flow triggers UI recompositions on table modifications

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItem(productId: String): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteByProductId(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
```

---

### Business Logic & ViewModel Tier (State Machine Engine)

#### Combined Live Search Integrator (`GroceryViewModel.kt`)
The application implements highly efficient active search filtering. By combining search queries and selected categories in real time using Coroutines `combine()`, product lists are subset instantly as the user types:

```kotlin
private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

private val _selectedCategory = MutableStateFlow("All")
val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

private val _filteredProducts = MutableStateFlow<List<Product>>(repository.allProducts)
val filteredProducts: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

init {
    // Jetpack Coroutine Flow integration actively listens on typing and category chips
    combine(searchQuery, selectedCategory) { query, category ->
        var items = repository.allProducts
        if (category != "All") {
            items = items.filter { it.category.equals(category, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            items = items.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) 
            }
        }
        items
    }.onEach {
        _filteredProducts.value = it // Direct assignment feeds the Home listing Composable
    }.launchIn(viewModelScope)
}
```

#### Order Lifecycle Processing
Places orders within safe local asynchronous routines, resetting active cart elements only when transactional validation passes:
```kotlin
fun placeOrder(onSuccess: (String) -> Unit) {
    if (!validateCheckoutForm()) return // Ensure validation rules are strictly respected before placing database insertions

    val items = cartItems.value
    if (items.isEmpty()) return

    viewModelScope.launch {
        val orderId = "ORD${(10000..99999).random()}"
        val totalAmt = items.sumOf { it.subtotal }
        val totalQuantities = items.sumOf { it.quantity }

        val order = Order(
            orderId = orderId,
            timestamp = System.currentTimeMillis(),
            totalAmount = totalAmt,
            totalItems = items.size,
            totalQuantity = totalQuantities,
            status = "Pending",
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

        // Writes order parameters, dumps relational receipts, cleans the original shopping cart
        repository.placeOrder(order, orderDetails)
        _lastPlacedOrderId.value = orderId

        // Retain the authenticated first name and mobile records during field reset
        _checkoutName.value = _firstName.value
        _checkoutMobile.value = _phoneNumber.value
        _checkoutAddress.value = ""
        _checkoutPincode.value = ""

        onSuccess(orderId)
    }
}
```

---

### UI Tier (Jetpack Compose Screen Components)

#### Edge-to-Edge Navigation Engine (`MainActivity.kt`)
The navigation file sets up type-safe routes, handles popstack resets to optimize the memory footprint of back entries, and parses deep path elements as typed parameters:

```kotlin
@Composable
fun GroceryAppNavigation(
    viewModel: GroceryViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    // Navigate to Home if user credentials are valid; otherwise redirect to Login
                    val destination = if (viewModel.isLoggedIn.value) "home" else "login"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToProduct = { productId -> navController.navigate("product_details/$productId") },
                onNavigateToCart = { navController.navigate("cart") },
                onNavigateToHistory = { navController.navigate("order_history") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        
        // Additional Screens: cart, checkout, order_success, order_history, profile
    }
}
```

---

## 8. Data Flow, Workflows & Sequence Diagrams

Here is a visual breakdown of how state information is shared across the application.

### Unified Checkout Flow Diagram

```
[SplashScreen] 
      │
      ├─► (Has valid session keys in SharedPrefs) ─────► [HomeScreen] 🎉
      │                                                       │
      └─► (No session registered) ──► [LoginScreen] ───┐      │ (Browse UI)
                                            ▲          │      v
                                            │          └─► [ProductDetails]
                                     Reset Session            │ (Add item to bag)
                                            │                 v
[LoginScreen] ◄──── (Sign Out) ◄────── [ProfilePage] ◄─── [CartView] 🛒
                                                              │
                                                              ▼
                                                        [CheckoutForm] 
                                                        (Name/Mobile Locked)
                                                              │
                                                              ├─► (Verify fail) ──► Retain and warn user
                                                              │
                                                              └─► (Commit success) 
                                                                       │
                                                                       ▼
                                                             [Database Relational Transaction]
                                                             - Write completed Order record
                                                             - Write precise OrderDetails
                                                             - Flush item rows in Cart
                                                                       │
                                                                       ▼
                                                             [OrderSuccessPage] ⭐
```

---

## 9. Installation & Setup Manual

To build, install, and run this application locally, follow these steps:

### System Requirements
1. **Operating System**: Windows 11/10 (64-bit), macOS Ventura (13.0) or higher, or Linux (Ubuntu 22.04 LTS or newer).
2. **Java Development Kit**: JDK 17 (Mandatory).
3. **Android Studio**: Android Studio Jellyfish (2023.3.1) or Koala (2024.1.1) or newer.

### Step-by-Step Installation Procedures

1. **Clone the Repository**
   Clone the repository from your Git cloud host to your workspace:
   ```bash
   git clone https://github.com/your-repo/grocery-shopping.git
   cd grocery-shopping
   ```

2. **Verify JDK Version Setup**
   Open your terminal and verify that Java JDK 17 is set as your default compiler:
   ```bash
   java -version
   ```
   *Expected output snippet:* `openjdk version "17.0.x" ...`

3. **Open the Project in Android Studio**
   - Click `File -> Open` or `Import Project`.
   - Select the root folder `/grocery-shopping`.
   - Android Studio will start parsing build scripts. Let the background indexing complete.

4. **Synchronize Gradle Dependencies**
   - Click the **Sync Project with Gradle Files** elephant icon in the top right, or run in the built-in terminal:
     ```bash
     gradle build --dry-run
     ```

5. **Generate a Debug Keystore (If Missing)**
   The project is preconfigured to use `debug.keystore`. If it is missing from the root project folder, you can regenerate it locally:
   ```bash
   keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
   ```

6. **Create an Android Virtual Device (AVD)**
   - Click `Tools -> Device Manager` in Android Studio.
   - Click **Create Device**. Select a device frame (e.g. Pixel 8 pro).
   - Choose a target system image (API Level 34 or Level 35) and download it.
   - Start the Emulator.

7. **Compile and Launch the App**
   - Select `app` from the run configuration dropdown.
   - Select your active virtual emulator from the device menu.
   - Click the green **Run (Play)** button, or execute:
     ```bash
     gradle assembleDebug
     ```

---

## 10. Configuration Guide

Dependencies are managed using modern **Gradle Version Catalogs** located at `/gradle/libs.versions.toml`.

### App SDK Target Configuration (`/app/build.gradle.kts`)
```kotlin
android {
  namespace = "com.example"
  compileSdk { version = release(36) } // Compiles under Android 16 (API 36)

  defaultConfig {
    applicationId = "com.example"
    minSdk = 24  // Supports users running Android 7.0 and up
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }
}
```

### Environment Secrets Configuration
The **Secrets Gradle Plugin** is fully integrated, enabling developers to inject external API credentials (e.g. payment SDK tokens or database URLs) securely.
- **`.env.example`** serves as a template with mock keys.
- **`.env`** contains active production keys (Local run configurations read this file automatically. Ensure `.env` is added to your `.gitignore` to prevent committing sensitive keys to repository history).

### Firebase Cloud Credentials (`google-services.json`)
The application is preconfigured with Firebase Services. To modify or sync with another Firebase Firestore backend:
1. Place your own custom `google-services.json` file inside the `/app` directory.
2. Ensure you register the package identifier matching the `applicationId` (e.g. `com.aistudio.groceryshopping.nkmqas`).
3. Enable **Anonymous Authentication** and configure **Cloud Firestore** inside your Firebase console with appropriate security rules allowing write scopes for users path.

---

## 11. Testing & Verification Strategy

The validation pipeline utilizes **Robolectric** to run unit and visual UI tests directly on development machines, reducing target simulation overhead.

### Running Tests
To run standard unit tests and Robolectric mock components, execute the following Gradle task:
```bash
gradle :app:testDebugUnitTest
```

### Executing visual UI captures (Roborazzi)
To verify screenshot regressions and compare screen pixels:
- Record new reference screenshots:
  ```bash
  gradle :app:recordRoborazziDebug
  ```
- Compare active states with stored reference screenshots:
  ```bash
  gradle :app:verifyRoborazziDebug
  ```

---

## 12. Troubleshooting & Debugging Guide

Below are common exceptions encountered during Android development and instructions for resolving them.

### Problem 1: SQLite Schema Mismatches
- **Cause**: Modifying entity properties (such as fields in `CartItem.kt` or `Order.kt`) without incrementing the Room database version in `AppDatabase.kt`.
- **Error message**: `Room cannot verify the data integrity. IllegalStateException...`
- **Correction**: 
  - Increment `version = 1` to `version = 2` in the `@Database` annotation in `AppDatabase.kt`.
  - To clear existing debug data, uninstall the app on the target device or clear its storage inside the app settings to let `.fallbackToDestructiveMigration()` reset the database cleanly.

### Problem 2: Resource ID Duplicate Inconsistencies
- **Cause**: XML variables in `res/values/strings.xml` or custom drawing vectors in `res/drawable` overlapping with template placeholders.
- **Error message**: `Execution failed for task ':app:mergeDebugResources'. Duplicate resources found...`
- **Correction**: Locate and rename duplicate names to ensure keys are unique (e.g. replace `btn_back` with `checkout_back_btn`).

### Problem 3: Broken Window Input Dispatcher
- **Cause**: Infinite recursion during navigation transitions or multiple parallel route execution events.
- **Error message**: `channel 'com.example/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!`
- **Correction**: Ensure buttons triggering navigation pops handle states safely. For instance, in `SplashScreen.kt`, avoid trigger loops by implementing a local check to prevent duplicate navigations:
  ```kotlin
  val navigateOnce = remember(onNavigateToHome) {
      {
          if (!hasNavigated) {
              hasNavigated = true
              onNavigateToHome()
          }
      }
  }
  ```

### Problem 4: Checkout Screen displays "0" or empty summaries during transitions
- **Cause**: Standard Room flows can transiently emit empty values on database table flushes or category switches, leading to temporary visual flashes of `0` in item count or cost fields before transitioning.
- **Error message**: Checkout button displays `Place Order • ₹0` or empty invoice totals unexpectedly.
- **Correction**: Use a state-hardening architecture where Jetpack Compose caches previous successful state frames via `remember` and updates them in a `LaunchedEffect` only when items are non-empty:
  ```kotlin
  var cachedCartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
  var cachedTotalPrice by remember { mutableStateOf(0.0) }
  LaunchedEffect(cartItemsState) {
      if (cartItemsState.isNotEmpty()) cachedCartItems = cartItemsState
  }
  LaunchedEffect(totalPriceState) {
      if (totalPriceState > 0.0) cachedTotalPrice = totalPriceState
  }
  val cartItems = if (cartItemsState.isNotEmpty()) cartItemsState else cachedCartItems
  val totalPrice = if (totalPriceState > 0.0) totalPriceState else cachedTotalPrice
  ```

### Problem 5: Double Checkout / Multi-threading order creation payload
- **Cause**: Rapidly double-clicking the checkout trigger button executes multiple coroutine operations concurrently, spawning duplicate order documents and syncing multiple frames.
- **Error message**: Multiple duplicate order receipts logged simultaneously in SQLite or Firestore tables under different unique IDs.
- **Correction**: Implement dynamic thread-safe `isOrdering` check blocks inside `GroceryViewModel` and use it to disable the button as soon as order generation starts, showing clear feedback to the user:
  ```kotlin
  Button(
      onClick = {
          if (!isOrdering) {
              viewModel.placeOrder { orderId -> onOrderPlacedSuccessfully(orderId) }
          }
      },
      enabled = !isOrdering
  ) {
      if (isOrdering) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp))
          Text("Processing...")
      } else {
          Text("Place Order")
      }
  }
  ```

---

## 13. Feature Modification & Development Playbook

Follow these guides to extend and customize the application safely:

### Task A: Updating Theme Color Accents
Want to transition the theme palette to a brand color?
1. Open `/app/src/main/java/com/example/ui/theme/Color.kt`.
2. Redefine primary palette constants:
   ```kotlin
   val Purple80 = Color(0xFFD0BCFF)
   val PurpleGrey80 = Color(0xFFCCC2DC)
   ```
3. Open `/app/src/main/java/com/example/ui/theme/Theme.kt` and adjust color scheme mappings in `LightColorScheme` and `DarkColorScheme` as desired.

### Task B: Injecting a New Shop Category
To add a new product category matching business expansions (e.g. "Pet Foods" or "Organic Beverages"):
1. Open `/app/src/main/java/com/example/data/MockProducts.kt`.
2. Append a new product using your new category designation:
   ```kotlin
   Product(
       id = "p13",
       name = "Organic Green Tea",
       category = "Beverages",
       price = 4.99,
       stock = 45,
       unit = "20 Bags",
       imageUrl = "https://images.unsplash.com/photo-1597481499750-3e6b22637e12?w=500",
       description = "Pure ceremonial matcha green tea leaves."
   )
   ```
3. Open `/app/src/main/java/com/example/ui/screens/HomeScreen.kt`.
4. Locate the categories list:
   ```kotlin
   val categories = listOf("All", "Fruits", "Vegetables", "Dairy", "Bakery", "Grocery", "Beverages")
   ```
5. Run `gradle assembleDebug` to verify. The Home screen will automatically add the new "Beverages" category chip, fully wired to the search and category flow.

---

## 14. Deployment & Release Guide

To create a production-ready package to publish to the Google Play Store or distribute as an APK:

### 1. Build a Release APK / AAB
Locate your project menu inside Android Studio and click:
`Build -> Build Bundle(s) / APK(s) -> Build APK(s)`
Alternatively, run the release bundler task in the terminal:
```bash
gradle :app:bundleRelease
```
This compilation saves your signed Android App Bundle inside the following folder:
`/app/build/outputs/bundle/release/app-release.aab`

### 2. Proguard Minimization optimization
To protect source code files from reverse-engineering and reduce package size, verify that `isMinifyEnabled = true` is set inside `/app/build.gradle.kts`. This strips unused classes and resources automatically during build optimization.

---

## 15. Frequently Asked Questions (FAQ)

#### Q1: "Where is database?" (How do I inspect local database data?)
The physical Room database file resides inside the sandbox directory of the target Android device at:
`/data/data/com.example/databases/grocery_database`

To view and inspect raw SQLite tables dynamically:
1. Start your emulator or link your live testing device.
2. Open Android Studio and select the **App Inspection** tab (usually visible at the bottom or accessible via `View -> Tool Windows -> App Inspection`).
3. Select your target process process (`com.example`).
4. Click **Database Inspector**. This provides a real-time table viewer where you can run custom SQL queries on `cart_items` or `orders` tables during active app execution.

#### Q2: How can I link the local database to actual remote database web APIs?
To connect the application to external REST APIs, replace the mock operations inside `GroceryRepository.kt` with network calls:
1. Define a Retrofit interface (e.g. `GroceryApiService.kt`) containing standard HTTP mappings (`@GET`, `@POST`).
2. Pass the Retrofit client instance to `GroceryRepository.kt` to load catalog elements, instead of reading static mock items from `MockProducts.kt`.

---

## 16. Maintenance & Software Lifecycle Support

To keep compile environments fast, secure, and clean:
1. **Periodic Gradle Housekeeping**: Run a deep cache cleaner periodically to clear build caches:
   ```bash
   gradle clean
   ```
2. **Library Upgrades**: Run standard configuration reviews with the help of the `libs.versions.toml` file to pull updated security patches for dependencies monthly.
3. **Logcat Monitoring**: Run filter `package:mine tag:GroceryViewModel` to observe lifecycle callbacks, warning exceptions, database transaction states, and user profile session mutations.

---

Enjoy developing and building with the **Grocery Shopping** Android codebase! For further architectural questions, contact your technical lead or review the official Jetpack Compose guidelines at `https://developer.android.com/compose`.
