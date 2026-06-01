package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.db.AppDatabase
import com.example.data.repository.GroceryRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.GroceryViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database, Repo, and Viewmodel via simple constructor inject
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GroceryRepository(database.cartDao(), database.orderDao())
        val viewModel: GroceryViewModel by viewModels { GroceryViewModelFactory(repository, applicationContext) }

        setContent {
            MyApplicationTheme {
                GroceryAppNavigation(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

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
        // --- 1. Splash Screen ---
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    val destination = if (viewModel.isLoggedIn.value) "home" else "login"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // --- Login Screen ---
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

        // --- 2. Home Screen (Product Listing) ---
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToProduct = { productId ->
                    navController.navigate("product_details/$productId")
                },
                onNavigateToCart = {
                    navController.navigate("cart")
                },
                onNavigateToHistory = {
                    navController.navigate("order_history")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }

        // --- Profile Screen ---
        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // --- 3. Product Details Screen ---
        composable(
            route = "product_details/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(
                productId = productId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- 4. Cart Screen ---
        composable("cart") {
            CartScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCheckout = {
                    navController.navigate("checkout")
                }
            )
        }

        // --- 5. Checkout Screen ---
        composable("checkout") {
            CheckoutScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderPlacedSuccessfully = { orderId ->
                    navController.navigate("order_success/$orderId") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        // --- 6. Order Success Screen ---
        composable(
            route = "order_success/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderSuccessScreen(
                orderId = orderId,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate("order_history") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        // --- 7. Order History Screen ---
        composable("order_history") {
            OrderHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
