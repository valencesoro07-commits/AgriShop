package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.DistanceBadge
import com.example.ui.components.EcoBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteCollectionProgramScreen(
    wasteRequests: List<WasteCollectionRequest>,
    userProfile: UserProfile,
    userCoordinates: Pair<Double, Double>,
    currentCity: String,
    onNavigateBack: () -> Unit,
    onDeclareWaste: (name: String, phone: String, location: String, wasteType: WasteType, weightKg: Int, date: String, slot: String, mode: String, notes: String) -> Unit,
    onUpdateStatus: (requestId: String, newStatus: String) -> Unit,
    onCancelRequest: (requestId: String) -> Unit
) {
    val context = LocalContext.current
    var selectedMainTab by remember { mutableStateOf(0) } // 0: Historique & Suivi, 1: Déclarer des Déchets, 2: Guide & Impact
    var historyFilter by remember { mutableStateOf("TOUT") } // TOUT, EN_ATTENTE, EN_COURS, COLLECTEE
    var selectedRequestForReceipt by remember { mutableStateOf<WasteCollectionRequest?>(null) }
    var selectedRequestForStatusChange by remember { mutableStateOf<WasteCollectionRequest?>(null) }

    // Declaration Form State
    var farmerNameInput by remember { mutableStateOf(userProfile.fullName) }
    var farmerPhoneInput by remember { mutableStateOf(userProfile.phone.ifBlank { "+225 07 " }) }
    var locationInput by remember { mutableStateOf(currentCity.ifBlank { userProfile.region }) }
    var selectedWasteType by remember { mutableStateOf(WasteType.COCOA_PODS) }
    var wasteWeightKgStr by remember { mutableStateOf("2500") }
    var pickupDateStr by remember { mutableStateOf("26 Août 2026") }
    var selectedSlot by remember { mutableStateOf("Matinée (08h00 - 12h00)") }
    var selectedMode by remember { mutableStateOf("Enlèvement Camion Benne 5T (AgriShop)") }
    var notesInput by remember { mutableStateOf("") }
    var declarationSuccessMessage by remember { mutableStateOf<String?>(null) }

    val weight = wasteWeightKgStr.toIntOrNull() ?: 2500
    val calculatedEcoPoints = remember(weight, selectedWasteType, selectedMode) {
        val base = (weight * selectedWasteType.carbonRate * 0.15).toInt().coerceAtLeast(15)
        val bonus = if (selectedMode.contains("Dépôt Direct", ignoreCase = true)) (base * 0.2).toInt() else 0
        base + bonus
    }
    val calculatedCo2Saved = (weight * 0.75).toInt()
    val calculatedCompostYield = (weight * 0.42).toInt()

    val pendingCount = wasteRequests.count { it.status == "EN_ATTENTE" }
    val inProgressCount = wasteRequests.count { it.status == "PLANIFIEE" || it.status == "EN_COURS" }
    val completedCount = wasteRequests.count { it.status == "COLLECTEE" }
    val totalWeightRecycledKg = wasteRequests.sumOf { it.weightKg.toLong() }

    val filteredRequests = remember(wasteRequests, historyFilter) {
        when (historyFilter) {
            "EN_ATTENTE" -> wasteRequests.filter { it.status == "EN_ATTENTE" }
            "EN_COURS" -> wasteRequests.filter { it.status == "PLANIFIEE" || it.status == "EN_COURS" }
            "COLLECTEE" -> wasteRequests.filter { it.status == "COLLECTEE" }
            else -> wasteRequests
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Programme Éco-Collecte",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Valorisation & Historique des Déchets Organiques",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = HarvestGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile.ecoPoints} Pts",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestGreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Impact Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.eco_compost_banner),
                    contentDescription = "Compost Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x880E3A1A), Color(0xF007200D))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MintLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Recycling,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Économie Circulaire Zéro-Brûlis",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenDark,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "Côte d'Ivoire",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Key Stat Highlights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ImpactMetricChip(
                            label = "Biomasse Suivie",
                            value = "${totalWeightRecycledKg / 1000} T",
                            icon = Icons.Default.Scale
                        )
                        ImpactMetricChip(
                            label = "En Cours / Planifiées",
                            value = "$inProgressCount",
                            icon = Icons.Default.LocalShipping
                        )
                        ImpactMetricChip(
                            label = "À Effectuer",
                            value = "$pendingCount",
                            icon = Icons.Default.HourglassTop
                        )
                        ImpactMetricChip(
                            label = "Collectées",
                            value = "$completedCount",
                            icon = Icons.Default.CheckCircle
                        )
                    }
                }
            }

            // Tab Navigation Row
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ForestGreenPrimary
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Historique (${wasteRequests.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Déclarer Déchets", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedMainTab == 2,
                    onClick = { selectedMainTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guide Pratique", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }

            when (selectedMainTab) {
                0 -> {
                    // TAB 0: HISTORIQUE ET SUIVI DES COLLECTES
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Filter Chips Row
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = historyFilter == "TOUT",
                                    onClick = { historyFilter = "TOUT" },
                                    label = { Text("Toutes (${wasteRequests.size})") },
                                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = historyFilter == "EN_ATTENTE",
                                    onClick = { historyFilter = "EN_ATTENTE" },
                                    label = { Text("⏳ À effectuer ($pendingCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF9800),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = historyFilter == "EN_COURS",
                                    onClick = { historyFilter = "EN_COURS" },
                                    label = { Text("🚚 En cours / Planifiées ($inProgressCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1976D2),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = historyFilter == "COLLECTEE",
                                    onClick = { historyFilter = "COLLECTEE" },
                                    label = { Text("✅ Collectées ($completedCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (filteredRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inbox,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Aucune collecte dans cette catégorie",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Déclarez vos résidus organiques pour programmer le passage d'un camion benne partenaire.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { selectedMainTab = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Faire une Déclaration")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Suivi en Temps Réel des Tournées",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ForestGreenDark
                                        )
                                        Text(
                                            text = "${filteredRequests.size} collecte(s)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                items(filteredRequests, key = { it.id }) { req ->
                                    WasteCollectionDetailedCard(
                                        request = req,
                                        userCoordinates = userCoordinates,
                                        onCallContact = { phone ->
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                            context.startActivity(intent)
                                        },
                                        onChangeStatusClick = {
                                            selectedRequestForStatusChange = req
                                        },
                                        onViewReceiptClick = {
                                            selectedRequestForReceipt = req
                                        },
                                        onCancelClick = {
                                            onCancelRequest(req.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: FORMULAIRE DE DECLARATION DE DECHETS ORGANIQUES
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Intro Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MintLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EnergySavingsLeaf,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Déclaration & Valorisation Gratuite",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ForestGreenDark
                                    )
                                    Text(
                                        text = "Transformez vos sous-produits de récolte en engrais et gagnez des Éco-Points crédités immédiatement.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ForestGreenDark.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        if (declarationSuccessMessage != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MintLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenPrimary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ForestGreenPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Déclaration Enregistrée avec Succès !",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ForestGreenDark
                                        )
                                        Text(
                                            text = declarationSuccessMessage!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ForestGreenDark
                                        )
                                    }
                                }
                            }
                        }

                        // STEP 1: TYPE DE DÉCHET
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ForestGreenPrimary,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Nature des Déchets Organiques",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "Sélectionnez la matière première issue de vos parcelles ou de votre élevage :",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                WasteType.entries.forEach { type ->
                                    val isSelected = selectedWasteType == type
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MintLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreenPrimary) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedWasteType = type }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = type.icon, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = type.label,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) ForestGreenDark else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                Text(
                                                    text = "Taux d'enrichissement Carbone/Azote : ${(type.carbonRate * 100).toInt()}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedWasteType = type },
                                                colors = RadioButtonDefaults.colors(selectedColor = ForestGreenPrimary)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 2: QUANTITÉ & ESTIMATION IMPACT
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ForestGreenPrimary,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Quantité & Estimation d'Éco-Gains",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = wasteWeightKgStr,
                                    onValueChange = { wasteWeightKgStr = it },
                                    label = { Text("Poids estimé disponible (en kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    trailingIcon = { Text("kg  ", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick presets
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("500 kg", "1 200 kg", "2 500 kg", "5 000 kg", "10 000 kg").forEach { preset ->
                                        val cleanNum = preset.replace(" ", "").replace("kg", "")
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (wasteWeightKgStr == cleanNum) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { wasteWeightKgStr = cleanNum }
                                        ) {
                                            Text(
                                                text = preset,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (wasteWeightKgStr == cleanNum) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Live Gain Calculation
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MintLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Éco-Points Gagnés", style = MaterialTheme.typography.labelSmall, color = ForestGreenDark)
                                                Text(
                                                    text = "+$calculatedEcoPoints Pts",
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = ForestGreenPrimary
                                                    )
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("CO2e Évité", style = MaterialTheme.typography.labelSmall, color = ForestGreenDark)
                                                Text(
                                                    text = "-$calculatedCo2Saved kg",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = ForestGreenDark
                                                    )
                                                )
                                            }
                                        }
                                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = ForestGreenPrimary.copy(alpha = 0.2f))
                                        Text(
                                            text = "🪴 Produira environ ~$calculatedCompostYield kg de compost organique pour enrichir les sols locaux.",
                                            style = MaterialTheme.typography.labelSmall.copy(color = ForestGreenDark, fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            }
                        }

                        // STEP 3: LOGISTIQUE, DATE & LOCALISATION
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ForestGreenPrimary,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Logistique & Coordonnées d'Enlèvement",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = farmerNameInput,
                                    onValueChange = { farmerNameInput = it },
                                    label = { Text("Nom du Contact / Coopérative") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = farmerPhoneInput,
                                    onValueChange = { farmerPhoneInput = it },
                                    label = { Text("Téléphone (pour coordination chauffeur)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = locationInput,
                                    onValueChange = { locationInput = it },
                                    label = { Text("Ville / Village / Repère de la parcelle") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Date & Slot
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = pickupDateStr,
                                        onValueChange = { pickupDateStr = it },
                                        label = { Text("Date d'enlèvement") },
                                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Créneau horaire préféré :", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Matinée (08h00 - 12h00)", "Après-midi (14h00 - 18h00)").forEach { slot ->
                                        val isSlotSelected = selectedSlot == slot
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSlotSelected) MintLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = if (isSlotSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreenPrimary) else null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { selectedSlot = slot }
                                        ) {
                                            Text(
                                                text = slot,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSlotSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSlotSelected) ForestGreenDark else MaterialTheme.colorScheme.onSurface
                                                ),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Mode de collecte :", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        "Enlèvement Camion Benne 5T (AgriShop)" to "Un camion vient sur votre parcelle",
                                        "Dépôt Direct au Centre de Compostage" to "Bonus +20% Éco-Points supplémentaires !"
                                    ).forEach { (mode, desc) ->
                                        val isModeSelected = selectedMode.contains(mode.take(15))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isModeSelected) MintLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = if (isModeSelected) androidx.compose.foundation.BorderStroke(1.dp, ForestGreenPrimary) else null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { selectedMode = mode }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isModeSelected,
                                                    onClick = { selectedMode = mode },
                                                    colors = RadioButtonDefaults.colors(selectedColor = ForestGreenPrimary)
                                                )
                                                Column {
                                                    Text(
                                                        text = mode,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isModeSelected) ForestGreenDark else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    )
                                                    Text(
                                                        text = desc,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (desc.contains("Bonus")) ForestGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = notesInput,
                                    onValueChange = { notesInput = it },
                                    label = { Text("Précisions d'accès (ex: Piste carrossable, meules au bord du champ)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        // SUBMIT BUTTON
                        Button(
                            onClick = {
                                if (farmerNameInput.isNotBlank()) {
                                    val safeWeight = wasteWeightKgStr.toIntOrNull() ?: 2000
                                    onDeclareWaste(
                                        farmerNameInput,
                                        farmerPhoneInput,
                                        locationInput,
                                        selectedWasteType,
                                        safeWeight,
                                        pickupDateStr,
                                        selectedSlot,
                                        selectedMode,
                                        notesInput
                                    )
                                    declarationSuccessMessage = "Collecte de $safeWeight kg de ${selectedWasteType.label} programmée. Vous gagnez +$calculatedEcoPoints Éco-Points !"
                                    notesInput = ""
                                    selectedMainTab = 0 // Switch to history to view newly added request
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_waste_declaration_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirmer la Déclaration (+ $calculatedEcoPoints Pts)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                2 -> {
                    // TAB 2: GUIDE PRATIQUE & BONNES PRATIQUES D'ÉCO-VALORISATION
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircleOutline, contentDescription = null, tint = ForestGreenPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pourquoi valoriser au lieu de brûler ?",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Le brûlis détruit la matière organique superficielle du sol, tue les micro-organismes bénéfiques et dégage d'épaisses fumées polluantes.\n\n" +
                                            "En confiant vos résidus (cabosses, paille, fientes) au réseau AgriShop :\n" +
                                            "• Vous évitez jusqu'à 0.9 kg de CO2 par kg de déchet végétal.\n" +
                                            "• Vous recevez des Éco-Points convertibles en sacs de compost riche ou en réductions sur la location de tracteurs.\n" +
                                            "• Vous contribuez à restaurer la fertilité des terres ivoiriennes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MintLight)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Conseils de Préparation avant le Passage du Camion",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ForestGreenDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                PracticalStepRow(number = "1", title = "Regroupement en bordure de piste", description = "Rassemblez les meules de paille ou cabosses à un endroit accessible pour un camion benne ou triporteur.")
                                PracticalStepRow(number = "2", title = "Matières pures et saines", description = "Veillez à ne pas mélanger avec des plastiques, métaux, ou pierres qui abîmeraient les broyeurs.")
                                PracticalStepRow(number = "3", title = "Conservation de l'humidité", description = "Pour les cabosses, un stockage de moins de 15 jours après écabossage garantit un compostage optimal.")
                            }
                        }

                        Button(
                            onClick = { selectedMainTab = 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Déclarer une Collecte Maintenant")
                        }
                    }
                }
            }
        }
    }

    // Modal / BottomSheet: Visualiser le Bordereau Officiel de Traçabilité Verte
    if (selectedRequestForReceipt != null) {
        val req = selectedRequestForReceipt!!
        ModalBottomSheet(
            onDismissRequest = { selectedRequestForReceipt = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bordereau de Traçabilité Verte",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ForestGreenDark
                        )
                        Text(
                            text = "Certificat Officiel AgriShop CI • ${req.trackingCode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintLight
                    ) {
                        Text(
                            text = req.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReceiptRowItem(label = "Exploitant / Producteur", value = req.farmerName)
                        ReceiptRowItem(label = "Contact Téléphonique", value = req.farmerPhone)
                        ReceiptRowItem(label = "Parcelle & Localisation", value = req.location)
                        ReceiptRowItem(label = "Type de Biomasse", value = req.wasteType.label)
                        ReceiptRowItem(label = "Poids Déclaré", value = "${req.weightKg} kg (${req.weightKg / 1000.0} T)")
                        ReceiptRowItem(label = "Date & Créneau de Passage", value = "${req.pickupDate} • ${req.pickupSlot}")
                        ReceiptRowItem(label = "Transporteur Assigné", value = "${req.assignedDriver} (${req.driverPhone})")
                        ReceiptRowItem(label = "Véhicule Mobilisé", value = req.vehicleType)
                        ReceiptRowItem(label = "Mode d'Acheminement", value = req.pickupMode)
                        ReceiptRowItem(label = "Impact Carbone Évité", value = "-${req.co2SavedKg.toInt()} kg CO2e")
                        ReceiptRowItem(label = "Crédit Éco-Points Attribué", value = "+${req.rewardEcoPoints} Éco-Points")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${req.driverPhone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Appeler Chauffeur")
                    }

                    Button(
                        onClick = { selectedRequestForReceipt = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }

    // Modal: Modifier / Mettre à jour le statut d'une collecte
    if (selectedRequestForStatusChange != null) {
        val req = selectedRequestForStatusChange!!
        AlertDialog(
            onDismissRequest = { selectedRequestForStatusChange = null },
            title = {
                Text(
                    text = "Mettre à jour le statut de la collecte",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Collecte #${req.id} • ${req.farmerName} (${req.weightKg} kg de ${req.wasteType.label})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    listOf(
                        "EN_ATTENTE" to "⏳ À effectuer (En attente d'affectation)",
                        "PLANIFIEE" to "📅 Planifiée (Chauffeur et créneau fixés)",
                        "EN_COURS" to "🚚 En cours d'enlèvement (Camion en route)",
                        "COLLECTEE" to "✅ Collectée & Revalorisée en Compost (+Bonus)",
                        "ANNULEE" to "❌ Annulée"
                    ).forEach { (statusKey, label) ->
                        val isCurrent = req.status == statusKey
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) MintLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, ForestGreenPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateStatus(req.id, statusKey)
                                    selectedRequestForStatusChange = null
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) ForestGreenDark else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRequestForStatusChange = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun WasteCollectionDetailedCard(
    request: WasteCollectionRequest,
    userCoordinates: Pair<Double, Double>,
    onCallContact: (phone: String) -> Unit,
    onChangeStatusClick: () -> Unit,
    onViewReceiptClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val statusInfo = when (request.status) {
        "EN_ATTENTE" -> Triple("À effectuer", Color(0xFFFF9800), Icons.Default.HourglassTop)
        "PLANIFIEE" -> Triple("Planifiée", Color(0xFF1976D2), Icons.Default.Event)
        "EN_COURS" -> Triple("En transit / Enlèvement", Color(0xFF7B1FA2), Icons.Default.LocalShipping)
        "COLLECTEE" -> Triple("Collectée & Recyclée", ForestGreenPrimary, Icons.Default.CheckCircle)
        else -> Triple(request.status, Color.Gray, Icons.Default.Info)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type Badge + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MintLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = request.wasteType.icon, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = request.wasteType.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusInfo.second.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusInfo.third,
                            contentDescription = null,
                            tint = statusInfo.second,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusInfo.first,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusInfo.second
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.farmerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = request.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DistanceBadge(
                    itemLat = request.latitude,
                    itemLng = request.longitude,
                    userLat = userCoordinates.first,
                    userLng = userCoordinates.second
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stepper timeline
            CollectionProgressStepper(status = request.status)

            Spacer(modifier = Modifier.height(8.dp))

            // Key Metrics Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Volume estimé", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${request.weightKg} kg",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                        )
                    }
                    Column {
                        Text("Date de passage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = request.pickupDate,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Éco-Points", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "+${request.rewardEcoPoints} Pts",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = ForestGreenPrimary)
                        )
                    }
                }
            }

            // Transport info
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${request.assignedDriver} • ${request.vehicleType}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (request.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes : ${request.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onCallContact(request.farmerPhone) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Appeler", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onViewReceiptClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bordereau", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onChangeStatusClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mettre à jour", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CollectionProgressStepper(status: String) {
    val currentStep = when (status) {
        "EN_ATTENTE" -> 1
        "PLANIFIEE" -> 2
        "EN_COURS" -> 3
        "COLLECTEE" -> 4
        else -> 1
    }

    val steps = listOf("Déclarée", "Planifiée", "En route", "Collectée")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, stepName ->
            val stepNumber = index + 1
            val isDone = stepNumber <= currentStep
            val isCurrent = stepNumber == currentStep

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDone) ForestGreenPrimary else Color.LightGray.copy(alpha = 0.6f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone && stepNumber < currentStep) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    } else {
                        Text(
                            text = "$stepNumber",
                            color = if (isDone) Color.White else Color.DarkGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = stepName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isDone) ForestGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 2.dp)
                        .background(if (stepNumber < currentStep) ForestGreenPrimary else Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
fun ImpactMetricChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = AmberSun, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 9.sp
            )
        )
    }
}

@Composable
fun PracticalStepRow(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(ForestGreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ForestGreenDark)
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = ForestGreenDark.copy(alpha = 0.85f))
        }
    }
}

@Composable
fun ReceiptRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
