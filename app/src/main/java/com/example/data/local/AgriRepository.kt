package com.example.data.local

import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AgriRepository(private val dao: AgriDao) {

    // User Profile Stream
    val currentUser: Flow<UserProfile?> = dao.getCurrentUser().map { it?.toDomain() }

    // Notifications Stream
    val notifications: Flow<List<AppNotification>> = dao.getAllNotifications().map { list ->
        list.map { it.toDomain() }
    }

    // Equipment Stream
    val equipmentList: Flow<List<EquipmentItem>> = dao.getAllEquipment().map { list ->
        list.map { it.toDomain() }
    }

    // Produce Stream
    val produceList: Flow<List<ProduceItem>> = dao.getAllProduce().map { list ->
        list.map { it.toDomain() }
    }

    // Compost Stream
    val compostList: Flow<List<CompostItem>> = dao.getAllCompost().map { list ->
        list.map { it.toDomain() }
    }

    // Waste Requests Stream
    val wasteRequests: Flow<List<WasteCollectionRequest>> = dao.getAllWasteRequests().map { list ->
        list.map { it.toDomain() }
    }

    // Rental Contracts Stream
    val rentalContracts: Flow<List<RentalContract>> = dao.getAllContracts().map { list ->
        list.map { it.toDomain() }
    }

    // Payment History Stream
    val paymentHistory: Flow<List<PaymentTransaction>> = dao.getAllPayments().map { list ->
        list.map { it.toDomain() }
    }

    // Forum Stream
    val forumPosts: Flow<List<ForumPost>> = dao.getAllForumPosts().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun seedInitialDataIfEmpty() {
        // 0. Initial User
        val defaultUser = UserEntity(
            id = "usr_01",
            fullName = "Kouassi Jean-Marc",
            phone = "+225 07 88 99 11",
            email = "kouassi.agri@gmail.com",
            role = UserRole.FARMER.name,
            region = "Yamoussoukro",
            latitude = 6.8276,
            longitude = -5.2893,
            ecoPoints = 450,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80",
            isVerified = true,
            memberSince = "Mars 2026"
        )
        dao.insertOrUpdateUser(defaultUser)

        // Initial Notifications
        val initialNotifications = listOf(
            NotificationEntity(
                id = "notif_1",
                title = "Bienvenue sur AgriShop !",
                message = "Votre compte a été crédité de 450 Éco-Points de bienvenue pour encourager vos pratiques agricoles durables.",
                type = NotificationType.ECO_POINTS.name,
                timestamp = System.currentTimeMillis() - 3600000L * 5,
                isRead = false,
                targetDestination = "COMPOST"
            ),
            NotificationEntity(
                id = "notif_2",
                title = "Rappel Restitution Tracteur MF 375",
                message = "Le contrat pour le tracteur Massey Ferguson arrive à échéance dans 2 jours. Pensez à planifier la restitution.",
                type = NotificationType.RENTAL_REMINDER.name,
                timestamp = System.currentTimeMillis() - 3600000L * 2,
                isRead = false,
                targetDestination = "RENTALS"
            ),
            NotificationEntity(
                id = "notif_3",
                title = "Collecte de déchets organiques planifiée",
                message = "Le camion de compostage passera le 18 Août pour récupérer vos cabosses de cacao (120 pts à la clé).",
                type = NotificationType.WASTE_PICKUP.name,
                timestamp = System.currentTimeMillis() - 3600000L * 24,
                isRead = true,
                targetDestination = "COMPOST"
            )
        )
        for (n in initialNotifications) {
            dao.insertNotification(n)
        }

        // 1. Initial Equipment with matching high-resolution photos (including Biogas and Eco equipment)
        val initialEquipment = listOf(
            EquipmentEntity(
                id = "eq_biogas_1",
                title = "Biodigesteur Agricole Modulaire 10m³ (Biogaz & Digestat bio)",
                category = EquipmentCategory.BIOGAS.name,
                offerType = OfferType.BOTH.name,
                priceCfa = 65000,
                rentalUnit = "mois",
                hpPower = 0,
                condition = "Neuf - Certifié Éco",
                location = "Yamoussoukro",
                latitude = 6.8276,
                longitude = -5.2893,
                ownerName = "BioGaz Côte d'Ivoire & AgriTech",
                ownerPhone = "+225 07 10 20 30",
                authorId = "usr_biogas_ci",
                imageUrl = "img_biogas_digester",
                rating = 4.95f,
                reviewCount = 31,
                depositCfa = 80000,
                operatorAvailable = true,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Système de méthanisation autonome transformant les déjections animales et résidus de récolte en biogaz de cuisson/électricité et digestat liquide fertilisant (remplace les engrais chimiques).",
                specsString = "Production 3.5 m³ biogaz/jour|Digestat bio 80 L/jour|Idéal 4 à 10 bovins ou 20 porcs|Géomembrane garantie 10 ans",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_biogas_2",
                title = "Générateur Biogaz & Biomasse 7.5 kVA Propre",
                category = EquipmentCategory.BIOGAS.name,
                offerType = OfferType.RENT.name,
                priceCfa = 25000,
                rentalUnit = "jour",
                hpPower = 10,
                condition = "Excellent état",
                location = "Bouaké",
                latitude = 7.6905,
                longitude = -5.0300,
                ownerName = "Énergie Verte Rurale CI",
                ownerPhone = "+225 05 88 44 22",
                authorId = "usr_energie_verte",
                imageUrl = "img_biogas_digester",
                rating = 4.88f,
                reviewCount = 17,
                depositCfa = 50000,
                operatorAvailable = true,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Groupe électrogène 100% biogaz purifié. Fournit de l'électricité propre pour alimenter moulins, décortiqueuses et chambres froides sans consommer une seule goutte de carburant fossile.",
                specsString = "Puissance 7.5 kVA / 230V|Moteur 100% biogaz pur|Zéro émission CO2 fossile|Silencieux et renforcé",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_shredder_1",
                title = "Broyeur de Résidus Agricoles & Biomasse Mobile 5T/h",
                category = EquipmentCategory.ECO_TRANSFORMATION.name,
                offerType = OfferType.RENT.name,
                priceCfa = 30000,
                rentalUnit = "jour",
                hpPower = 25,
                condition = "Très bon état",
                location = "Daloa",
                latitude = 6.8774,
                longitude = -6.4502,
                ownerName = "Coopérative Cacao Durable",
                ownerPhone = "+225 07 65 43 21",
                authorId = "usr_coop_daloa",
                imageUrl = "img_biomass_shredder",
                rating = 4.92f,
                reviewCount = 24,
                depositCfa = 60000,
                operatorAvailable = true,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Broyeur mobile haute capacité pour cabosses de cacao, branches de taille, rafles de palmier et pailles de céréales. Accélère le compostage par 4 et produit un paillis protecteur anti-érosion.",
                specsString = "Débit 3 à 5 Tonnes/heure|Rotor 24 marteaux trempés|Prise de force ou moteur diesel autonome|Calibre 5 à 30 mm",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_solar_dryer_1",
                title = "Séchoir Solaire Hybride Cacao & Café 500kg",
                category = EquipmentCategory.SOLAR_ENERGY.name,
                offerType = OfferType.BOTH.name,
                priceCfa = 18000,
                rentalUnit = "jour",
                hpPower = 0,
                condition = "Neuf - Norme Export",
                location = "San-Pédro",
                latitude = 4.7485,
                longitude = -6.6363,
                ownerName = "Sika Agri Solaire",
                ownerPhone = "+225 01 23 45 67",
                authorId = "usr_sika_solaire",
                imageUrl = "img_solar_dryer",
                rating = 4.97f,
                reviewCount = 29,
                depositCfa = 40000,
                operatorAvailable = false,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Serre de séchage solaire automatisée avec ventilation photovoltaïque. Évite les moisissures, protège de la pluie et supprime le séchage au feu de bois polluant.",
                specsString = "Capacité 500 kg par cycle|Ventilation solaire régulée|Séchage uniforme 4-5 jours|Sans émission de fumée",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_1",
                title = "Tracteur Massey Ferguson 375 (75 CV)",
                category = EquipmentCategory.TRACTOR.name,
                offerType = OfferType.RENT.name,
                priceCfa = 65000,
                rentalUnit = "jour",
                hpPower = 75,
                condition = "Très bon état",
                location = "Bouaké",
                latitude = 7.6905,
                longitude = -5.0300,
                ownerName = "Coopérative N'Zrama",
                ownerPhone = "+225 07 45 89 12",
                authorId = "usr_coop_bouake",
                imageUrl = "img_tractor_mf375",
                rating = 4.9f,
                reviewCount = 28,
                depositCfa = 100000,
                operatorAvailable = true,
                isRentedCurrently = true,
                currentRenterName = "Koffi Agricole Sarl",
                daysRemaining = 3,
                description = "Tracteur 4x4 puissant idéal pour labour profond, hersage et transport lourd. Entretien régulier à jour. Chauffeur expérimenté inclus si souhaité.",
                specsString = "75 CV|Diesel 4 cylindres|Attelage 3 points|Prise de force 540 tr/min",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_2",
                title = "Motoculteur Multifonction Yanmar 12 CV",
                category = EquipmentCategory.TILLER.name,
                offerType = OfferType.BOTH.name,
                priceCfa = 20000,
                rentalUnit = "jour",
                hpPower = 12,
                condition = "Neuf",
                location = "Korhogo",
                latitude = 9.4580,
                longitude = -5.6296,
                ownerName = "Soro Mamadou Équipements",
                ownerPhone = "+225 05 12 34 56",
                authorId = "usr_soro",
                imageUrl = "img_motoculteur_yanmar",
                rating = 4.8f,
                reviewCount = 19,
                depositCfa = 50000,
                operatorAvailable = false,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Motoculteur maniable avec kit de labour et fraises rotatives. Parfait pour les parcelles maraîchères de 1 à 5 hectares.",
                specsString = "12 CV Diesel|Démarrage électrique|Largeur travail 90 cm|Consommation 1.2L/h",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_3",
                title = "Moissonneuse-Batteuse de Riz Claas Dominator",
                category = EquipmentCategory.HARVESTER.name,
                offerType = OfferType.RENT.name,
                priceCfa = 180000,
                rentalUnit = "jour",
                hpPower = 130,
                condition = "Excellent état",
                location = "Yamoussoukro",
                latitude = 6.8276,
                longitude = -5.2893,
                ownerName = "AgriTech Vallée du Bandama",
                ownerPhone = "+225 01 78 90 23",
                authorId = "usr_agritech",
                imageUrl = "img_combine_harvester",
                rating = 4.95f,
                reviewCount = 42,
                depositCfa = 300000,
                operatorAvailable = true,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Capacité de récolte jusqu'à 8 hectares/jour en riz irrigué ou maïs. Réduit les pertes de grains de 30%. Équipe de techniciens d'appui incluse.",
                specsString = "130 CV Turbo|Barre de coupe 3.6m|Trémie 3200 Litres|Chenilles caoutchouc",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_4",
                title = "Kit d'Irrigation Goutte-à-Goutte Solaire 1 Ha",
                category = EquipmentCategory.IRRIGATION.name,
                offerType = OfferType.SALE.name,
                priceCfa = 850000,
                rentalUnit = "vente",
                hpPower = 3,
                condition = "Neuf - Garantie 2 ans",
                location = "Bingerville",
                latitude = 5.3600,
                longitude = -4.0083,
                ownerName = "GreenAgro Solutions",
                ownerPhone = "+225 07 99 88 77",
                authorId = "usr_greenagro",
                imageUrl = "img_solar_irrigation",
                rating = 5.0f,
                reviewCount = 15,
                depositCfa = 0,
                operatorAvailable = false,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Système complet avec pompe solaire submersible 1.5kW, 4 panneaux photovoltaïques, filtres à disque et tuyaux perforés pour 10 000 m². Économise 65% d'eau.",
                specsString = "Pompe solaire 1.5kW|Débit 6 m3/h|Panneaux 4x350W|Garantie 24 mois",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_5",
                title = "Pulvérisateur Tracté 600L à Rampe Écologique",
                category = EquipmentCategory.SPRAYER.name,
                offerType = OfferType.RENT.name,
                priceCfa = 35000,
                rentalUnit = "jour",
                hpPower = 0,
                condition = "Bon état",
                location = "San-Pédro",
                latitude = 4.7485,
                longitude = -6.6363,
                ownerName = "Plantations du Sud",
                ownerPhone = "+225 05 67 43 21",
                authorId = "usr_sud",
                imageUrl = "img_boom_sprayer",
                rating = 4.7f,
                reviewCount = 9,
                depositCfa = 40000,
                operatorAvailable = true,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Pulvérisateur adapté pour les traitements bio et bio-stimulants liquides. Buse anti-dérive pour préserver la biodiversité.",
                specsString = "Cuve 600 Litres|Rampe 10 mètres|Buses anti-dérive|Pompe à membrane",
                isEcoCertified = true
            ),
            EquipmentEntity(
                id = "eq_6",
                title = "Semoir Pneumatique 4 Rangs pour Maïs & Soja",
                category = EquipmentCategory.PLANTER.name,
                offerType = OfferType.SALE.name,
                priceCfa = 1650000,
                rentalUnit = "vente",
                hpPower = 0,
                condition = "Neuf",
                location = "Daloa",
                latitude = 6.8774,
                longitude = -6.4502,
                ownerName = "Sika Matériel Agricole",
                ownerPhone = "+225 07 22 33 44",
                authorId = "usr_sika",
                imageUrl = "img_semoir_pneumatique",
                rating = 4.85f,
                reviewCount = 14,
                depositCfa = 0,
                operatorAvailable = false,
                isRentedCurrently = false,
                currentRenterName = "",
                daysRemaining = 0,
                description = "Précision millimétrique du semis avec enfouisseur d'engrais organique localisé. Augmente le taux de levée de 25%.",
                specsString = "4 Rangs réglables|Distribution pneumatique|Trémie fertilisant|Trémie semence 4x40L",
                isEcoCertified = true
            )
        )
        dao.insertAllEquipment(initialEquipment)

        // 2. Initial Produce Listings with matching real crop photos
        val initialProduce = listOf(
            ProduceEntity(
                id = "prod_1",
                title = "Maïs Jaune Grain Sec (Qualité Supérieure)",
                category = ProduceCategory.CEREALS.name,
                producerName = "Ferme Kouamé & Fils",
                producerRole = "Producteur Agréé",
                location = "Ferkessédougou",
                latitude = 9.5928,
                longitude = -5.1945,
                priceCfa = 18500,
                unit = "Sac de 100kg",
                availableStock = 350,
                minOrder = 10,
                isOrganicCertified = false,
                harvestDate = "Août 2026",
                phone = "+225 07 11 22 33",
                authorId = "usr_kouame",
                imageUrl = "img_corn_maize",
                description = "Maïs bien séché (taux d'humidité < 12%), idéal pour élevage avicole ou alimentation humaine. Conditionné en sacs neufs."
            ),
            ProduceEntity(
                id = "prod_2",
                title = "Fèves de Cacao Fermentées Séchées Bio",
                category = ProduceCategory.CASH_CROPS.name,
                producerName = "Coopérative Éco-Cacao Toumodi",
                producerRole = "Coopérative Certifiée",
                location = "Toumodi",
                latitude = 6.5574,
                longitude = -5.0177,
                priceCfa = 2200,
                unit = "kg",
                availableStock = 12000,
                minOrder = 100,
                isOrganicCertified = true,
                harvestDate = "Juillet 2026",
                phone = "+225 05 88 99 00",
                authorId = "usr_ecocacao",
                imageUrl = "img_cocoa_beans",
                description = "Cacao issu de l'agroforesterie régénératrice, certifié bio sans pesticides chimiques. Traçabilité GPS garantie."
            ),
            ProduceEntity(
                id = "prod_3",
                title = "Manioc Doux Frais Récolté du Jour",
                category = ProduceCategory.TUBERS.name,
                producerName = "Groupement Féminin d'Agboville",
                producerRole = "Productrices Locales",
                location = "Agboville",
                latitude = 5.9280,
                longitude = -4.2133,
                priceCfa = 95000,
                unit = "Tonne",
                availableStock = 45,
                minOrder = 2,
                isOrganicCertified = true,
                harvestDate = "Directement au champ",
                phone = "+225 01 44 55 66",
                authorId = "usr_agboville",
                imageUrl = "img_cassava_roots",
                description = "Variété améliorée riche en amidon, parfaite pour la fabrication d'Attiéké ou de farine panifiable. Récolte à la demande."
            ),
            ProduceEntity(
                id = "prod_4",
                title = "Tomates Maraîchères Plein Champ",
                category = ProduceCategory.VEGETABLES.name,
                producerName = "Jardins du N'Zi",
                producerRole = "Ferme Maraîchère",
                location = "Dimbokro",
                latitude = 6.6467,
                longitude = -4.7051,
                priceCfa = 8000,
                unit = "Caisse 25kg",
                availableStock = 120,
                minOrder = 5,
                isOrganicCertified = true,
                harvestDate = "Récolte quotidienne",
                phone = "+225 07 33 22 11",
                authorId = "usr_nzi",
                imageUrl = "img_field_tomatoes",
                description = "Tomates fermes cultivées avec compost organique et biopesticides à base de neem. Excellente conservation."
            )
        )
        dao.insertAllProduce(initialProduce)

        // 3. Initial Compost & Bio-Fertilizer Items with matching real compost photos
        val initialCompost = listOf(
            CompostEntity(
                id = "comp_1",
                title = "Compost Ennobli Bio-Actif 'Terre Noire'",
                category = CompostCategory.MATURE_COMPOST.name,
                pricePerUnitCfa = 4500,
                unit = "Sac de 50kg",
                volumeAvailable = 450,
                npkRatio = "3.5 - 2.8 - 3.2 + Oligo-éléments",
                maturityWeeks = 14,
                producerName = "BioCompost Côte d'Ivoire",
                location = "Yamoussoukro",
                latitude = 6.8276,
                longitude = -5.2893,
                phone = "+225 07 60 70 80",
                authorId = "usr_biocompost",
                imageUrl = "img_compost_terre_noire",
                co2SavedKgPerUnit = 42.5,
                description = "Compost 100% végétal et fumier animal décomposé en aérobiose contrôlée. Régénère la vie microbienne des sols appauvris.",
                isCertifiedBio = true
            ),
            CompostEntity(
                id = "comp_2",
                title = "Extrait de Purin Bio-Stimulant Concentré",
                category = CompostCategory.BIO_LIQUID.name,
                pricePerUnitCfa = 12500,
                unit = "Bidon de 20 Litres",
                volumeAvailable = 80,
                npkRatio = "Riche en Azote & Silice naturelle",
                maturityWeeks = 6,
                producerName = "ÉcoFerme du Poro",
                location = "Korhogo",
                latitude = 9.4580,
                longitude = -5.6296,
                phone = "+225 05 90 80 70",
                authorId = "usr_ecoporo",
                imageUrl = "img_bio_liquid",
                co2SavedKgPerUnit = 18.0,
                description = "Bio-fertilisant foliaire à diluer (1L pour 20L d'eau). Renforce l'immunité des plantes et repousse les ravageurs.",
                isCertifiedBio = true
            ),
            CompostEntity(
                id = "comp_3",
                title = "Compost Brut de Cabosses de Cacao & Paille",
                category = CompostCategory.ORGANIC_FERTILIZER.name,
                pricePerUnitCfa = 65000,
                unit = "Tonne en vrac",
                volumeAvailable = 25,
                npkRatio = "2.8 - 1.9 - 4.1 (Très riche en Potasse)",
                maturityWeeks = 10,
                producerName = "Centre de Recyclage Agroécologique de Divo",
                location = "Divo",
                latitude = 5.8374,
                longitude = -5.3572,
                phone = "+225 01 23 45 67",
                authorId = "usr_divo",
                imageUrl = "img_cocoa_compost_straw",
                co2SavedKgPerUnit = 780.0,
                description = "Matière organique compostée à base de résidus de récolte de cacao. Idéal pour l'amendement de fond des vergers et cultures pérennes.",
                isCertifiedBio = true
            )
        )
        dao.insertAllCompost(initialCompost)

        // 4. Initial Waste Collection Requests (Pending, Scheduled, In-Progress, and Completed)
        val initialRequests = listOf(
            WasteRequestEntity(
                id = "req_1",
                farmerName = "Traoré Bakary (Coop. Céréales)",
                farmerPhone = "+225 07 55 44 33",
                location = "Bouaké (Zone Industrielle & Rurale)",
                latitude = 7.6905,
                longitude = -5.0300,
                wasteType = WasteType.CEREAL_STRAW.name,
                weightKg = 3200,
                pickupDate = "22 Août 2026",
                notes = "Résidus de battage de maïs mis en meules accessibles pour camion benne.",
                status = "EN_COURS",
                rewardEcoPoints = 320,
                assignedDriver = "Diallo Moussa",
                driverPhone = "+225 07 44 22 11",
                pickupSlot = "Matinée (08h00 - 12h00)",
                pickupMode = "Enlèvement Camion Benne 5T (AgriShop)",
                vehicleType = "Camion Benne 5T",
                createdAt = System.currentTimeMillis() - 86400000L * 1
            ),
            WasteRequestEntity(
                id = "req_2",
                farmerName = "Adjoua Delphine (Plantations N'Zi)",
                farmerPhone = "+225 05 66 77 88",
                location = "Gagnoa (Route de Soubré)",
                latitude = 6.1319,
                longitude = -5.9506,
                wasteType = WasteType.COCOA_PODS.name,
                weightKg = 4500,
                pickupDate = "24 Août 2026",
                notes = "Cabosses de cacao après écabossage, prêtes pour transformation en compost riche en potasse.",
                status = "PLANIFIEE",
                rewardEcoPoints = 450,
                assignedDriver = "Koffi Serge",
                driverPhone = "+225 05 88 12 90",
                pickupSlot = "Après-midi (14h00 - 18h00)",
                pickupMode = "Enlèvement Camion Benne 5T (AgriShop)",
                vehicleType = "Camion Benne 5T",
                createdAt = System.currentTimeMillis() - 86400000L * 2
            ),
            WasteRequestEntity(
                id = "req_3",
                farmerName = "Coopérative Maraîchère du Bélier",
                farmerPhone = "+225 07 19 28 37",
                location = "Yamoussoukro (Morofé)",
                latitude = 6.8276,
                longitude = -5.2893,
                wasteType = WasteType.VEGETABLE_SCRAPS.name,
                weightKg = 1800,
                pickupDate = "25 Août 2026",
                notes = "Déchets maraîchers frais et épluchures de légumes pour compostage rapide.",
                status = "EN_ATTENTE",
                rewardEcoPoints = 180,
                assignedDriver = "Touré Amadou",
                driverPhone = "+225 07 99 33 44",
                pickupSlot = "Matinée (08h00 - 12h00)",
                pickupMode = "Enlèvement Camion Benne 5T (AgriShop)",
                vehicleType = "Triporteur Agricole 1.5T",
                createdAt = System.currentTimeMillis() - 3600000L * 6
            ),
            WasteRequestEntity(
                id = "req_4",
                farmerName = "Ferme Avicole Soro & Fils",
                farmerPhone = "+225 05 12 34 56",
                location = "Korhogo (Route de M'Bengué)",
                latitude = 9.4580,
                longitude = -5.6296,
                wasteType = WasteType.MANURE.name,
                weightKg = 5000,
                pickupDate = "19 Août 2026",
                notes = "Fientes de volaille séchées et conditionnées en sacs, très riches en azote.",
                status = "COLLECTEE",
                rewardEcoPoints = 500,
                assignedDriver = "Ouattara Brahima",
                driverPhone = "+225 01 44 77 00",
                pickupSlot = "Matinée (08h00 - 12h00)",
                pickupMode = "Dépôt Direct au Centre de Compostage",
                vehicleType = "Camion Plateau 10T",
                createdAt = System.currentTimeMillis() - 86400000L * 5
            ),
            WasteRequestEntity(
                id = "req_5",
                farmerName = "Kouassi Jean-Marc (Mon Exploitation)",
                farmerPhone = "+225 07 88 99 11",
                location = "Yamoussoukro",
                latitude = 6.8276,
                longitude = -5.2893,
                wasteType = WasteType.CASSAVA_PEELS.name,
                weightKg = 2200,
                pickupDate = "26 Août 2026",
                notes = "Épluchures de manioc d'une unité de transformation Attiéké.",
                status = "EN_ATTENTE",
                rewardEcoPoints = 220,
                assignedDriver = "Diallo Moussa",
                driverPhone = "+225 07 44 22 11",
                pickupSlot = "Après-midi (14h00 - 18h00)",
                pickupMode = "Enlèvement Camion Benne 5T (AgriShop)",
                vehicleType = "Camion Benne 5T",
                createdAt = System.currentTimeMillis() - 3600000L * 2
            )
        )
        for (req in initialRequests) {
            dao.insertWasteRequest(req)
        }

        // 5. Initial Rental Contract
        val initialContract = RentalContractEntity(
            id = "ctr_101",
            equipmentId = "eq_1",
            equipmentTitle = "Tracteur Massey Ferguson 375 (75 CV)",
            renterName = "Koffi Agricole Sarl",
            renterPhone = "+225 07 88 12 34",
            ownerPhone = "+225 07 45 89 12",
            startDate = "12 Août 2026",
            endDate = "17 Août 2026",
            durationDays = 5,
            dailyRateCfa = 65000,
            totalAmountCfa = 325000,
            depositPaidCfa = 100000,
            operatorIncluded = true,
            status = ContractStatus.ACTIVE.name,
            paymentProvider = PaymentProvider.ORANGE_MONEY.name,
            transactionRef = "OM-20260812-7894"
        )
        dao.insertContract(initialContract)

        // 6. Initial Payment
        val initialPayment = PaymentTransactionEntity(
            id = "pay_01",
            transactionRef = "OM-20260812-7894",
            amountCfa = 425000,
            feeCfa = 0,
            provider = PaymentProvider.ORANGE_MONEY.name,
            phoneNumber = "+225 07 88 12 34",
            purpose = "Location Tracteur MF 375 (5 jours + caution)",
            status = "SUCCÈS",
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            receiptCode = "REC-AGRI-99824"
        )
        dao.insertPayment(initialPayment)

        // 7. Initial Forum Posts
        val initialPosts = listOf(
            ForumPostEntity(
                id = "post_1",
                authorName = "Konan Yao (Planteur de Manioc)",
                authorRole = "Producteur",
                region = "Bouaké",
                timestampStr = "Il y a 2 heures",
                topic = "Recherche groupement pour achat groupé de compost 50kg",
                content = "Bonjour confrères agriculteurs, nous sommes 4 producteurs à vouloir commander 10 tonnes de compost ennobli pour nos parcelles. Si d'autres veulent se joindre pour réduire le transport en camion, contactez-moi !",
                repliesCount = 6,
                likesCount = 14,
                isQuestion = false
            ),
            ForumPostEntity(
                id = "post_2",
                authorName = "Dr. Sanogo (Ingénieur Agronome)",
                authorRole = "Conseiller Agro",
                region = "Yamoussoukro",
                timestampStr = "Hier à 16:30",
                topic = "Conseils pratiques : réussir son compostage de cabosses de cacao",
                content = "Pour éviter l'acidification excessive du tas de compost lors du recyclage des cabosses de cacao, veillez à ajouter 30% de matière sèche azotée (fientes ou paille broyée) et à aérer tous les 10 jours !",
                repliesCount = 12,
                likesCount = 38,
                isQuestion = false
            )
        )
        dao.insertAllForumPosts(initialPosts)
    }

    // User Operations
    suspend fun saveUserProfile(profile: UserProfile) {
        dao.insertOrUpdateUser(profile.toEntity())
    }

    suspend fun updateUserEcoPoints(userId: String, points: Int) {
        dao.updateUserEcoPoints(userId, points)
    }

    // Notification Operations
    suspend fun addNotification(notification: AppNotification) {
        dao.insertNotification(notification.toEntity())
    }

    suspend fun markNotificationAsRead(id: String) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        dao.markAllNotificationsAsRead()
    }

    // Equipment operations
    suspend fun addEquipment(item: EquipmentItem) {
        dao.insertEquipment(item.toEntity())
    }

    suspend fun updateEquipment(item: EquipmentItem) {
        dao.updateEquipment(item.toEntity())
    }

    suspend fun updateEquipmentRentalStatus(id: String, isRented: Boolean, renterName: String, days: Int) {
        dao.updateEquipmentRentalStatus(id, isRented, renterName, days)
    }

    // Produce operations
    suspend fun addProduce(item: ProduceItem) {
        dao.insertProduce(item.toEntity())
    }

    suspend fun updateProduceStock(id: String, newStock: Int) {
        dao.updateProduceStock(id, newStock)
    }

    // Compost operations
    suspend fun addCompost(item: CompostItem) {
        dao.insertCompost(item.toEntity())
    }

    suspend fun updateCompostVolume(id: String, newVolume: Int) {
        dao.updateCompostVolume(id, newVolume)
    }

    suspend fun insertWasteRequest(request: WasteCollectionRequest) {
        dao.insertWasteRequest(request.toEntity())
    }

    suspend fun insertContract(contract: RentalContract) {
        dao.insertContract(contract.toEntity())
    }

    suspend fun updateContractStatus(contractId: String, status: ContractStatus) {
        dao.updateContractStatus(contractId, status.name)
    }

    suspend fun insertTransaction(transaction: PaymentTransaction) {
        dao.insertPayment(transaction.toEntity())
    }

    suspend fun addForumPost(post: ForumPost) {
        dao.insertForumPost(post.toEntity())
    }

    suspend fun bookRental(
        equipment: EquipmentItem,
        renterName: String,
        renterPhone: String,
        durationDays: Int,
        operatorIncluded: Boolean,
        paymentProvider: PaymentProvider,
        paymentPhone: String
    ): RentalContract {
        val totalAmount = equipment.priceCfa * durationDays
        val deposit = equipment.depositCfa
        val contractId = "ctr_${System.currentTimeMillis().toString().takeLast(6)}"
        val txRef = "${paymentProvider.code}-${System.currentTimeMillis().toString().takeLast(8)}"
        
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        val startDate = sdf.format(Date())
        val endDate = sdf.format(Date(System.currentTimeMillis() + durationDays * 86400000L))

        val contract = RentalContract(
            id = contractId,
            equipmentId = equipment.id,
            equipmentTitle = equipment.title,
            renterName = renterName,
            renterPhone = renterPhone,
            ownerPhone = equipment.ownerPhone,
            startDate = startDate,
            endDate = endDate,
            durationDays = durationDays,
            dailyRateCfa = equipment.priceCfa,
            totalAmountCfa = totalAmount,
            depositPaidCfa = deposit,
            operatorIncluded = operatorIncluded,
            status = ContractStatus.ACTIVE,
            paymentProvider = paymentProvider,
            transactionRef = txRef
        )
        dao.insertContract(contract.toEntity())
        dao.updateEquipmentRentalStatus(equipment.id, true, renterName, durationDays)

        val payment = PaymentTransaction(
            id = "pay_${UUID.randomUUID().toString().take(6)}",
            transactionRef = txRef,
            amountCfa = totalAmount + deposit,
            feeCfa = if (paymentProvider == PaymentProvider.WAVE) ((totalAmount + deposit) * 0.01).toLong() else 0L,
            provider = paymentProvider,
            phoneNumber = paymentPhone,
            purpose = "Location ${equipment.title} ($durationDays j)",
            status = "SUCCÈS",
            timestamp = System.currentTimeMillis(),
            receiptCode = "REC-AGRI-${(10000..99999).random()}"
        )
        dao.insertPayment(payment.toEntity())

        return contract
    }

    suspend fun processPurchase(
        title: String,
        amountCfa: Long,
        quantity: Int,
        provider: PaymentProvider,
        buyerPhone: String
    ): PaymentTransaction {
        val txRef = "${provider.code}-${System.currentTimeMillis().toString().takeLast(8)}"
        val payment = PaymentTransaction(
            id = "pay_${UUID.randomUUID().toString().take(6)}",
            transactionRef = txRef,
            amountCfa = amountCfa,
            feeCfa = if (provider == PaymentProvider.WAVE) (amountCfa * 0.01).toLong() else 0L,
            provider = provider,
            phoneNumber = buyerPhone,
            purpose = "Achat $title (Qté: $quantity)",
            status = "SUCCÈS",
            timestamp = System.currentTimeMillis(),
            receiptCode = "REC-AGRI-${(10000..99999).random()}"
        )
        dao.insertPayment(payment.toEntity())
        return payment
    }

    suspend fun returnEquipment(contractId: String, equipmentId: String) {
        dao.updateContractStatus(contractId, ContractStatus.COMPLETED.name)
        dao.updateEquipmentRentalStatus(equipmentId, false, "", 0)
    }

    suspend fun requestWastePickup(
        farmerName: String,
        farmerPhone: String,
        location: String,
        wasteType: WasteType,
        weightKg: Int,
        pickupDate: String,
        notes: String
    ): String {
        val id = "req_${System.currentTimeMillis().toString().takeLast(6)}"
        val rewardEcoPoints = (weightKg * wasteType.carbonRate * 0.15).toInt().coerceAtLeast(10)
        val coords = GeoUtils.getCityCoordinates(location)
        val req = WasteCollectionRequest(
            id = id,
            farmerName = farmerName,
            farmerPhone = farmerPhone,
            location = location,
            latitude = coords.first,
            longitude = coords.second,
            wasteType = wasteType,
            weightKg = weightKg,
            pickupDate = pickupDate,
            notes = notes,
            status = "EN_ATTENTE",
            rewardEcoPoints = rewardEcoPoints,
            assignedDriver = "Diallo Moussa (Transporteur Partenaire)",
            driverPhone = "+225 07 44 22 11",
            pickupSlot = "Matinée (08h00 - 12h00)",
            pickupMode = "Enlèvement Camion Benne 5T (AgriShop)",
            vehicleType = "Camion Benne 5 Tonnes",
            createdAt = System.currentTimeMillis()
        )
        dao.insertWasteRequest(req.toEntity())
        return id
    }

    suspend fun updateWasteRequestStatus(id: String, status: String) {
        dao.updateWasteRequestStatus(id, status)
    }

    suspend fun deleteWasteRequest(id: String) {
        dao.deleteWasteRequest(id)
    }
}

