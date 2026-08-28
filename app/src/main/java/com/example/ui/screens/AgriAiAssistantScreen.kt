package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AgriLanguage
import com.example.service.GeminiAgriService
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: String = "Maintenant"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriAiAssistantScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(AgriLanguage.FRENCH) }
    val listState = rememberLazyListState()

    // Text To Speech instance
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var showScanDiagnosisDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Default French or locale
                tts?.language = Locale.FRENCH
            }
        }
        tts = ttsInstance

        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    fun speakText(text: String) {
        tts?.let { player ->
            player.stop()
            val cleanText = text.replace("*", "").replace("#", "")
            player.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "agri_tts_${System.currentTimeMillis()}")
            isSpeaking = true
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        isSpeaking = false
    }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "msg_1",
                    sender = "ai",
                    text = "🌱 Bonjour ! Je suis votre **Conseiller Agronome Vocal & Machinisme AgriShop**.\n\nVous pouvez me poser vos questions en **parlant au micro 🎤** ou par écrit en **Français**, **Dioula**, **Baoulé** ou **Wolof** !\n\n- 🚜 Choix & rentabilité de machines (tracteurs, motoculteurs, pompes solaires)\n- ♻️ Recettes de compostage organique (ratio C/N, matières locales)\n- 🌾 Prévisions de récoltes et prix bord champ."
                )
            )
        )
    }

    // Speech to text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputQuery = spoken
                // Auto send prompt
                val userMsg = ChatMessage(
                    id = "msg_${System.currentTimeMillis()}",
                    sender = "user",
                    text = spoken
                )
                messages = messages + userMsg
                inputQuery = ""
                isLoading = true

                coroutineScope.launch {
                    val replyText = GeminiAgriService.askAgriExpert(spoken, selectedLanguage)
                    val aiMsg = ChatMessage(
                        id = "msg_${System.currentTimeMillis() + 1}",
                        sender = "ai",
                        text = replyText
                    )
                    messages = messages + aiMsg
                    isLoading = false
                    listState.animateScrollToItem(messages.size - 1)
                    speakText(replyText)
                }
            }
        }
    }

    fun startVoiceRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (selectedLanguage == AgriLanguage.FRENCH) "fr-FR" else "fr-FR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant (AgriShop IA Vocale)...")
            }
            isListening = true
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            isListening = false
            Toast.makeText(context, "Reconnaissance vocale non disponible sur cet appareil", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendPrompt(prompt: String) {
        if (prompt.isBlank() || isLoading) return
        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "user",
            text = prompt.trim()
        )
        messages = messages + userMsg
        inputQuery = ""
        isLoading = true

        coroutineScope.launch {
            val replyText = GeminiAgriService.askAgriExpert(prompt, selectedLanguage)
            val aiMsg = ChatMessage(
                id = "msg_${System.currentTimeMillis() + 1}",
                sender = "ai",
                text = replyText
            )
            messages = messages + aiMsg
            isLoading = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestedPrompts = listOf(
        "🎤 Nɔgɔ kɛcogo (Recette compost Dioula)",
        "🍂 Recette compost cabosses de cacao & fientes",
        "🚜 Quel tracteur pour 10 hectares de maïs ?",
        "💧 Calculer débit pompe irrigation solaire",
        "🐛 Traitement naturel bio chenille légionnaire"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ForestGreenPrimary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MintLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "IA",
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Agri-Conseiller Vocal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "IA Vocale & Multilingue (FR • Dioula • Baoulé)",
                        style = MaterialTheme.typography.labelSmall.copy(color = MintLight)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberSun,
                    modifier = Modifier.clickable { showScanDiagnosisDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Diagnostic Feuille", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 10.sp))
                    }
                }

                if (isSpeaking) {
                    IconButton(onClick = { stopSpeaking() }) {
                        Icon(imageVector = Icons.Default.VolumeOff, contentDescription = "Arrêter l'audio", tint = Color.White)
                    }
                }
            }
        }

        // Multilingual language selector chips
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Langue :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(end = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(AgriLanguage.entries) { lang ->
                        val isSelected = selectedLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLanguage = lang },
                            label = { Text("${lang.flag} ${lang.label}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Suggestions chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestedPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MintLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { sendPrompt(prompt) }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenDark
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                val isAi = msg.sender == "ai"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    if (isAi) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isAi) 4.dp else 16.dp,
                            bottomEnd = if (isAi) 16.dp else 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAi) MaterialTheme.colorScheme.surface else ForestGreenPrimary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isAi) 2.dp else 0.dp),
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isAi) MaterialTheme.colorScheme.onSurface else Color.White,
                                    lineHeight = 20.sp
                                )
                            )

                            if (isAi) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { speakText(msg.text) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Écouter la réponse",
                                            tint = ForestGreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ForestGreenPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "L'agronome IA analyse votre demande...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }

        // Bottom Input Row with Voice Microphone button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Speech-To-Text Microphone Button
                FilledIconButton(
                    onClick = { startVoiceRecognition() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isListening) Color(0xFFE53935) else MintLight,
                        contentColor = if (isListening) Color.White else ForestGreenDark
                    ),
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("voice_input_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = "Parler au micro",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = {
                        Text(
                            if (selectedLanguage == AgriLanguage.FRENCH) "Posez une question agronomique..."
                            else "I ka ɲiningali kɛ yan..."
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_input_text_field"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { sendPrompt(inputQuery) },
                    enabled = inputQuery.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (inputQuery.isNotBlank() && !isLoading) ForestGreenPrimary else Color.LightGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Envoyer",
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (showScanDiagnosisDialog) {
        PlantDiseaseScanDialog(
            onDismiss = { showScanDiagnosisDialog = false },
            onSelectDiagnosis = { diagnosisText ->
                showScanDiagnosisDialog = false
                sendPrompt(diagnosisText)
            }
        )
    }
}

data class CropDiseaseSample(
    val crop: String,
    val disease: String,
    val emoji: String,
    val severity: String,
    val symptoms: String,
    val organicRemedy: String,
    val agriShopEquipment: String
)

@Composable
fun PlantDiseaseScanDialog(
    onDismiss: () -> Unit,
    onSelectDiagnosis: (String) -> Unit
) {
    val samples = listOf(
        CropDiseaseSample(
            crop = "Cacao (Theobroma)",
            disease = "Pourriture Brune des Cabosses (Phytophthora)",
            emoji = "🍫",
            severity = "Élevée (Perte jusqu'à 60%)",
            symptoms = "Taches brunes sur les cabosses avec odeur acide, dessèchement prématuré.",
            organicRemedy = "Élagage d'aération, ramassage des cabosses infectées, pulvérisation de bouillie bordelaise ou purin de prêle.",
            agriShopEquipment = "Pulvérisateur dorsal à pression + Compost riche en Trichoderma"
        ),
        CropDiseaseSample(
            crop = "Maïs (Zea mays)",
            disease = "Chenille Légionnaire d'Automne (Spodoptera)",
            emoji = "🌽",
            severity = "Critique",
            symptoms = "Feuilles trouées 'en coup de fusil', sciure végétale au cœur du cornet.",
            organicRemedy = "Application de cendre de bois au cœur, biopesticide à base de Bacillus thuringiensis (Bt) ou huile de neem.",
            agriShopEquipment = "Pulvérisateur manuel à buse conique + Bio-fertilisant azoté"
        ),
        CropDiseaseSample(
            crop = "Manioc (Manihot)",
            disease = "Mosaïque Africaine du Manioc (CMD)",
            emoji = "🍠",
            severity = "Moyenne à Élevée",
            symptoms = "Feuilles déformées avec marbrures vert clair et jaunes, rabougrissement.",
            organicRemedy = "Utilisation exclusive de boutures saines certifiées, arrachage précoce des pieds infectés.",
            agriShopEquipment = "Boutures assainies certifiées + Compost mûr de biomasse"
        ),
        CropDiseaseSample(
            crop = "Maraîchage (Tomate/Piment)",
            disease = "Flétrissement Bactérien & Mildiou",
            emoji = "🍅",
            severity = "Élevée en saison des pluies",
            symptoms = "Feuilles pendantes sans jaunissement préalable, tige creuse et noircie.",
            organicRemedy = "Rotation avec graminées, apport massif de compost enrichi, paillage organique propre.",
            agriShopEquipment = "Système d'irrigation goutte-à-goutte solaire + Paillis végétal"
        ),
        CropDiseaseSample(
            crop = "Santé du Sol (Parcelle)",
            disease = "Acidité & Carence en Matière Organique",
            emoji = "🧪",
            severity = "Sol dégradé (Rendement -40%)",
            symptoms = "Sol compacté, faible rétention d'eau, chlorose foliaire généralisée.",
            organicRemedy = "Apport de 3 à 5 tonnes de Compost Organique Ennobli AgriShop par hectare.",
            agriShopEquipment = "Compost Bio Haute Performance + Motoculteur d'enfouissement"
        )
    )

    var selectedSample by remember { mutableStateOf(samples.first()) }
    var isSimulatingScan by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Scanner Vision IA",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Diagnostic Santé des Plantes & Sols",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sélectionnez une culture à analyser ou capturez une photo au champ :",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Crop chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(samples) { sample ->
                        val isSelected = selectedSample == sample
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ForestGreenPrimary else MintLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedSample = sample }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sample.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sample.crop.takeWhile { it != '(' }.trim(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else ForestGreenDark,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Diagnostic Result Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = LightSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSample.crop,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedSample.severity.contains("Critique")) Color(0xFFFFEBEE) else AmberLight
                            ) {
                                Text(
                                    text = selectedSample.severity,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (selectedSample.severity.contains("Critique")) Color(0xFFC62828) else HarvestGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Pathologie détectée : ${selectedSample.disease}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E312F))
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        Text(
                            text = "🔍 Symptômes observés :",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = selectedSample.symptoms,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🌿 Remède Bio & Agroécologique :",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        )
                        Text(
                            text = selectedSample.organicRemedy,
                            style = MaterialTheme.typography.bodySmall.copy(color = ForestGreenDark)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🚜 Équipement & Intrant recommandé AgriShop :",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = HarvestGold)
                        )
                        Text(
                            text = selectedSample.agriShopEquipment,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSelectDiagnosis("IA Diagnostic : Ma parcelle de ${selectedSample.crop} est touchée par ${selectedSample.disease}. Quels sont les dosages précis et les conseils pour appliquer ${selectedSample.organicRemedy} avec du matériel AgriShop ?")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Approfondir le traitement avec l'IA")
                }
            }
        }
    }
}
