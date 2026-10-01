package com.example.expencetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expencetracker.ui.AddExpenseScreen
import com.example.expencetracker.ui.AnalyticsScreen
import com.example.expencetracker.ui.ExpenseViewModel
import com.example.expencetracker.ui.ExpenseViewModelFactory
import com.example.expencetracker.ui.FriendDetailScreen
import com.example.expencetracker.ui.FriendsScreen
import com.example.expencetracker.ui.HistoryScreen
import com.example.expencetracker.ui.ThemeMode
import com.example.expencetracker.ui.theme.ExpenceTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as SplitFlowApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ExpenceTrackerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SplitFlowAppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SplitFlowAppNavHost(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier
    ) {
        composable(
            route = "main",
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            MainContainerScreen(
                viewModel = viewModel,
                onNavigateToAddExpense = {
                    viewModel.resetForm()
                    navController.navigate("add_expense")
                },
                onNavigateToFriendDetail = { friendId ->
                    navController.navigate("friend_detail/$friendId")
                }
            )
        }

        composable(
            route = "friend_detail/{friendId}",
            arguments = listOf(navArgument("friendId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getLong("friendId") ?: 0L
            FriendDetailScreen(
                friendId = friendId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpenseForFriend = { fId, fName ->
                    viewModel.resetForm()
                    viewModel.selectSingleFriendForExpense(fId, fName)
                    navController.navigate("add_expense")
                },
                onNavigateToEditExpense = {
                    navController.navigate("add_expense")
                }
            )
        }

        composable(
            route = "add_expense",
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            AddExpenseScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainContainerScreen(
    viewModel: ExpenseViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToFriendDetail: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Expenses") },
                    label = { Text("Expenses", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Friends") },
                    label = { Text("Friends", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToAddExpense = onNavigateToAddExpense
                )
                1 -> FriendsScreen(
                    viewModel = viewModel,
                    onNavigateToFriendDetail = onNavigateToFriendDetail
                )
                2 -> AnalyticsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ThemeToggleButton(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showMenu = true },
        modifier = modifier
    ) {
        val icon = when (themeMode) {
            ThemeMode.LIGHT -> Icons.Default.LightMode
            ThemeMode.DARK -> Icons.Default.DarkMode
            ThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
        }
        Icon(
            imageVector = icon,
            contentDescription = "Change App Theme",
            tint = MaterialTheme.colorScheme.primary
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Light Theme") },
                onClick = {
                    viewModel.setThemeMode(ThemeMode.LIGHT)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Dark Theme") },
                onClick = {
                    viewModel.setThemeMode(ThemeMode.DARK)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("System Theme") },
                onClick = {
                    viewModel.setThemeMode(ThemeMode.SYSTEM)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.SettingsSuggest, contentDescription = null) }
            )
        }
    }
}