// Mapper extensions
fun UserEntity.toDomain() = UserProfile(
    id = id,
    fullName = fullName,
    phone = phone,
    email = email,
    role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.FARMER },
    region = region,
    latitude = latitude,
    longitude = longitude,
    ecoPoints = ecoPoints,
    avatarUrl = avatarUrl,
    isVerified = isVerified,
    memberSince = memberSince
)

fun UserProfile.toEntity() = UserEntity(
    id = id,
    fullName = fullName,
    phone = phone,
    email = email,
    role = role.name,
    region = region,
    latitude = latitude,
    longitude = longitude,
    ecoPoints = ecoPoints,
    avatarUrl = avatarUrl,
    isVerified = isVerified,
    memberSince = memberSince
)

fun NotificationEntity.toDomain() = AppNotification(
    id = id,
    title = title,
    message = message,
    type = try { NotificationType.valueOf(type) } catch (e: Exception) { NotificationType.ECO_POINTS },
    timestamp = timestamp,
    isRead = isRead,
    targetDestination = targetDestination
)

fun AppNotification.toEntity() = NotificationEntity(
    id = id,
    title = title,
    message = message,
    type = type.name,
    timestamp = timestamp,
    isRead = isRead,
    targetDestination = targetDestination
)

fun EquipmentEntity.toDomain() = EquipmentItem(
    id = id,
    title = title,
    category = try { EquipmentCategory.valueOf(category) } catch (e: Exception) { EquipmentCategory.ALL },
    offerType = try { OfferType.valueOf(offerType) } catch (e: Exception) { OfferType.RENT },
    priceCfa = priceCfa,
    rentalUnit = rentalUnit,
    hpPower = hpPower,
    condition = condition,
    location = location,
    latitude = latitude,
    longitude = longitude,
    ownerName = ownerName,
    ownerPhone = ownerPhone,
    authorId = authorId,
    imageUrl = imageUrl,
    rating = rating,
    reviewCount = reviewCount,
    depositCfa = depositCfa,
    operatorAvailable = operatorAvailable,
    isRentedCurrently = isRentedCurrently,
    currentRenterName = currentRenterName,
    daysRemaining = daysRemaining,
    description = description,
    specs = if (specsString.isEmpty()) emptyList() else specsString.split("|"),
    isEcoCertified = isEcoCertified
)

