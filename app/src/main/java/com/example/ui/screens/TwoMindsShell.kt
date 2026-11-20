package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.model.PartnerRole
import com.example.ui.components.OnlineStatusDot
import com.example.ui.theme.TwoMindsThemeTokens
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TradingViewModel
import com.example.ui.viewmodel.UiState

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_DEEN = "deen"
const val ROUTE_BOOKS = "books"
const val ROUTE_SKILLS = "skills"
const val ROUTE_BUSINESS = "business"
const val ROUTE_TRADING = "trading"
const val ROUTE_LANGUAGES = "languages"
const val ROUTE_FITNESS = "fitness"
const val ROUTE_TOGETHER = "together"
const val ROUTE_SETTINGS = "settings"

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val allNavItems = listOf(
    NavDestinationItem(ROUTE_DASHBOARD, "Dashboard", Icons.Default.GridView),
    NavDestinationItem(ROUTE_DEEN, "Deen", Icons.Default.Mosque),
    NavDestinationItem(ROUTE_BOOKS, "Books", Icons.AutoMirrored.Filled.MenuBook),
    NavDestinationItem(ROUTE_SKILLS, "Skills", Icons.Default.School),
    NavDestinationItem(ROUTE_BUSINESS, "Business", Icons.Default.BusinessCenter),
    NavDestinationItem(ROUTE_TRADING, "Trading", Icons.Default.CandlestickChart),
    NavDestinationItem(ROUTE_LANGUAGES, "Languages", Icons.Default.Translate),
    NavDestinationItem(ROUTE_FITNESS, "Fitness", Icons.Default.FitnessCenter),
    NavDestinationItem(ROUTE_TOGETHER, "Together", Icons.Default.Group),
    NavDestinationItem(ROUTE_SETTINGS, "Settings", Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoMindsShell(
    activeRole: PartnerRole,
    themeMode: String,
    mainViewModel: MainViewModel,
    chatViewModel: ChatViewModel,
    tradingViewModel: TradingViewModel,
    onLockApp: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val colors = TwoMindsThemeTokens.colors
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_DASHBOARD
    var showMoreSheet by remember { mutableStateOf(false) }

    // Request notification permission once on Android 13+ for live message and price alerts
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val userProfileState by mainViewModel.userProfileState.collectAsStateWithLifecycle()
    val workspaceState by mainViewModel.workspaceState.collectAsStateWithLifecycle()
    val myProfileState by mainViewModel.myProfileState.collectAsStateWithLifecycle()
    val partnerProfileState by mainViewModel.partnerProfileState.collectAsStateWithLifecycle()
    val businessItemsState by mainViewModel.businessItemsState.collectAsStateWithLifecycle()
    val bookNotesState by mainViewModel.bookNotesState.collectAsStateWithLifecycle()
    val customBooksState by mainViewModel.customBooksState.collectAsStateWithLifecycle()
    val statusBanner by mainViewModel.statusBanner.collectAsStateWithLifecycle()

    val messagesState by chatViewModel.messagesState.collectAsStateWithLifecycle()
    val callsState by chatViewModel.callsState.collectAsStateWithLifecycle()
    val isRecordingVoice by chatViewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val playingMessageId by chatViewModel.playingMessageId.collectAsStateWithLifecycle()
    val chatErrorBanner by chatViewModel.errorBanner.collectAsStateWithLifecycle()

    val tickers by tradingViewModel.tickers.collectAsStateWithLifecycle()
    val selectedSymbol by tradingViewModel.selectedSymbol.collectAsStateWithLifecycle()
    val selectedInterval by tradingViewModel.selectedInterval.collectAsStateWithLifecycle()
    val candles by tradingViewModel.candles.collectAsStateWithLifecycle()
    val news by tradingViewModel.news.collectAsStateWithLifecycle()
    val marketStatus by tradingViewModel.marketStatus.collectAsStateWithLifecycle()
    val tradesState by tradingViewModel.tradesState.collectAsStateWithLifecycle()
    val alertsState by tradingViewModel.alertsState.collectAsStateWithLifecycle()
    val tradeFeedback by tradingViewModel.tradeFeedback.collectAsStateWithLifecycle()

    val userProfile = (userProfileState as? UiState.Success)?.data
    val workspace = (workspaceState as? UiState.Success)?.data
    val myProfile = (myProfileState as? UiState.Success)?.data
    val partnerProfile = (partnerProfileState as? UiState.Success)?.data
    val businessItems = (businessItemsState as? UiState.Success)?.data.orEmpty()
    val bookNotes = (bookNotesState as? UiState.Success)?.data.orEmpty()
    val customBooks = (customBooksState as? UiState.Success)?.data.orEmpty()

    val messages = (messagesState as? UiState.Success)?.data.orEmpty()
    val calls = (callsState as? UiState.Success)?.data.orEmpty()
    val trades = (tradesState as? UiState.Success)?.data.orEmpty()
    val alerts = (alertsState as? UiState.Success)?.data.orEmpty()

    val memberUids = remember(workspace, mainViewModel.currentUserId) {
        val list = workspace?.memberUids.orEmpty()
        if (mainViewModel.currentUserId in list) list else (list + mainViewModel.currentUserId).distinct()
    }
    val unreadCount = remember(messages, mainViewModel.currentUserId) {
        messages.count { it.senderUid != mainViewModel.currentUserId && mainViewModel.currentUserId !in it.readByUids }
    }
    val incomingRingingCall = remember(calls, mainViewModel.currentUserId) {
        calls.firstOrNull { it.status == "ringing" && it.callerUid != mainViewModel.currentUserId }
    }

    val navigateTo: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val activeTitle = remember(currentRoute) {
        allNavItems.firstOrNull { it.route == currentRoute }?.label ?: "Two Minds"
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val isExpandedLayout = maxWidth >= 700.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isExpandedLayout) {
                DesktopDarkSidebar(
                    currentRoute = currentRoute,
                    unreadCount = unreadCount,
                    onNavigate = navigateTo
                )
            }

            Scaffold(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                containerColor = colors.background,
                topBar = {
                    TwoMindsTopBar(
                        isExpandedLayout = isExpandedLayout,
                        title = activeTitle,
                        partnerName = partnerProfile?.displayName ?: activeRole.partner().defaultName,
                        partnerOnline = partnerProfile?.online == true,
                        unreadCount = unreadCount,
                        isDark = colors.isDark,
                        onToggleTheme = {
                            mainViewModel.setThemeMode(if (colors.isDark) "light" else "dark")
                        },
                        onOpenChat = { navigateTo(ROUTE_TOGETHER) },
                        onOpenSettings = { navigateTo(ROUTE_SETTINGS) }
                    )
                },
                bottomBar = {
                    if (!isExpandedLayout) {
                        NavigationBar(
                            containerColor = colors.card,
                            tonalElevation = 0.dp
                        ) {
                            val bottomItems = listOf(
                                Triple(ROUTE_DASHBOARD, "Home", Icons.Default.Home),
                                Triple(ROUTE_DEEN, "Deen", Icons.Default.Mosque),
                                Triple(ROUTE_TRADING, "Trading", Icons.Default.CandlestickChart),
                                Triple(ROUTE_TOGETHER, "Together", Icons.Default.Group)
                            )
                            bottomItems.forEach { (route, label, icon) ->
                                val selected = currentRoute == route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(route) },
                                    modifier = Modifier.testTag("nav_$route"),
                                    icon = {
                                        if (route == ROUTE_TOGETHER && unreadCount > 0) {
                                            BadgedBox(badge = {
                                                Badge(containerColor = colors.coral) {
                                                    Text("$unreadCount", color = Color.White)
                                                }
                                            }) {
                                                Icon(icon, contentDescription = label)
                                            }
                                        } else {
                                            Icon(icon, contentDescription = label)
                                        }
                                    },
                                    label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colors.gold,
                                        selectedTextColor = colors.gold,
                                        unselectedIconColor = colors.textSecondary,
                                        unselectedTextColor = colors.textSecondary,
                                        indicatorColor = colors.goldBg
                                    )
                                )
                            }
                            val moreSelected = currentRoute in setOf(
                                ROUTE_BOOKS, ROUTE_SKILLS, ROUTE_BUSINESS, ROUTE_LANGUAGES, ROUTE_FITNESS, ROUTE_SETTINGS
                            )
                            NavigationBarItem(
                                selected = moreSelected,
                                onClick = { showMoreSheet = true },
                                modifier = Modifier.testTag("nav_more"),
                                icon = { Icon(Icons.Default.Apps, contentDescription = "More modules") },
                                label = { Text("More", maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.gold,
                                    selectedTextColor = colors.gold,
                                    unselectedIconColor = colors.textSecondary,
                                    unselectedTextColor = colors.textSecondary,
                                    indicatorColor = colors.goldBg
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val activeAlert = statusBanner ?: chatErrorBanner
                    if (activeAlert != null) {
                        Surface(
                            color = colors.goldBg,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeAlert,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = {
                                    mainViewModel.clearBanner()
                                    chatViewModel.clearError()
                                }) {
                                    Text("Dismiss", color = colors.gold)
                                }
                            }
                        }
                    }

                    if (incomingRingingCall != null && currentRoute != ROUTE_TOGETHER) {
                        Surface(
                            color = colors.emeraldBg,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.emerald),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (incomingRingingCall.callType == "video") Icons.Default.Videocam else Icons.Default.Call,
                                    contentDescription = "Incoming Call",
                                    tint = colors.emerald
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Incoming ${incomingRingingCall.callType} call",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "From ${partnerProfile?.displayName ?: activeRole.partner().defaultName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                                Button(
                                    onClick = {
                                        chatViewModel.updateCallStatus(incomingRingingCall.id, "active")
                                        navigateTo(ROUTE_TOGETHER)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.emerald, contentColor = Color.White)
                                ) {
                                    Text("Answer")
                                }
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_DASHBOARD,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        composable(ROUTE_DASHBOARD) {
                            DashboardScreen(
                                activeRole = activeRole,
                                myProfile = myProfile,
                                partnerProfile = partnerProfile,
                                onToggleChecklist = mainViewModel::toggleChecklist,
                                onNavigateToRoute = navigateTo
                            )
                        }
                        composable(ROUTE_DEEN) {
                            DeenScreen(
                                activeRole = activeRole,
                                myProfile = myProfile,
                                partnerProfile = partnerProfile,
                                onToggleNamaz = mainViewModel::toggleNamaz,
                                onToggleSunnat = mainViewModel::toggleSunnat,
                                onIncrementTasbeeh = mainViewModel::incrementTasbeeh,
                                onResetTasbeeh = mainViewModel::resetTasbeeh,
                                onSelectTasbeehPreset = mainViewModel::selectTasbeehPreset,
                                onUpdateQuran = mainViewModel::updateQuranProgress
                            )
                        }
                        composable(ROUTE_BOOKS) {
                            BooksScreen(
                                myProfile = myProfile,
                                bookNotes = bookNotes,
                                customBooks = customBooks,
                                onUpdateReadingProgress = mainViewModel::updateReadingProgress,
                                onAddBookNote = mainViewModel::addBookNote,
                                onAddCustomBook = mainViewModel::addCustomBook
                            )
                        }
                        composable(ROUTE_SKILLS) {
                            SkillsScreen(
                                activeRole = activeRole,
                                myProfile = myProfile,
                                partnerProfile = partnerProfile,
                                onBoostSkill = mainViewModel::boostSkillProgress
                            )
                        }
                        composable(ROUTE_BUSINESS) {
                            BusinessScreen(
                                businessItems = businessItems,
                                onAddBusinessItem = mainViewModel::addBusinessItem,
                                onToggleBusinessItem = mainViewModel::toggleBusinessItem
                            )
                        }
                        composable(ROUTE_TRADING) {
                            TradingScreen(
                                tickers = tickers,
                                selectedSymbol = selectedSymbol,
                                selectedInterval = selectedInterval,
                                candles = candles,
                                news = news,
                                marketStatus = marketStatus,
                                trades = trades,
                                alerts = alerts,
                                feedbackMessage = tradeFeedback,
                                onSelectSymbol = tradingViewModel::selectSymbol,
                                onSelectInterval = tradingViewModel::selectInterval,
                                onPlaceOrder = tradingViewModel::placePaperOrder,
                                onAddAlert = tradingViewModel::addPriceAlert,
                                onDeleteAlert = tradingViewModel::deletePriceAlert,
                                onClearFeedback = tradingViewModel::clearFeedback
                            )
                        }
                        composable(ROUTE_LANGUAGES) {
                            LanguagesScreen(
                                activeRole = activeRole,
                                myProfile = myProfile,
                                partnerProfile = partnerProfile,
                                onWordLearned = mainViewModel::markLanguageWordLearned
                            )
                        }
                        composable(ROUTE_FITNESS) {
                            FitnessScreen(
                                myProfile = myProfile,
                                onToggleWorkoutCompleted = mainViewModel::toggleWorkoutCompleted,
                                onUpdateWater = mainViewModel::updateWaterGlasses,
                                onUpdateSleep = mainViewModel::updateSleepHours
                            )
                        }
                        composable(ROUTE_TOGETHER) {
                            TogetherAndChatScreen(
                                activeRole = activeRole,
                                currentUserId = mainViewModel.currentUserId,
                                memberUids = memberUids,
                                myProfile = myProfile,
                                partnerProfile = partnerProfile,
                                messages = messages,
                                calls = calls,
                                isRecordingVoice = isRecordingVoice,
                                playingMessageId = playingMessageId,
                                disappearingSec = userProfile?.disappearingMessagesSec ?: 0L,
                                blockedPartner = userProfile?.blockedPartner ?: false,
                                initialTab = 1,
                                onTypingChanged = chatViewModel::onTypingChanged,
                                onSendMessage = { text, type, name, size, dur, reply, exp ->
                                    chatViewModel.sendMessage(memberUids, text, type, name, size, dur, reply, exp)
                                },
                                onEditMessage = chatViewModel::editMessage,
                                onDeleteMessage = chatViewModel::deleteMessage,
                                onTogglePin = chatViewModel::togglePin,
                                onReact = chatViewModel::react,
                                onStartVoiceRecord = chatViewModel::startVoiceRecording,
                                onStopSendVoiceRecord = {
                                    chatViewModel.stopAndSendVoiceRecording(
                                        memberUids,
                                        userProfile?.disappearingMessagesSec ?: 0L
                                    )
                                },
                                onCancelVoiceRecord = chatViewModel::cancelVoiceRecording,
                                onTogglePlayVoice = chatViewModel::togglePlayVoiceNote,
                                onStartCall = { type -> chatViewModel.startCall(memberUids, type) },
                                onUpdateCallStatus = chatViewModel::updateCallStatus,
                                onUpdateDisappearingSec = { sec ->
                                    mainViewModel.updateNotificationAndPrivacy(
                                        notificationsEnabled = userProfile?.notificationsEnabled ?: true,
                                        disappearingSec = sec,
                                        blockedPartner = userProfile?.blockedPartner ?: false
                                    )
                                }
                            )
                        }
                        composable(ROUTE_SETTINGS) {
                            ProfileAndSettingsScreen(
                                activeRole = activeRole,
                                userProfile = userProfile,
                                myProfile = myProfile,
                                themeMode = themeMode,
                                onSetThemeMode = mainViewModel::setThemeMode,
                                onUpdateProfileInfo = mainViewModel::updateProfileCustomization,
                                onUpdatePrivacy = mainViewModel::updateNotificationAndPrivacy,
                                onLockApp = onLockApp,
                                onSignOut = onSignOut
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = colors.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "All Workspace Modules",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                val extraModules = listOf(
                    NavDestinationItem(ROUTE_BOOKS, "Books & PDFs", Icons.AutoMirrored.Filled.MenuBook),
                    NavDestinationItem(ROUTE_FITNESS, "Fitness & Diet", Icons.Default.FitnessCenter),
                    NavDestinationItem(ROUTE_SKILLS, "Skills Track", Icons.Default.School),
                    NavDestinationItem(ROUTE_LANGUAGES, "Languages", Icons.Default.Translate),
                    NavDestinationItem(ROUTE_BUSINESS, "Business & Drive", Icons.Default.CloudQueue),
                    NavDestinationItem(ROUTE_SETTINGS, "Profile & Settings", Icons.Default.Settings)
                )
                extraModules.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { item ->
                            val selected = currentRoute == item.route
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) colors.goldBg else colors.subtleSurface)
                                    .border(
                                        1.dp,
                                        if (selected) colors.gold else colors.border,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        showMoreSheet = false
                                        navigateTo(item.route)
                                    }
                                    .padding(vertical = 16.dp, horizontal = 8.dp)
                                    .testTag("more_${item.route}"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) colors.gold else colors.textPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) colors.gold else colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TwoMindsTopBar(
    isExpandedLayout: Boolean,
    title: String,
    partnerName: String,
    partnerOnline: Boolean,
    unreadCount: Int,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    Surface(
        color = colors.card,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isExpandedLayout) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_twominds),
                        contentDescription = "Two Minds Logo",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.clickable(onClick = onOpenChat)
                    ) {
                        OnlineStatusDot(online = partnerOnline, modifier = Modifier.size(7.dp))
                        Text(
                            text = "$partnerName • ${if (partnerOnline) "Online" else "Offline"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (partnerOnline) colors.emerald else colors.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onOpenChat,
                    modifier = Modifier.testTag("top_chat_button")
                ) {
                    BadgedBox(badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = colors.coral) {
                                Text("$unreadCount", color = Color.White)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Live Messages & Alerts",
                            tint = colors.textPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("top_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Light or Dark Theme",
                        tint = colors.gold
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile and Settings",
                        tint = colors.textPrimary
                    )
                }
            }
            HorizontalDivider(color = colors.border)
        }
    }
}

@Composable
private fun DesktopDarkSidebar(
    currentRoute: String,
    unreadCount: Int,
    onNavigate: (String) -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(colors.sidebarBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_twominds),
                contentDescription = "Two Minds Emblem",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(
                    text = "TWO MINDS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Grow Together",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA0A5B5)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            allNavItems.forEach { item ->
                val selected = currentRoute == item.route
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) colors.gold.copy(alpha = 0.16f) else Color.Transparent)
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("sidebar_${item.route}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) colors.gold else Color(0xFFB4B8C5),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selected) Color.White else Color(0xFFB4B8C5),
                        modifier = Modifier.weight(1f)
                    )
                    if (item.route == ROUTE_TOGETHER && unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.coral)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$unreadCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1A1D24)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.gold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Consistency Streak",
                        tint = colors.gold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Stay Consistent",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Text(
                        text = "You're doing great!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA0A5B5)
                    )
                }
            }
        }
    }
}
