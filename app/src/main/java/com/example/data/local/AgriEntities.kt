package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val role: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val ecoPoints: Int,
    val avatarUrl: String,
    val isVerified: Boolean,
    val memberSince: String
)

@Entity(tableName = "app_notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean,
    val targetDestination: String
)

@Entity(tableName = "equipment_items")
data class EquipmentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val offerType: String,
    val priceCfa: Long,
    val rentalUnit: String,
    val hpPower: Int,
    val condition: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val ownerName: String,
    val ownerPhone: String,
    val authorId: String,
    val imageUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val depositCfa: Long,
    val operatorAvailable: Boolean,
    val isRentedCurrently: Boolean,
    val currentRenterName: String,
    val daysRemaining: Int,
    val description: String,
    val specsString: String,
    val isEcoCertified: Boolean
)

@Entity(tableName = "produce_items")
data class ProduceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val producerName: String,
    val producerRole: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val priceCfa: Long,
    val unit: String,
    val availableStock: Int,
    val minOrder: Int,
    val isOrganicCertified: Boolean,
    val harvestDate: String,
    val phone: String,
    val authorId: String,
    val imageUrl: String,
    val description: String
)

@Entity(tableName = "compost_items")
data class CompostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val pricePerUnitCfa: Long,
    val unit: String,
    val volumeAvailable: Int,
    val npkRatio: String,
    val maturityWeeks: Int,
    val producerName: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val authorId: String,
    val imageUrl: String,
    val co2SavedKgPerUnit: Double,
    val description: String,
    val isCertifiedBio: Boolean
)

@Entity(tableName = "waste_requests")
data class WasteRequestEntity(
    @PrimaryKey val id: String,
    val farmerName: String,
    val farmerPhone: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val wasteType: String,
    val weightKg: Int,
    val pickupDate: String,
    val notes: String = "",
    val status: String = "EN_ATTENTE",
    val rewardEcoPoints: Int = 100,
    val assignedDriver: String = "Diallo Moussa (Transporteur Partenaire)",
    val driverPhone: String = "+225 07 44 22 11",
    val pickupSlot: String = "Matinée (08h00 - 12h00)",
    val pickupMode: String = "Enlèvement Camion AgriShop",
    val vehicleType: String = "Camion Benne 5 Tonnes",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rental_contracts")
data class RentalContractEntity(
    @PrimaryKey val id: String,
    val equipmentId: String,
    val equipmentTitle: String,
    val renterName: String,
    val renterPhone: String,
    val ownerPhone: String,
    val startDate: String,
    val endDate: String,
    val durationDays: Int,
    val dailyRateCfa: Long,
    val totalAmountCfa: Long,
    val depositPaidCfa: Long,
    val operatorIncluded: Boolean,
    val status: String,
    val paymentProvider: String,
    val transactionRef: String
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey val id: String,
    val transactionRef: String,
    val amountCfa: Long,
    val feeCfa: Long,
    val provider: String,
    val phoneNumber: String,
    val purpose: String,
    val status: String,
    val timestamp: Long,
    val receiptCode: String
)

@Entity(tableName = "forum_posts")
data class ForumPostEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorRole: String,
    val region: String,
    val timestampStr: String,
    val topic: String,
    val content: String,
    val repliesCount: Int,
    val likesCount: Int,
    val isQuestion: Boolean
)
