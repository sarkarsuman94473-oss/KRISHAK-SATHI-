package com.example.ui.expert

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.datasource.ExpertTranslations
import com.example.data.local.entity.ExpertEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriAlertRed
import com.example.ui.theme.AgriGreenPrimaryContainer
import com.example.util.isCompactNarrowScreen
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsultationCallScreen(
    expert: ExpertEntity,
    isVideoCall: Boolean,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    val isNarrow = isCompactNarrowScreen()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        if (isVideoCall && !hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Call status lifecycle: 0 CONNECTING -> 1 RINGING -> 2 CONNECTED
    var callPhase by remember { mutableIntStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isVideoEnabled by remember { mutableStateOf(isVideoCall) }
    var isFrontCamera by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1200)
        callPhase = 1
        delay(2200)
        callPhase = 2
        isConnected = true
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    val callStateText = when (callPhase) {
        0 -> when (currentLanguage) {
            AppLanguage.BENGALI -> "সংযোগ স্থাপন করা হচ্ছে..."
            AppLanguage.HINDI -> "कनेक्ट हो रहा है..."
            AppLanguage.ENGLISH -> "Connecting..."
        }
        1 -> when (currentLanguage) {
            AppLanguage.BENGALI -> "রিং হচ্ছে..."
            AppLanguage.HINDI -> "रिंग हो रही है..."
            AppLanguage.ENGLISH -> "Ringing..."
        }
        else -> when (currentLanguage) {
            AppLanguage.BENGALI -> "সংযুক্ত"
            AppLanguage.HINDI -> "कनेक्टेड"
            AppLanguage.ENGLISH -> "Connected"
        }
    }

    fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = com.example.ui.theme.RoyalGreenPrimaryBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isNarrow) 16.dp else 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Info & Security Badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x33FFFFFF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = com.example.ui.theme.RoyalGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVideoCall) {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "কৃষি ভিডিও পরামর্শ (E2E Encrypted)"
                                        AppLanguage.HINDI -> "कृषि वीडियो परामर्श (E2E Encrypted)"
                                        AppLanguage.ENGLISH -> "Agri Video Consultation (E2E Encrypted)"
                                    }
                                } else {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "কৃষি অডিও পরামর্শ (Verified VoIP)"
                                        AppLanguage.HINDI -> "कृषि ऑडियो परामर्श (Verified VoIP)"
                                        AppLanguage.ENGLISH -> "Agri Audio Consultation (Verified VoIP)"
                                    }
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Avatar / Simulated Camera feed
                    Box(
                        modifier = Modifier
                            .size(if (isNarrow) 112.dp else 136.dp)
                            .background(AgriGreenPrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = expert.photoAvatar,
                            fontSize = if (isNarrow) 54.sp else 64.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = ExpertTranslations.getDisplayName(expert, currentLanguage),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = ExpertTranslations.getDisplayDesignation(expert, currentLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.ui.theme.RoyalTextSecondary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = ExpertTranslations.getDisplayInstitution(expert, currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.ui.theme.RoyalSoftGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isConnected) com.example.ui.theme.RoyalGreenEmerald else com.example.ui.theme.RoyalWarningBg
                    ) {
                        val timePrefix = when (currentLanguage) {
                            AppLanguage.BENGALI -> "সময়:"
                            AppLanguage.HINDI -> "समय:"
                            AppLanguage.ENGLISH -> "Time:"
                        }
                        Text(
                            text = if (isConnected) "$timePrefix ${formatDuration(secondsElapsed)}" else callStateText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    if (isVideoCall && isVideoEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val cameraLabel = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "📷 লাইভ ক্যামেরা সক্রিয় (${if (isFrontCamera) "সামনের ক্যামেরা" else "পেছনের ক্যামেরা - ফসলের দিকে"})"
                                    AppLanguage.HINDI -> "📷 लाइव कैमरा सक्रिय (${if (isFrontCamera) "फ़्रंट कैमरा" else "रियर कैमरा - फसल की ओर"})"
                                    AppLanguage.ENGLISH -> "📷 Live Camera Active (${if (isFrontCamera) "Front Camera" else "Rear Camera - Facing Crop"})"
                                }
                                Text(
                                    text = cameraLabel,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Controls at Bottom
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (isNarrow) 14.dp else 20.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mute Toggle
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(56.dp)
                                .background(if (isMuted) Color.White else Color(0x33FFFFFF), CircleShape)
                                .testTag("call_mute_toggle")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isMuted) Color.Black else Color.White
                            )
                        }

                        // Speaker Toggle
                        IconButton(
                            onClick = { isSpeakerOn = !isSpeakerOn },
                            modifier = Modifier
                                .size(56.dp)
                                .background(if (isSpeakerOn) com.example.ui.theme.RoyalGold else Color(0x33FFFFFF), CircleShape)
                                .testTag("call_speaker_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) com.example.ui.theme.RoyalGreenPrimaryBg else Color.White
                            )
                        }

                        // Video Toggle (if video call)
                        if (isVideoCall) {
                            IconButton(
                                onClick = { isVideoEnabled = !isVideoEnabled },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(if (isVideoEnabled) com.example.ui.theme.RoyalGold else Color(0x33FFFFFF), CircleShape)
                                    .testTag("call_video_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = "Video",
                                    tint = if (isVideoEnabled) com.example.ui.theme.RoyalGreenPrimaryBg else Color.White
                                )
                            }

                            // Switch Camera
                            IconButton(
                                onClick = { isFrontCamera = !isFrontCamera },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0x33FFFFFF), CircleShape)
                                    .testTag("call_switch_camera")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cameraswitch,
                                    contentDescription = "Switch Camera",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // End Call Button
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(72.dp)
                            .background(AgriAlertRed, CircleShape)
                            .testTag("call_end_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কল কাটুন"
                            AppLanguage.HINDI -> "कॉल काटें"
                            AppLanguage.ENGLISH -> "End Call"
                        },
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
