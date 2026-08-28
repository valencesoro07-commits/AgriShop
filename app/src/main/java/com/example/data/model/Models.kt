package com.example.data.model

import kotlin.math.*

enum class UserRole(val label: String, val icon: String) {
    FARMER("Producteur / Planteur", "🌾"),
    EQUIPMENT_OWNER("Propriétaire d'Engins", "🚜"),
    BUYER("Acheteur / Grossiste", "🛒"),
    RECYCLER("Recycleur & Composteur", "♻️")
}

data class UserProfile(
    val id: String = "usr_default",
    val fullName: String = "Kouassi Jean-Marc",
    val phone: String = "+225 07 88 99 11",
    val email: String = "kouassi.agri@gmail.com",
    val role: UserRole = UserRole.FARMER,
    val region: String = "Yamoussoukro",
    val latitude: Double = 6.8276,
    val longitude: Double = -5.2893,
    val ecoPoints: Int = 450,
    val avatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80",
    val isVerified: Boolean = true,
    val memberSince: String = "Mars 2026"
)

enum class NotificationType(val label: String, val icon: String) {
    RENTAL_REMINDER("Rappel Restitution", "🚜"),
    ECO_POINTS("Points Éco Gagnés", "🌱"),
    FORUM_REPLY("Réponse Forum", "💬"),
    PAYMENT_SUCCESS("Paiement Confirmé", "💳"),
    WASTE_PICKUP("Collecte Déchets", "♻️"),
    NEW_LISTING("Nouvelle Offre Proche", "📍")
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetDestination: String = "HOME"
)

enum class EquipmentCategory(val label: String, val iconName: String) {
    ALL("Tous", "all"),
    BIOGAS("Biogaz & Biodigesteurs 🌱", "biogas"),
    SOLAR_ENERGY("Solaire & Pompes Vertes ☀️", "solar"),
    ECO_TRANSFORMATION("Broyeurs & Valorisation ♻️", "recycling"),
    TRACTOR("Tracteurs", "tractor"),
    TILLER("Motoculteurs", "tiller"),
    HARVESTER("Moissonneuses", "harvester"),
    IRRIGATION("Irrigation & Pompes", "irrigation"),
    SPRAYER("Pulvérisateurs", "sprayer"),
    PLANTER("Semoirs & Planteuses", "planter"),
    TRAILER("Remorques & Transport", "trailer")
}

enum class OfferType(val label: String) {
    RENT("À Louer"),
    SALE("À Vendre"),
    BOTH("Location & Vente")
}

enum class ProduceCategory(val label: String) {
    ALL("Toutes"),
    CEREALS("Céréales (Maïs, Riz, Mil)"),
    TUBERS("Tubercules (Manioc, Igname)"),
    CASH_CROPS("Cultures de rente (Cacao, Café, Anacarde)"),
    VEGETABLES("Légumes & Maraîchage"),
    FRUITS("Fruits Tropicaux")
}

enum class CompostCategory(val label: String) {
    ALL("Tout"),
    MATURE_COMPOST("Compost Ennobli"),
    BIO_LIQUID("Bio-fertilisant Liquide"),
    PLANT_MULCH("Broyat & Paillis Végétal"),
    ORGANIC_FERTILIZER("Fumier & Engrais Organique")
}

enum class WasteType(val label: String, val carbonRate: Double, val icon: String = "🍂") {
    CEREAL_STRAW("Paille & Tiges de Maïs/Riz", 0.75, "🌾"),
    COCOA_PODS("Cabosses de Cacao & Café", 0.90, "🍫"),
    MANURE("Fumier bovin & Fientes de volaille", 0.60, "🐓"),
    CASSAVA_PEELS("Épluchures & Pulpe de Manioc", 0.70, "🍠"),
    VEGETABLE_SCRAPS("Déchets de maraîchage & fruits", 0.50, "🥬"),
    COTTON_STALKS("Tiges de Coton & Biomasse sèche", 0.80, "🌿"),
    PALM_OIL_BUNCHES("Rafles & Fibres de Palmier à Huile", 0.85, "🌴")
}

enum class PaymentProvider(val displayName: String, val code: String, val prefix: String) {
    ORANGE_MONEY("Orange Money", "OM", "#144#"),
    MTN_MONEY("MTN Mobile Money", "MTN", "*133#"),
    MOOV_MONEY("Moov Money", "MOOV", "*155#"),
    WAVE("Wave", "WAVE", "Wave App / QR"),
    CINETPAY("CinetPay Multi-Passerelle", "CINETPAY", "API v2 / Tous Réseaux"),
    CREDIT_CARD("Carte Bancaire", "CARD", "Visa / Mastercard")
}

enum class ContractStatus(val label: String) {
    ACTIVE("En cours"),
    COMPLETED("Terminé / Restitué"),
    OVERDUE("À restituer")
}

enum class CollectionStatus(val label: String, val badgeColorHex: Long) {
    EN_ATTENTE("À effectuer (En attente)", 0xFFFF9800),
    PLANIFIEE("Planifiée", 0xFF2196F3),
    EN_COURS("En cours d'enlèvement", 0xFF9C27B0),
    COLLECTEE("Collectée & Recyclée", 0xFF2E7D32),
    ANNULEE("Annulée", 0xFF757575)
}

