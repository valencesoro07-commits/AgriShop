package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.service.GeminiAgriService
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintLight
import com.example.ui.theme.OrangeAccent
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingDialog(
    onDismiss: () -> Unit,
    onAddEquipment: (EquipmentItem) -> Unit,
    onAddProduce: (ProduceItem) -> Unit,
    onAddCompost: (CompostItem) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Equipment, 1: Produce, 2: Compost
    var isAnalyzingPhoto by remember { mutableStateOf(false) }
    var photoAnalysisSuccess by remember { mutableStateOf<String?>(null) }

    // Preset high-res image options for equipment with matching machine photos
    val equipmentImagePresets = listOf(
        "img_biogas_digester" to "Biodigesteur Biogaz",
        "img_biomass_shredder" to "Broyeur Biomasse",
        "img_solar_dryer" to "Séchoir Solaire",
        "img_tractor_mf375" to "Tracteur MF",
        "img_motoculteur_yanmar" to "Motoculteur",
        "img_combine_harvester" to "Moissonneuse",
        "img_solar_irrigation" to "Kit Solaire",
        "img_boom_sprayer" to "Pulvérisateur",
        "img_semoir_pneumatique" to "Semoir 4 Rangs"
    )

    // Preset images for produce with matching real crop photos
    val produceImagePresets = listOf(
        "img_corn_maize" to "Maïs / Céréales",
        "img_cocoa_beans" to "Cacao / Café",
        "img_cassava_roots" to "Manioc / Tubercules",
        "img_field_tomatoes" to "Tomates / Légumes"
    )

    // Preset images for compost with matching real bio-fertilizer photos
    val compostImagePresets = listOf(
        "img_compost_terre_noire" to "Compost Noir",
        "img_bio_liquid" to "Bio-Liquide",
        "img_cocoa_compost_straw" to "Cabosses & Paille"
    )

    // Equipment state
    var eqTitle by remember { mutableStateOf("") }
    var eqCategory by remember { mutableStateOf(EquipmentCategory.TRACTOR) }
    var eqOfferType by remember { mutableStateOf(OfferType.RENT) }
    var eqPriceStr by remember { mutableStateOf("") }
    var eqDepositStr by remember { mutableStateOf("") }
    var eqPowerStr by remember { mutableStateOf("") }
    var eqCondition by remember { mutableStateOf("Très bon état") }
    var eqLocation by remember { mutableStateOf("Bouaké") }
    var eqOwnerName by remember { mutableStateOf("") }
    var eqOwnerPhone by remember { mutableStateOf("+225 07 ") }
    var eqOperatorAvailable by remember { mutableStateOf(true) }
    var eqImageUrl by remember { mutableStateOf(equipmentImagePresets[0].first) }
    var eqDescription by remember { mutableStateOf("") }

    // Produce state
    var prodTitle by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf(ProduceCategory.CEREALS) }
    var prodProducerName by remember { mutableStateOf("") }
    var prodLocation by remember { mutableStateOf("Yamoussoukro") }
    var prodPriceStr by remember { mutableStateOf("") }
    var prodUnit by remember { mutableStateOf("Sac de 50kg") }
    var prodStockStr by remember { mutableStateOf("50") }
    var prodIsOrganic by remember { mutableStateOf(true) }
    var prodPhone by remember { mutableStateOf("+225 05 ") }
    var prodImageUrl by remember { mutableStateOf(produceImagePresets[0].first) }
    var prodDescription by remember { mutableStateOf("") }

    // Compost state
    var compTitle by remember { mutableStateOf("") }
    var compCategory by remember { mutableStateOf(CompostCategory.MATURE_COMPOST) }
    var compPriceStr by remember { mutableStateOf("4500") }
    var compUnit by remember { mutableStateOf("Sac de 50kg") }
    var compVolumeStr by remember { mutableStateOf("100") }
    var compNpk by remember { mutableStateOf("3.0 - 2.5 - 3.0 (Enrichi)") }
    var compMaturityStr by remember { mutableStateOf("12") }
    var compProducerName by remember { mutableStateOf("") }
    var compLocation by remember { mutableStateOf("Korhogo") }
    var compPhone by remember { mutableStateOf("+225 07 ") }
    var compImageUrl by remember { mutableStateOf(compostImagePresets[0].first) }
    var compDescription by remember { mutableStateOf("") }

    fun analyzeCurrentPhotoWithAi(imageHint: String) {
        coroutineScope.launch {
            isAnalyzingPhoto = true
            photoAnalysisSuccess = null
            val analysis = GeminiAgriService.analyzeImageForListing(imageHint)
            isAnalyzingPhoto = false

            when (selectedTab) {
                0 -> {
                    eqTitle = analysis.title
                    eqPriceStr = analysis.suggestedPriceCfa.toString()
                    eqDepositStr = (analysis.suggestedPriceCfa * 3).toString()
                    eqDescription = analysis.description
                    photoAnalysisSuccess = "Photo reconnue : ${analysis.title}"
                }
                1 -> {
                    prodTitle = analysis.title
                    prodPriceStr = analysis.suggestedPriceCfa.toString()
                    prodStockStr = analysis.quantity.toString()
                    prodUnit = analysis.unit
                    prodDescription = analysis.description
                    photoAnalysisSuccess = "Photo reconnue : ${analysis.title}"
                }
                2 -> {
                    compTitle = analysis.title
                    compPriceStr = analysis.suggestedPriceCfa.toString()
                    compVolumeStr = analysis.quantity.toString()
                    compUnit = analysis.unit
                    compDescription = analysis.description
                    photoAnalysisSuccess = "Photo reconnue : ${analysis.title}"
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Publier une Annonce",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                // Category Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🚜 Machine", style = MaterialTheme.typography.labelMedium) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("🌾 Récolte", style = MaterialTheme.typography.labelMedium) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("♻️ Compost", style = MaterialTheme.typography.labelMedium) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // AI Auto-Fill Photo Banner
                Surface(
                    color = MintLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = photoAnalysisSuccess ?: "Sélectionnez une photo & analysez par IA",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Button(
                            onClick = {
                                val currentImg = when (selectedTab) {
                                    0 -> eqImageUrl
                                    1 -> prodImageUrl
                                    else -> compImageUrl
                                }
                                analyzeCurrentPhotoWithAi(currentImg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            enabled = !isAnalyzingPhoto
                        ) {
                            if (isAnalyzingPhoto) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remplir par IA", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> { // Equipment Form
                            Text("Photo de l'équipement :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(equipmentImagePresets) { (url, label) ->
                                    val isSelected = eqImageUrl == url
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            eqImageUrl = url
                                            analyzeCurrentPhotoWithAi(url)
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) ForestGreenPrimary else Color.Transparent)
                                                .padding(if (isSelected) 3.dp else 0.dp)
                                        ) {
                                            AppAsyncImage(
                                                imageUrl = url,
                                                contentDescription = label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                            )
                                        }
                                        Text(label, fontSize = 10.sp, color = if (isSelected) ForestGreenPrimary else Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Catégorie d'équipement :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(EquipmentCategory.entries.filter { it != EquipmentCategory.ALL }) { cat ->
                                    FilterChip(
                                        selected = eqCategory == cat,
                                        onClick = { eqCategory = cat },
                                        label = { Text(cat.label, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = eqTitle,
                                onValueChange = { eqTitle = it },
                                label = { Text("Nom du matériel (ex: Biodigesteur 10m³, Tracteur 75CV)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = eqPriceStr,
                                    onValueChange = { eqPriceStr = it },
                                    label = { Text("Prix (FCFA/jour)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = eqDepositStr,
                                    onValueChange = { eqDepositStr = it },
                                    label = { Text("Caution (FCFA)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = eqPowerStr,
                                    onValueChange = { eqPowerStr = it },
                                    label = { Text("Puissance (CV)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = eqLocation,
                                    onValueChange = { eqLocation = it },
                                    label = { Text("Ville (GPS)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = eqOwnerName,
                                onValueChange = { eqOwnerName = it },
                                label = { Text("Nom du propriétaire / Entreprise") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = eqOwnerPhone,
                                onValueChange = { eqOwnerPhone = it },
                                label = { Text("Téléphone de contact") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = eqDescription,
                                onValueChange = { eqDescription = it },
                                label = { Text("Description & spécifications techniques") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        1 -> { // Produce Form
                            Text("Photo de la récolte :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(produceImagePresets) { (url, label) ->
                                    val isSelected = prodImageUrl == url
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            prodImageUrl = url
                                            analyzeCurrentPhotoWithAi(url)
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) ForestGreenPrimary else Color.Transparent)
                                                .padding(if (isSelected) 3.dp else 0.dp)
                                        ) {
                                            AppAsyncImage(
                                                imageUrl = url,
                                                contentDescription = label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                            )
                                        }
                                        Text(label, fontSize = 10.sp, color = if (isSelected) ForestGreenPrimary else Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = prodTitle,
                                onValueChange = { prodTitle = it },
                                label = { Text("Titre de la récolte (ex: Maïs grain séché)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = prodPriceStr,
                                    onValueChange = { prodPriceStr = it },
                                    label = { Text("Prix unitaire (FCFA)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = prodUnit,
                                    onValueChange = { prodUnit = it },
                                    label = { Text("Unité (kg, sac...)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = prodStockStr,
                                    onValueChange = { prodStockStr = it },
                                    label = { Text("Stock disponible") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = prodLocation,
                                    onValueChange = { prodLocation = it },
                                    label = { Text("Ville (GPS)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = prodProducerName,
                                onValueChange = { prodProducerName = it },
                                label = { Text("Nom du producteur / Coopérative") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = prodDescription,
                                onValueChange = { prodDescription = it },
                                label = { Text("Description & qualité de la récolte") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        2 -> { // Compost Form
                            Text("Photo du fertilisant / compost :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(compostImagePresets) { (url, label) ->
                                    val isSelected = compImageUrl == url
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            compImageUrl = url
                                            analyzeCurrentPhotoWithAi(url)
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) ForestGreenPrimary else Color.Transparent)
                                                .padding(if (isSelected) 3.dp else 0.dp)
                                        ) {
                                            AppAsyncImage(
                                                imageUrl = url,
                                                contentDescription = label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                            )
                                        }
                                        Text(label, fontSize = 10.sp, color = if (isSelected) ForestGreenPrimary else Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = compTitle,
                                onValueChange = { compTitle = it },
                                label = { Text("Nom du produit (ex: Compost Terre Noire)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = compPriceStr,
                                    onValueChange = { compPriceStr = it },
                                    label = { Text("Prix (FCFA)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = compUnit,
                                    onValueChange = { compUnit = it },
                                    label = { Text("Unité (Sac 50kg, L)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = compVolumeStr,
                                    onValueChange = { compVolumeStr = it },
                                    label = { Text("Quantité en stock") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = compLocation,
                                    onValueChange = { compLocation = it },
                                    label = { Text("Ville (GPS)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = compProducerName,
                                onValueChange = { compProducerName = it },
                                label = { Text("Producteur / Centre de recyclage") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = compDescription,
                                onValueChange = { compDescription = it },
                                label = { Text("Description & composition NPK") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Submit Button
                Button(
                    onClick = {
                        when (selectedTab) {
                            0 -> {
                                val price = eqPriceStr.toIntOrNull() ?: 50000
                                val deposit = eqDepositStr.toIntOrNull() ?: (price * 3)
                                val power = eqPowerStr.toIntOrNull() ?: 75
                                val coords = GeoUtils.getCityCoordinates(eqLocation)
                                val item = EquipmentItem(
                                    id = "eq_${UUID.randomUUID().toString().take(6)}",
                                    title = eqTitle.ifBlank { "Équipement Agricole" },
                                    category = eqCategory,
                                    offerType = eqOfferType,
                                    priceCfa = price.toLong(),
                                    depositCfa = deposit.toLong(),
                                    hpPower = power,
                                    condition = eqCondition,
                                    location = eqLocation,
                                    latitude = coords.first,
                                    longitude = coords.second,
                                    ownerName = eqOwnerName.ifBlank { "Propriétaire AgriShop" },
                                    ownerPhone = eqOwnerPhone,
                                    operatorAvailable = eqOperatorAvailable,
                                    imageUrl = eqImageUrl,
                                    description = eqDescription
                                )
                                onAddEquipment(item)
                            }
                            1 -> {
                                val price = prodPriceStr.toIntOrNull() ?: 500
                                val stock = prodStockStr.toIntOrNull() ?: 50
                                val coords = GeoUtils.getCityCoordinates(prodLocation)
                                val item = ProduceItem(
                                    id = "prod_${UUID.randomUUID().toString().take(6)}",
                                    title = prodTitle.ifBlank { "Récolte Fraîche" },
                                    category = prodCategory,
                                    producerName = prodProducerName.ifBlank { "Producteur AgriShop" },
                                    producerRole = "Producteur local",
                                    location = prodLocation,
                                    latitude = coords.first,
                                    longitude = coords.second,
                                    priceCfa = price.toLong(),
                                    unit = prodUnit,
                                    availableStock = stock,
                                    minOrder = 1,
                                    isOrganicCertified = prodIsOrganic,
                                    harvestDate = "Récent",
                                    phone = prodPhone,
                                    imageUrl = prodImageUrl,
                                    description = prodDescription
                                )
                                onAddProduce(item)
                            }
                            2 -> {
                                val price = compPriceStr.toIntOrNull() ?: 4500
                                val vol = compVolumeStr.toIntOrNull() ?: 100
                                val maturity = compMaturityStr.toIntOrNull() ?: 12
                                val coords = GeoUtils.getCityCoordinates(compLocation)
                                val item = CompostItem(
                                    id = "comp_${UUID.randomUUID().toString().take(6)}",
                                    title = compTitle.ifBlank { "Compost Bio" },
                                    category = compCategory,
                                    producerName = compProducerName.ifBlank { "Compost" },
                                    location = compLocation,
                                    latitude = coords.first,
                                    longitude = coords.second,
                                    pricePerUnitCfa = price.toLong(),
                                    unit = compUnit,
                                    volumeAvailable = vol,
                                    npkRatio = compNpk,
                                    maturityWeeks = maturity,
                                    phone = compPhone,
                                    imageUrl = compImageUrl,
                                    co2SavedKgPerUnit = 2.4,
                                    description = compDescription
                                )
                                onAddCompost(item)
                            }
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_listing_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publier l'annonce maintenant", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
