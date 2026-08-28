package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractStatus
import com.example.data.model.RentalContract
import com.example.ui.components.ProviderBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatCfa
import com.example.ui.theme.*

@Composable
fun RentalsManagementScreen(
    contracts: List<RentalContract>,
    onReturnEquipment: (contractId: String, equipmentId: String) -> Unit,
    onNavigateToEquipment: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: En cours, 1: Historique / Restitués
    var returnedSuccessMessage by remember { mutableStateOf<String?>(null) }

    val activeContracts = remember(contracts) { contracts.filter { it.status == ContractStatus.ACTIVE } }
    val completedContracts = remember(contracts) { contracts.filter { it.status == ContractStatus.COMPLETED } }

    val totalSpent = remember(contracts) { contracts.sumOf { it.totalAmountCfa } }
    val totalDepositsHeld = remember(activeContracts) { activeContracts.sumOf { it.depositPaidCfa } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // Top Overview Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ForestGreenPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Gestion du Parc & Locations",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Suivez vos machines en cours d'exploitation et restitutions",
                    style = MaterialTheme.typography.bodySmall.copy(color = MintLight)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x33FFFFFF)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Machines Actives", style = MaterialTheme.typography.labelSmall, color = MintLight)
                            Text(
                                text = "${activeContracts.size} engin(s)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x33FFFFFF)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Cautions Engagées", style = MaterialTheme.typography.labelSmall, color = MintLight)
                            Text(
                                text = formatCfa(totalDepositsHeld),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AmberSun
                                )
                            )
                        }
                    }
                }
            }
        }

        // Notification Banner if returned
        if (returnedSuccessMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        text = returnedSuccessMessage!!,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = ForestGreenDark,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { returnedSuccessMessage = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Tabs (En cours / Historique)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("En Exploitation (${activeContracts.size})")
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Historique & Clôturés (${completedContracts.size})") }
            )
        }

        val displayList = if (selectedTab == 0) activeContracts else completedContracts

        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.AssignmentLate else Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedTab == 0) "Aucune location en cours actuellement" else "Aucun contrat archivé",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedTab == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToEquipment,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Agriculture, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Parcourir les Équipements")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayList, key = { it.id }) { contract ->
                    RentalContractCard(
                        contract = contract,
                        onReturnClick = {
                            onReturnEquipment(contract.id, contract.equipmentId)
                            returnedSuccessMessage = "La machine '${contract.equipmentTitle}' a été marquée comme restituée. Caution de ${formatCfa(contract.depositPaidCfa)} débloquée !"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RentalContractCard(
    contract: RentalContract,
    onReturnClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contract_card_${contract.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (contract.status == ContractStatus.ACTIVE) MintLight else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = contract.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (contract.status == ContractStatus.ACTIVE) ForestGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                ProviderBadge(provider = contract.paymentProvider)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = contract.equipmentTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Période :", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${contract.startDate} -> ${contract.endDate}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Durée :", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${contract.durationDays} jour(s)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = LightSurfaceVariant
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Locataire : ${contract.renterName}", style = MaterialTheme.typography.bodySmall)
                        Text(text = contract.renterPhone, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    if (contract.operatorIncluded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chauffeur / Opérateur inclus",
                                style = MaterialTheme.typography.bodySmall.copy(color = ForestGreenPrimary, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Contrat :", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatCfa(contract.totalAmountCfa),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestGreenPrimary
                        )
                    )
                    Text(
                        text = "(Dont caution : ${formatCfa(contract.depositPaidCfa)})",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (contract.status == ContractStatus.ACTIVE) {
                    OutlinedButton(
                        onClick = onReturnClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("return_button_${contract.id}")
                    ) {
                        Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restituer Engin", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