fun EquipmentItem.toEntity() = EquipmentEntity(
    id = id,
    title = title,
    category = category.name,
    offerType = offerType.name,
    priceCfa = priceCfa,
    rentalUnit = rentalUnit,
    hpPower = hpPower,
    condition = condition,
    location = location,
    latitude = latitude,
    longitude = longitude,
    ownerName = ownerName,
    ownerPhone = ownerPhone,
    authorId = authorId,
    imageUrl = imageUrl,
    rating = rating,
    reviewCount = reviewCount,
    depositCfa = depositCfa,
    operatorAvailable = operatorAvailable,
    isRentedCurrently = isRentedCurrently,
    currentRenterName = currentRenterName,
    daysRemaining = daysRemaining,
    description = description,
    specsString = specs.joinToString("|"),
    isEcoCertified = isEcoCertified
)

fun ProduceEntity.toDomain() = ProduceItem(
    id = id,
    title = title,
    category = try { ProduceCategory.valueOf(category) } catch (e: Exception) { ProduceCategory.ALL },
    producerName = producerName,
    producerRole = producerRole,
    location = location,
    latitude = latitude,
    longitude = longitude,
    priceCfa = priceCfa,
    unit = unit,
    availableStock = availableStock,
    minOrder = minOrder,
    isOrganicCertified = isOrganicCertified,
    harvestDate = harvestDate,
    phone = phone,
    authorId = authorId,
    imageUrl = imageUrl,
    description = description
)

