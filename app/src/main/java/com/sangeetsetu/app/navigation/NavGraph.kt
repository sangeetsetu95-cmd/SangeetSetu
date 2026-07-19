package com.sangeetsetu.app.navigation

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.sangeetsetu.app.ai.AISupportScreen
import com.sangeetsetu.app.domain.repository.IAuthRepository
import com.sangeetsetu.app.ui.screens.AboutScreen
import com.sangeetsetu.app.ui.screens.AddressesScreen
import com.sangeetsetu.app.ui.screens.AdminAnalyticsScreen
import com.sangeetsetu.app.ui.screens.AdminAppSettingsScreen
import com.sangeetsetu.app.ui.screens.AdminChatControlScreen
import com.sangeetsetu.app.ui.screens.AdminArtistDetailsScreen
import com.sangeetsetu.app.ui.screens.AdminArtistListScreen
import com.sangeetsetu.app.ui.screens.AdminBadgeManagementScreen
import com.sangeetsetu.app.ui.screens.AdminBannerManagerScreen
import com.sangeetsetu.app.ui.screens.AdminBhajansScreen
import com.sangeetsetu.app.ui.screens.AdminBookingsScreen
import com.sangeetsetu.app.ui.screens.AdminCategoryArtistListScreen
import com.sangeetsetu.app.ui.screens.AdminCategoryScreen
import com.sangeetsetu.app.ui.screens.AdminConfigManagerScreen
import com.sangeetsetu.app.ui.screens.AdminDashboardScreen
import com.sangeetsetu.app.ui.screens.AdminFormBuilderScreen
import com.sangeetsetu.app.ui.screens.AdminHomeSectionsScreen
import com.sangeetsetu.app.ui.screens.AdminLogsScreen
import com.sangeetsetu.app.ui.screens.AdminMoreScreen
import com.sangeetsetu.app.ui.screens.AdminPaymentsScreen
import com.sangeetsetu.app.ui.screens.AdminRBACScreen
import com.sangeetsetu.app.ui.screens.AdminProfileScreen
import com.sangeetsetu.app.ui.screens.AdminRequestsScreen
import com.sangeetsetu.app.ui.screens.AdminServicesScreen
import com.sangeetsetu.app.ui.screens.AdminUnlockPaymentsScreen
import com.sangeetsetu.app.ui.screens.AdminUserListScreen
import com.sangeetsetu.app.ui.screens.AllEventsScreen
import com.sangeetsetu.app.ui.screens.AllServicesScreen
import com.sangeetsetu.app.ui.screens.AnnouncementsScreen
import com.sangeetsetu.app.ui.screens.ArtistDashboardScreen
import com.sangeetsetu.app.ui.screens.ArtistDetailsScreen
import com.sangeetsetu.app.ui.screens.ArtistRegistrationScreen
import com.sangeetsetu.app.ui.screens.BookingScreen
import com.sangeetsetu.app.ui.screens.BroadcastScreen
import com.sangeetsetu.app.ui.screens.CategoriesScreen
import com.sangeetsetu.app.ui.screens.CategoryArtistsScreen
import com.sangeetsetu.app.ui.screens.ChangePasswordScreen
import com.sangeetsetu.app.ui.screens.ChatDetailScreen
import com.sangeetsetu.app.ui.screens.ChatScreen
import com.sangeetsetu.app.viewmodel.ChatViewModel
import com.sangeetsetu.app.ui.screens.ContactUsScreen
import com.sangeetsetu.app.ui.screens.DonationScreen
import com.sangeetsetu.app.ui.screens.DynamicRegistrationScreen
import com.sangeetsetu.app.ui.screens.EditProfileScreen
import com.sangeetsetu.app.ui.screens.EventDetailsScreen
import com.sangeetsetu.app.ui.screens.ForgotPasswordScreen
import com.sangeetsetu.app.ui.screens.PremiumHomeScreen
import com.sangeetsetu.app.ui.screens.LanguageScreen
import com.sangeetsetu.app.ui.screens.LoginScreen
import com.sangeetsetu.app.ui.screens.ManageReportsScreen
import com.sangeetsetu.app.ui.screens.MyBookingsScreen
import com.sangeetsetu.app.ui.screens.MyPaymentsScreen
import com.sangeetsetu.app.ui.screens.NotificationsScreen
import com.sangeetsetu.app.ui.screens.OrganizerDashboardScreen
import com.sangeetsetu.app.ui.screens.ProfileScreen
import com.sangeetsetu.app.ui.screens.ProfileSetupScreen
import com.sangeetsetu.app.ui.screens.SavedArtistsScreen
import com.sangeetsetu.app.ui.screens.SearchScreen
import com.sangeetsetu.app.ui.screens.SettingsScreen
import com.sangeetsetu.app.ui.screens.SignUpScreen
import com.sangeetsetu.app.ui.screens.SplashScreen
import com.sangeetsetu.app.ui.screens.SubscriptionScreen
import com.sangeetsetu.app.ui.screens.ThemeScreen
import com.sangeetsetu.app.ui.screens.WelcomeScreen
import com.sangeetsetu.app.viewmodel.AdminViewModel
import com.sangeetsetu.app.viewmodel.AuthState
import com.sangeetsetu.app.viewmodel.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun ProtectedRoute(
    authState: AuthState,
    navController: NavHostController,
    requiredRole: String? = null,
    requiredPermission: String? = null,
    mainViewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val userRole = when (authState) {
        is AuthState.AuthenticatedAdmin -> "admin"
        is AuthState.AuthenticatedArtist -> "artist"
        is AuthState.AuthenticatedOrganizer -> "organizer"
        is AuthState.AuthenticatedUser -> "user"
        else -> null
    }

    val hasAccess = remember(authState, requiredRole, requiredPermission) {
        when {
            authState is AuthState.AuthenticatedAdmin -> true
            requiredRole != null -> userRole == requiredRole
            requiredPermission != null -> mainViewModel.hasPermission(requiredPermission)
            else -> true
        }
    }

    if (hasAccess) {
        content()
    } else {
        LaunchedEffect(authState) {
            if (authState !is AuthState.Loading && authState !is AuthState.Idle) {
                val destination = when (userRole) {
                    "admin" -> Screen.AdminDashboard.route
                    "artist" -> Screen.Home.route
                    "organizer" -> Screen.Home.route
                    "user" -> Screen.Home.route
                    else -> Screen.Welcome.route
                }
                navController.navigate(destination) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authRepository: IAuthRepository,
    googleSignInClient: GoogleSignInClient,
    startDestination: String = Screen.Splash.route,
) {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = hiltViewModel()
    val authState by mainViewModel.authState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    // Navigation Helper to prevent loops and duplicate screens
    val navigateTo: (String) -> Unit = { route ->
        if (navController.currentDestination?.route != route) {
            val isTopLevel = route == Screen.Home.route || 
                             route == Screen.Categories.route || 
                             route == Screen.MyBookings.route || 
                             route == Screen.Profile.route ||
                             route == Screen.Chat.route ||
                             route == Screen.Announcements.route

            navController.navigate(route) {
                if (isTopLevel) {
                    val startId = navController.graph.findStartDestination().id
                    popUpTo(startId) {
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Start checking auth state immediately
    androidx.compose.runtime.LaunchedEffect(Unit) {
        mainViewModel.checkAuthState()
    }

    androidx.compose.runtime.LaunchedEffect(authState) {
        Log.d("StartupTrace", "Auth State Changed: $authState")
        
        val currentRoute = try { navController.currentDestination?.route } catch (e: Exception) { null }
        
        // Don't perform auto-navigation if the user is already on a relevant entry screen
        // and they are unauthenticated. This allows them to stay on Login/SignUp/Welcome.
        if (authState is AuthState.Unauthenticated) {
            if (currentRoute == Screen.Login.route || 
                currentRoute == Screen.SignUp.route || 
                currentRoute == Screen.Welcome.route ||
                currentRoute == Screen.ForgotPassword.route) {
                return@LaunchedEffect
            }
        }

        when (authState) {
            is AuthState.AuthenticatedAdmin -> {
                if (currentRoute != Screen.AdminDashboard.route) {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.AuthenticatedArtist -> {
                if (currentRoute == Screen.Splash.route || currentRoute == Screen.Welcome.route || 
                    currentRoute == Screen.Login.route || currentRoute == Screen.SignUp.route || 
                    currentRoute == null) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.AuthenticatedOrganizer -> {
                if (currentRoute == Screen.Splash.route || currentRoute == Screen.Welcome.route || 
                    currentRoute == Screen.Login.route || currentRoute == Screen.SignUp.route || 
                    currentRoute == null) {
                    navController.navigate(Screen.OrganizerDashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.AuthenticatedUser -> {
                if (currentRoute == Screen.Splash.route || currentRoute == Screen.Welcome.route || 
                    currentRoute == Screen.Login.route || currentRoute == Screen.SignUp.route || 
                    currentRoute == null) {
                    
                    val redirectRoute = navController.currentBackStackEntry?.savedStateHandle?.get<String>("redirect_route")
                    if (redirectRoute != null) {
                        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("redirect_route")
                        navController.navigate(redirectRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            is AuthState.ProfileIncomplete -> {
                if (currentRoute != Screen.ProfileSetup.route) {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                // If we're at Splash, go to Welcome
                if (currentRoute == Screen.Splash.route || currentRoute == null) {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                if (currentRoute == Screen.Splash.route || currentRoute == null) {
                    navController.navigate(Screen.Welcome.route) { popUpTo(0) }
                }
            }
            else -> {}
        }
    }

    val authCallback = remember {
        object : IAuthRepository.AuthCallback {
            override fun onAuthSuccess() { 
                isLoading = false
                mainViewModel.checkAuthState() 
            }
            override fun onAuthError(error: String) { 
                isLoading = false
                Toast.makeText(context, error, Toast.LENGTH_LONG).show() 
            }
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)!!
            isLoading = true
            authRepository.signInWithGoogle(account.idToken!!, authCallback)
        } catch (e: Exception) {
            isLoading = false
            Toast.makeText(context, "Google Sign-In Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route) {
            SplashScreen(onAdminAccess = { mainViewModel.checkAuthState() }, onTimeout = {})
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen { navController.navigate(Screen.Login.route) }
        }
        composable(Screen.Login.route) {
            LoginScreen(
                isLoading = isLoading,
                onLoginRequested = { e, p -> isLoading = true; authRepository.loginWithEmail(e, p, authCallback) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                onGoogleLoginRequested = { googleSignInClient.signInIntent.let { googleLauncher.launch(it) } }
            )
        }
        composable(
            route = Screen.SignUp.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://sangeetsetu.com/invite/{ref}" },
                navDeepLink { uriPattern = "sangeetsetu://signup?ref={ref}" }
            ),
            arguments = listOf(navArgument("ref") { type = NavType.StringType; nullable = true })
        ) { b ->
            val refCode = b.arguments?.getString("ref") ?: ""
            SignUpScreen(
                isLoading = isLoading,
                initialReferralCode = refCode,
                onSignUpRequested = { n, e, p, r -> isLoading = true; authRepository.signUpWithEmail(n, e, p, r, authCallback) },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                isLoading = isLoading,
                onResetRequested = { e ->
                    isLoading = true
                    authRepository.resetPassword(e, object : IAuthRepository.AuthCallback {
                        override fun onAuthSuccess() { isLoading = false; Toast.makeText(context, "Reset email sent!", Toast.LENGTH_SHORT).show(); navController.navigate(Screen.Login.route) }
                        override fun onAuthError(err: String) { isLoading = false; Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                    })
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onProfileComplete = { navigateTo(Screen.Home.route) },
                onArtistSelected = { navigateTo(Screen.DynamicRegistration.route) }
            )
        }
        composable(Screen.ArtistRegistration.route) {
            ArtistRegistrationScreen(onBack = { navController.popBackStack() }, onComplete = { navigateTo(Screen.Home.route) })
        }
        composable(Screen.ArtistDashboard.route) {
            ProtectedRoute(authState, navController, requiredRole = "artist", mainViewModel = mainViewModel) {
                ArtistDashboardScreen(onLogout = { authRepository.logout(); navController.navigate(Screen.Login.route) { popUpTo(0) } })
            }
        }
        composable(Screen.OrganizerDashboard.route) {
            ProtectedRoute(authState, navController, requiredRole = "organizer", mainViewModel = mainViewModel) {
                OrganizerDashboardScreen(onLogout = { authRepository.logout(); navController.navigate(Screen.Login.route) { popUpTo(0) } }, onNavigate = navigateTo)
            }
        }
        composable(Screen.Home.route) {
            PremiumHomeScreen(
                onArtistClick = { id: String -> navigateTo(Screen.ArtistDetails.createRoute(id)) },
                onCategoryClick = { cat: String -> if (cat == "all_categories_view" || cat == "All") navigateTo(Screen.AllServices.route) else navigateTo(Screen.ArtistList.createRoute(cat)) },
                onSeeAllArtists = { navigateTo(Screen.ArtistList.createRoute("All")) },
                onProfileClick = { navigateTo(Screen.Profile.route) },
                onBookingsClick = { navigateTo(Screen.MyBookings.route) },
                onSearchClick = { query -> 
                    val route = if (query != null) Screen.Search.createRoute(query) else Screen.Search.route
                    navigateTo(route)
                },
                onNotificationClick = { navigateTo(Screen.Notifications.route) },
                onAnnouncementsClick = { navigateTo(Screen.Announcements.route) },
                onChatClick = { navigateTo(Screen.Chat.route) }
            )
        }
        composable(Screen.AllEvents.route) {
            AllEventsScreen(onBack = { navController.popBackStack() }, onEventClick = { id -> navigateTo(Screen.EventDetails.createRoute(id)) }, onBookClick = { id -> navigateTo(Screen.Booking.createRoute(id)) })
        }
        composable(route = Screen.EventDetails.route, arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { b ->
            EventDetailsScreen(eventId = b.arguments?.getString("eventId") ?: "", onBack = { navController.popBackStack() }, onBookClick = { id -> navigateTo(Screen.Booking.createRoute(id)) })
        }
        composable(Screen.Subscription.route) { SubscriptionScreen() }
        composable(Screen.AllServices.route) {
            AllServicesScreen(onCategoryClick = { c -> navigateTo(Screen.ArtistList.createRoute(c)) }, onBack = { navController.popBackStack() })
        }
        composable(Screen.Categories.route) {
            CategoriesScreen(onCategoryClick = { c -> navigateTo(Screen.ArtistList.createRoute(c)) }, onHome = { navigateTo(Screen.Home.route) }, onBookings = { navigateTo(Screen.MyBookings.route) }, onChat = { navigateTo(Screen.Chat.route) }, onProfile = { navigateTo(Screen.Profile.route) })
        }
        composable(route = Screen.ArtistList.route, arguments = listOf(navArgument("categoryId") { type = NavType.StringType })) { b ->
            CategoryArtistsScreen(categoryId = b.arguments?.getString("categoryId") ?: "All", onBack = { navController.popBackStack() }, onArtistClick = { id -> navigateTo(Screen.ArtistDetails.createRoute(id)) })
        }
        composable(
            route = Screen.ArtistDetails.route, 
            arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = Screen.ArtistDetails.deepLinkUri })
        ) { b ->
            val artistId = b.arguments?.getString("artistId") ?: ""
            val chatViewModel: ChatViewModel = hiltViewModel()
            ArtistDetailsScreen(
                artistId = artistId, 
                onBack = { navController.popBackStack() }, 
                onBook = { id -> navigateTo(Screen.Booking.createRoute(id)) },
                onChat = {
                    chatViewModel.createChatAndNavigate(artistId) { chatId ->
                        navController.navigate(Screen.MessageDetail.createRoute(chatId, artistId))
                    }
                }
            )
        }
        composable(route = Screen.Booking.route, arguments = listOf(navArgument("artistId") { type = NavType.StringType })) { b ->
            BookingScreen(artistId = b.arguments?.getString("artistId") ?: "", onBack = { navController.popBackStack() })
        }
        composable(Screen.MyBookings.route) {
            MyBookingsScreen(
                onBack = { navController.popBackStack() },
                onHome = { navigateTo(Screen.Home.route) }, 
                onCategories = { navigateTo(Screen.Categories.route) }, 
                onChat = { navigateTo(Screen.Chat.route) }, 
                onProfile = { navigateTo(Screen.Profile.route) }, 
                onArtistClick = { id -> navigateTo(Screen.ArtistDetails.createRoute(id)) }
            )
        }
        composable(Screen.Chat.route) {
            ChatScreen(
                onBack = { navController.popBackStack() }, 
                onHome = { navigateTo(Screen.Home.route) }, 
                onCategories = { navigateTo(Screen.Categories.route) }, 
                onBookings = { navigateTo(Screen.MyBookings.route) }, 
                onProfile = { navigateTo(Screen.Profile.route) },
                onChatClick = { chatId, receiverId ->
                    navController.navigate(Screen.MessageDetail.createRoute(chatId, receiverId))
                }
            )
        }
        composable(
            route = Screen.MessageDetail.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("receiverId") { type = NavType.StringType }
            )
        ) { b ->
            val chatId = b.arguments?.getString("chatId") ?: ""
            val receiverId = b.arguments?.getString("receiverId") ?: ""
            ChatDetailScreen(
                chatId = chatId,
                receiverId = receiverId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = { authRepository.logout(); navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                onNavigate = navigateTo
            )
        }
        composable(Screen.EditProfile.route) { EditProfileScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.MyPayments.route) { MyPaymentsScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.SavedArtists.route) { SavedArtistsScreen(onBack = { navController.popBackStack() }, onArtistClick = { id -> navigateTo(Screen.ArtistDetails.createRoute(id)) }) }
        composable(Screen.Addresses.route) { AddressesScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.About.route) { AboutScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.ContactUs.route) { ContactUsScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.ChangePassword.route) { ChangePasswordScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Donation.route) { DonationScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Settings.route) {
            val user = authRepository.getCurrentUser()
            SettingsScreen(onBack = { navController.popBackStack() }, isAdmin = user?.email == mainViewModel.adminEmail, onNavigate = navigateTo)
        }
        composable(Screen.Language.route) { LanguageScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Theme.route) { ThemeScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Notifications.route) { NotificationsScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Announcements.route) { AnnouncementsScreen(onBack = { navController.popBackStack() }) }
        composable(
            route = Screen.Search.route,
            arguments = listOf(navArgument("query") { type = NavType.StringType; defaultValue = ""; nullable = true })
        ) { backStackEntry ->
            val initialQuery = backStackEntry.arguments?.getString("query") ?: ""
            SearchScreen(
                onBack = { navController.popBackStack() }, 
                onArtistClick = { id -> navigateTo(Screen.ArtistDetails.createRoute(id)) }, 
                onCategoryClick = { c -> navigateTo(Screen.ArtistList.createRoute(c)) },
                initialQuery = initialQuery
            )
        }
        composable(Screen.AISupport.route) { AISupportScreen(onBack = { navController.popBackStack() }) }

        // Admin Routes
        composable(Screen.AdminDashboard.route) {
            val adminViewModel: AdminViewModel = hiltViewModel()
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminDashboardScreen(onLogout = { authRepository.logout(); navController.navigate(Screen.Login.route) { popUpTo(0) } }, onNavigate = navigateTo, viewModel = adminViewModel)
            }
        }

        composable(Screen.AdminArtistList.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminArtistListScreen(onBack = { navController.popBackStack() }, onNavigate = navigateTo, onArtistClick = { id -> navigateTo(Screen.AdminArtistDetails.createRoute(id)) })
            }
        }

        composable(
            route = Screen.AdminArtistDetails.route,
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) { b ->
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminArtistDetailsScreen(artistId = b.arguments?.getString("artistId") ?: "", onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminUserList.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminUserListScreen(onBack = { navController.popBackStack() }, onNavigate = navigateTo)
            }
        }

        composable(Screen.AdminBookings.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminBookingsScreen(onBack = { navController.popBackStack() }, onNavigate = navigateTo)
            }
        }

        composable(Screen.AdminPayments.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminPaymentsScreen(onBack = { navController.popBackStack() }, onNavigate = navigateTo)
            }
        }

        composable(Screen.AdminUnlockPayments.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminUnlockPaymentsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminMore.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminMoreScreen(onBack = { navController.popBackStack() }, onNavigate = navigateTo)
            }
        }

        composable(Screen.AdminCategories.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminCategoryScreen(onBack = { navController.popBackStack() }, onNavigateToCategory = { cat -> navigateTo(Screen.AdminCategoryArtists.createRoute(cat)) })
            }
        }

        composable(
            route = Screen.AdminCategoryArtists.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { b ->
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminCategoryArtistListScreen(
                    categoryId = b.arguments?.getString("categoryId") ?: "",
                    onBack = { navController.popBackStack() },
                    onEditArtist = { id -> navigateTo(Screen.AdminArtistDetails.createRoute(id)) }
                )
            }
        }

        composable(Screen.Banners.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminBannerManagerScreen(
                    onBack = { navController.popBackStack() },
                    onNavigate = navigateTo
                )
            }
        }

        composable(Screen.AdminAppSettings.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminAppSettingsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminChatControl.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminChatControlScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(
            route = Screen.AdminConfig.route,
            arguments = listOf(
                navArgument("collection") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { b ->
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminConfigManagerScreen(
                    collectionName = b.arguments?.getString("collection") ?: "",
                    title = b.arguments?.getString("title") ?: "",
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.AdminHomeSections.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminHomeSectionsScreen(onBack = { navController.popBackStack() }, onNavigate = navigateTo)
            }
        }

        composable(Screen.AdminBadgeManagement.route) {
            val adminViewModel: AdminViewModel = hiltViewModel()
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminBadgeManagementScreen(onBack = { navController.popBackStack() }, viewModel = adminViewModel)
            }
        }

        composable(Screen.AdminLogs.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminLogsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminAnalytics.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminAnalyticsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminProfile.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminProfileScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminRequests.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminRequestsScreen(onBack = { navController.popBackStack() }, onNavigate = navigateTo)
            }
        }

        composable(Screen.ManageReports.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                ManageReportsScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminServices.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminServicesScreen(onBack = { navController.popBackStack() }, onNavigate = navigateTo)
            }
        }

        composable(Screen.AdminBhajans.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminBhajansScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminRBAC.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminRBACScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminBroadcast.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                BroadcastScreen(onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AdminFormBuilder.route) {
            ProtectedRoute(authState, navController, requiredRole = "admin", mainViewModel = mainViewModel) {
                AdminFormBuilderScreen(onPreview = { navigateTo(Screen.DynamicRegistration.route) })
            }
        }

        composable(Screen.DynamicRegistration.route) {
            DynamicRegistrationScreen(
                onNavigateBack = { 
                    mainViewModel.checkAuthState()
                    navController.popBackStack() 
                }
            )
        }
    }
}
