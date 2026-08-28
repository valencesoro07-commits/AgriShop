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
    var renterNameInput by remember { mutableStateOf("Mamadou Koné") }
    var renterPhoneInput by remember { mutableStateOf("+225 07 12 34 56") }

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
                    .testTag("add_equipment_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Proposer du Matériel", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search & Filter Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher tracteur, motoculteur, ville...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("equipment_search_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Offer Type & Proximity Quick Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedOfferType == null,
                        onClick = { selectedOfferType = null },
                        label = { Text("Tout") }
                    )
                    FilterChip(
                        selected = selectedOfferType == OfferType.RENT,
                        onClick = { selectedOfferType = OfferType.RENT },
                        label = { Text("🚜 À Louer") }
                    )
                    FilterChip(
                        selected = selectedOfferType == OfferType.SALE,
                        onClick = { selectedOfferType = OfferType.SALE },
                        label = { Text("🏷️ À Vendre") }
                    )
                    FilterChip(
                        selected = sortByProximity,
                        onClick = { sortByProximity = !sortByProximity },
                        label = { Text("📍 Plus proches") },
                        leadingIcon = {
                            Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category Scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(EquipmentCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }
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
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
