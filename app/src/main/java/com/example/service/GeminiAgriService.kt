package com.example.service

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = "user"
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

data class AiListingAnalysis(
    val title: String,
    val categoryName: String,
    val suggestedPriceCfa: Int,
    val unit: String,
    val quantity: Int,
    val description: String,
    val detectedType: String // "EQUIPMENT", "PRODUCE", "COMPOST"
)

enum class AgriLanguage(val code: String, val label: String, val flag: String) {
    FRENCH("fr", "Français", "🇫🇷"),
    DIOULA("dyo", "Dioula (Julakan)", "🇨🇮"),
    BAOULE("bci", "Baoulé", "🇨🇮"),
    WOLOF("wo", "Wolof", "🇸🇳")
}

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiAgriService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun askAgriExpert(
        prompt: String,
        language: AgriLanguage = AgriLanguage.FRENCH
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineAgriAdvice(prompt, language)
        }

        val langInstructions = when (language) {
            AgriLanguage.FRENCH -> "Réponds en français clair, bienveillant, pratique et structuré."
            AgriLanguage.DIOULA -> "Réponds principalement en langue Dioula (Julakan d'Afrique de l'Ouest) avec la traduction phonétique et quelques termes clés en français pour faciliter la compréhension de l'agriculteur."
            AgriLanguage.BAOULE -> "Réponds en langue Baoulé de Côte d'Ivoire avec explications agronomiques claires et amicales (inclure termes français si nécessaire)."
            AgriLanguage.WOLOF -> "Réponds en langue Wolof avec termes agronomiques simples et bienveillants."
        }

        val systemPrompt = """
            Tu es l'Ingénieur Agronome et Expert en Machinisme Agricole & Compostage Organique d'AgriShop Côte d'Ivoire.
            Ton rôle est d'aider les agriculteurs et producteurs africains sur :
            1. Le choix et le dimensionnement du matériel agricole (tracteurs, motoculteurs, irrigation solaire, moissonneuses).
            2. Le recyclage des déchets organiques agricoles (cabosses de cacao, paille de riz/maïs, fumier, pulpe de manioc) en compost riche de haute qualité et bio-fertilisants.
            3. Les estimations de ratio C/N, taux d'humidité, délais de maturation et valeur fertilisante NPK.
            4. La tarification équitable et la rentabilisation des équipements loués ou achetés.
            5. La santé des cultures et la transition agro-écologique.
            
            Langue demandée : ${language.label}.
            $langInstructions
            
            Question de l'agriculteur : $prompt
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            )
        )

        try {
            val response = api.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!reply.isNullOrBlank()) {
                reply
            } else {
                getOfflineAgriAdvice(prompt, language)
            }
        } catch (e: Exception) {
            getOfflineAgriAdvice(prompt, language)
        }
    }

    suspend fun analyzeImageForListing(imageHint: String): AiListingAnalysis = withContext(Dispatchers.IO) {
        val lower = imageHint.lowercase()
        when {
            lower.contains("tract") || lower.contains("375") || lower.contains("mf") -> {
                AiListingAnalysis(
                    title = "Tracteur Massey Ferguson 375 4x4",
                    categoryName = "Tracteurs",
                    suggestedPriceCfa = 65000,
                    unit = "jour",
                    quantity = 1,
                    description = "Tracteur agricole 75 CV en excellent état mécanique, équipé de relevage hydraulique et prise de force. Idéal labour intensif et hersage.",
                    detectedType = "EQUIPMENT"
                )
            }
            lower.contains("moto") || lower.contains("yanmar") || lower.contains("culteur") -> {
                AiListingAnalysis(
                    title = "Motoculteur Yanmar 12 CV Diesel",
                    categoryName = "Motoculteurs",
                    suggestedPriceCfa = 22000,
                    unit = "jour",
                    quantity = 1,
                    description = "Motoculteur diesel compact et puissant, livré avec fraise rotative et charrue brabant. Parfait pour maraîchage et petites parcelles.",
                    detectedType = "EQUIPMENT"
                )
            }
            lower.contains("moiss") || lower.contains("combine") || lower.contains("harvester") -> {
                AiListingAnalysis(
                    title = "Moissonneuse-Batteuse Riz & Maïs",
                    categoryName = "Moissonneuses",
                    suggestedPriceCfa = 180000,
                    unit = "jour",
                    quantity = 1,
                    description = "Moissonneuse-batteuse à chenilles pour récolte rizicole et céréalière avec faible taux de casse de grains et haut rendement.",
                    detectedType = "EQUIPMENT"
                )
            }
            lower.contains("irrig") || lower.contains("solaire") || lower.contains("pompe") -> {
                AiListingAnalysis(
                    title = "Kit Pompe d'Irrigation Solaire 2kW",
                    categoryName = "Irrigation & Pompes",
                    suggestedPriceCfa = 35000,
                    unit = "jour",
                    quantity = 1,
                    description = "Pompe immergée solaire à haut débit (8m3/h) avec 4 panneaux solaires photovoltaïques et régulateur MPPT.",
                    detectedType = "EQUIPMENT"
                )
            }
            lower.contains("maïs") || lower.contains("mais") || lower.contains("corn") -> {
                AiListingAnalysis(
                    title = "Maïs Grain Jaune Séché Extra (Grade A)",
                    categoryName = "Céréales",
                    suggestedPriceCfa = 250,
                    unit = "kg",
                    quantity = 800,
                    description = "Récolte de maïs grain jaune séché à moins de 13% d'humidité. Conditionné en sacs aérés de 50 kg.",
                    detectedType = "PRODUCE"
                )
            }
            lower.contains("cacao") || lower.contains("cocoa") -> {
                AiListingAnalysis(
                    title = "Fèves de Cacao Triées & Bien Fermentées",
                    categoryName = "Cultures de Rente",
                    suggestedPriceCfa = 1500,
                    unit = "kg",
                    quantity = 1200,
                    description = "Fèves de cacao ivoiriennes certifiées, fermentées 6 jours et séchées au soleil. Taux de grainage supérieur.",
                    detectedType = "PRODUCE"
                )
            }
            lower.contains("manioc") || lower.contains("cassava") -> {
                AiListingAnalysis(
                    title = "Racines de Manioc Fraîchement Déterrées",
                    categoryName = "Tubercules",
                    suggestedPriceCfa = 140,
                    unit = "kg",
                    quantity = 2500,
                    description = "Manioc doux riche en amidon, récolté le matin même. Idéal pour attiéké, placali ou vente directe au marché.",
                    detectedType = "PRODUCE"
                )
            }
            lower.contains("tomat") -> {
                AiListingAnalysis(
                    title = "Tomates Fraîches Plein Champ (Cobra F1)",
                    categoryName = "Légumes & Maraîchage",
                    suggestedPriceCfa = 450,
                    unit = "kg",
                    quantity = 350,
                    description = "Tomates fermes et juteuses récoltées à maturité optimale. Conditionnées en cageots de 25 kg.",
                    detectedType = "PRODUCE"
                )
            }
            lower.contains("terre_noire") || lower.contains("noir") -> {
                AiListingAnalysis(
                    title = "Compost Organique Terre Noire Enrichie",
                    categoryName = "Compost Mûr",
                    suggestedPriceCfa = 2500,
                    unit = "sac 50kg",
                    quantity = 60,
                    description = "Compost 100% naturel criblé à 10mm, riche en matière organique, azote et potasse. Idéal pour maraîchage et régénération des sols.",
                    detectedType = "COMPOST"
                )
            }
            lower.contains("liquid") || lower.contains("purin") -> {
                AiListingAnalysis(
                    title = "Bio-Fertilisant Liquide Extrait Organique",
                    categoryName = "Bio-Fertilisant Liquide",
                    suggestedPriceCfa = 4000,
                    unit = "bidon 20L",
                    quantity = 30,
                    description = "Engrais foliaire biologique concentré riche en micro-organismes bénéfiques et oligo-éléments pour stimuler la floraison.",
                    detectedType = "COMPOST"
                )
            }
            else -> {
                AiListingAnalysis(
                    title = "Lot Agricole Éco-Certifié AgriShop",
                    categoryName = "Maraîchage & Vivriers",
                    suggestedPriceCfa = 500,
                    unit = "kg",
                    quantity = 100,
                    description = "Produit agricole naturel vérifié, disponible en circuit court direct producteur sans intermédiaire.",
                    detectedType = "PRODUCE"
                )
            }
        }
    }

    private fun getOfflineAgriAdvice(prompt: String, language: AgriLanguage): String {
        if (language == AgriLanguage.DIOULA) {
            return """🌿 **AgriShop Senekɛla dɛmɛbaga (Conseil en Dioula)** :
            
