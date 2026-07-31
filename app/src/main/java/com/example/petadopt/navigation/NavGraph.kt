package com.example.petadopt.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.example.petadopt.ui.screens.AddEditPetScreen
import com.example.petadopt.ui.screens.PetApplicationsScreen
import com.example.petadopt.ui.screens.PetApplicationDetailScreen
import com.example.petadopt.ui.screens.ChatScreen
import com.example.petadopt.ui.screens.ApplicationDetailWithChatScreen
import com.example.petadopt.ui.screens.BreederCabinetScreen
import com.example.petadopt.ui.screens.MarketplaceScreen
import com.example.petadopt.ui.screens.SaleListingDetailScreen
import com.example.petadopt.ui.screens.SaleListingFormScreen
import com.example.petadopt.viewmodel.NavViewModel
import com.example.petadopt.viewmodel.StartDestination
import com.example.petadopt.viewmodel.SwipeViewModel
import com.example.petadopt.viewmodel.AccountViewModel
import com.example.petadopt.viewmodel.QuestionnaireViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navViewModel: NavViewModel = hiltViewModel()

    fun openAuth(returnTo: String = "loading", register: Boolean = false) {
        navController.navigate(
            "auth?returnTo=${Uri.encode(returnTo)}&register=$register"
        )
    }

    fun openProtected(returnTo: String) {
        if (navViewModel.isAuthenticated()) {
            navController.navigate(returnTo)
        } else {
            openAuth(returnTo)
        }
    }

    fun openAccount() = openProtected("account_entry")

    NavHost(navController, startDestination = "loading") {

        composable(
            route = "auth?returnTo={returnTo}&register={register}",
            arguments = listOf(
                navArgument("returnTo") {
                    type = NavType.StringType
                    defaultValue = "loading"
                },
                navArgument("register") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val returnTo = backStackEntry.arguments?.getString("returnTo") ?: "loading"
            val register = backStackEntry.arguments?.getBoolean("register") ?: false
            AuthScreen(
                onAuthSuccess = { isNewAccount ->
                    if (isNewAccount) {
                        navController.navigate("questionnaire_after_registration")
                    } else if (returnTo == "back") {
                        navController.popBackStack()
                    } else {
                        navController.navigate(returnTo) {
                            popUpTo(backStackEntry.destination.id) { inclusive = true }
                        }
                    }
                },
                onBreederAuthSuccess = {
                    navController.navigate("breeder_cabinet") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                initialIsRegister = register,
                reason = if (register) {
                    "Создайте аккаунт или войдите, чтобы отправить заявку в приют"
                } else null
            )
        }

        composable("loading") { backStackEntry ->
            val destination by navViewModel.startDestination.collectAsState()

            LaunchedEffect(Unit) { navViewModel.checkQuestionnaire() }

            when (destination) {
                StartDestination.LOADING -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                StartDestination.AUTH -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("auth") {
                            popUpTo("loading") { inclusive = true }
                        }
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
                StartDestination.BREEDER -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("breeder_cabinet") {
                            popUpTo("loading") { inclusive = true }
                        }
                    }
                }
            }
        }

        composable("onboarding") {
            OnboardingScreen(
                onStart = { navController.navigate("questionnaire") },
                onAccount = { openAccount() }
            )
        }

        composable("questionnaire") {
            QuestionnaireScreen(
                onFinish = { withRiskAssessment ->
                    // Р’СЃРµРіРґР° РїРµСЂРµС…РѕРґРёРј РЅР° swipe РїРѕСЃР»Рµ Р·Р°РІРµСЂС€РµРЅРёСЏ РѕРїСЂРѕСЃРЅРёРєР°
                    // (СЃ РѕС†РµРЅРєРѕР№ СЂРёСЃРєРѕРІ РёР»Рё Р±РµР· - СЌС‚Рѕ РѕР±СЂР°Р±Р°С‚С‹РІР°РµС‚СЃСЏ РІРЅСѓС‚СЂРё QuestionnaireScreen)
                    navController.navigate("swipe") {
                        popUpTo("questionnaire") { inclusive = true }
                    }
                }
            )
        }

        composable("swipe") {
            SwipeScreen(
                onDetails = { petId -> navController.navigate("details/$petId") },
                onMatches = { openProtected("matches") },
                onAccount = { openAccount() },
                onMarketplace = { navController.navigate("marketplace") },
                onLogout = {
                    navController.navigate("swipe") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                isAuthenticated = navViewModel.isAuthenticated()
            )
        }

        composable("questionnaire_after_registration") {
            QuestionnaireScreen(
                onFinish = {
                    val authEntry = navController.previousBackStackEntry
                    val returnTo = authEntry?.arguments?.getString("returnTo") ?: "swipe"

                    if (returnTo == "back") {
                        navController.popBackStack()
                        navController.popBackStack()
                    } else {
                        navController.navigate(returnTo) {
                            authEntry?.let { entry ->
                                popUpTo(entry.destination.id) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable("details/{petId}") { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            DetailsScreen(
                onBack = { navController.popBackStack() },
                onAccount = { openAccount() },
                onApply = { pet ->
                    navController.navigate(
                        "application/${pet.id}/${Uri.encode(pet.name)}"
                    )
                },
                petId = petId
            )
        }

        composable("application/{petId}/{petName}") { backStackEntry ->
            ApplicationScreen(
                onSuccess = { navController.navigate("swipe") { popUpTo("swipe") { inclusive = false } } },
                onAccount = { openAccount() },
                onApplications = { navController.navigate("applications") },
                onAuthRequired = { openAuth(returnTo = "back", register = true) }
            )
        }

        composable("matches") {
            MatchesScreen(
                onPetClick = { petId -> navController.navigate("details/$petId") },
                onBack = { navController.popBackStack() },
                onAccount = { openAccount() }
            )
        }

        composable("account_entry") {
            LaunchedEffect(Unit) {
                val route = navViewModel.getAccountRoute()
                navController.navigate(route) {
                    popUpTo("account_entry") { inclusive = true }
                    launchSingleTop = true
                }
            }

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("account") {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onApplications = { navController.navigate("applications") },
                onEditProfile = { navController.navigate("edit_profile") },
                onRetakeQuestionnaire = { navController.navigate("questionnaire") },
                onAdminPanel = { navController.navigate("shelter") },
                onMarketplace = { navController.navigate("marketplace") }
            )
        }

        composable("marketplace") {
            MarketplaceScreen(
                onBack = { navController.popBackStack() },
                onListingClick = { listingId ->
                    navController.navigate("marketplace/$listingId")
                },
                onCabinet = { navController.navigate("breeder_cabinet") }
            )
        }

        composable("marketplace/{listingId}") { backStackEntry ->
            SaleListingDetailScreen(
                listingId = backStackEntry.arguments?.getString("listingId").orEmpty(),
                onBack = { navController.popBackStack() }
            )
        }

        composable("breeder_cabinet") {
            BreederCabinetScreen(
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAddListing = { navController.navigate("breeder_listing/new") },
                onEditListing = { listingId ->
                    navController.navigate("breeder_listing/$listingId")
                }
            )
        }

        composable("breeder_listing/{listingId}") { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId")
                ?.takeUnless { it == "new" }
            SaleListingFormScreen(
                listingId = listingId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
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
                onChatClick = { applicationId ->
                    navController.navigate("application_chat/$applicationId")
                }
            )
        }

        composable("details_from_application/{petId}") {
            val backStackEntry = it
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            DetailsScreen(
                onBack = { navController.popBackStack() },
                onAccount = { openAccount() },
                onApply = { pet ->
                    navController.navigate(
                        "application/${pet.id}/${Uri.encode(pet.name)}"
                    )
                },
                petId = petId
            )
        }

        // РљР°Р±РёРЅРµС‚ РїСЂРёСЋС‚Р° / РђРґРјРёРЅ-РїР°РЅРµР»СЊ
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

        // РџСЂРѕСЃРјРѕС‚СЂ Р·Р°СЏРІРѕРє РЅР° РїРёС‚РѕРјС†Р°
        composable("admin/applications/{petId}/{petName}") { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            val petName = backStackEntry.arguments?.getString("petName") ?: ""
            PetApplicationsScreen(
                navController = navController,
                petId = petId,
                petName = petName
            )
        }

        // Р”РµС‚Р°Р»СЊРЅС‹Р№ РїСЂРѕСЃРјРѕС‚СЂ Р·Р°СЏРІРєРё
        composable("admin/application/detail/{applicationId}") { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            PetApplicationDetailScreen(
                navController = navController,
                applicationId = applicationId
            )
        }

        // Р­РєСЂР°РЅ С‡Р°С‚Р° РґР»СЏ РїРѕР»СЊР·РѕРІР°С‚РµР»РµР№ (РґРµС‚Р°Р»Рё Р·Р°СЏРІРєРё + С‡Р°С‚)
        composable("application_chat/{applicationId}") { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            ApplicationDetailWithChatScreen(
                navController = navController,
                applicationId = applicationId,
                onBack = { navController.popBackStack() }
            )
        }

        // Р­РєСЂР°РЅ С‡Р°С‚Р° (С‚РѕР»СЊРєРѕ С‡Р°С‚)
        composable("chat/{applicationId}") { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            ChatScreen(
                applicationId = applicationId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
