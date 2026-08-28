package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentTransaction
import com.example.ui.components.ProviderBadge
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.formatCfa
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentsHistoryScreen(
    transactions: List<PaymentTransaction>,
    onNavigateBack: () -> Unit
) {
    var selectedTransactionForReceipt by remember { mutableStateOf<PaymentTransaction?>(null) }
    var showApiDialog by remember { mutableStateOf(false) }
    val totalVolume = remember(transactions) { transactions.sumOf { it.amountCfa } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ForestGreenPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Paiements & CinetPay",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    IconButton(onClick = { showApiDialog = true }) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Clés API", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x2EFFFFFF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Volume Total Transigé (CinetPay v2)", style = MaterialTheme.typography.labelSmall, color = MintLight)
                            Text(
                                text = formatCfa(totalVolume),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MintLight
                        ) {
                            Text(
                                text = "${transactions.size} transactions",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aucune transaction enregistrée",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions, key = { it.id }) { tx ->
                    PaymentTransactionItemCard(
                        transaction = tx,
                        onClick = { selectedTransactionForReceipt = tx }
                    )
                }
            }
        }
    }

    if (selectedTransactionForReceipt != null) {
        ReceiptDialog(
            transaction = selectedTransactionForReceipt!!,
            onDismiss = { selectedTransactionForReceipt = null }
        )
    }

    if (showApiDialog) {
        com.example.ui.components.CinetPayApiInfoDialog(
            apiKey = com.example.data.cinetpay.CinetPayClient.getApiKey(),
            siteId = com.example.data.cinetpay.CinetPayClient.getSiteId(),
            isCustomKey = com.example.data.cinetpay.CinetPayClient.isCustomKeyConfigured(),
            onDismiss = { showApiDialog = false }
        )
    }
}

@Composable
fun PaymentTransactionItemCard(
    transaction: PaymentTransaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("tx_item_${transaction.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MintLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.purpose,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${transaction.transactionRef} • ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(transaction.timestamp))}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                ProviderBadge(provider = transaction.provider)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCfa(transaction.amountCfa),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                )
                Text(
                    text = "Reçu 📄",
                    style = MaterialTheme.typography.labelSmall.copy(color = ForestGreenDark)
                )
            }
        }
    }
}
