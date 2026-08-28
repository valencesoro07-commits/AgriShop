package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AudioLanguage(
    val code: String,
    val label: String,
    val flag: String,
    val regionName: String,
    val greetingLocal: String,
    val greetingPhonetic: String
) {
    FRENCH_EASY(
        code = "fr",
        label = "Français Facile (Oral)",
        flag = "🇨🇮",
        regionName = "National (Côte d'Ivoire)",
        greetingLocal = "Bonjour ! Je suis votre assistante vocale AgriShop.",
        greetingPhonetic = "Bonjour ! Je suis votre assistante vocale AgriShop. Je vous guide pour vos machines, récoltes et compost."
    ),
    DIOULA(
        code = "diou",
        label = "Dioula / Malinké",
        flag = "🗣️",
        regionName = "Nord & Ouest (Bouaké, Korhogo, Odienné)",
        greetingLocal = "I ni ce ! N ye i ka dɛmɛbaga ye AgriShop kan.",
        greetingPhonetic = "I ni tché ! Nè yé i ka dèmèbaga yé AgriShop kan. Nè bèna i dèmè ka masini don, ka soumana fééré ani ka norgo sanya sôrô."
    ),
    BAOULE(
        code = "baou",
        label = "Baoulé",
        flag = "🗣️",
        regionName = "Centre & Sud (Yamoussoukro, Toumodi, Tiassalé)",
        greetingLocal = "Mo o ! N ti wo dɛmɛbaga AgriShop su.",
        greetingPhonetic = "Mo oh ! Nè ti wo dèmèbaga AgriShop sou. Nè sou wa klan ndè kpa klé wor, ashié i guié kpa, masini kpa nio fètè ale mun."
    ),
    SENOUFO(
        code = "sen",
        label = "Sénoufo",
        flag = "🗣️",
        regionName = "Grand Nord (Korhogo, Ferkessédougou, Boundiali)",
        greetingLocal = "Foté ! Mí kà ma tɛmɛ na AgriShop pu.",
        greetingPhonetic = "Foté ! Mi ka ma tèmè na AgriShop pou. Wou bè kparikari masini nio sumana tchenli koro la."
    ),
    BETE(
        code = "bete",
        label = "Bété",
        flag = "🗣️",
        regionName = "Centre-Ouest (Gagnoa, Daloa, Soubré)",
        greetingLocal = "Ayô ! N bɛ wa kpalo AgriShop wa.",
        greetingPhonetic = "Ayo ! Nè bè wa kpalo AgriShop wa. Nè ya gbéhi kpa kpolo masini kpa nio kpakpa gbogbo liri."
    )
}

