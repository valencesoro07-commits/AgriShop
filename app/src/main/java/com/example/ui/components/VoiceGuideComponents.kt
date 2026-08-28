package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.*
import com.example.util.AudioGuideManager
import com.example.util.AudioLanguage

@Composable
fun VoiceNarratorButton(
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 38
) {
    val isSpeaking by AudioGuideManager.isSpeaking.collectAsState()

    Surface(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .clickable {
                if (isSpeaking) {
                    AudioGuideManager.stop()
                } else {
                    onSpeak()
                }
            }
            .testTag("voice_narrator_button"),
        shape = CircleShape,
        color = if (isSpeaking) AmberLight else ForestGreenPrimary.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSpeaking) 2.dp else 1.dp,
            color = if (isSpeaking) HarvestGold else ForestGreenPrimary.copy(alpha = 0.4f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                contentDescription = "Écouter en audio dans votre langue",
                tint = if (isSpeaking) ForestGreenDark else ForestGreenPrimary,
                modifier = Modifier.size((size * 0.55).dp)
            )
        }
    }
}

/**
 * Floating Subtitle Bar showing live text spoken by the assistant in the chosen local language + french translation
 */
@Composable
fun FloatingVoiceSubtitleBar(
    modifier: Modifier = Modifier
) {
    val isSpeaking by AudioGuideManager.isSpeaking.collectAsState()
    val currentLang by AudioGuideManager.selectedLanguage.collectAsState()
    val subtitle by AudioGuideManager.currentSubtitle.collectAsState()
    val translationFr by AudioGuideManager.currentTranslationFr.collectAsState()

    AnimatedVisibility(
        visible = isSpeaking && subtitle.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("voice_subtitle_bar"),
            shape = RoundedCornerShape(16.dp),
            color = ForestGreenDark,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, HarvestGold)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound Wave Avatar
                Surface(
                    shape = CircleShape,
                    color = HarvestGold,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = currentLang.flag, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Assistance Vocale (${currentLang.label})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = HarvestGold,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 12.sp
                        ),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Stop Button
                IconButton(
                    onClick = { AudioGuideManager.stop() },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Arrêter la voix",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AudioLanguageSelectorDialog(
    onDismiss: () -> Unit
) {
    val currentLang by AudioGuideManager.selectedLanguage.collectAsState()
    val isSpeaking by AudioGuideManager.isSpeaking.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(ForestGreenPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Langues Locales de l'Assistante",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ForestGreenDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sélectionnez votre langue pour écouter tous les prix, machines et récoltes oralement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioLanguage.entries.forEach { lang ->
                        val isSelected = lang == currentLang
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    AudioGuideManager.setSelectedLanguage(lang)
                                    AudioGuideManager.previewLanguage(lang)
                                },
                            color = if (isSelected) MintLight else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = lang.flag, fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lang.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) ForestGreenDark else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = ForestGreenPrimary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIF",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = lang.regionName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "« ${lang.greetingLocal} »",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.5.sp,
                                            color = ForestGreenPrimary
                                        ),
                                        maxLines = 1
                                    )
                                }

                                // Play Preview Voice Button
                                IconButton(
                                    onClick = {
                                        AudioGuideManager.previewLanguage(lang)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (isSelected) ForestGreenPrimary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Écouter l'extrait vocal",
                                        tint = ForestGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Valider la Langue", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdvisorCallDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val advisorPhone = "+2250700112233"
    val whatsappNumber = "+2250700112233"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(ForestGreenPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Conseiller Agricole de Zone",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ForestGreenDark
                )

                Text(
                    text = "Besoin d'aide ? Un agent relais vous répond immédiatement par téléphone ou WhatsApp.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Direct Call Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$advisorPhone"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("call_advisor_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Appeler l'Agent (Gratuit)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // WhatsApp Direct Button
                Button(
                    onClick = {
                        val uri = Uri.parse("https://wa.me/$whatsappNumber?text=Bonjour%20je%20suis%20agriculteur%20et%20j'ai%20besoin%20d'aide%20sur%20AgriShop")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("whatsapp_advisor_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Message Vocal WhatsApp",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Fermer")
                }
            }
        }
    }
}

/**
 * The "Mode Simplifié / Mode Champ" - 4 Huge high-contrast visual tiles for illiterate/senior farmers
 * with full descriptive breakdown and direct switch back to normal mode.
 */
@Composable
fun SimplifiedFieldDashboard(
    onNavigateToEquipment: () -> Unit,
    onNavigateToProduce: () -> Unit,
    onNavigateToCompost: () -> Unit,
    onOpenAdvisorCall: () -> Unit,
    onSwitchToNormalMode: () -> Unit
) {
    val currentLang by AudioGuideManager.selectedLanguage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("simplified_dashboard"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Banner explaining mode with explanation & Return to normal button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = AmberLight,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, HarvestGold)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.size(38.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.agrishop_logo),
                                    contentDescription = "AgriShop Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AgriShop • Mode Facile (${currentLang.label})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                            )
                            Text(
                                text = "Conçu pour une utilisation vocale & sans lecture",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = ForestGreenDark.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Button to switch back to normal mode immediately
                    Button(
                        onClick = onSwitchToNormalMode,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Mode Normal",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = HarvestGold.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // What to expect in Easy Mode (Descriptions claires)
                Text(
                    text = "Ce que vous apporte le Mode Facile :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
                )

                Spacer(modifier = Modifier.height(6.dp))

                EasyModeFeatureBullet(
                    emoji = "🔊",
                    title = "Assistance Vocale en Langues Locales",
                    desc = "Écoutez les prix et détails en Français Facile, Dioula, Baoulé, Sénoufo ou Bété."
                )
                EasyModeFeatureBullet(
                    emoji = "🚜",
                    title = "4 Grandes Dalles Visuelles",
                    desc = "Navigation directe et simplifiée sans menus complexes ni formulaires longs."
                )
                EasyModeFeatureBullet(
                    emoji = "📞",
                    title = "Appel Conseiller Direct",
                    desc = "Mise en relation téléphonique et WhatsApp avec un agent de terrain en 1 clic."
                )
                EasyModeFeatureBullet(
                    emoji = "💳",
                    title = "Paiement Mobile Vocalisé",
                    desc = "Lecture vocale du montant et sélection par logos très visibles (Orange, MTN, Moov, Wave)."
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Large Easy-Access Visual Tiles
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tile 1: Tractors & Machines
            SimplifiedActionCard(
                icon = Icons.Default.Agriculture,
                emoji = "🚜",
                title = "LOUER DU MATÉRIEL",
                subtitle = "Tracteurs, Motoculteurs, Biogaz",
                backgroundColor = ForestGreenPrimary,
                textColor = Color.White,
                onClick = onNavigateToEquipment,
                onAudioPrompt = {
                    AudioGuideManager.speak(
                        frenchEasyText = "Case numéro 1 : Louer une machine agricole. Tracteurs, motoculteurs, biodigesteurs et pompes solaires. Cliquez ici pour voir les machines.",
                        dioulaText = "Kɛrɛnbɛ fɔlɔ : Ka masini don foroko la. Traktɛri ani masini wɛrɛw bɛ yan.",
                        baouleText = "Like klikli : Fa masini kɔ fie nun. Nian masini kpa mun.",
                        senoufoText = "Kɛrɛnbɛ fɔlɔ : Masiniw bɛ yan.",
                        beteText = "Kɛrɛnbɛ klikli : Masini kpa wo lɛ."
                    )
                }
            )

            // Tile 2: Produce & Harvests
            SimplifiedActionCard(
                icon = Icons.Default.Grass,
                emoji = "🌾",
                title = "RÉCOLTES & VENTE",
                subtitle = "Maïs, Tomates, Cacao, Manioc",
                backgroundColor = HarvestGold,
                textColor = ForestGreenDark,
                onClick = onNavigateToProduce,
                onAudioPrompt = {
                    AudioGuideManager.speak(
                        frenchEasyText = "Case numéro 2 : Vendre ou acheter des récoltes. Cacao, maïs, manioc, légumes au meilleur prix du marché.",
                        dioulaText = "Kɛrɛnbɛ filanan : Ka sumana feere walima k'a san. Koko, kaba, banaku bɛ yan.",
                        baouleText = "Like nnyɔn su : Fɛtɛ ɔ ale mun fite. Koko, abuo, i ngba wo lɛ.",
                        senoufoText = "Kɛrɛnbɛ filanan : Sumana feereyɔrɔ bɛ yan.",
                        beteText = "Kɛrɛnbɛ nnyɔn : Ale fɛtɛlɛ wo lɛ."
                    )
                }
            )

            // Tile 3: Eco Compost & Waste
            SimplifiedActionCard(
                icon = Icons.Default.Eco,
                emoji = "♻️",
                title = "COMPOST & DÉCHETS",
                subtitle = "Engrais bio & Ramassage de résidus",
                backgroundColor = ForestGreenDark,
                textColor = Color.White,
                onClick = onNavigateToCompost,
                onAudioPrompt = {
                    AudioGuideManager.speak(
                        frenchEasyText = "Case numéro 3 : Compost et engrais 100% bio. Demandez le ramassage de vos déchets de champ et recevez du bon engrais pour enrichir la terre.",
                        dioulaText = "Kɛrɛnbɛ sabanan : Nɔgɔ sanya ani nɔgɔlajɛ. I ka fiɛ kɔnɔ fɛnw bɛ yɛlɛma ka kɛ nɔgɔ ye.",
                        baouleText = "Like nsan su : Asie i guie kpa. Fa fie nun ninnge mun yo guie.",
                        senoufoText = "Kɛrɛnbɛ sabanan : Nɔgɔ ɲuman yɔrɔ bɛ yan.",
                        beteText = "Kɛrɛnbɛ nsan : Asiɛ guie kpa wo lɛ."
                    )
                }
            )

            // Tile 4: Advisor & Phone
            SimplifiedActionCard(
                icon = Icons.Default.PhoneInTalk,
                emoji = "📞",
                title = "PARLER AU CONSEILLER",
                subtitle = "Appel téléphonique & WhatsApp direct",
                backgroundColor = Color(0xFF2E7D32),
                textColor = Color.White,
                onClick = onOpenAdvisorCall,
                onAudioPrompt = {
                    AudioGuideManager.speak(
                        frenchEasyText = "Case numéro 4 : Parler au conseiller agricole de votre zone. Cliquez ici pour téléphoner directement à un agent qui va vous aider.",
                        dioulaText = "Kɛrɛnbɛ naaninan : Ka kuma ni dɛmɛbaga ye telefoni la. Digi yan k'a weele.",
                        baouleText = "Like nnan su : Frɛ sran ng'ɔ si fie junman kpa i telefɔni su.",
                        senoufoText = "Kɛrɛnbɛ naaninan : Ka kuma ni dɛmɛbaga ye telefoni na.",
                        beteText = "Kɛrɛnbɛ nnan : Frɛ conseiller telefɔni su."
                    )
                }
            )
        }
    }
}

@Composable
private fun EasyModeFeatureBullet(
    emoji: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = emoji, fontSize = 14.sp, modifier = Modifier.padding(top = 1.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenDark)
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = ForestGreenDark.copy(alpha = 0.85f)),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun SimplifiedActionCard(
    icon: ImageVector,
    emoji: String,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    onAudioPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("simplified_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big Emoji / Icon Avatar
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = textColor
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                )
            }

            // Audio Helper Button inside the card
            IconButton(
                onClick = onAudioPrompt,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Écouter l'explication",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