I ni sogoma ! I ka senekɛ kow la :
1. **Nɔgɔ (Compost)** : Cacao golo ni sogo nɔgɔ nungulen be dugu jukɔrɔ barika don kosɛbɛ. Ni i ye nɔgɔ kɛ sisan, i be sɔrɔ sɔrɔ kosɛbɛ.
2. **Masini (Matériel)** : Traktɛri dɔgɔman (15 CV) ka fisa maraicher baara kama (20 000 FCFA don kelen).
3. **AgriShop** be i dɛmɛ walasa ka i ka kow feere feerefeere sɔrɔ yɔrɔ jan tɛ."""
        }

        if (language == AgriLanguage.BAOULE) {
            return """🌾 **AgriShop Akwaba - Conseil en Baoulé** :
            
Mo n'de kpa !
1. **Alikoto nin compost** : Like nga be yo kete kete man awe nzué nin dotié kpa. Cacao waka ba be fa yo compost man awule kpa.
2. **Tracteur nin masini** : Se a le hectare kun anzé n'nion, motoculteur ti kpa be tran suan kpa.
3. AgriShop su, a kwla to like kpa man a feze nin a gbanflen nian su."""
        }

        val lower = prompt.lowercase()
        return when {
            lower.contains("compost") || lower.contains("déchet") || lower.contains("matière") || lower.contains("cacao") -> {
                """🌱 **Guide d'Or du Compostage Organique AgriShop** :
                
