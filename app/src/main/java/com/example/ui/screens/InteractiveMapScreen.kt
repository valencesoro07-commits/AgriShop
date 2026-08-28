package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AppAsyncImage
import com.example.ui.components.PaymentBottomSheet
import com.example.ui.theme.*
import kotlin.math.*

sealed class MapItemPin {
    abstract val id: String
    abstract val title: String
    abstract val location: String
    abstract val latitude: Double
    abstract val longitude: Double
    abstract val priceCfa: Long
    abstract val imageUrl: String
    abstract val categoryName: String

    data class Equipment(val item: EquipmentItem) : MapItemPin() {
        override val id = item.id
        override val title = item.title
        override val location = item.location
        override val latitude = item.latitude
        override val longitude = item.longitude
        override val priceCfa = item.priceCfa
        override val imageUrl = item.imageUrl
        override val categoryName = item.category.label
    }

    data class Produce(val item: ProduceItem) : MapItemPin() {
        override val id = item.id
        override val title = item.title
        override val location = item.location
        override val latitude = item.latitude
        override val longitude = item.longitude
        override val priceCfa = item.priceCfa
        override val imageUrl = item.imageUrl
        override val categoryName = item.category.label
    }

    data class Compost(val item: CompostItem) : MapItemPin() {
        override val id = item.id
        override val title = item.title
        override val location = item.location
        override val latitude = item.latitude
        override val longitude = item.longitude
        override val priceCfa = item.pricePerUnitCfa
        override val imageUrl = item.imageUrl
        override val categoryName = item.category.label
    }
}

