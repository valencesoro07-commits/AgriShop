package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(
    equipmentList: List<EquipmentItem>,
    userCoordinates: Pair<Double, Double>,
    onAddListingClick: () -> Unit,
    onBookEquipment: (equipment: EquipmentItem, renterName: String, renterPhone: String, days: Int, operator: Boolean, provider: PaymentProvider, paymentPhone: String) -> Unit,
    onPurchaseEquipment: (equipment: EquipmentItem, provider: PaymentProvider, buyerPhone: String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(EquipmentCategory.ALL) }
    var selectedOfferType by remember { mutableStateOf<OfferType?>(null) }
    var sortByProximity by remember { mutableStateOf(false) }
    var maxDistanceKm by remember { mutableStateOf<Double?>(null) } // null: any distance
    var selectedEquipmentForDetail by remember { mutableStateOf<EquipmentItem?>(null) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    // Rental booking temporary parameters
    var bookingDurationDays by remember { mutableStateOf(3) }
    var includeOperator by remember { mutableStateOf(true) }
    var isGroupBooking by remember { mutableStateOf(false) }
    var groupPartnersCount by remember { mutableStateOf(2) }
    var renterNameInput by remember { mutableStateOf("Mamadou Koné") }
    var renterPhoneInput by remember { mutableStateOf("+225 07 12 34 56") }

    var showRoiCalculator by remember { mutableStateOf(false) }
    var roiSelectedEquipment by remember { mutableStateOf<EquipmentItem?>(null) }

    val filteredList = remember(equipmentList, searchQuery, selectedCategory, selectedOfferType, sortByProximity, maxDistanceKm, userCoordinates) {
        var list = equipmentList.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true) ||
                    item.ownerName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == EquipmentCategory.ALL || item.category == selectedCategory
            val matchesOfferType = selectedOfferType == null || item.offerType == selectedOfferType || item.offerType == OfferType.BOTH

            val dist = GeoUtils.calculateDistanceKm(userCoordinates.first, userCoordinates.second, item.latitude, item.longitude)
            val matchesDistance = maxDistanceKm == null || dist <= maxDistanceKm!!

            matchesQuery && matchesCategory && matchesOfferType && matchesDistance
        }

        if (sortByProximity) {
            list = list.sortedBy { item ->
                GeoUtils.calculateDistanceKm(userCoordinates.first, userCoordinates.second, item.latitude, item.longitude)
            }
        }
        list
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddListingClick,
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .size(56.dp) // Reduced size
                    .testTag("add_equipment_fab"),
                shape = CircleShape // Circular shape for compactness
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Proposer du Matériel")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search & Filter Header - Reorganized and Compacted
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // 1. FILTERS (Tous + Tout) - AT THE VERY TOP
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedOfferType == null,
                            onClick = { selectedOfferType = null },
                            label = { Text("Tout", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedOfferType == OfferType.RENT,
                            onClick = { selectedOfferType = OfferType.RENT },
                            label = { Text("🚜 Louer", fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedOfferType == OfferType.SALE,
                            onClick = { selectedOfferType = OfferType.SALE },
                            label = { Text("🏷️ Vendre", fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    item {
                        VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 4.dp))
                    }
                    items(EquipmentCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. SEARCH BAR & ROI BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rechercher un engin...", fontSize = 14.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Effacer", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("equipment_search_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = LightSurfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = Color.White
                        )
                    )
                    
                    // ROI Button - Standard IconButton for reliability
                    IconButton(
                        onClick = { 
                            roiSelectedEquipment = null
                            showRoiCalculator = true 
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MintLight, CircleShape)
                            .border(1.dp, ForestGreenPrimary.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate, 
                            contentDescription = "Rentabilité", 
                            tint = ForestGreenPrimary, 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Proximity Toggle
                FilterChip(
                    selected = sortByProximity,
                    onClick = { sortByProximity = !sortByProximity },
                    label = { Text("📍 Machines les plus proches", fontSize = 11.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Equipment List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucun matériel ne correspond à vos critères",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedCategory = EquipmentCategory.ALL
                                selectedOfferType = null
                                sortByProximity = false
                                maxDistanceKm = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Text("Réinitialiser les filtres")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { equipment ->
                        EquipmentFullCard(
                            equipment = equipment,
                            userCoordinates = userCoordinates,
                            onClick = {
                                selectedEquipmentForDetail = equipment
                            }
                        )
                    }
                }
            }
        }
    }

    // Equipment Details BottomSheet
    if (selectedEquipmentForDetail != null) {
        val eq = selectedEquipmentForDetail!!
        val isRental = eq.offerType == OfferType.RENT || eq.offerType == OfferType.BOTH
        val operatorCostPerDay = if (includeOperator && eq.operatorAvailable) 10000L else 0L
        val calculatedTotal = if (isRental) {
            (eq.priceCfa + operatorCostPerDay) * bookingDurationDays + eq.depositCfa
        } else {
            eq.priceCfa
        }

        ModalBottomSheet(
            onDismissRequest = { selectedEquipmentForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AppAsyncImage(
                        imageUrl = eq.imageUrl,
                        contentDescription = eq.title,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        color = if (isRental) MintLight else AmberLight,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = eq.offerType.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isRental) ForestGreenDark else Color(0xFF8D5B00)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    DistanceBadge(
                        itemLat = eq.latitude,
                        itemLng = eq.longitude,
                        userLat = userCoordinates.first,
                        userLng = userCoordinates.second,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = eq.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    StarRatingView(rating = eq.rating, reviewCount = eq.reviewCount)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${eq.location} • Propriétaire: ${eq.ownerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Callout Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MintSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tarif ${if (isRental) "de Location" else "de Vente"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ForestGreenDark
                            )
                            Text(
                                text = formatCfa(eq.priceCfa),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestGreenPrimary
                                )
                            )
                            if (isRental) {
                                Text(
                                    text = "par ${eq.rentalUnit} (Caution : ${formatCfa(eq.depositCfa)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ForestGreenDark.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (eq.isEcoCertified) {
                            EcoBadge(text = "Éco-Conforme")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Description & État",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = eq.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Technical Specs Chips
                Text(
                    text = "Spécifications Techniques",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    eq.specs.forEach { spec ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = spec,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Direct Contact Button
                OutlinedButton(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${eq.ownerPhone.replace(" ", "")}"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Appeler le Propriétaire (${eq.ownerPhone})")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rental Duration & Operator Configuration (If Rental)
                if (isRental) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Paramètres de la Location",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Durée : $bookingDurationDays jour(s)", style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (bookingDurationDays > 1) bookingDurationDays-- },
                                        enabled = bookingDurationDays > 1
                                    ) {
                                        Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Moins")
                                    }
                                    Text(
                                        text = "$bookingDurationDays",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { if (bookingDurationDays < 30) bookingDurationDays++ }
                                    ) {
                                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Plus")
                                    }
                                }
                            }

                            if (eq.operatorAvailable) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = includeOperator,
                                        onCheckedChange = { includeOperator = it }
                                    )
                                    Text(
                                        text = "Inclure un chauffeur / opérateur (+10 000 FCFA/jour)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            // Group-Booking (Location Partagée entre voisins)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Location Groupée (Co-partage)",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Divisez les frais avec des agriculteurs voisins",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                                Switch(
                                    checked = isGroupBooking,
                                    onCheckedChange = { isGroupBooking = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ForestGreenPrimary)
                                )
                            }

                            if (isGroupBooking) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Nombre d'agriculteurs : $groupPartnersCount", style = MaterialTheme.typography.bodySmall)
                                    Row {
                                        listOf(2, 3, 4).forEach { count ->
                                            FilterChip(
                                                selected = groupPartnersCount == count,
                                                onClick = { groupPartnersCount = count },
                                                label = { Text("$count", fontSize = 11.sp) },
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }
                                val perPerson = calculatedTotal / groupPartnersCount
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MintLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🤝 Part individuelle : ${formatCfa(perPerson)} / agriculteur pour $bookingDurationDays j.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calculateur Rentabilité Button inside modal
                    OutlinedButton(
                        onClick = {
                            roiSelectedEquipment = eq
                            showRoiCalculator = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = ForestGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📊 Calculer la Rentabilité & Rendement Hectare", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = renterNameInput,
                        onValueChange = { renterNameInput = it },
                        label = { Text("Votre Nom Complet / Exploitation") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        showPaymentSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("book_or_buy_now_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !eq.isRentedCurrently
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRental) "Réserver & Payer ${formatCfa(calculatedTotal)}" else "Acheter via Mobile Money (${formatCfa(calculatedTotal)})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Payment Sheet Flow
    if (showPaymentSheet && selectedEquipmentForDetail != null) {
        val eq = selectedEquipmentForDetail!!
        val isRental = eq.offerType == OfferType.RENT || eq.offerType == OfferType.BOTH
        val operatorCostPerDay = if (includeOperator && eq.operatorAvailable) 10000L else 0L
        val calculatedTotal = if (isRental) {
            (eq.priceCfa + operatorCostPerDay) * bookingDurationDays + eq.depositCfa
        } else {
            eq.priceCfa
        }

        PaymentBottomSheet(
            title = if (isRental) "Contrat de Location Machine" else "Achat de Matériel Agricole",
            purpose = if (isRental) "${eq.title} ($bookingDurationDays jours + caution)" else "${eq.title} (Achat direct)",
            totalAmountCfa = calculatedTotal,
            onDismiss = {
                showPaymentSheet = false
                selectedEquipmentForDetail = null
            },
            onPaymentConfirmed = { provider, phone ->
                if (isRental) {
                    onBookEquipment(eq, renterNameInput, renterPhoneInput, bookingDurationDays, includeOperator, provider, phone)
                } else {
                    onPurchaseEquipment(eq, provider, phone)
                }
                com.example.data.model.PaymentTransaction(
                    id = "pay_temp",
                    transactionRef = "${provider.code}-SUCCESS",
                    amountCfa = calculatedTotal,
                    feeCfa = if (provider == PaymentProvider.WAVE) (calculatedTotal * 0.01).toLong() else 0L,
                    provider = provider,
                    phoneNumber = phone,
                    purpose = eq.title,
                    receiptCode = "REC-AGRI-${(1000..9999).random()}"
                )
            }
        )
    }

    if (showRoiCalculator) {
        EquipmentRoiCalculatorDialog(
            presetEquipment = roiSelectedEquipment,
            onDismiss = {
                showRoiCalculator = false
                roiSelectedEquipment = null
            }
        )
    }
}

@Composable
fun EquipmentFullCard(
    equipment: EquipmentItem,
    userCoordinates: Pair<Double, Double>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("equipment_card_${equipment.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Real Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AppAsyncImage(
                    imageUrl = equipment.imageUrl,
                    contentDescription = equipment.title,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    color = if (equipment.offerType == OfferType.SALE) AmberLight else MintLight,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = equipment.offerType.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (equipment.offerType == OfferType.SALE) Color(0xFF8D5B00) else ForestGreenDark
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                DistanceBadge(
                    itemLat = equipment.latitude,
                    itemLng = equipment.longitude,
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
                    if (equipment.hpPower > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${equipment.hpPower} CV",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (equipment.isRentedCurrently) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = "En location (${equipment.daysRemaining}j)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MintLight
                        ) {
                            Text(
                                text = "Disponible",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = equipment.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = equipment.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${equipment.location} • ${equipment.ownerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    StarRatingView(rating = equipment.rating, reviewCount = equipment.reviewCount)
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatCfa(equipment.priceCfa),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreenPrimary
                            )
                        )
                        Text(
                            text = "/ ${equipment.rentalUnit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VoiceNarratorButton(
                            onSpeak = {
                                com.example.util.AudioGuideManager.speakEquipment(
                                    title = equipment.title,
                                    priceCfa = equipment.priceCfa,
                                    unit = equipment.rentalUnit,
                                    location = equipment.location,
                                    isOperator = equipment.operatorAvailable
                                )
                            },
                            size = 36
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (equipment.offerType == OfferType.SALE) "Voir / Acheter" else "Louer",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentRoiCalculatorDialog(
    presetEquipment: EquipmentItem?,
    onDismiss: () -> Unit
) {
    var selectedCrop by remember { mutableStateOf("Maïs (Zea mays)") }
    var hectares by remember { mutableStateOf(5) }
    var machineType by remember {
        mutableStateOf(
            if (presetEquipment?.category == EquipmentCategory.TRACTOR) "Tracteur 4x4 + Charrue"
            else if (presetEquipment?.category == EquipmentCategory.HARVESTER) "Moissonneuse-Batteuse"
            else if (presetEquipment?.category == EquipmentCategory.IRRIGATION) "Pompe Solaire Goutte-à-goutte"
            else "Motoculteur & Semoir Polyvalent"
        )
    }

    val crops = listOf("Maïs (Zea mays)", "Riz Irrigué", "Manioc", "Cacao", "Maraîchage (Piment/Tomate)")
    val machines = listOf(
        "Tracteur 4x4 + Charrue",
        "Motoculteur & Semoir Polyvalent",
        "Moissonneuse-Batteuse",
        "Pompe Solaire Goutte-à-goutte"
    )

    // ROI Computations based on rural Ivory Coast agricultural research (ANADER / CNRA baseline)
    val manualDaysPerHa = when (machineType) {
        "Tracteur 4x4 + Charrue" -> 14
        "Motoculteur & Semoir Polyvalent" -> 8
        "Moissonneuse-Batteuse" -> 20
        else -> 10
    }
    val machineHoursPerHa = when (machineType) {
        "Tracteur 4x4 + Charrue" -> 2.5
        "Motoculteur & Semoir Polyvalent" -> 5.0
        "Moissonneuse-Batteuse" -> 1.5
        else -> 3.0
    }

    val manualLaborCostPerDay = 3500L // FCFA/jour/ouvrier
    val totalManualLaborCost = manualDaysPerHa * manualLaborCostPerDay * hectares

    val machineCostPerHa = when (machineType) {
        "Tracteur 4x4 + Charrue" -> 25000L
        "Motoculteur & Semoir Polyvalent" -> 15000L
        "Moissonneuse-Batteuse" -> 35000L
        else -> 12000L
    }
    val totalMachineCost = machineCostPerHa * hectares
    val netSavingsCfa = totalManualLaborCost - totalMachineCost

    val yieldGainPercent = when (selectedCrop) {
        "Maïs (Zea mays)" -> 28
        "Riz Irrigué" -> 35
        "Manioc" -> 22
        "Cacao" -> 18
        else -> 30
    }

    val baseYieldValCfaPerHa = when (selectedCrop) {
        "Maïs (Zea mays)" -> 450000L
        "Riz Irrigué" -> 600000L
        "Manioc" -> 400000L
        "Cacao" -> 850000L
        else -> 700000L
    }
    val estimatedYieldSurplusCfa = (baseYieldValCfaPerHa * yieldGainPercent / 100) * hectares
    val totalFinancialBenefitCfa = netSavingsCfa + estimatedYieldSurplusCfa

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Calculateur Rentabilité",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Gains économiques & Rendement ha",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Crop selector
                Text(
                    text = "Culture principale :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(crops) { crop ->
                        FilterChip(
                            selected = selectedCrop == crop,
                            onClick = { selectedCrop = crop },
                            label = { Text(crop, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hectares Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Superficie de votre parcelle :",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForestGreenPrimary
                    ) {
                        Text(
                            text = "$hectares Hectares",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Slider(
                    value = hectares.toFloat(),
                    onValueChange = { hectares = it.toInt() },
                    valueRange = 1f..30f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = ForestGreenPrimary,
                        activeTrackColor = ForestGreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Machine Selector
                Text(
                    text = "Type de mécanisation :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(machines) { m ->
                        FilterChip(
                            selected = machineType == m,
                            onClick = { machineType = m },
                            label = { Text(m, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results Summary Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MintLight,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bénéfice Net Estimé",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                            )
                            Text(
                                text = "+${formatCfa(totalFinancialBenefitCfa)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestGreenDark
                                )
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = ForestGreenPrimary.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("⏱️ Gain de temps", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(
                                    text = "${manualDaysPerHa * hectares} j. → ${(machineHoursPerHa * hectares).toInt()} h.",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("📈 Gain de Rendement", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(
                                    text = "+$yieldGainPercent% (+${formatCfa(estimatedYieldSurplusCfa)})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("💰 Économie Main d'œuvre", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(
                                    text = formatCfa(netSavingsCfa.coerceAtLeast(0L)),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = HarvestGold)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("🌱 Empreinte Carbone", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(
                                    text = "-32% CO2 / tonne récoltée",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Appliquer ce plan & Trouver le matériel")
                }
            }
        }
    }
}