1. **Ratio Carbone/Azote (C/N idéal 25:1 à 30:1)** :
   - Matières Brunes (Carbone) : Paille, cabosses de cacao concassées, tiges de maïs séchées.
   - Matières Vertes (Azote) : Fientes de volaille, bouse de vache, restes de légumes, pulpe de manioc.

2. **Empilement en couches alternées** : 20 cm de matières brunes pour 10 cm de matières vertes. Arroser légèrement chaque couche (humidité ~50-60%, comme une éponge pressée).

3. **Aération & Température** : Retourner le tas tous les 12-15 jours. La température monte à 60°C (détruit les graines d'adventices et agents pathogènes).

4. **Maturation** : Prêt en 8 à 12 semaines. Le compost mûr a une couleur noire homogène et une bonne odeur d'humus de sous-bois. Rendement : 1000 kg de déchets = ~400 kg de compost premium."""
            }
            lower.contains("tracteur") || lower.contains("location") || lower.contains("machine") || lower.contains("motoculteur") -> {
                """🚜 **Conseil Choix & Rentabilité Machine AgriShop** :
                
- **Pour 1 à 4 Hectares** : Un motoculteur 12-15 CV avec kit labour et rotavator est très économique (20 000 FCFA/jour ou achat rentable dès la 1ère saison).
- **Pour 5 à 20+ Hectares** : Un tracteur 65-75 CV 4x4 (Massey Ferguson ou John Deere) permet de préparer 3 à 5 hectares par jour.
- **Sécurisation Location** : Exigez toujours une caution et optez pour un chauffeur certifié pour préserver la mécanique de l'engin."""
            }
            lower.contains("irrigation") || lower.contains("eau") || lower.contains("pompe") -> {
                """💧 **Système d'Irrigation Solaire Éco-Performant** :
                
- Le goutte-à-goutte solaire permet d'économiser jusqu'à 65% d'eau comparé à l'arrosage manuel ou par aspersion.
- Pour 1 hectare maraîcher : Pompe solaire 1.5 kW à 2 kW avec 4 panneaux 350W fournissant 5 à 8 m³/heure.
- Amortissement rapide en moins de 18 mois grâce à la suppression des coûts de carburant."""
            }
            else -> {
                """🌾 **Conseil de l'Ingénieur Agronome AgriShop** :
                
Pour maximiser votre rendement agricole de manière durable :
1. Privilégiez l'apport de **compost organique ennobli** (3 à 5 tonnes/ha) pour restaurer la structure du sol et réduire les achats d'engrais chimiques.
2. Pour les récoltes, mutualisez le transport et l'utilisation de machines via la plateforme **AgriShop** afin de réduire vos charges jusqu'à 40%.
3. Enregistrez vos récoltes sur la mise en relation directe pour vendre au meilleur prix sans intermédiaires abusifs."""
            }
        }
    }
}
