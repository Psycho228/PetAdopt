package com.example.petadopt.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petadopt.ui.screens.AccountScreen
import com.example.petadopt.ui.screens.AuthScreen
import com.example.petadopt.ui.screens.OnboardingScreen
import com.example.petadopt.ui.screens.QuestionnaireScreen
import com.example.petadopt.ui.screens.SwipeScreen
import com.example.petadopt.ui.screens.ShelterScreen
import com.example.petadopt.ui.screens.DetailsScreen
import com.example.petadopt.ui.screens.ApplicationScreen
import com.example.petadopt.ui.screens.MatchesScreen
import com.example.petadopt.ui.screens.ApplicationsScreen
import com.example.petadopt.ui.screens.EditProfileScreen
import com.example.petadopt.ui.screens.AdminScreen
import com.example.petadopt.ui.screens.AddEditPetScreen
import com.example.petadopt.viewmodel.NavViewModel
import com.example.petadopt.viewmodel.StartDestination
import com.example.petadopt.viewmodel.SwipeViewModel
import com.example.petadopt.viewmodel.AccountViewModel
import com.example.petadopt.viewmodel.QuestionnaireViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    var isLoggedIn by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Проверяем сессию через StateFlow из AuthRepository
    // Для простоты используем задержку и предполагаем, что сессия уже инициализирована
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        // В реальном приложении здесь будет проверка через AuthRepository.currentUser
        // Пока просто переходим на auth если нет данных
        isLoading = false
        isLoggedIn = false // По умолчанию не авторизован
    }

    val startDestination = if (isLoading) "loading" else if (isLoggedIn) "swipe" else "auth"

    NavHost(navController, startDestination = startDestination) {

        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate("loading") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("loading") { backStackEntry ->
            val navViewModel: NavViewModel = hiltViewModel(backStackEntry)
            val destination by navViewModel.startDestination.collectAsState()

            LaunchedEffect(Unit) { navViewModel.checkQuestionnaire() }

            when (destination) {
                StartDestination.LOADING -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                StartDestination.QUESTIONNAIRE -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("questionnaire") {
                            popUpTo("loading") { inclusive = true }
                        }
                    }
                }
                StartDestination.SWIPE -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("swipe") {
                            popUpTo("loading") { inclusive = true }
                        }
                    }
                }
                StartDestination.SHELTER -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("shelter") {
                            popUpTo("loading") { inclusive = true }
                        }
                    }
                }
            }
        }

        composable("onboarding") {
            OnboardingScreen(
                onStart = { navController.navigate("questionnaire") },
                onAccount = { navController.navigate("account") }
            )
        }

        composable("questionnaire") {
            QuestionnaireScreen(
                onFinish = { withRiskAssessment ->
                    // Всегда переходим на swipe после завершения опросника
                    // (с оценкой рисков или без - это обрабатывается внутри QuestionnaireScreen)
                    navController.navigate("swipe") {
                        popUpTo("questionnaire") { inclusive = true }
                    }
                }
            )
        }

        composable("swipe") {
            SwipeScreen(
                onDetails = { petId -> navController.navigate("details/$petId") },
                onMatches = { navController.navigate("matches") },
                onAccount = { navController.navigate("account") },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("details/{petId}") { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            DetailsScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onAccount = { navController.navigate("account") },
                petId = petId
            )
        }

        composable("application/{petId}/{petName}") { backStackEntry ->
            ApplicationScreen(
                onSuccess = { navController.navigate("swipe") { popUpTo("swipe") { inclusive = false } } },
                onAccount = { navController.navigate("account") },
                onApplications = { navController.navigate("applications") }
            )
        }

        composable("matches") {
            MatchesScreen(
                onPetClick = { petId -> navController.navigate("details/$petId") },
                onBack = { navController.popBackStack() },
                onAccount = { navController.navigate("account") }
            )
        }

        composable("account") {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onApplications = { navController.navigate("applications") },
                onEditProfile = { navController.navigate("edit_profile") },
                onRetakeQuestionnaire = { navController.navigate("questionnaire") },
                onAdminPanel = { navController.navigate("admin") }
            )
        }

        composable("edit_profile") { backStackEntry ->
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("applications") { backStackEntry ->
            ApplicationsScreen(
                onBack = { navController.popBackStack() },
                onPetClick = { petId ->
                    navController.navigate("details_from_application/$petId")
                }
            )
        }

        composable("details_from_application/{petId}") {
            val backStackEntry = it
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            DetailsScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onAccount = { navController.navigate("account") },
                petId = petId
            )
        }

        // Админ-панель (перенаправляет в shelter screen)
        composable("admin") {
            navController.navigate("shelter") {
                popUpTo("shelter") { inclusive = false }
            }
        }

        composable("shelter") {
            ShelterScreen(navController = navController)
        }

        composable("admin/addPet") {
            AddEditPetScreen(
                navController = navController
            )
        }

        composable("admin/editPet/{petId}") { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            AddEditPetScreen(
                navController = navController,
                petId = petId
            )
        }
    }
}