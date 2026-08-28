package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduceNetworkScreen(
    produceList: List<ProduceItem>,
    forumPosts: List<ForumPost>,
    userCoordinates: Pair<Double, Double>,
    onAddProduceClick: () -> Unit,
    onAddForumPost: (ForumPost) -> Unit,
    onBuyProduce: (produce: ProduceItem, quantity: Int, provider: PaymentProvider, phone: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTopTab by remember { mutableStateOf(0) } // 0: Marché des Récoltes, 1: Réseau & Entraide Agricole
    var selectedCategory by remember { mutableStateOf(ProduceCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduceForDetail by remember { mutableStateOf<ProduceItem?>(null) }
    var purchaseQuantity by remember { mutableStateOf(1) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    // New Forum Post State
    var showNewPostDialog by remember { mutableStateOf(false) }
    var newPostTopic by remember { mutableStateOf("") }
    var newPostContent by remember { mutableStateOf("") }
    var newPostAuthor by remember { mutableStateOf("Planteur Innovant") }
    var newPostRegion by remember { mutableStateOf("Bouaké") }

    val filteredProduce = remember(produceList, selectedCategory, searchQuery) {
        produceList.filter { item ->
            val matchesCategory = selectedCategory == ProduceCategory.ALL || item.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.producerName.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTopTab == 0) onAddProduceClick() else showNewPostDialog = true
                },
                containerColor = if (selectedTopTab == 0) HarvestGold else ForestGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .testTag("add_produce_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedTopTab == 0) Icons.Default.Add else Icons.Default.Create,
                        contentDescription = "Ajouter"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (selectedTopTab == 0) "Vendre Récolte" else "Poser une question",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Tab Row (Marketplace vs Network)
            TabRow(
                selectedTabIndex = selectedTopTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTopTab == 0,
                    onClick = { selectedTopTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bourse Récoltes")
                        }
                    }
                )
                Tab(
                    selected = selectedTopTab == 1,
                    onClick = { selectedTopTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Réseau & Entraide")
                        }
                    }
                )
            }

            if (selectedTopTab == 0) {
                // Marketplace Subview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rechercher maïs, cacao, manioc, tomate...", fontSize = 14.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ProduceCategory.entries) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.label, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProduce, key = { it.id }) { produce ->
                        ProduceItemCard(
                            produce = produce,
                            userCoordinates = userCoordinates,
                            onClick = {
                                selectedProduceForDetail = produce
                                purchaseQuantity = produce.minOrder.coerceAtLeast(1)
                            }
                        )
                    }
                }
            } else {
                // Farmer Network & Forum Subview
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
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
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.People, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Communauté des Agriculteurs & Producteurs",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ForestGreenDark
                                    )
                                    Text(
                                        text = "Regroupement pour achat en gros, partage d'expériences et alertes prix.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ForestGreenDark.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    items(forumPosts, key = { it.id }) { post ->
                        ForumPostCard(post = post)
                    }
                }
            }
        }
    }

    // Produce Detail Sheet
    if (selectedProduceForDetail != null) {
        val prod = selectedProduceForDetail!!
        val calculatedTotal = prod.priceCfa * purchaseQuantity

        ModalBottomSheet(
            onDismissRequest = { selectedProduceForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Real Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AppAsyncImage(
                        imageUrl = prod.imageUrl,
                        contentDescription = prod.title,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (prod.isOrganicCertified) {
                        Surface(
                            shape = RoundedCornerShape(bottomEnd = 10.dp),
                            color = MintLight,
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "🌱 Certifié Bio",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenDark
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    DistanceBadge(
                        itemLat = prod.latitude,
                        itemLng = prod.longitude,
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
                        text = prod.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = prod.harvestDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Producteur : ${prod.producerName} (${prod.producerRole})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = ForestGreenPrimary
                )

                Text(
                    text = "Lieu de récolte : ${prod.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AmberLight
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Prix Direct Producteur", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6D4C00))
                            Text(
                                text = formatCfa(prod.priceCfa),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF6D4C00)
                                )
                            )
                            Text(
                                text = "par ${prod.unit} (Stock disponible : ${prod.availableStock} ${prod.unit})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6D4C00).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Description du lot", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = prod.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                // Direct Contact Button
                OutlinedButton(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${prod.phone.replace(" ", "")}"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contacter le Producteur (${prod.phone})")
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
                        Column {
                            Text("Quantité commandée :", style = MaterialTheme.typography.bodyMedium)
                            Text("Min. ${prod.minOrder} ${prod.unit}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (purchaseQuantity > prod.minOrder) purchaseQuantity-- },
                                enabled = purchaseQuantity > prod.minOrder
                            ) {
                                Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = null)
                            }
                            Text(
                                text = "$purchaseQuantity ${prod.unit}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = { if (purchaseQuantity < prod.availableStock) purchaseQuantity++ },
                                enabled = purchaseQuantity < prod.availableStock
                            ) {
                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { showPaymentSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("buy_produce_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Commander & Payer ${formatCfa(calculatedTotal)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Payment Sheet for produce
    if (showPaymentSheet && selectedProduceForDetail != null) {
        val prod = selectedProduceForDetail!!
        val calculatedTotal = prod.priceCfa * purchaseQuantity

        PaymentBottomSheet(
            title = "Achat Récolte Directe",
            purpose = "${prod.title} (Qté: $purchaseQuantity ${prod.unit})",
            totalAmountCfa = calculatedTotal,
            onDismiss = {
                showPaymentSheet = false
                selectedProduceForDetail = null
            },
            onPaymentConfirmed = { provider, phone ->
                onBuyProduce(prod, purchaseQuantity, provider, phone)
                com.example.data.model.PaymentTransaction(
                    id = "pay_prod",
                    transactionRef = "${provider.code}-PROD",
                    amountCfa = calculatedTotal,
                    feeCfa = if (provider == PaymentProvider.WAVE) (calculatedTotal * 0.01).toLong() else 0L,
                    provider = provider,
                    phoneNumber = phone,
                    purpose = prod.title,
                    receiptCode = "REC-PROD-${(1000..9999).random()}"
                )
            }
        )
    }

    // New Forum Post Dialog
    if (showNewPostDialog) {
        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            title = { Text("Nouvelle Question / Annonce d'Entraide") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPostTopic,
                        onValueChange = { newPostTopic = it },
                        label = { Text("Titre / Sujet") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPostAuthor,
                        onValueChange = { newPostAuthor = it },
                        label = { Text("Votre Nom & Statut") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPostRegion,
                        onValueChange = { newPostRegion = it },
                        label = { Text("Votre Région") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPostContent,
                        onValueChange = { newPostContent = it },
                        label = { Text("Message / Demande de groupement") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPostTopic.isNotBlank() && newPostContent.isNotBlank()) {
                            val post = ForumPost(
                                id = "post_${UUID.randomUUID().toString().take(6)}",
                                authorName = newPostAuthor,
                                authorRole = "Agriculteur Membre",
                                region = newPostRegion,
                                timestampStr = "À l'instant",
                                topic = newPostTopic,
                                content = newPostContent,
                                repliesCount = 0,
                                likesCount = 1,
                                isQuestion = true
                            )
                            onAddForumPost(post)
                            showNewPostDialog = false
                            newPostTopic = ""
                            newPostContent = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) {
                    Text("Publier")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPostDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun ProduceItemCard(
    produce: ProduceItem,
    userCoordinates: Pair<Double, Double>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("produce_item_${produce.id}"),
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
                    imageUrl = produce.imageUrl,
                    contentDescription = produce.title,
                    modifier = Modifier.fillMaxSize()
                )

                if (produce.isOrganicCertified) {
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        color = MintLight,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "🌱 Bio",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenDark
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                DistanceBadge(
                    itemLat = produce.latitude,
                    itemLng = produce.longitude,
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
                        text = produce.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintLight
                    ) {
                        Text(
                            text = "Stock : ${produce.availableStock} ${produce.unit}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${produce.producerName} • ${produce.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = produce.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatCfa(produce.priceCfa),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreenPrimary
                            )
                        )
                        Text(
                            text = "/ ${produce.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                            size = 36
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Acheter Récolte", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForumPostCard(
    post: ForumPost
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${post.authorRole} • ${post.region}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = post.timestampStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.topic,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Likes",
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.likesCount}", style = MaterialTheme.typography.labelSmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Réponses",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.repliesCount} réponses", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
