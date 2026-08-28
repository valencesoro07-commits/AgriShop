package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    equipmentList: List<EquipmentItem>,
    produceList: List<ProduceItem>,
    compostList: List<CompostItem>,
    activeContractsCount: Int,
    userCoordinates: Pair<Double, Double>,
    currentCity: String,
    onCityChangeClick: () -> Unit,
    onNavigateToEquipment: () -> Unit,
    onNavigateToRentals: () -> Unit,
    onNavigateToProduce: () -> Unit,
    onNavigateToCompost: () -> Unit,
    onNavigateToWasteProgram: () -> Unit = {},
    onNavigateToAi: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToMap: () -> Unit,
    onOpenEquipmentDetail: (EquipmentItem) -> Unit,
    onOpenCompostDetail: (CompostItem) -> Unit,
    onOpenProduceDetail: (ProduceItem) -> Unit,
    onOpenAdvisorCall: () -> Unit = {},
    onOpenLanguageDialog: () -> Unit = {}
) {
    val isSimplifiedMode by com.example.util.AudioGuideManager.isSimplifiedMode.collectAsState()
    val audioLanguage by com.example.util.AudioGuideManager.selectedLanguage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Accessibility & Mode Switcher Bar (Normal vs Facile)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isSimplifiedMode) AmberLight else MintLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSimplifiedMode) HarvestGold else ForestGreenPrimary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Segmented Toggle Pill: Normal vs Facile
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Normal Mode Tab
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { com.example.util.AudioGuideManager.setSimplifiedMode(false) },
                                color = if (!isSimplifiedMode) ForestGreenPrimary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Mode Normal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isSimplifiedMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            // Easy Mode Tab
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { com.example.util.AudioGuideManager.setSimplifiedMode(true) },
                                color = if (isSimplifiedMode) HarvestGold else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Mode Facile 🌾",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSimplifiedMode) ForestGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Audio Language Selector Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenLanguageDialog() },
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = audioLanguage.flag, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = audioLanguage.code.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Advisor Call Button
                        IconButton(
                            onClick = onOpenAdvisorCall,
                            modifier = Modifier
                                .size(32.dp)
                                .background(ForestGreenPrimary.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = "Conseiller",
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Subtitle description under the mode bar
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isSimplifiedMode)
                        "Mode Grandes Icônes avec assistance vocale pour naviguer sans lire."
                    else
                        "Mode Complet : filtres détaillés, fiches techniques et carte interactive. Basculez sur Mode Facile pour l'audio.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = if (isSimplifiedMode) ForestGreenDark.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // If Simplified Mode is active, show the 4 huge visual tiles directly at top!
        if (isSimplifiedMode) {
            SimplifiedFieldDashboard(
                onNavigateToEquipment = onNavigateToEquipment,
                onNavigateToProduce = onNavigateToProduce,
                onNavigateToCompost = onNavigateToCompost,
                onOpenAdvisorCall = onOpenAdvisorCall,
                onSwitchToNormalMode = { com.example.util.AudioGuideManager.setSimplifiedMode(false) }
            )
        }

        // Location Bar with GPS city selector & Map quick launch
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCityChangeClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Région GPS : ",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = currentCity,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Interactive Map Shortcut Button
                Button(
                    onClick = onNavigateToMap,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Carte GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hero Card with AgriShop Official Logo & Agricultural Landscape Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.hero_agri_banner),
                contentDescription = "AgriShop Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0x20000000), Color(0xB80A3B2C), Color(0xF0082E22))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row with Official Logo Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.agrishop_logo),
                                contentDescription = "Logo AgriShop",
                                modifier = Modifier.size(34.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "AgriShop CI",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = ForestGreenDark
                                    )
                                )
                                Text(
                                    text = "Plateforme Officielle",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        color = ForestGreenPrimary
                                    )
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AmberSun.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Matériel • Récoltes • Bio",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }
                }

                // Bottom Content
                Column {
                    Text(
                        text = "Cultivons l'Avenir Ensemble 🌾",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Machines avec photos réelles, circuit court de récoltes et fertilisants 100% bio certifiés.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFD4E6D9),
                            fontSize = 11.5.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Green Eco Banner Spotlight
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onNavigateToWasteProgram() }
                .testTag("green_hub_card"),
            colors = CardDefaults.cardColors(containerColor = MintLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ForestGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Recycling,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Programme Éco-Collecte Déchets",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ForestGreenDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = ForestGreenMedium,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Suivi complet des collectes en cours & à effectuer. Déclarez vos résidus pour camion benne.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestGreenDark.copy(alpha = 0.85f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ForestGreenPrimary
                )
            }
        }

        // Action Shortcuts Grid
        SectionHeader(title = "Services & Actions Rapides")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeShortcutItem(
                title = "Matériel",
                subtitle = "Louer / Acheter",
                icon = Icons.Default.Agriculture,
                badgeColor = ForestGreenPrimary,
                onClick = onNavigateToEquipment,
                modifier = Modifier.weight(1f)
            )
            HomeShortcutItem(
                title = "Récoltes",
                subtitle = "Vente directe",
                icon = Icons.Default.Grass,
                badgeColor = HarvestGold,
                onClick = onNavigateToProduce,
                modifier = Modifier.weight(1f)
            )
            HomeShortcutItem(
                title = "Compost",
                subtitle = "Bio-fertilisant",
                icon = Icons.Default.Yard,
                badgeColor = EarthBrown,
                onClick = onNavigateToCompost,
                modifier = Modifier.weight(1f)
            )
            HomeShortcutItem(
                title = "IA Agro",
                subtitle = "Conseils 24/7",
                icon = Icons.Default.AutoAwesome,
                badgeColor = Color(0xFF6A1B9A),
                onClick = onNavigateToAi,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCard(
                title = "Locations Actives",
                value = "$activeContractsCount machine(s)",
                subtitle = "Gérer les contrats",
                icon = Icons.Default.Assignment,
                accentColor = ForestGreenPrimary,
                onClick = onNavigateToRentals,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                title = "Paiements CinetPay",
                value = "Mobile Money & Cartes",
                subtitle = "Passerelle sécurisée",
                icon = Icons.Default.Payment,
                accentColor = CinetPayColor,
                onClick = onNavigateToPayments,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Biogas & Green Energy Spotlight Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigateToEquipment() },
            color = MintLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ForestGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EnergySavingsLeaf,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Biogaz & Équipements Verts 🌱",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ForestGreenDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = ForestGreenPrimary) {
                            Text(
                                text = "ÉCO",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Biodigesteurs 10m³, pompes solaires, broyeurs et séchoirs écologiques disponibles.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = ForestGreenDark.copy(alpha = 0.85f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ForestGreenPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Featured Equipment Section
        SectionHeader(
            title = "Équipements & Machines Proches",
            subtitle = "Tracteurs, motoculteurs et pompes géolocalisés",
            actionText = "Tout voir",
            onActionClick = onNavigateToEquipment
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(equipmentList) { equipment ->
                EquipmentHorizontalCard(
                    equipment = equipment,
                    userCoordinates = userCoordinates,
                    onClick = { onOpenEquipmentDetail(equipment) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Compost Spotlight Section
        SectionHeader(
            title = "Compost & Bio-Fertilisants Ennoblis",
            subtitle = "Issus du recyclage agroécologique certifié",
            actionText = "Voir le Compost",
            onActionClick = onNavigateToCompost
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(compostList) { compost ->
                CompostHorizontalCard(
                    item = compost,
                    userCoordinates = userCoordinates,
                    onClick = { onOpenCompostDetail(compost) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fresh Produce Section
        SectionHeader(
            title = "Bourse des Producteurs & Récoltes",
            subtitle = "Achetez directement aux agriculteurs sans intermédiaire",
            actionText = "Explorer",
            onActionClick = onNavigateToProduce
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            produceList.take(3).forEach { produce ->
                ProduceSummaryCard(
                    produce = produce,
                    userCoordinates = userCoordinates,
                    onClick = { onOpenProduceDetail(produce) }
                )
            }
        }
    }
}

@Composable
fun HomeShortcutItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EquipmentHorizontalCard(
    equipment: EquipmentItem,
    userCoordinates: Pair<Double, Double>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Real Image with Badge Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
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
                            color = if (equipment.offerType == OfferType.SALE) Color(0xFF8D5B00) else ForestGreenDark,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                DistanceBadge(
                    itemLat = equipment.latitude,
                    itemLng = equipment.longitude,
                    userLat = userCoordinates.first,
                    userLng = userCoordinates.second,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = equipment.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = equipment.location,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatCfa(equipment.priceCfa),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreenPrimary,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "/ ${equipment.rentalUnit}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
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
                            size = 32
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        StarRatingView(rating = equipment.rating, reviewCount = equipment.reviewCount)
                    }
                }
            }
        }
    }
}

@Composable
fun CompostHorizontalCard(
    item: CompostItem,
    userCoordinates: Pair<Double, Double>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
            ) {
                AppAsyncImage(
                    imageUrl = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize()
                )

                DistanceBadge(
                    itemLat = item.latitude,
                    itemLng = item.longitude,
                    userLat = userCoordinates.first,
                    userLng = userCoordinates.second,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                EcoBadge(text = "-${item.co2SavedKgPerUnit.toInt()} kg CO2e")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "NPK : ${item.npkRatio}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = EarthBrown,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatCfa(item.pricePerUnitCfa)} / ${item.unit}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    )
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
                            size = 30
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProduceSummaryCard(
    produce: ProduceItem,
    userCoordinates: Pair<Double, Double>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AppAsyncImage(
                    imageUrl = produce.imageUrl,
                    contentDescription = produce.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = produce.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (produce.isOrganicCertified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Bio",
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${produce.producerName} • ${produce.location}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    DistanceBadge(
                        itemLat = produce.latitude,
                        itemLng = produce.longitude,
                        userLat = userCoordinates.first,
                        userLng = userCoordinates.second
                    )
                }
                Text(
                    text = "Stock : ${produce.availableStock} ${produce.unit}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = ForestGreenDark
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VoiceNarratorButton(
                        onSpeak = {
                            com.example.util.AudioGuideManager.speakProduce(
                                cropName = produce.title,
                                pricePerKg = produce.priceCfa.toInt(),
                                quantityKg = produce.availableStock,
                                location = produce.location,
                                farmerName = produce.producerName
                            )
                        },
                        size = 28
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatCfa(produce.priceCfa),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    )
                }
                Text(
                    text = "/ ${produce.unit}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
