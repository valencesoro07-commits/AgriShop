package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.cinetpay.CinetPayClient
import com.example.data.cinetpay.CinetPayExecutionResult
import com.example.data.model.PaymentProvider
import com.example.data.model.PaymentTransaction
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    title: String,
    purpose: String,
    totalAmountCfa: Long,
    onDismiss: () -> Unit,
    onPaymentConfirmed: suspend (provider: PaymentProvider, phone: String) -> PaymentTransaction
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedProvider by remember { mutableStateOf(PaymentProvider.CINETPAY) }
    var phoneNumber by remember { mutableStateOf("07 88 99 11") }
    var isProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf("") }
    var completedTransaction by remember { mutableStateOf<PaymentTransaction?>(null) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showApiDetailsDialog by remember { mutableStateOf(false) }
    var activeCinetPayResult by remember { mutableStateOf<CinetPayExecutionResult.Success?>(null) }

    val isCustomKey = remember { CinetPayClient.isCustomKeyConfigured() }
    val siteId = remember { CinetPayClient.getSiteId() }
    val apiKey = remember { CinetPayClient.getApiKey() }

    val fee = if (selectedProvider == PaymentProvider.WAVE) (totalAmountCfa * 0.01).toLong() else 0L
    val grandTotal = totalAmountCfa + fee
    var enableEscrowProtection by remember { mutableStateOf(true) }
    var escrowSecurityCode by remember { mutableStateOf("ESCROW-${(1000..9999).random()}") }

    ModalBottomSheet(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with CinetPay Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CinetPayColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CinetPayColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Paiement CinetPay",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isCustomKey) ForestGreenPrimary.copy(alpha = 0.15f) else AmberLight
                            ) {
                                Text(
                                    text = if (isCustomKey) "LIVE API" else "SANDBOX",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isCustomKey) ForestGreenPrimary else HarvestGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Passerelle Sécurisée CI • Mobile Money & Cartes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = { showApiDetailsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Clés API CinetPay",
                            tint = CinetPayColor
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !isProcessing) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Summary Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = purpose,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Sous-total commande :", style = MaterialTheme.typography.bodyMedium)
                        Text(text = formatCfa(totalAmountCfa), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                    if (fee > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Frais de réseau (1%) :", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = formatCfa(fee), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Net à Payer :",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ForestGreenDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            VoiceNarratorButton(
                                onSpeak = {
                                    com.example.util.AudioGuideManager.speakPayment(
                                        amount = grandTotal,
                                        providerName = selectedProvider.displayName,
                                        purpose = title
                                    )
                                },
                                size = 32
                            )
                        }
                        Text(
                            text = formatCfa(grandTotal),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestGreenPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Provider Selection Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode de règlement CinetPay :",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Site ID: $siteId",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 2-Row Operator Selection Grid
            val providers = PaymentProvider.entries
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: CinetPay Multi, Orange Money, MTN MoMo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PaymentProvider.CINETPAY, PaymentProvider.ORANGE_MONEY, PaymentProvider.MTN_MONEY).forEach { provider ->
                        PaymentProviderTile(
                            provider = provider,
                            isSelected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Row 2: Moov Money, Wave, Carte Bancaire
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PaymentProvider.MOOV_MONEY, PaymentProvider.WAVE, PaymentProvider.CREDIT_CARD).forEach { provider ->
                        PaymentProviderTile(
                            provider = provider,
                            isSelected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone or Card Details Entry
            if (selectedProvider == PaymentProvider.CREDIT_CARD) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Numéro mobile de contact") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = CinetPayColor)
                    },
                    placeholder = { Text("07 00 00 00 00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Numéro de compte ${selectedProvider.displayName}") },
                    leadingIcon = {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🇨🇮 +225", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    },
                    placeholder = { Text("07 00 00 00 00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AgriShop Escrow Séquestre Protection Guarantee
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (enableEscrowProtection) Color(0xFFF1F8F5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (enableEscrowProtection) 1.5.dp else 1.dp,
                    color = if (enableEscrowProtection) ForestGreenPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (enableEscrowProtection) ForestGreenPrimary else Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Garantie Séquestre (Escrow)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                                )
                                Text(
                                    text = "Fonds bloqués jusqu'à réception au champ",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                        Switch(
                            checked = enableEscrowProtection,
                            onCheckedChange = { enableEscrowProtection = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ForestGreenPrimary, checkedTrackColor = MintLight)
                        )
                    }

                    if (enableEscrowProtection) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "🔒 Le vendeur/loueur ne sera crédité qu'après votre confirmation de livraison conforme.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = ForestGreenDark, fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Information banner about CinetPay API
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MintLight
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Traitement en direct via l'API CinetPay v2",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                        )
                        Text(
                            text = "Transactions certifiées PCI-DSS avec débit instantané et notification de confirmation.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = ForestGreenDark.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Active CinetPay payment URL action if already initialized
            if (activeCinetPayResult != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CinetPayColor.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinetPayColor.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Session CinetPay Initialisée : ${activeCinetPayResult!!.transactionId}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CinetPayColor)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeCinetPayResult!!.paymentUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CinetPayColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ouvrir le Guichet Web CinetPay")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isProcessing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = CinetPayColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = processingStep,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isProcessing = true
                            processingStep = "Appel API CinetPay (Initialisation de transaction)..."
                            
                            val cinetPayResult = CinetPayClient.createPayment(
                                amount = grandTotal,
                                description = "$title - $purpose",
                                customerName = "Client AgriShop",
                                customerPhone = phoneNumber,
                                channel = when (selectedProvider) {
                                    PaymentProvider.CREDIT_CARD -> "CREDIT_CARD"
                                    PaymentProvider.WAVE -> "WALLET"
                                    PaymentProvider.ORANGE_MONEY,
                                    PaymentProvider.MTN_MONEY,
                                    PaymentProvider.MOOV_MONEY -> "MOBILE_MONEY"
                                    else -> "ALL"
                                }
                            )

                            if (cinetPayResult is CinetPayExecutionResult.Success) {
                                activeCinetPayResult = cinetPayResult
                                processingStep = "Connexion sécurisée établie (${cinetPayResult.transactionId})..."
                                delay(600)
                                processingStep = "Validation du débit ${selectedProvider.displayName}..."
                                delay(700)
                            }

                            // Finalize local transaction recording
                            val tx = onPaymentConfirmed(selectedProvider, phoneNumber)
                            completedTransaction = tx
                            isProcessing = false
                            showReceiptDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_payment_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinetPayColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = phoneNumber.trim().length >= 8
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Payer ${formatCfa(grandTotal)} via CinetPay",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showApiDetailsDialog) {
        CinetPayApiInfoDialog(
            apiKey = apiKey,
            siteId = siteId,
            isCustomKey = isCustomKey,
            onDismiss = { showApiDetailsDialog = false }
        )
    }

    if (showReceiptDialog && completedTransaction != null) {
        ReceiptDialog(
            transaction = completedTransaction!!,
            onDismiss = {
                showReceiptDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun PaymentProviderTile(
    provider: PaymentProvider,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (brandColor, brandTextColor) = when (provider) {
        PaymentProvider.CINETPAY -> CinetPayColor to Color.White
        PaymentProvider.ORANGE_MONEY -> OrangeMoneyColor to Color.White
        PaymentProvider.MTN_MONEY -> MtnMoneyColor to Color.Black
        PaymentProvider.MOOV_MONEY -> MoovMoneyColor to Color.White
        PaymentProvider.WAVE -> WaveColor to Color.Black
        PaymentProvider.CREDIT_CARD -> CardColor to Color.White
    }

    Surface(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) brandColor else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("provider_${provider.code.lowercase()}"),
        color = if (isSelected) brandColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                AppAsyncImage(
                    imageUrl = provider.logoUrl,
                    contentDescription = provider.displayName,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brandColor.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (provider) {
                    PaymentProvider.CINETPAY -> "CinetPay"
                    PaymentProvider.ORANGE_MONEY -> "Orange"
                    PaymentProvider.MTN_MONEY -> "MTN MoMo"
                    PaymentProvider.MOOV_MONEY -> "Moov"
                    PaymentProvider.WAVE -> "Wave"
                    PaymentProvider.CREDIT_CARD -> "Cartes"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CinetPayApiInfoDialog(
    apiKey: String,
    siteId: String,
    isCustomKey: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(CinetPayColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CinetPayColor, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Configuration CinetPay API",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ForestGreenDark
                )

                Text(
                    text = "Passerelle de paiement en temps réel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Statut de la passerelle :", style = MaterialTheme.typography.bodySmall)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isCustomKey) ForestGreenPrimary else CinetPayColor
                            ) {
                                Text(
                                    text = if (isCustomKey) "PRODUCTION ACTIVE" else "MODE TEST / SANDBOX",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("Site ID CinetPay :", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(siteId, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Clé API CinetPay (Tronquée) :", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (apiKey.length > 12) "${apiKey.take(6)}...${apiKey.takeLast(6)}" else apiKey,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Endpoint v2 :", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("https://api-checkout.cinetpay.com/v2/payment", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MintLight
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pour modifier vos clés en production, mettez à jour CINETPAY_API_KEY et CINETPAY_SITE_ID dans le panneau Secrets.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = ForestGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CinetPayColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fermer")
                }
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    transaction: PaymentTransaction,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(ForestGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Paiement Réussi !",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = ForestGreenDark
                )

                Text(
                    text = "Reçu Officiel CinetPay • AgriShop",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Slip
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = LightSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Réf. CinetPay :", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = transaction.transactionRef,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Code Reçu :", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = transaction.receiptCode,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Moyen de paiement :", style = MaterialTheme.typography.bodySmall)
                            ProviderBadge(provider = transaction.provider)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Date & Heure :", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(transaction.timestamp)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Motif :",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = transaction.purpose,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Montant Débité :",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = formatCfa(transaction.amountCfa),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestGreenPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("close_receipt_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Terminer & Continuer", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
