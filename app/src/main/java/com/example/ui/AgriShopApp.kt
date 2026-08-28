package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AddListingDialog
import com.example.ui.components.AdvisorCallDialog
import com.example.ui.components.AudioLanguageSelectorDialog
import com.example.ui.components.FloatingVoiceSubtitleBar
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.UserProfileDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.AudioGuideManager

enum class AppDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Accueil", Icons.Default.Home),
    EQUIPMENT("Matériel", Icons.Default.Agriculture),
    RENTALS("Locations", Icons.Default.Assignment),
    PRODUCE("Récoltes", Icons.Default.Grass),
    COMPOST("Hub Compost", Icons.Default.Recycling)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriShopApp(
    viewModel: AgriViewModel
) {
    val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var showAiScreen by remember { mutableStateOf(false) }
    var showMapScreen by remember { mutableStateOf(false) }
    var showPaymentsScreen by remember { mutableStateOf(false) }
    var showWasteProgramScreen by remember { mutableStateOf(false) }
    var showAddListingModal by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAdvisorDialog by remember { mutableStateOf(false) }

    val equipmentList by viewModel.equipmentList.collectAsStateWithLifecycle()
    val produceList by viewModel.produceList.collectAsStateWithLifecycle()
    val compostList by viewModel.compostList.collectAsStateWithLifecycle()
    val wasteRequests by viewModel.wasteRequests.collectAsStateWithLifecycle()
    val rentalContracts by viewModel.rentalContracts.collectAsStateWithLifecycle()
    val paymentTransactions by viewModel.paymentTransactions.collectAsStateWithLifecycle()
    val forumPosts by viewModel.forumPosts.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val ecoPoints by viewModel.userEcoPoints.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val currentCity by viewModel.currentCity.collectAsStateWithLifecycle()
    val userCoordinates by viewModel.userCoordinates.collectAsStateWithLifecycle()

    val activeContractsCount = rentalContracts.count { it.status.name == "ACTIVE" }

    // If user is not authenticated, show Firebase Auth Screen
    if (!isUserAuthenticated) {
        AuthScreen(
            isLoading = authLoading,
            errorMessage = authError,
            onSignInWithEmail = { email, pass ->
                viewModel.signInWithEmail(email, pass)
            },
            onSignUpWithEmail = { email, pass, name, phone, role, reg ->
                viewModel.signUpWithEmail(email, pass, name, phone, role, reg)
            },
            onSignInWithGoogle = { token, email, name, photo ->
                viewModel.signInWithGoogle(token, email, name, photo)
            },
            onGuestLogin = {
                viewModel.guestLogin()
            },
            onResetPassword = { email ->
                viewModel.sendPasswordReset(email)
            }
        )
        return
    }

    if (showAiScreen) {
        AgriAiAssistantScreen(
            onNavigateBack = { showAiScreen = false }
        )
    } else if (showMapScreen) {
        InteractiveMapScreen(
            equipmentList = equipmentList,
            produceList = produceList,
            compostList = compostList,
            userCoordinates = userCoordinates,
            currentCity = currentCity,
            onNavigateBack = { showMapScreen = false },
            onBookEquipment = { eq, name, phone, days, op, prov, payPhone ->
                viewModel.bookEquipment(eq, name, phone, days, op, prov, payPhone)
            },
            onPurchaseEquipment = { eq, prov, phone ->
                viewModel.purchaseEquipment(eq, prov, phone)
            },
            onBuyProduce = { prod, qty, prov, phone ->
                viewModel.buyProduce(prod, qty, prov, phone)
            },
            onBuyCompost = { comp, qty, prov, phone ->
                viewModel.buyCompost(comp, qty, prov, phone)
            }
        )
    } else if (showPaymentsScreen) {
        PaymentsHistoryScreen(
            transactions = paymentTransactions,
            onNavigateBack = { showPaymentsScreen = false }
        )
    } else if (showWasteProgramScreen) {
        WasteCollectionProgramScreen(
            wasteRequests = wasteRequests,
            userProfile = userProfile,
            userCoordinates = userCoordinates,
            currentCity = currentCity,
            onNavigateBack = { showWasteProgramScreen = false },
            onDeclareWaste = { name, phone, loc, type, wt, date, slot, mode, notes ->
                viewModel.declareWasteCollection(name, phone, loc, type, wt, date, slot, mode, notes)
            },
            onUpdateStatus = { reqId, status ->
                viewModel.updateWasteCollectionStatus(reqId, status)
            },
            onCancelRequest = { reqId ->
                viewModel.cancelWasteCollection(reqId)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.agrishop_logo),
                                        contentDescription = "AgriShop Logo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "AgriShop",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            letterSpacing = 0.3.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "CI",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Agri-Tech & Écologie",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        // Voice Screen Narrator & Dialect Shortcut Button
                        val selectedAudioLang by AudioGuideManager.selectedLanguage.collectAsStateWithLifecycle()
                        val isAudioSpeaking by AudioGuideManager.isSpeaking.collectAsStateWithLifecycle()

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAudioSpeaking) AmberLight else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (isAudioSpeaking) {
                                        AudioGuideManager.stop()
                                    } else {
                                        AudioGuideManager.speakScreenExplanation(currentDestination.name)
                                    }
                                }
                                .testTag("topbar_voice_screen_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAudioSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                    contentDescription = "Écouter la page en audio",
                                    tint = if (isAudioSpeaking) ForestGreenDark else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedAudioLang.flag,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // AI Assistant Button (highlighted as requested: "sauf l'ia")
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showAiScreen = true }
                                .testTag("topbar_ai_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Assistant IA",
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "IA Agri",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = ForestGreenPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // 3-Bar Menu Button (Hamburger / Overflow Menu)
                        Box {
                            IconButton(
                                onClick = { showTopMenu = true },
                                modifier = Modifier.testTag("topbar_menu_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationsCount > 0) {
                                            Badge(
                                                containerColor = Color(0xFFFF5252),
                                                contentColor = Color.White
                                            ) {
                                                Text("$unreadNotificationsCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu des Options",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { showTopMenu = false },
                                modifier = Modifier
                                    .width(270.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                // Header summary in menu
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "Menu & Services",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = ForestGreenPrimary
                                                )
                                            )
                                            Text(
                                                text = "Région : $currentCity • $ecoPoints pts",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    },
                                    onClick = { },
                                    enabled = false
                                )
                                HorizontalDivider()

                                // 0. Mode Facile / Voix
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Mode Facile & Audio", fontWeight = FontWeight.SemiBold)
                                            Surface(
                                                color = AmberLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "VOIX 🇨🇮",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = ForestGreenDark,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        showLanguageDialog = true
                                    }
                                )

                                // 0.1 Conseiller de Zone Direct
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PhoneInTalk,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = {
                                        Text("Appeler un Conseiller 📞", fontWeight = FontWeight.SemiBold)
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        showAdvisorDialog = true
                                    }
                                )

                                HorizontalDivider()

                                // 1. Interactive GPS Map
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Carte GPS Interactive", fontWeight = FontWeight.SemiBold)
                                            Surface(
                                                color = MintLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "GPS",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = ForestGreenPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        showMapScreen = true
                                    }
                                )

                                // 2. Programme Collecte Déchets
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Recycling,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Programme Éco-Collecte", fontWeight = FontWeight.SemiBold)
                                            Surface(
                                                color = MintLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "${wasteRequests.size}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = ForestGreenPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        showWasteProgramScreen = true
                                    }
                                )

                                // 2. Notifications with unread badge
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Notifications", fontWeight = FontWeight.SemiBold)
                                            if (unreadNotificationsCount > 0) {
                                                Surface(
                                                    color = Color(0xFFE53935),
                                                    shape = CircleShape
                                                ) {
                                                    Text(
                                                        text = "$unreadNotificationsCount",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        showNotificationsSheet = true
                                    }
                                )

                                // 3. Mon Profil Agricole
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = { Text("Mon Profil Agricole", fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        showTopMenu = false
                                        showProfileDialog = true
                                    }
                                )

                                // 4. Historique des Paiements
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = { Text("Paiements & Reçus", fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        showTopMenu = false
                                        showPaymentsScreen = true
                                    }
                                )

                                // 5. Changer de Ville GPS
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = {
                                        Column {
                                            Text("Changer Région GPS", fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "Actuelle : $currentCity",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        showProfileDialog = true
                                    }
                                )

                                // 6. Solde Éco-Points
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Eco,
                                            contentDescription = null,
                                            tint = ForestGreenDark
                                        )
                                    },
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Mes Éco-Points", fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "$ecoPoints pts",
                                                color = ForestGreenDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        currentDestination = AppDestination.COMPOST
                                    }
                                )

                                HorizontalDivider()

                                // 7. Synchronisation Réseau / Hors-ligne
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary
                                        )
                                    },
                                    text = {
                                        Column {
                                            Text(
                                                if (isSyncing) "Synchronisation en cours..." else "Synchroniser les données",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "Synchro : $lastSyncTime",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    },
                                    onClick = {
                                        showTopMenu = false
                                        viewModel.syncNetworkData()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ForestGreenPrimary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    AppDestination.entries.forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ForestGreenPrimary,
                                selectedTextColor = ForestGreenPrimary,
                                indicatorColor = MintLight
                            ),
                            modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentDestination) {
                    AppDestination.HOME -> {
                        HomeScreen(
                            equipmentList = equipmentList,
                            produceList = produceList,
                            compostList = compostList,
                            activeContractsCount = activeContractsCount,
                            userCoordinates = userCoordinates,
                            currentCity = currentCity,
                            onCityChangeClick = { showProfileDialog = true },
                            onNavigateToEquipment = { currentDestination = AppDestination.EQUIPMENT },
                            onNavigateToRentals = { currentDestination = AppDestination.RENTALS },
                            onNavigateToProduce = { currentDestination = AppDestination.PRODUCE },
                            onNavigateToCompost = { currentDestination = AppDestination.COMPOST },
                            onNavigateToWasteProgram = { showWasteProgramScreen = true },
                            onNavigateToAi = { showAiScreen = true },
                            onNavigateToPayments = { showPaymentsScreen = true },
                            onNavigateToMap = { showMapScreen = true },
                            onOpenEquipmentDetail = { currentDestination = AppDestination.EQUIPMENT },
                            onOpenCompostDetail = { currentDestination = AppDestination.COMPOST },
                            onOpenProduceDetail = { currentDestination = AppDestination.PRODUCE },
                            onOpenAdvisorCall = { showAdvisorDialog = true },
                            onOpenLanguageDialog = { showLanguageDialog = true }
                        )
                    }

                    AppDestination.EQUIPMENT -> {
                        EquipmentScreen(
                            equipmentList = equipmentList,
                            userCoordinates = userCoordinates,
                            onAddListingClick = { showAddListingModal = true },
                            onBookEquipment = { eq, name, phone, days, op, prov, payPhone ->
                                viewModel.bookEquipment(eq, name, phone, days, op, prov, payPhone)
                            },
                            onPurchaseEquipment = { eq, prov, phone ->
                                viewModel.purchaseEquipment(eq, prov, phone)
                            }
                        )
                    }

                    AppDestination.RENTALS -> {
                        RentalsManagementScreen(
                            contracts = rentalContracts,
                            onReturnEquipment = { contractId, equipmentId ->
                                viewModel.returnEquipment(contractId, equipmentId)
                            },
                            onNavigateToEquipment = { currentDestination = AppDestination.EQUIPMENT }
                        )
                    }

                    AppDestination.PRODUCE -> {
                        ProduceNetworkScreen(
                            produceList = produceList,
                            forumPosts = forumPosts,
                            userCoordinates = userCoordinates,
                            onAddProduceClick = { showAddListingModal = true },
                            onAddForumPost = { viewModel.addForumPost(it) },
                            onBuyProduce = { prod, qty, prov, phone ->
                                viewModel.buyProduce(prod, qty, prov, phone)
                            }
                        )
                    }

                    AppDestination.COMPOST -> {
                        GreenCompostScreen(
                            compostList = compostList,
                            wasteRequests = wasteRequests,
                            userCoordinates = userCoordinates,
                            onRequestPickup = { name, phone, loc, type, wt, date, notes ->
                                viewModel.requestWastePickup(name, phone, loc, type, wt, date, notes)
                            },
                            onBuyCompost = { comp, qty, prov, phone ->
                                viewModel.buyCompost(comp, qty, prov, phone)
                            },
                            onNavigateToAi = { showAiScreen = true },
                            onOpenWasteProgram = { showWasteProgramScreen = true },
                            onUpdateStatus = { reqId, status ->
                                viewModel.updateWasteCollectionStatus(reqId, status)
                            },
                            onCancelRequest = { reqId ->
                                viewModel.cancelWasteCollection(reqId)
                            }
                        )
                    }
                }

                // Live Floating Audio Subtitles overlay for selected local dialect
                FloatingVoiceSubtitleBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                )
            }
        }
    }

    if (showAddListingModal) {
        AddListingDialog(
            onDismiss = { showAddListingModal = false },
            onAddEquipment = { viewModel.addEquipment(it) },
            onAddProduce = { viewModel.addProduce(it) },
            onAddCompost = { viewModel.addCompost(it) }
        )
    }

    if (showProfileDialog) {
        UserProfileDialog(
            user = userProfile,
            isSyncing = isSyncing,
            lastSyncTime = lastSyncTime,
            onDismiss = { showProfileDialog = false },
            onSaveProfile = { name, phone, email, role, region ->
                viewModel.saveUserProfile(name, phone, email, role, region)
            },
            onSyncNow = {
                viewModel.syncNetworkData()
            },
            onLogout = {
                viewModel.logout()
            }
        )
    }

    if (showNotificationsSheet) {
        NotificationsSheet(
            notifications = notifications,
            onDismiss = { showNotificationsSheet = false },
            onNotificationClick = { notif ->
                viewModel.markNotificationAsRead(notif)
            },
            onMarkAllRead = {
                viewModel.markAllNotificationsAsRead()
            }
        )
    }

    if (showLanguageDialog) {
        AudioLanguageSelectorDialog(
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showAdvisorDialog) {
        AdvisorCallDialog(
            onDismiss = { showAdvisorDialog = false }
        )
    }
}