object AudioGuideManager {
    private const val TAG = "AudioGuideManager"
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AudioLanguage.FRENCH_EASY)
    val selectedLanguage: StateFlow<AudioLanguage> = _selectedLanguage.asStateFlow()

    private val _isSimplifiedMode = MutableStateFlow(false)
    val isSimplifiedMode: StateFlow<Boolean> = _isSimplifiedMode.asStateFlow()

    private val _currentSubtitle = MutableStateFlow("")
    val currentSubtitle: StateFlow<String> = _currentSubtitle.asStateFlow()

    private val _currentTranslationFr = MutableStateFlow("")
    val currentTranslationFr: StateFlow<String> = _currentTranslationFr.asStateFlow()

    fun setSimplifiedMode(enabled: Boolean) {
        _isSimplifiedMode.value = enabled
    }

    fun toggleSimplifiedMode() {
        _isSimplifiedMode.value = !_isSimplifiedMode.value
    }

    fun setSelectedLanguage(language: AudioLanguage) {
        _selectedLanguage.value = language
    }

    fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.FRENCH)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "TTS French language not supported or missing data")
                    } else {
                        tts?.setPitch(1.02f)
                        tts?.setSpeechRate(0.88f) // Clear and steady pace for comfortable listening
                        isTtsReady = true
                    }
                } else {
                    Log.e(TAG, "TTS Initialization failed: $status")
                }
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentSubtitle.value = ""
                    _currentTranslationFr.value = ""
                }
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentSubtitle.value = ""
                    _currentTranslationFr.value = ""
                }
            })
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            _currentSubtitle.value = ""
            _currentTranslationFr.value = ""
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    /**
     * Speaks text adapted to selected local language or clear easy spoken French.
     */
    fun speak(
        frenchEasyText: String,
        dioulaText: String? = null,
        baouleText: String? = null,
        senoufoText: String? = null,
        beteText: String? = null
    ) {
        stop()

        val chosenLang = _selectedLanguage.value
        val (textToSpeak, displayText) = when (chosenLang) {
            AudioLanguage.DIOULA -> {
                val d = dioulaText ?: ("En Dioula : " + frenchEasyText)
                Pair(d, d)
            }
            AudioLanguage.BAOULE -> {
                val b = baouleText ?: ("En Baoulé : " + frenchEasyText)
                Pair(b, b)
            }
            AudioLanguage.SENOUFO -> {
                val s = senoufoText ?: ("En Sénoufo : " + frenchEasyText)
                Pair(s, s)
            }
            AudioLanguage.BETE -> {
                val bt = beteText ?: ("En Bété : " + frenchEasyText)
                Pair(bt, bt)
            }
            AudioLanguage.FRENCH_EASY -> {
                Pair(frenchEasyText, frenchEasyText)
            }
        }

        _currentSubtitle.value = displayText
        _currentTranslationFr.value = frenchEasyText

        if (isTtsReady && tts != null) {
            _isSpeaking.value = true
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_${System.currentTimeMillis()}")
        } else {
            Log.w(TAG, "TTS not ready, speaking fallback: $textToSpeak")
        }
    }

    /**
     * Preview sample voice for a specific language
     */
    fun previewLanguage(language: AudioLanguage) {
        stop()
        val textToSpeak = language.greetingPhonetic
        _currentSubtitle.value = language.greetingLocal
        _currentTranslationFr.value = language.greetingPhonetic

        if (isTtsReady && tts != null) {
            _isSpeaking.value = true
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "PREVIEW_${language.code}")
        }
    }

    fun speakEquipment(title: String, priceCfa: Long, unit: String, location: String, isOperator: Boolean) {
        val opText = if (isOperator) "avec chauffeur inclus." else "sans chauffeur."
        val fr = "Voici le matériel : $title. Le prix est de $priceCfa francs CFA par $unit, situé à $location, $opText Cliquez sur le bouton vert pour réserver."
        val dioula = "I ka masini in ye : $title. A sɔngɔ ye dɔrɔme $priceCfa le ye $unit kɔnɔ, $location dugu kɔnɔ. Ni i b'a fɛ, a digi ka taa ɲɛ."
        val baoule = "Like nga yɛ : $title. Ɔ ti kpa, a nuan ti fran $priceCfa $unit kun su, $location lɔ. Sɛ a klo i kɔ su."
        val senoufo = "Masini in ye : $title. A sɔngɔ ye fran $priceCfa $unit kɔnɔ, $location. Ma digi ka louer."
        val bete = "Masini gbagba ye : $title. A gba fran $priceCfa $unit su, $location. Bli bouton vert su."
        speak(
            frenchEasyText = fr,
            dioulaText = dioula,
            baouleText = baoule,
            senoufoText = senoufo,
            beteText = bete
        )
    }

    fun speakProduce(cropName: String, pricePerKg: Int, quantityKg: Int, location: String, farmerName: String) {
        val total = pricePerKg.toLong() * quantityKg
        val fr = "Récolte disponible : $cropName de $farmerName à $location. Prix : $pricePerKg francs le kilo. Quantité : $quantityKg kilos. Total estimé : $total francs CFA."
        val dioula = "Sumana bɛ yan : $cropName, $farmerName bolo $location dugu la. Kilo kelen sɔngɔ ye $pricePerKg dɔrɔme le ye. A bɛɛ lajɛlen ye $quantityKg kilo ye."
        val baoule = "Ale nga ti $cropName, $farmerName i liɛ $location lɔ. Kilo kun ti fran $pricePerKg. Kilo $quantityKg o wo lɛ."
        val senoufo = "Sumana kpa ye : $cropName, $farmerName bolo $location. Kilo kelen ye fran $pricePerKg. Kilo $quantityKg bɛ yan."
        val bete = "Gbogbo kpa ye : $cropName, $farmerName bolo $location. Kilo kun fran $pricePerKg. Kilo $quantityKg liri."
        speak(
            frenchEasyText = fr,
            dioulaText = dioula,
            baouleText = baoule,
            senoufoText = senoufo,
            beteText = bete
        )
    }

    fun speakCompost(name: String, priceCfa: Long, organicRate: Int = 85, nitrogenRatio: String = "NPK 4-3-3", co2Saved: Double = 35.0) {
        val fr = "Engrais biologique et compost : $name. C'est du 100% naturel certifié NPK $nitrogenRatio pour enrichir votre champ durablement. Évite $co2Saved kilos de gaz polluant. Prix : $priceCfa francs CFA."
        val dioula = "Nɔgɔ sanya nin ye : $name. A ka ɲi foroko kosɔbɛ, a sɔngɔ ye $priceCfa dɔrɔme ye. A bɛ dugukolo gɛlɛya."
        val baoule = "Asiɛ i guie kpa yɛ : $name. Ɔ yo asiɛ kpa, a nuan ti fran $priceCfa."
        val senoufo = "Nɔgɔ ɲuman ye : $name. A bɛ dugu kɛ ɲuman, a sɔngɔ ye fran $priceCfa."
        val bete = "Asiɛ guie kpa ye : $name. A bɛ za kpa, fran $priceCfa."
        speak(
            frenchEasyText = fr,
            dioulaText = dioula,
            baouleText = baoule,
            senoufoText = senoufo,
            beteText = bete
        )
    }

    fun speakPayment(amount: Long, providerName: String, purpose: String) {
        val fr = "Paiement sécurisé CinetPay. Vous allez payer la somme de $amount francs CFA avec $providerName pour : $purpose. Vérifiez votre numéro et validez."
        val dioula = "Wari sara CinetPay : I bɛna wari $amount sara ni $providerName ye, $purpose kosɔn. I ka nimɔrɔ lajɛ ka sara."
        val baoule = "Wari tualɛ : A su fa fran $amount gua $providerName su, $purpose ti. Nian ɔ nimero kpa."
        val senoufo = "Wari sara : Ma wari $amount sara ni $providerName ye, $purpose koro. Lajɛ ka sara."
        val bete = "Wari tualɛ : Bli wari $amount $providerName su, $purpose ti."
        speak(
            frenchEasyText = fr,
            dioulaText = dioula,
            baouleText = baoule,
            senoufoText = senoufo,
            beteText = bete
        )
    }

    fun speakScreenExplanation(screenName: String) {
        when (screenName) {
            "HOME" -> {
                speak(
                    frenchEasyText = "Bienvenue sur l'écran d'accueil. Ici, vous pouvez louer des tracteurs, vendre vos récoltes et commander du compost bio.",
                    dioulaText = "I bisimila so la ! Yan, i bɛ se ka masini don, ka sumana feere ani ka nɔgɔ san.",
                    baouleText = "Mo o fie nun ! Yan, a kwla fa masini kɔ fie nun, fɛtɛ ale mun fite, yɛ san asiɛ guie.",
                    senoufoText = "Foté ! Yan, ma se ka masini louer, ka sumana san ani ka nɔgɔ san.",
                    beteText = "Ayô ! Yan, a kwla fa masini kpa nio fɛtɛ ale mun fite."
                )
            }
            "EQUIPMENT" -> {
                speak(
                    frenchEasyText = "Voici la liste des machines agricoles disponibles à la location et vente. Touchez le bouton haut-parleur pour entendre les détails.",
                    dioulaText = "Foroko masiniw bɛɛ bɛ yan. Tractɛri, motoculteur ani pɔnpu solɛri. A digi ka mɛn.",
                    baouleText = "Masini mun ngba wo lɛ. Tractɛri, pomp solaire. Fa ɔ sa kan su ka ti.",
                    senoufoText = "Masiniw bɛ yan. Digui ka mɛn.",
                    beteText = "Masini gbogbo wo lɛ. Bli su ka ti."
                )
            }
            "PRODUCE" -> {
                speak(
                    frenchEasyText = "Espace Récoltes et Bourse Agricole. Trouvez des acheteurs directs pour votre maïs, cacao, manioc et légumes au meilleur prix.",
                    dioulaText = "Sumana feereyɔrɔ bɛ yan. Kaba, koko, banaku ani nakɔfɛnw bɛɛ sɔngɔ ɲuman na.",
                    baouleText = "Ale fɛtɛlɛ lɔ. Abuo, koko, kpa nian nuan su kpa.",
                    senoufoText = "Sumana feereyɔrɔ bɛ yan.",
                    beteText = "Ale fɛtɛlɛ wo lɛ."
                )
            }
            "COMPOST" -> {
                speak(
                    frenchEasyText = "Compost écologique et gestion des déchets. Transformez vos résidus de récolte en engrais riche et naturel.",
                    dioulaText = "Nɔgɔ sanya ani nɔgɔlajɛ yɔrɔ bɛ yan. I ka fiɛ kɔnɔ fɛnw bɛ yɛlɛma ka kɛ nɔgɔ ɲuman ye.",
                    baouleText = "Asiɛ i guie kpa yɛ. Fa fie nun ninnge mun yo guie kpa man asiɛ.",
                    senoufoText = "Nɔgɔ ɲuman yɔrɔ bɛ yan.",
                    beteText = "Asiɛ guie kpa wo lɛ."
                )
            }
        }
    }
}