enum class MapFilterCategory(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ALL("Tout afficher", Icons.Default.Public),
    EQUIPMENT("Machines & Engins", Icons.Default.Agriculture),
    PRODUCE("Vivriers & Récoltes", Icons.Default.Grass),
    COMPOST("Compost", Icons.Default.Recycling)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveMapScreen(
    equipmentList: List<EquipmentItem>,
    produceList: List<ProduceItem>,
    compostList: List<CompostItem>,
    userCoordinates: Pair<Double, Double>,
    currentCity: String,
    onNavigateBack: () -> Unit,
    onBookEquipment: (EquipmentItem, String, String, Int, Boolean, PaymentProvider, String) -> Unit,
    onPurchaseEquipment: (EquipmentItem, PaymentProvider, String) -> Unit,
    onBuyProduce: (ProduceItem, Int, PaymentProvider, String) -> Unit,
    onBuyCompost: (CompostItem, Int, PaymentProvider, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(MapFilterCategory.ALL) }
    var maxDistanceKm by remember { mutableFloatStateOf(150f) } // Default max distance filter
    var selectedPin by remember { mutableStateOf<MapItemPin?>(null) }
    var isListView by remember { mutableStateOf(false) }

    // Payment bottom sheet for direct booking/buying from map
    var paymentItem by remember { mutableStateOf<MapItemPin?>(null) }

    // Aggregate all map items with distance calculations
    val allPins = remember(equipmentList, produceList, compostList, selectedCategory, maxDistanceKm, userCoordinates) {
        val list = mutableListOf<MapItemPin>()

        if (selectedCategory == MapFilterCategory.ALL || selectedCategory == MapFilterCategory.EQUIPMENT) {
            equipmentList.forEach { list.add(MapItemPin.Equipment(it)) }
        }
        if (selectedCategory == MapFilterCategory.ALL || selectedCategory == MapFilterCategory.PRODUCE) {
            produceList.forEach { list.add(MapItemPin.Produce(it)) }
        }
        if (selectedCategory == MapFilterCategory.ALL || selectedCategory == MapFilterCategory.COMPOST) {
            compostList.forEach { list.add(MapItemPin.Compost(it)) }
        }

        list.map { pin ->
            val dist = GeoUtils.calculateDistanceKm(
                userCoordinates.first, userCoordinates.second,
                pin.latitude, pin.longitude
            )
            Pair(pin, dist)
        }.filter { (_, dist) ->
            maxDistanceKm >= 150f || dist <= maxDistanceKm
        }.sortedBy { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Carte & Géolocalisation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "Position : $currentCity (Rayon ${if (maxDistanceKm >= 150f) "Tout CI" else "${maxDistanceKm.toInt()} km"})",
                            style = MaterialTheme.typography.labelSmall.copy(color = ForestGreenPrimary)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { isListView = !isListView }) {
                        Icon(
                            imageVector = if (isListView) Icons.Default.Map else Icons.Default.List,
                            contentDescription = "Basculer vue",
                            tint = ForestGreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isListView) {
                // Proximity Sorted List View
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filters Row
                    MapFilterHeader(
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        maxDistanceKm = maxDistanceKm,
                        onDistanceChange = { maxDistanceKm = it }
                    )

                    Text(
                        text = "${allPins.size} offres triées par proximité kilométrique :",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allPins) { (pin, distKm) ->
                            ProximityPinCard(
                                pin = pin,
                                distanceKm = distKm,
                                onClick = {
                                    selectedPin = pin
                                    isListView = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Interactive Visual Map View
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filters Header
                    MapFilterHeader(
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        maxDistanceKm = maxDistanceKm,
                        onDistanceChange = { maxDistanceKm = it }
                    )

                    // Interactive Map Canvas with Pins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFE8F0E6))
                    ) {
                        InteractiveIvoryCoastMap(
                            userCoordinates = userCoordinates,
                            userCity = currentCity,
                            pinsWithDist = allPins,
                            selectedPin = selectedPin,
                            onPinClick = { pin -> selectedPin = pin }
                        )

                        // Floating GPS Center button
                        SmallFloatingActionButton(
                            onClick = { selectedPin = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            containerColor = ForestGreenPrimary,
                            contentColor = Color.White
                        ) {
                            Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Ma position")
                        }
                    }

                    // Bottom Pin Detail Sheet Card (if a pin is selected)
                    AnimatedVisibility(
                        visible = selectedPin != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        selectedPin?.let { pin ->
                            val dist = GeoUtils.calculateDistanceKm(
                                userCoordinates.first, userCoordinates.second,
                                pin.latitude, pin.longitude
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                                text = pin.categoryName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = ForestGreenDark
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.NearMe,
                                                contentDescription = null,
                                                tint = ForestGreenPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "à ${String.format("%.1f", dist)} km",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = ForestGreenPrimary
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { selectedPin = null },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppAsyncImage(
                                            imageUrl = pin.imageUrl,
                                            contentDescription = pin.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pin.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = pin.location,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                                )
                                            }
                                            Text(
                                                text = "${pin.priceCfa} FCFA",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = OrangeAccent
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action Button to initiate transaction / booking
                                    Button(
                                        onClick = { paymentItem = pin },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                                    ) {
                                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (pin) {
                                                is MapItemPin.Equipment -> "Réserver / Louer cet engin"
                                                is MapItemPin.Produce -> "Acheter cette récolte"
                                                is MapItemPin.Compost -> "Commander ce fertilisant"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Bottom Sheet when user clicks Action from Map
    paymentItem?.let { item ->
        PaymentBottomSheet(
            title = item.title,
            purpose = "Paiement ${item.title} (Via Carte GPS)",
            totalAmountCfa = item.priceCfa,
            onDismiss = { paymentItem = null },
            onPaymentConfirmed = { provider, phone ->
                when (item) {
                    is MapItemPin.Equipment -> {
                        onPurchaseEquipment(item.item, provider, phone)
                    }
                    is MapItemPin.Produce -> {
                        onBuyProduce(item.item, 1, provider, phone)
                    }
                    is MapItemPin.Compost -> {
                        onBuyCompost(item.item, 1, provider, phone)
                    }
                }
                val tx = com.example.data.model.PaymentTransaction(
                    id = "pay_map_${System.currentTimeMillis()}",
                    transactionRef = "${provider.code}-MAP",
                    amountCfa = item.priceCfa,
                    feeCfa = if (provider == PaymentProvider.WAVE) (item.priceCfa * 0.01).toLong() else 0L,
                    provider = provider,
                    phoneNumber = phone,
                    purpose = item.title,
                    receiptCode = "REC-MAP-${(1000..9999).random()}"
                )
                paymentItem = null
                selectedPin = null
                tx
            }
        )
    }
}

@Composable
fun MapFilterHeader(
    selectedCategory: MapFilterCategory,
    onCategorySelect: (MapFilterCategory) -> Unit,
    maxDistanceKm: Float,
    onDistanceChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Category filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(MapFilterCategory.entries) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(cat) },
                    label = { Text(cat.label, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = cat.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForestGreenPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }
        }

        // Distance range filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Rayon max : ${if (maxDistanceKm >= 150f) "Toute la CI" else "${maxDistanceKm.toInt()} km"}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Slider(
                value = maxDistanceKm,
                onValueChange = onDistanceChange,
                valueRange = 15f..150f,
                steps = 8,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = ForestGreenPrimary,
                    activeTrackColor = ForestGreenPrimary
                )
            )
        }
    }
}

@Composable
fun InteractiveIvoryCoastMap(
    userCoordinates: Pair<Double, Double>,
    userCity: String,
    pinsWithDist: List<Pair<MapItemPin, Double>>,
    selectedPin: MapItemPin?,
    onPinClick: (MapItemPin) -> Unit
) {
    // Ivory Coast geographical bounds approx:
    // Min Lat: 4.3 (San Pedro/Coast), Max Lat: 10.7 (Korhogo/North)
    // Min Lng: -8.6 (West/Man), Max Lng: -2.5 (East/Abengourou)
    val minLat = 4.5
    val maxLat = 10.6
    val minLng = -8.5
    val maxLng = -2.8

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        fun coordsToOffset(lat: Double, lng: Double): Offset {
            val normX = ((lng - minLng) / (maxLng - minLng)).coerceIn(0.05, 0.95).toFloat()
            val normY = (1f - ((lat - minLat) / (maxLat - minLat))).coerceIn(0.05, 0.95).toFloat()
            return Offset(normX * width, normY * height)
        }

        val userOffset = coordsToOffset(userCoordinates.first, userCoordinates.second)

        // Draw Map Background with City Regions & Distance Circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Regional grid background
            val gridColor = Color(0xFFD0E2CC)
            for (i in 1..6) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, height * (i / 7f)),
                    end = Offset(width, height * (i / 7f)),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawLine(
                    color = gridColor,
                    start = Offset(width * (i / 7f), 0f),
                    end = Offset(width * (i / 7f), height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Draw Côte d'Ivoire major hub nodes
            GeoUtils.PRESET_CITIES.forEach { city ->
                val cityOffset = coordsToOffset(city.lat, city.lng)
                drawCircle(
                    color = Color(0xFF94A3B8).copy(alpha = 0.4f),
                    radius = 8f,
                    center = cityOffset
                )
            }

            // Draw User Location Pulsing Radius
            drawCircle(
                color = ForestGreenPrimary.copy(alpha = 0.15f),
                radius = 60f,
                center = userOffset
            )
            drawCircle(
                color = ForestGreenPrimary.copy(alpha = 0.35f),
                radius = 35f,
                center = userOffset
            )
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = userOffset
            )
            drawCircle(
                color = Color(0xFF1E88E5), // Blue User GPS marker
                radius = 8f,
                center = userOffset
            )
        }

        // Overlay Interactive Pins
        pinsWithDist.forEach { (pin, dist) ->
            val pinOffset = coordsToOffset(pin.latitude, pin.longitude)
            val isSelected = selectedPin?.id == pin.id

            Box(
                modifier = Modifier
                    .offset(
                        x = (pinOffset.x / androidx.compose.ui.platform.LocalDensity.current.density).dp - 20.dp,
                        y = (pinOffset.y / androidx.compose.ui.platform.LocalDensity.current.density).dp - 36.dp
                    )
                    .clickable { onPinClick(pin) }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Pin badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when (pin) {
                            is MapItemPin.Equipment -> OrangeAccent
                            is MapItemPin.Produce -> ForestGreenPrimary
                            is MapItemPin.Compost -> Color(0xFF2E7D32)
                        },
                        shadowElevation = if (isSelected) 8.dp else 3.dp,
                        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (pin) {
                                    is MapItemPin.Equipment -> Icons.Default.Agriculture
                                    is MapItemPin.Produce -> Icons.Default.Grass
                                    is MapItemPin.Compost -> Icons.Default.Recycling
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${dist.toInt()}km",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Pin pointer triangle
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = when (pin) {
                            is MapItemPin.Equipment -> OrangeAccent
                            is MapItemPin.Produce -> ForestGreenPrimary
                            is MapItemPin.Compost -> Color(0xFF2E7D32)
                        },
                        modifier = Modifier
                            .size(18.dp)
                            .offset(y = (-5).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProximityPinCard(
    pin: MapItemPin,
    distanceKm: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppAsyncImage(
                imageUrl = pin.imageUrl,
                contentDescription = pin.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = pin.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MintLight
                    ) {
                        Text(
                            text = "${String.format("%.1f", distanceKm)} km",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenDark
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${pin.categoryName} • ${pin.location}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${pin.priceCfa} FCFA",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeAccent
                    )
                )
            }
        }
    }
}
