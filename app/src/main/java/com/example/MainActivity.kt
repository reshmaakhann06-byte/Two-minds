package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.core.AppPreferences
import com.example.core.NotificationHelper
import com.example.data.model.PartnerRole
import com.example.data.repository.TradingMarketRepository
import com.example.data.repository.TwoMindsRepository
import com.example.ui.screens.GoogleAuthScreen
import com.example.ui.screens.SecretCodeLockScreen
import com.example.ui.screens.TwoMindsShell
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.theme.TwoMindsTheme
import com.example.ui.theme.TwoMindsThemeTokens
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TradingViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.ensureChannels(applicationContext)

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val prefs = remember(context) { AppPreferences(context.applicationContext) }
            val themeMode by prefs.themeModeFlow.collectAsStateWithLifecycle(initialValue = "dark")
            val credentialManager = remember(context) { CredentialManager.create(context) }

            var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
            var unlockedRoleId by rememberSaveable { mutableStateOf<String?>(null) }

            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { auth ->
                    currentUser = auth.currentUser
                    if (auth.currentUser == null) {
                        unlockedRoleId = null
                    }
                }
                Firebase.auth.addAuthStateListener(listener)
                onDispose {
                    Firebase.auth.removeAuthStateListener(listener)
                }
            }

            fun signOutGoogle() {
                scope.launch {
                    withContext(NonCancellable) {
                        try {
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                        } catch (_: Exception) {
                        }
                        Firebase.auth.signOut()
                        unlockedRoleId = null
                    }
                }
            }

            TwoMindsTheme(themeMode = themeMode) {
                val view = LocalView.current
                val colors = TwoMindsThemeTokens.colors
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as? Activity)?.window
                        if (window != null) {
                            val controller = WindowCompat.getInsetsController(window, view)
                            controller.isAppearanceLightStatusBars = !colors.isDark
                            controller.isAppearanceLightNavigationBars = !colors.isDark
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    attemptAutoSignIn(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                        onUnauthenticated = {},
                        scope = scope
                    )
                }

                val activeUser = currentUser
                val unlockedRole = PartnerRole.fromId(unlockedRoleId)

                when {
                    unlockedRole == null -> {
                        SecretCodeLockScreen(
                            onUnlocked = { role ->
                                unlockedRoleId = role.id
                            },
                            onSignOutGoogle = { signOutGoogle() }
                        )
                    }
                    activeUser == null -> {
                        GoogleAuthScreen(
                            unlockedRole = unlockedRole,
                            onAuthSuccess = {
                                currentUser = Firebase.auth.currentUser
                            },
                            onBackToLock = {
                                unlockedRoleId = null
                            }
                        )
                    }
                    else -> {
                        val sessionKey = "${activeUser.uid}_${unlockedRole.id}"
                        key(sessionKey) {
                            val repository = remember(context) {
                                TwoMindsRepository(
                                    FirebaseFirestore.getInstance(
                                        context.applicationContext.getString(R.string.firestore_database_id)
                                    ),
                                    Firebase.auth
                                )
                            }
                            val marketRepo = remember { TradingMarketRepository() }

                            val mainViewModel: MainViewModel = viewModel(
                                key = "main_$sessionKey",
                                factory = viewModelFactory {
                                    initializer {
                                        MainViewModel(
                                            repository = repository,
                                            prefs = prefs,
                                            currentUserId = activeUser.uid,
                                            activeRole = unlockedRole
                                        )
                                    }
                                }
                            )
                            val chatViewModel: ChatViewModel = viewModel(
                                key = "chat_$sessionKey",
                                factory = viewModelFactory {
                                    initializer {
                                        ChatViewModel(
                                            repository = repository,
                                            appContext = context.applicationContext,
                                            currentUserId = activeUser.uid,
                                            activeRole = unlockedRole
                                        )
                                    }
                                }
                            )
                            val tradingViewModel: TradingViewModel = viewModel(
                                key = "trading_$sessionKey",
                                factory = viewModelFactory {
                                    initializer {
                                        TradingViewModel(
                                            firestoreRepo = repository,
                                            marketRepo = marketRepo,
                                            appContext = context.applicationContext,
                                            currentUserId = activeUser.uid
                                        )
                                    }
                                }
                            )

                            val lifecycleOwner = LocalLifecycleOwner.current
                            DisposableEffect(lifecycleOwner, mainViewModel) {
                                val observer = LifecycleEventObserver { _, event ->
                                    when (event) {
                                        Lifecycle.Event.ON_START -> mainViewModel.setOnlinePresence(true)
                                        Lifecycle.Event.ON_STOP -> mainViewModel.setOnlinePresence(false)
                                        else -> Unit
                                    }
                                }
                                lifecycleOwner.lifecycle.addObserver(observer)
                                onDispose {
                                    lifecycleOwner.lifecycle.removeObserver(observer)
                                    mainViewModel.setOnlinePresence(false)
                                }
                            }

                            TwoMindsShell(
                                activeRole = unlockedRole,
                                themeMode = themeMode,
                                mainViewModel = mainViewModel,
                                chatViewModel = chatViewModel,
                                tradingViewModel = tradingViewModel,
                                onLockApp = {
                                    mainViewModel.setOnlinePresence(false)
                                    unlockedRoleId = null
                                },
                                onSignOut = {
                                    mainViewModel.setOnlinePresence(false)
                                    signOutGoogle()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