fun ProduceItem.toEntity() = ProduceEntity(
    id = id,
    title = title,
    category = category.name,
    producerName = producerName,
    producerRole = producerRole,
    location = location,
    latitude = latitude,
    longitude = longitude,
    priceCfa = priceCfa,
    unit = unit,
    availableStock = availableStock,
    minOrder = minOrder,
    isOrganicCertified = isOrganicCertified,
    harvestDate = harvestDate,
    phone = phone,
    authorId = authorId,
    imageUrl = imageUrl,
    description = description
)

fun CompostEntity.toDomain() = CompostItem(
    id = id,
    title = title,
    category = try { CompostCategory.valueOf(category) } catch (e: Exception) { CompostCategory.ALL },
    pricePerUnitCfa = pricePerUnitCfa,
    unit = unit,
    volumeAvailable = volumeAvailable,
    npkRatio = npkRatio,
    maturityWeeks = maturityWeeks,
    producerName = producerName,
    location = location,
    latitude = latitude,
    longitude = longitude,
    phone = phone,
    authorId = authorId,
    imageUrl = imageUrl,
    co2SavedKgPerUnit = co2SavedKgPerUnit,
    description = description,
    isCertifiedBio = isCertifiedBio
)

fun CompostItem.toEntity() = CompostEntity(
    id = id,
    title = title,
    category = category.name,
    pricePerUnitCfa = pricePerUnitCfa,
    unit = unit,
    volumeAvailable = volumeAvailable,
    npkRatio = npkRatio,
    maturityWeeks = maturityWeeks,
    producerName = producerName,
    location = location,
    latitude = latitude,
    longitude = longitude,
    phone = phone,
    authorId = authorId,
    imageUrl = imageUrl,
    co2SavedKgPerUnit = co2SavedKgPerUnit,
    description = description,
    isCertifiedBio = isCertifiedBio
)

