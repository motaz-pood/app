package com.example.ui

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AiGeneratorScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuestionsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisaalApp() {
    val context = LocalContext.current
    val viewModel: WisaalViewModel = viewModel(
        factory = WisaalViewModelFactory(context.applicationContext as Application)
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = {
                        if (currentRoute != "home") {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("الرئيسية") },
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // Questions Tab
                NavigationBarItem(
                    selected = currentRoute == "questions",
                    onClick = {
                        if (currentRoute != "questions") {
                            navController.navigate("questions") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(imageVector = Icons.Filled.Forum, contentDescription = "Questions") },
                    label = { Text("أسئلة") },
                    modifier = Modifier.testTag("nav_tab_questions")
                )

                // Games Tab
                NavigationBarItem(
                    selected = currentRoute == "games",
                    onClick = {
                        if (currentRoute != "games") {
                            navController.navigate("games") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(imageVector = Icons.Filled.SportsEsports, contentDescription = "Games") },
                    label = { Text("ألعاب") },
                    modifier = Modifier.testTag("nav_tab_games")
                )

                // AI Spark Tab
                NavigationBarItem(
                    selected = currentRoute == "ai_spark",
                    onClick = {
                        if (currentRoute != "ai_spark") {
                            navController.navigate("ai_spark") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = "AI Spark") },
                    label = { Text("ذكاء اصطناعي") },
                    modifier = Modifier.testTag("nav_tab_ai_spark")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(viewModel = viewModel)
            }
            composable("questions") {
                QuestionsScreen(viewModel = viewModel)
            }
            composable("games") {
                GamesScreen(viewModel = viewModel)
            }
            composable("ai_spark") {
                AiGeneratorScreen(viewModel = viewModel)
            }
        }
    }
}
