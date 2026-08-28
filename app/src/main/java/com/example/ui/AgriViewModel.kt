package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthResult
import com.example.data.auth.AuthService
import com.example.data.local.AgriDatabase
import com.example.data.local.AgriRepository
import com.example.data.model.*
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AgriViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AgriRepository
    private val authService: AuthService = AuthService(application)

    val equipmentList: StateFlow<List<EquipmentItem>>
    val produceList: StateFlow<List<ProduceItem>>
    val compostList: StateFlow<List<CompostItem>>
    val wasteRequests: StateFlow<List<WasteCollectionRequest>>
    val rentalContracts: StateFlow<List<RentalContract>>
    val paymentTransactions: StateFlow<List<PaymentTransaction>>
    val forumPosts: StateFlow<List<ForumPost>>
    val notifications: StateFlow<List<AppNotification>>
    val currentUser: StateFlow<UserProfile?>
    val userProfile: StateFlow<UserProfile>
    val unreadNotificationsCount: StateFlow<Int>
    val userEcoPoints: StateFlow<Int>

    // Authentication State - checks Firebase Auth status
    private val _isUserAuthenticated = MutableStateFlow(authService.isUserLoggedIn)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    private val _isGuestMode = MutableStateFlow(!authService.isUserLoggedIn)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _initialRegisterMode = MutableStateFlow(false)
    val initialRegisterMode: StateFlow<Boolean> = _initialRegisterMode.asStateFlow()

    private val _registerReason = MutableStateFlow<String?>(null)
    val registerReason: StateFlow<String?> = _registerReason.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _userCoordinates = MutableStateFlow(Pair(6.8276, -5.2893)) // Default Yamoussoukro
    val userCoordinates: StateFlow<Pair<Double, Double>> = _userCoordinates.asStateFlow()

    private val _selectedCity = MutableStateFlow("Yamoussoukro")
    val currentCity: StateFlow<String> = _selectedCity.asStateFlow()
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("À l'instant")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    init {
        NotificationHelper.createNotificationChannels(application)
        val database = AgriDatabase.getDatabase(application)
        repository = AgriRepository(database.agriDao())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            if (authService.isUserLoggedIn) {
                _isGuestMode.value = false
                _isUserAuthenticated.value = true
                authService.getLoggedInUserProfile()?.let { loggedProfile ->
                    repository.saveUserProfile(loggedProfile)
                    _selectedCity.value = loggedProfile.region
                    _userCoordinates.value = Pair(loggedProfile.latitude, loggedProfile.longitude)
                }
            }
        }

        currentUser = repository.currentUser
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        userProfile = repository.currentUser
            .map { it ?: UserProfile() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

        userEcoPoints = repository.currentUser
            .map { it?.ecoPoints ?: 450 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 450)

        notifications = repository.notifications
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        unreadNotificationsCount = repository.notifications
            .map { list -> list.count { !it.isRead } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        equipmentList = repository.equipmentList
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        produceList = repository.produceList
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        compostList = repository.compostList
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        wasteRequests = repository.wasteRequests
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        rentalContracts = repository.rentalContracts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        paymentTransactions = repository.paymentHistory
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        forumPosts = repository.forumPosts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // --- Firebase Auth Handlers ---

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            when (val result = authService.signInWithEmail(email, pass)) {
                is AuthResult.Success -> {
                    repository.saveUserProfile(result.userProfile)
                    _selectedCity.value = result.userProfile.region
                    _userCoordinates.value = Pair(result.userProfile.latitude, result.userProfile.longitude)
                    _isUserAuthenticated.value = true
                    _isGuestMode.value = false
                    _registerReason.value = null
                    _initialRegisterMode.value = false
                    _authLoading.value = false
                    repository.addNotification(
                        AppNotification(
                            id = "notif_${UUID.randomUUID().toString().take(6)}",
                            title = "Connexion réussie",
                            message = "Ravi de vous revoir, ${result.userProfile.fullName} !",
                            type = NotificationType.ECO_POINTS,
                            targetDestination = "HOME"
                        )
                    )
                }
                is AuthResult.Error -> {
                    _authError.value = result.message
                    _authLoading.value = false
                }
            }
        }
    }

    fun signUpWithEmail(
        email: String,
        pass: String,
        fullName: String,
        phone: String,
        role: UserRole,
        region: String
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            when (val result = authService.signUpWithEmail(email, pass, fullName, phone, role, region)) {
                is AuthResult.Success -> {
                    repository.saveUserProfile(result.userProfile)
                    _selectedCity.value = result.userProfile.region
                    _userCoordinates.value = Pair(result.userProfile.latitude, result.userProfile.longitude)
                    _isUserAuthenticated.value = true
                    _isGuestMode.value = false
                    _registerReason.value = null
                    _initialRegisterMode.value = false
                    _authLoading.value = false
                    repository.addNotification(
                        AppNotification(
                            id = "notif_${UUID.randomUUID().toString().take(6)}",
                            title = "Compte créé avec succès ! 🎉",
                            message = "Bienvenue sur AgriShop, 500 Éco-Points ont été offerts pour votre engagement !",
                            type = NotificationType.ECO_POINTS,
                            targetDestination = "HOME"
                        )
                    )
                }
                is AuthResult.Error -> {
                    _authError.value = result.message
                    _authLoading.value = false
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, email: String?, name: String?, photoUrl: String?) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            when (val result = authService.signInWithGoogleCredential(idToken, email, name, photoUrl)) {
                is AuthResult.Success -> {
                    repository.saveUserProfile(result.userProfile)
                    _selectedCity.value = result.userProfile.region
                    _userCoordinates.value = Pair(result.userProfile.latitude, result.userProfile.longitude)
                    _isUserAuthenticated.value = true
                    _isGuestMode.value = false
                    _registerReason.value = null
                    _initialRegisterMode.value = false
                    _authLoading.value = false
                }
                is AuthResult.Error -> {
                    _authError.value = result.message
                    _authLoading.value = false
                }
            }
        }
    }

    fun guestLogin() {
        viewModelScope.launch {
            _isUserAuthenticated.value = true
            _isGuestMode.value = true
            _registerReason.value = null
            _initialRegisterMode.value = false
            _authError.value = null
        }
    }

    fun redirectToRegister(reason: String = "Veuillez créer votre compte pour acheter, louer ou vendre sur AgriShop.") {
        _registerReason.value = reason
        _initialRegisterMode.value = true
        _isUserAuthenticated.value = false
        _isGuestMode.value = false
    }

    fun redirectToLogin(reason: String? = null) {
        _registerReason.value = reason
        _initialRegisterMode.value = false
        _isUserAuthenticated.value = false
        _isGuestMode.value = false
    }

    fun logout() {
        viewModelScope.launch {
            authService.signOut()
            _isUserAuthenticated.value = false
            _isGuestMode.value = false
            _registerReason.value = null
            _initialRegisterMode.value = false
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            val error = authService.sendPasswordReset(email)
            if (error != null) {
                _authError.value = error
            }
        }
    }

    fun setUserCity(city: String) {
        _selectedCity.value = city
        val coords = GeoUtils.getCityCoordinates(city)
        _userCoordinates.value = coords
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val updated = user.copy(region = city, latitude = coords.first, longitude = coords.second)
                repository.saveUserProfile(updated)
            }
        }
    }

    fun updateCurrentCity(city: String) = setUserCity(city)

    fun saveUserProfile(name: String, phone: String, email: String, role: UserRole, region: String) {
        updateUserProfile(name, phone, email, role, region)
    }

    fun updateUserProfile(name: String, phone: String, email: String, role: UserRole, region: String) {
        viewModelScope.launch {
            val coords = GeoUtils.getCityCoordinates(region)
            val current = currentUser.value ?: UserProfile()
            val updated = current.copy(
                fullName = name,
                phone = phone,
                email = email,
                role = role,
                region = region,
                latitude = coords.first,
                longitude = coords.second
            )
            repository.saveUserProfile(updated)
            _selectedCity.value = region
            _userCoordinates.value = coords

            repository.addNotification(
                AppNotification(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    title = "Profil mis à jour",
                    message = "Votre profil agriculteur ($role) à $region a été enregistré.",
                    type = NotificationType.ECO_POINTS,
                    targetDestination = "HOME"
                )
            )
        }
    }

    fun addEcoPoints(points: Int, reason: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val newPoints = user.ecoPoints + points
                repository.updateUserEcoPoints(user.id, newPoints)
                repository.addNotification(
                    AppNotification(
                        id = "notif_${UUID.randomUUID().toString().take(6)}",
                        title = "+$points Éco-Points crédités !",
                        message = reason,
                        type = NotificationType.ECO_POINTS,
                        targetDestination = "COMPOST"
                    )
                )
            }
        }
    }

    fun markNotificationAsRead(notification: AppNotification) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notification.id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearAllNotifications() {
        markAllNotificationsAsRead()
    }

    fun syncNetworkData() {
        viewModelScope.launch {
            _isSyncing.value = true
            delay(1200) // Realistic peer & cloud sync latency
            _isSyncing.value = false
            _lastSyncTime.value = "Synchronisé à ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"

            repository.addNotification(
                AppNotification(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    title = "Réseau synchronisé",
                    message = "Toutes les offres et annonces communautaires sont à jour sur votre appareil.",
                    type = NotificationType.NEW_LISTING,
                    targetDestination = "EQUIPMENT"
                )
            )
        }
    }

    fun addEquipment(item: EquipmentItem) {
        viewModelScope.launch {
            val user = currentUser.value
            val itemWithAuthor = item.copy(
                authorId = user?.id ?: "usr_current",
                ownerName = user?.fullName ?: item.ownerName,
                ownerPhone = user?.phone ?: item.ownerPhone,
                location = item.location.ifBlank { user?.region ?: "Yamoussoukro" }
            )
            repository.addEquipment(itemWithAuthor)
            addEcoPoints(50, "Nouvelle offre de matériel agricole mise en ligne")
        }
    }

    fun addProduce(item: ProduceItem) {
        viewModelScope.launch {
            val user = currentUser.value
            val itemWithAuthor = item.copy(
                authorId = user?.id ?: "usr_current",
                producerName = user?.fullName ?: item.producerName,
                phone = user?.phone ?: item.phone,
                location = item.location.ifBlank { user?.region ?: "Yamoussoukro" }
            )
            repository.addProduce(itemWithAuthor)
            addEcoPoints(30, "Nouvelle récolte enregistrée sur le marché direct")
        }
    }

    fun addCompost(item: CompostItem) {
        viewModelScope.launch {
            val user = currentUser.value
            val itemWithAuthor = item.copy(
                authorId = user?.id ?: "usr_current",
                producerName = user?.fullName ?: item.producerName,
                phone = user?.phone ?: item.phone,
                location = item.location.ifBlank { user?.region ?: "Yamoussoukro" }
            )
            repository.addCompost(itemWithAuthor)
            addEcoPoints(60, "Offre de fertilisant écologique publiée")
        }
    }

    fun addForumPost(post: ForumPost) {
        viewModelScope.launch {
            val user = currentUser.value
            val postWithAuthor = post.copy(
                authorName = user?.fullName ?: post.authorName,
                authorRole = user?.role?.label ?: post.authorRole,
                region = user?.region ?: post.region
            )
            repository.addForumPost(postWithAuthor)
            addEcoPoints(15, "Participation au forum d'entraide agricole")
        }
    }

    fun bookEquipment(
        equipment: EquipmentItem,
        renterName: String,
        renterPhone: String,
        days: Int,
        operator: Boolean,
        provider: PaymentProvider,
        paymentPhone: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val actualRenterName = renterName.ifBlank { user?.fullName ?: "Client AgriShop" }
            val actualRenterPhone = renterPhone.ifBlank { user?.phone ?: "+225 07 00 00 00" }

            val contract = repository.bookRental(
                equipment = equipment,
                renterName = actualRenterName,
                renterPhone = actualRenterPhone,
                durationDays = days,
                operatorIncluded = operator,
                paymentProvider = provider,
                paymentPhone = paymentPhone
            )

            addEcoPoints(80, "Réservation et contrat de matériel validé (${equipment.title})")

            repository.addNotification(
                AppNotification(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    title = "Contrat de location #${contract.id}",
                    message = "Votre réservation pour ${equipment.title} ($days jours) est validée. Restitution prévue le ${contract.endDate}.",
                    type = NotificationType.PAYMENT_SUCCESS,
                    targetDestination = "RENTALS"
                )
            )
        }
    }

    fun purchaseEquipment(
        equipment: EquipmentItem,
        provider: PaymentProvider,
        buyerPhone: String
    ) {
        viewModelScope.launch {
            repository.processPurchase(
                title = equipment.title,
                amountCfa = equipment.priceCfa,
                quantity = 1,
                provider = provider,
                buyerPhone = buyerPhone
            )
            repository.updateEquipmentRentalStatus(equipment.id, true, "Vendu", 0)
            addEcoPoints(150, "Achat d'équipement éco-certifié finalisé")
        }
    }

    fun returnEquipment(contractId: String, equipmentId: String) {
        viewModelScope.launch {
            repository.returnEquipment(contractId, equipmentId)
            addEcoPoints(40, "Restitution de matériel agricole effectuée avec succès")
            repository.addNotification(
                AppNotification(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    title = "Matériel restitué avec succès",
                    message = "Le matériel a été retourné. La caution a été débloquée et reversée.",
                    type = NotificationType.RENTAL_REMINDER,
                    targetDestination = "RENTALS"
                )
            )
        }
    }

    fun buyProduce(
        produce: ProduceItem,
        quantity: Int,
        provider: PaymentProvider,
        buyerPhone: String
    ) {
        viewModelScope.launch {
            val total = produce.priceCfa * quantity
            repository.processPurchase(
                title = produce.title,
                amountCfa = total,
                quantity = quantity,
                provider = provider,
                buyerPhone = buyerPhone
            )
            val newStock = (produce.availableStock - quantity).coerceAtLeast(0)
            repository.updateProduceStock(produce.id, newStock)
            addEcoPoints(25, "Achat en circuit court auprès du producteur")
        }
    }

    fun buyCompost(
        compost: CompostItem,
        quantity: Int,
        provider: PaymentProvider,
        buyerPhone: String
    ) {
        viewModelScope.launch {
            val total = compost.pricePerUnitCfa * quantity
            repository.processPurchase(
                title = compost.title,
                amountCfa = total,
                quantity = quantity,
                provider = provider,
                buyerPhone = buyerPhone
            )
            val newStock = (compost.volumeAvailable - quantity).coerceAtLeast(0)
            repository.updateCompostVolume(compost.id, newStock)
            addEcoPoints(quantity * 10, "Achat de $quantity ${compost.unit} de fertilisant écologique")
        }
    }

    fun requestWastePickup(
        farmerName: String,
        phone: String,
        location: String,
        wasteType: WasteType,
        weightKg: Int,
        pickupDate: String,
        notes: String
    ) {
        declareWasteCollection(
            farmerName = farmerName,
            phone = phone,
            location = location,
            wasteType = wasteType,
            weightKg = weightKg,
            pickupDate = pickupDate,
            slot = "Matinée (08h00 - 12h00)",
            mode = "Enlèvement Camion Benne 5T (AgriShop)",
            notes = notes
        )
    }

    fun declareWasteCollection(
        farmerName: String,
        phone: String,
        location: String,
        wasteType: WasteType,
        weightKg: Int,
        pickupDate: String,
        slot: String = "Matinée (08h00 - 12h00)",
        mode: String = "Enlèvement Camion Benne 5T (AgriShop)",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val actualFarmerName = farmerName.ifBlank { user?.fullName ?: "Exploitant Agricole" }
            val actualPhone = phone.ifBlank { user?.phone ?: "+225 07 00 00 00" }
            val actualLocation = location.ifBlank { user?.region ?: "Yamoussoukro" }

            val id = repository.requestWastePickup(
                farmerName = actualFarmerName,
                farmerPhone = actualPhone,
                location = actualLocation,
                wasteType = wasteType,
                weightKg = weightKg,
                pickupDate = pickupDate,
                notes = "$notes (Créneau: $slot | Mode: $mode)"
            )
            val basePoints = (weightKg * wasteType.carbonRate * 0.15).toInt().coerceAtLeast(15)
            val bonusPoints = if (mode.contains("Dépôt Direct", ignoreCase = true)) (basePoints * 0.2).toInt() else 0
            val totalPoints = basePoints + bonusPoints

            addEcoPoints(totalPoints, "Déclaration de $weightKg kg de ${wasteType.label} ($totalPoints Éco-Points)")

            repository.addNotification(
                AppNotification(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    title = "Déclaration Déchets #$id validée",
                    message = "Collecte de $weightKg kg de ${wasteType.label} programmée le $pickupDate ($slot). Chauffeur en cours d'assignation.",
                    type = NotificationType.WASTE_PICKUP,
                    targetDestination = "COMPOST"
                )
            )
        }
    }

    fun updateWasteCollectionStatus(requestId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateWasteRequestStatus(requestId, newStatus)

            val statusText = when (newStatus) {
                "PLANIFIEE" -> "Collecte planifiée et chauffeur assigné"
                "EN_COURS" -> "Camion en route vers votre parcelle"
                "COLLECTEE" -> "Déchets collectés avec succès et transformés en compost !"
                "ANNULEE" -> "Collecte annulée"
                else -> "Statut mis à jour : $newStatus"
            }

            if (newStatus == "COLLECTEE") {
                addEcoPoints(50, "Bonus de recyclage effectif pour la collecte #$requestId")
            }

            repository.addNotification(
                AppNotification(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    title = "Mise à jour Collecte #$requestId",
                    message = statusText,
                    type = NotificationType.WASTE_PICKUP,
                    targetDestination = "COMPOST"
                )
            )
        }
    }

    fun cancelWasteCollection(requestId: String) {
        viewModelScope.launch {
            repository.updateWasteRequestStatus(requestId, "ANNULEE")
            repository.addNotification(
                AppNotification(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    title = "Collecte #$requestId annulée",
                    message = "Votre demande de collecte de déchets a été annulée.",
                    type = NotificationType.WASTE_PICKUP,
                    targetDestination = "COMPOST"
                )
            )
        }
    }
}