fun WasteRequestEntity.toDomain() = WasteCollectionRequest(
    id = id,
    farmerName = farmerName,
    farmerPhone = farmerPhone,
    location = location,
    latitude = latitude,
    longitude = longitude,
    wasteType = try { WasteType.valueOf(wasteType) } catch (e: Exception) { WasteType.CEREAL_STRAW },
    weightKg = weightKg,
    pickupDate = pickupDate,
    notes = notes,
    status = status,
    rewardEcoPoints = rewardEcoPoints,
    assignedDriver = assignedDriver,
    driverPhone = driverPhone,
    pickupSlot = pickupSlot,
    pickupMode = pickupMode,
    vehicleType = vehicleType,
    createdAt = createdAt
)

fun WasteCollectionRequest.toEntity() = WasteRequestEntity(
    id = id,
    farmerName = farmerName,
    farmerPhone = farmerPhone,
    location = location,
    latitude = latitude,
    longitude = longitude,
    wasteType = wasteType.name,
    weightKg = weightKg,
    pickupDate = pickupDate,
    notes = notes,
    status = status,
    rewardEcoPoints = rewardEcoPoints,
    assignedDriver = assignedDriver,
    driverPhone = driverPhone,
    pickupSlot = pickupSlot,
    pickupMode = pickupMode,
    vehicleType = vehicleType,
    createdAt = createdAt
)