data class EquipmentItem(
    val id: String,
    val title: String,
    val category: EquipmentCategory,
    val offerType: OfferType,
    val priceCfa: Long,
    val rentalUnit: String = "jour", // jour, semaine, vente directe
    val hpPower: Int = 0,
    val condition: String,
    val location: String,
    val latitude: Double = 6.8276,
    val longitude: Double = -5.2893,
    val ownerName: String,
    val ownerPhone: String,
    val authorId: String = "user_1",
    val imageUrl: String = "https://images.unsplash.com/photo-1592982537447-7440770cbfc9?w=600&auto=format&fit=crop&q=80",
    val rating: Float = 4.8f,
    val reviewCount: Int = 12,
    val depositCfa: Long = 0L,
    val operatorAvailable: Boolean = true,
    val isRentedCurrently: Boolean = false,
    val currentRenterName: String = "",
    val daysRemaining: Int = 0,
    val description: String,
    val specs: List<String> = emptyList(),
    val isEcoCertified: Boolean = true
)

data class ProduceItem(
    val id: String,
    val title: String,
    val category: ProduceCategory,
    val producerName: String,
    val producerRole: String,
    val location: String,
    val latitude: Double = 6.8276,
    val longitude: Double = -5.2893,
    val priceCfa: Long,
    val unit: String, // kg, sac 50kg, tonne
    val availableStock: Int,
    val minOrder: Int,
    val isOrganicCertified: Boolean,
    val harvestDate: String,
    val phone: String,
    val authorId: String = "user_2",
    val imageUrl: String = "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=600&auto=format&fit=crop&q=80",
    val description: String
)

data class CompostItem(
    val id: String,
    val title: String,
    val category: CompostCategory,
    val pricePerUnitCfa: Long,
    val unit: String, // Sac 25kg, Sac 50kg, Tonne, Bidon 20L
    val volumeAvailable: Int,
    val npkRatio: String,
    val maturityWeeks: Int,
    val producerName: String,
    val location: String,
    val latitude: Double = 6.8276,
    val longitude: Double = -5.2893,
    val phone: String,
    val authorId: String = "user_3",
    val imageUrl: String = "https://images.unsplash.com/photo-1588880331179-bc9b93a8cb5e?w=600&auto=format&fit=crop&q=80",
    val co2SavedKgPerUnit: Double,
    val description: String,
    val isCertifiedBio: Boolean = true
)

data class WasteCollectionRequest(
    val id: String,
    val farmerName: String,
    val farmerPhone: String,
    val location: String,
    val latitude: Double = 6.8276,
    val longitude: Double = -5.2893,
    val wasteType: WasteType,
    val weightKg: Int,
    val pickupDate: String,
    val notes: String = "",
    val status: String = "EN_ATTENTE", // EN_ATTENTE, PLANIFIEE, EN_COURS, COLLECTEE, ANNULEE
    val rewardEcoPoints: Int = 100,
    val assignedDriver: String = "Diallo Moussa (Transporteur Partenaire)",
    val driverPhone: String = "+225 07 44 22 11",
    val pickupSlot: String = "Matinée (08h00 - 12h00)",
    val pickupMode: String = "Enlèvement Camion AgriShop",
    val vehicleType: String = "Camion Benne 5 Tonnes",
    val trackingCode: String = "COL-CI-${id.takeLast(4).uppercase()}",
    val co2SavedKg: Double = weightKg * 0.75,
    val createdAt: Long = System.currentTimeMillis()
)

data class RentalContract(
    val id: String,
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
    val status: ContractStatus,
    val paymentProvider: PaymentProvider,
    val transactionRef: String
)

data class PaymentTransaction(
    val id: String,
    val transactionRef: String,
    val amountCfa: Long,
    val feeCfa: Long,
    val provider: PaymentProvider,
    val phoneNumber: String,
    val purpose: String,
    val status: String = "SUCCÈS",
    val timestamp: Long = System.currentTimeMillis(),
    val receiptCode: String
)

data class EcoImpactOverview(
    val totalCompostProducedKg: Long = 18450L,
    val organicWasteRecycledKg: Long = 32800L,
    val co2PreventedKg: Long = 24600L,
    val farmerPointsBalance: Int = 850,
    val currentEcoTier: String = "Maître Agro-Écologique"
)

data class ForumPost(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val region: String,
    val timestampStr: String,
    val topic: String,
    val content: String,
    val repliesCount: Int,
    val likesCount: Int,
    val isQuestion: Boolean = true
)

// Geo Utils & Haversine Distance
object GeoUtils {
    data class CityCoordinates(val name: String, val lat: Double, val lng: Double)

    val PRESET_CITIES = listOf(
        CityCoordinates("Yamoussoukro", 6.8276, -5.2893),
        CityCoordinates("Bouaké", 7.6905, -5.0300),
        CityCoordinates("Abidjan (Bingerville)", 5.3600, -4.0083),
        CityCoordinates("Korhogo", 9.4580, -5.6296),
        CityCoordinates("San-Pédro", 4.7485, -6.6363),
        CityCoordinates("Daloa", 6.8774, -6.4502),
        CityCoordinates("Man", 7.4125, -7.5538),
        CityCoordinates("Ferkessédougou", 9.5928, -5.1945),
        CityCoordinates("Divo", 5.8374, -5.3572),
        CityCoordinates("Toumodi", 6.5574, -5.0177),
        CityCoordinates("Agboville", 5.9280, -4.2133),
        CityCoordinates("Dimbokro", 6.6467, -4.7051)
    )

    fun getCityCoordinates(cityName: String): Pair<Double, Double> {
        val match = PRESET_CITIES.find { cityName.contains(it.name, ignoreCase = true) }
        return if (match != null) Pair(match.lat, match.lng) else Pair(6.8276, -5.2893)
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun formatDistance(km: Double): String {
        return if (km < 1.0) {
            "${(km * 1000).toInt()} m"
        } else if (km < 10.0) {
            String.format("%.1f km", km)
        } else {
            "${km.toInt()} km"
        }
    }
}
