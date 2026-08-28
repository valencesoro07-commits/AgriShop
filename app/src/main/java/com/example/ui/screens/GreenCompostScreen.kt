package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreenCompostScreen(
    compostList: List<CompostItem>,
    wasteRequests: List<WasteCollectionRequest>,
    userCoordinates: Pair<Double, Double>,
    onRequestPickup: (name: String, phone: String, location: String, wasteType: WasteType, weightKg: Int, date: String, notes: String) -> Unit,
    onBuyCompost: (item: CompostItem, quantity: Int, provider: PaymentProvider, phone: String) -> Unit,
    onNavigateToAi: () -> Unit,
    onOpenWasteProgram: () -> Unit = {},
    onUpdateStatus: (requestId: String, newStatus: String) -> Unit = { _, _ -> },
    onCancelRequest: (requestId: String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Boutique Compost, 1: Calculateur C/N, 2: Collecte Déchets

    // Purchase Compost Modal State
    var selectedCompostForBuy by remember { mutableStateOf<CompostItem?>(null) }
    var compostQuantity by remember { mutableStateOf(5) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    // Smart Compost Calculator State
    var brownWasteKgStr by remember { mutableStateOf("150") } // Paille, cabosses (Carbone)
    var greenWasteKgStr by remember { mutableStateOf("100") } // Fientes, fumier, maraîcher (Azote)
    var isFrequentTurning by remember { mutableStateOf(true) }

    val brownWeight = brownWasteKgStr.toDoubleOrNull() ?: 150.0
    val greenWeight = greenWasteKgStr.toDoubleOrNull() ?: 100.0
    val totalWaste = brownWeight + greenWeight
    val estimatedCnRatio = remember(brownWeight, greenWeight) {
        if (greenWeight <= 0) 60.0 else (brownWeight * 50.0 + greenWeight * 15.0) / (brownWeight * 1.0 + greenWeight * 1.0) / 2.2
    }
    val estimatedCompostYieldKg = (totalWaste * 0.42).toInt()
    val estimatedCo2PreventedKg = (totalWaste * 0.75).toInt()
    val estimatedWeeksToMature = if (isFrequentTurning) 8 else 14

    // Waste Request Form State
    var farmerNameInput by remember { mutableStateOf("") }
    var farmerPhoneInput by remember { mutableStateOf("+225 07 ") }
    var locationInput by remember { mutableStateOf("Yamoussoukro") }
    var selectedWasteType by remember { mutableStateOf(WasteType.COCOA_PODS) }
    var wasteWeightStr by remember { mutableStateOf("1500") }
    var pickupDateStr by remember { mutableStateOf("20 Août 2026") }
    var requestNotes by remember { mutableStateOf("") }
    var requestSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // Top Green Impact Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
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
                            colors = listOf(Color(0x770E3A1A), Color(0xEE092911))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MintLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Eco, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Économie Circulaire & Agroécologie", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Compost & Recyclage Vert",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                )
                Text(
                    text = "Valorisez 100% de la biomasse végétale en humus fertilisant sans brûlis.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD4E8DC))
                )
            }
        }

        // 3-Way Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🛒 Compost Bio") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("⚖️ Calculateur C/N") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("🚛 Collecte Résidus") }
            )
        }

        when (selectedTab) {
            0 -> { // Buy Compost Catalog
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
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
                                    Icon(imageVector = Icons.Default.EnergySavingsLeaf, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Composts Normés & Bio-Fertilisants",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ForestGreenDark
                                    )
                                    Text(
                                        text = "Remplacez les engrais chimiques onéreux par des fertilisants durables fabriqués localement.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ForestGreenDark.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    items(compostList, key = { it.id }) { item ->
                        CompostItemCard(
                            item = item,
                            userCoordinates = userCoordinates,
                            onClick = {
                                selectedCompostForBuy = item
                                compostQuantity = 2
                            }
                        )
                    }
                }
            }

            1 -> { // Smart C/N Compost Calculator
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
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
                                Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = ForestGreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simulateur de Recette de Compostage",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "Ajustez vos quantités de déchets bruns (riches en Carbone) et verts (riches en Azote) pour obtenir le compost parfait.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Brown Waste
                            OutlinedTextField(
                                value = brownWasteKgStr,
                                onValueChange = { brownWasteKgStr = it },
                                label = { Text("🍂 Matières Brunes / Carbone (kg)") },
                                placeholder = { Text("Paille de maïs/riz, cabosses de cacao, feuilles mortes") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Green Waste
                            OutlinedTextField(
                                value = greenWasteKgStr,
                                onValueChange = { greenWasteKgStr = it },
                                label = { Text("🌿 Matières Vertes / Azote (kg)") },
                                placeholder = { Text("Fumier, fientes de poulet, épluchures de manioc, déchets maraîchers") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isFrequentTurning,
                                    onCheckedChange = { isFrequentTurning = it }
                                )
                                Text(
                                    text = "Aération & retournement régulier (tous les 12 jours)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Calculation Results Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MintSurface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreenPrimary)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Résultats Prédictifs de Transformation",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ForestGreenDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Ratio C/N Estimé", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${estimatedCnRatio.toInt()}:1",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (estimatedCnRatio in 25.0..35.0) ForestGreenPrimary else HarvestGold
                                        )
                                    )
                                    Text(
                                        text = if (estimatedCnRatio in 25.0..35.0) "✅ Équilibre Optimal !" else "⚠️ Ajuster les proportions",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Production Finale", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "~$estimatedCompostYieldKg kg",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ForestGreenDark
                                        )
                                    )
                                    Text(
                                        text = "soit ${(estimatedCompostYieldKg / 50).coerceAtLeast(1)} sacs de 50kg",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = ForestGreenPrimary.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Temps de Maturation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$estimatedWeeksToMature Semaines",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Émissions CO2e Évitées", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "-$estimatedCo2PreventedKg kg CO2e",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onNavigateToAi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimiser ma recette avec l'IA Agronome")
                    }
                }
            }

            2 -> { // Waste Collection Request Form & History
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenWasteProgram() },
                            colors = CardDefaults.cardColors(containerColor = MintLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(ForestGreenPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Programme Éco-Collecte Déchets",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ForestGreenDark
                                        )
                                        Text(
                                            text = "Voir l'historique complet, les camions en cours et déclarer",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ForestGreenDark.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                                Button(
                                    onClick = onOpenWasteProgram,
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Ouvrir", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Demande d'Enlèvement de Biomasse Organique",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Ne brûlez plus vos résidus ! Nos camions partenaires collectent vos pailles et cabosses pour les valoriser en compost.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = farmerNameInput,
                                    onValueChange = { farmerNameInput = it },
                                    label = { Text("Votre Nom / Coopérative") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = farmerPhoneInput,
                                    onValueChange = { farmerPhoneInput = it },
                                    label = { Text("Numéro Téléphone") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = locationInput,
                                    onValueChange = { locationInput = it },
                                    label = { Text("Localisation de la parcelle") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = wasteWeightStr,
                                        onValueChange = { wasteWeightStr = it },
                                        label = { Text("Poids estimé (kg)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = pickupDateStr,
                                        onValueChange = { pickupDateStr = it },
                                        label = { Text("Date souhaitée") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (farmerNameInput.isNotBlank()) {
                                            val weight = wasteWeightStr.toIntOrNull() ?: 1000
                                            onRequestPickup(
                                                farmerNameInput,
                                                farmerPhoneInput,
                                                locationInput,
                                                selectedWasteType,
                                                weight,
                                                pickupDateStr,
                                                requestNotes
                                            )
                                            requestSuccessMessage = "Demande d'enlèvement enregistrée ! Vous avez gagné ${(weight * 0.1).toInt()} Éco-Points."
                                            farmerNameInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_waste_pickup_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Programmer la Collecte", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    if (requestSuccessMessage != null) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MintLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreenPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = requestSuccessMessage!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ForestGreenDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(title = "Demandes Récentes de Collecte")
                    }

                    items(wasteRequests, key = { it.id }) { req ->
                        WasteRequestCard(request = req)
                    }
                }
            }
        }
    }

    // Purchase Compost BottomSheet
    if (selectedCompostForBuy != null) {
        val comp = selectedCompostForBuy!!
        val calculatedTotal = comp.pricePerUnitCfa * compostQuantity

        ModalBottomSheet(
            onDismissRequest = { selectedCompostForBuy = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AppAsyncImage(
                        imageUrl = comp.imageUrl,
                        contentDescription = comp.title,
                        modifier = Modifier.fillMaxSize()
                    )

                    EcoBadge(
                        text = "Fertilisant Naturel Certifié",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )

                    DistanceBadge(
                        itemLat = comp.latitude,
                        itemLng = comp.longitude,
                        userLat = userCoordinates.first,
                        userLng = userCoordinates.second,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = comp.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Fournisseur : ${comp.producerName} • ${comp.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForestGreenPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MintSurface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Valeur Agronomique NPK : ${comp.npkRatio}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "Temps de maturation : ${comp.maturityWeeks} semaines en tas aéré", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Bilan Carbone : Évite ${comp.co2SavedKgPerUnit} kg de CO2e par ${comp.unit}", style = MaterialTheme.typography.bodySmall, color = ForestGreenDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity selector
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Nombre d'unités (${comp.unit}) :", style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (compostQuantity > 1) compostQuantity-- },
                                enabled = compostQuantity > 1
                            ) {
                                Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = null)
                            }
                            Text(
                                text = "$compostQuantity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { if (compostQuantity < comp.volumeAvailable) compostQuantity++ },
                                enabled = compostQuantity < comp.volumeAvailable
                            ) {
                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { showPaymentSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("buy_compost_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payer ${formatCfa(calculatedTotal)} via Mobile Money", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }

    // Payment Sheet for Compost
    if (showPaymentSheet && selectedCompostForBuy != null) {
        val comp = selectedCompostForBuy!!
        val calculatedTotal = comp.pricePerUnitCfa * compostQuantity

        PaymentBottomSheet(
            title = "Achat Compost Organique",
            purpose = "${comp.title} (Qté: $compostQuantity ${comp.unit})",
            totalAmountCfa = calculatedTotal,
            onDismiss = {
                showPaymentSheet = false
                selectedCompostForBuy = null
            },
            onPaymentConfirmed = { provider, phone ->
                onBuyCompost(comp, compostQuantity, provider, phone)
                com.example.data.model.PaymentTransaction(
                    id = "pay_comp",
                    transactionRef = "${provider.code}-COMP",
                    amountCfa = calculatedTotal,
                    feeCfa = if (provider == PaymentProvider.WAVE) (calculatedTotal * 0.01).toLong() else 0L,
                    provider = provider,
                    phoneNumber = phone,
                    purpose = comp.title,
                    receiptCode = "REC-COMP-${(1000..9999).random()}"
                )
            }
        )
    }
}

@Composable
fun CompostItemCard(
    item: CompostItem,
    userCoordinates: Pair<Double, Double>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("compost_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AppAsyncImage(
                    imageUrl = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize()
                )

                EcoBadge(
                    text = "-${item.co2SavedKgPerUnit.toInt()} kg CO2e / ${item.unit}",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                DistanceBadge(
                    itemLat = item.latitude,
                    itemLng = item.longitude,
                    userLat = userCoordinates.first,
                    userLng = userCoordinates.second,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintLight
                    ) {
                        Text(
                            text = "Stock : ${item.volumeAvailable} ${item.unit}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Formule NPK : ${item.npkRatio}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = EarthBrown
                )

                Text(
                    text = "${item.producerName} • ${item.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatCfa(item.pricePerUnitCfa),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreenPrimary
                            )
                        )
                        Text(
                            text = "/ ${item.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VoiceNarratorButton(
                            onSpeak = {
                                com.example.util.AudioGuideManager.speakCompost(
                                    name = item.title,
                                    priceCfa = item.pricePerUnitCfa,
                                    nitrogenRatio = item.npkRatio,
                                    co2Saved = item.co2SavedKgPerUnit
                                )
                            },
                            size = 36
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Commander", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WasteRequestCard(
    request: WasteCollectionRequest
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MintLight
                ) {
                    Text(
                        text = "${request.wasteType.icon} ${request.wasteType.label}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusInfo.second.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = statusInfo.third, contentDescription = null, tint = statusInfo.second, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusInfo.first,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = statusInfo.second)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${request.farmerName} • ${request.location}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Poids estimé : ${request.weightKg} kg (${request.weightKg / 1000.0} T) • Date : ${request.pickupDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚛 ${request.assignedDriver} • ${request.vehicleType}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+${request.rewardEcoPoints} Pts",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, color = ForestGreenPrimary)
                )
            }

            if (request.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note : ${request.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