fun RentalContractEntity.toDomain() = RentalContract(
    id = id,
    equipmentId = equipmentId,
    equipmentTitle = equipmentTitle,
    renterName = renterName,
    renterPhone = renterPhone,
    ownerPhone = ownerPhone,
    startDate = startDate,
    endDate = endDate,
    durationDays = durationDays,
    dailyRateCfa = dailyRateCfa,
    totalAmountCfa = totalAmountCfa,
    depositPaidCfa = depositPaidCfa,
    operatorIncluded = operatorIncluded,
    status = try { ContractStatus.valueOf(status) } catch (e: Exception) { ContractStatus.ACTIVE },
    paymentProvider = try { PaymentProvider.valueOf(paymentProvider) } catch (e: Exception) { PaymentProvider.ORANGE_MONEY },
    transactionRef = transactionRef
)

fun RentalContract.toEntity() = RentalContractEntity(
    id = id,
    equipmentId = equipmentId,
    equipmentTitle = equipmentTitle,
    renterName = renterName,
    renterPhone = renterPhone,
    ownerPhone = ownerPhone,
    startDate = startDate,
    endDate = endDate,
    durationDays = durationDays,
    dailyRateCfa = dailyRateCfa,
    totalAmountCfa = totalAmountCfa,
    depositPaidCfa = depositPaidCfa,
    operatorIncluded = operatorIncluded,
    status = status.name,
    paymentProvider = paymentProvider.name,
    transactionRef = transactionRef
)

fun PaymentTransactionEntity.toDomain() = PaymentTransaction(
    id = id,
    transactionRef = transactionRef,
    amountCfa = amountCfa,
    feeCfa = feeCfa,
    provider = try { PaymentProvider.valueOf(provider) } catch (e: Exception) { PaymentProvider.ORANGE_MONEY },
    phoneNumber = phoneNumber,
    purpose = purpose,
    status = status,
    timestamp = timestamp,
    receiptCode = receiptCode
)

fun PaymentTransaction.toEntity() = PaymentTransactionEntity(
    id = id,
    transactionRef = transactionRef,
    amountCfa = amountCfa,
    feeCfa = feeCfa,
    provider = provider.name,
    phoneNumber = phoneNumber,
    purpose = purpose,
    status = status,
    timestamp = timestamp,
    receiptCode = receiptCode
)

fun ForumPostEntity.toDomain() = ForumPost(
    id = id,
    authorName = authorName,
    authorRole = authorRole,
    region = region,
    timestampStr = timestampStr,
    topic = topic,
    content = content,
    repliesCount = repliesCount,
    likesCount = likesCount,
    isQuestion = isQuestion
)

fun ForumPost.toEntity() = ForumPostEntity(
    id = id,
    authorName = authorName,
    authorRole = authorRole,
    region = region,
    timestampStr = timestampStr,
    topic = topic,
    content = content,
    repliesCount = repliesCount,
    likesCount = likesCount,
    isQuestion = isQuestion
)
