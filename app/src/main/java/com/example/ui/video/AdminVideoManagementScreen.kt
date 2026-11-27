package com.example.ui.video

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.FarmingStage
import com.example.data.model.VideoCategory
import com.example.data.model.VideoLesson
import com.example.data.model.VideoPublicationStatus
import com.example.data.remote.VideoLearningRepository
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalInputBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminVideoManagementScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin = remember(context) { UserSessionManager.isAdmin(context) }

    BackHandler { onBack() }

    if (!isAdmin) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RoyalGreenPrimaryBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RoyalAlertBg),
                border = BorderStroke(1.dp, RoyalAlertText),
                modifier = Modifier.testTag("admin_video_access_denied")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অ্যাডমিন যাচাইকরণ প্রয়োজন"
                            AppLanguage.HINDI -> "एडमिन सत्यापन आवश्यक है"
                            AppLanguage.ENGLISH -> "Admin Verification Required"
                        },
                        color = RoyalAlertText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Back")
                    }
                }
            }
        }
        return
    }

    val repo = remember(context) { VideoLearningRepository(context) }
    val lessons by repo.observeLessons(includeUnpublished = true)
        .collectAsState(initial = repo.getCachedLessons(includeUnpublished = true))

    var editingLesson by remember { mutableStateOf<VideoLesson?>(null) }

    if (editingLesson != null) {
        AdminLessonEditorView(
            initialLesson = editingLesson!!,
            language = currentLanguage,
            repo = repo,
            onBack = { editingLesson = null }
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = RoyalGreenPrimaryBg,
        topBar = {
            Surface(color = Color(0xFF064E3B), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("admin_video_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🎬 কৃষি শিক্ষা অ্যাডমিন স্টুডিও"
                                AppLanguage.HINDI -> "🎬 कृषि शिक्षा एडमिन स्टूडियो"
                                AppLanguage.ENGLISH -> "🎬 Krishi Shiksha Video Studio (Admin)"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ভিডিও আপলোড, বহুভাষিক অডিও/সাবটাইটেল ও প্রকাশনা"
                                AppLanguage.HINDI -> "वीडियो अपलोड, बहुभाषी ऑडियो/सबटाइटल व प्रकाशन"
                                AppLanguage.ENGLISH -> "Upload videos, thumbnails, BN/HI/EN tracks & publish"
                            },
                            color = Color(0xFFC5D8C4),
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = {
                            val nextNum = (lessons.maxOfOrNull { it.lessonNumber } ?: 0) + 1
                            editingLesson = VideoLesson(
                                id = "vid_custom_${UUID.randomUUID().toString().take(8)}",
                                lessonNumber = nextNum,
                                publicationStatus = VideoPublicationStatus.DRAFT.wireValue,
                                isBuiltInCatalog = false,
                                lastReviewedDate = "2026-11-26"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = Color(0xFF062D20)),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("admin_video_new_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নতুন পাঠ"
                                AppLanguage.HINDI -> "नया पाठ"
                                AppLanguage.ENGLISH -> "New"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RoyalGreenPrimaryBg),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 860.dp),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lessons, key = { it.id }) { lesson ->
                    val status = lesson.status()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        border = BorderStroke(1.dp, RoyalBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingLesson = lesson }
                            .testTag("admin_video_row_${lesson.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${lesson.lessonNumber}. ${lesson.getTitle(currentLanguage)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = RoyalSoftGold)
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = when (status) {
                                        VideoPublicationStatus.PUBLISHED -> RoyalSuccessBg
                                        VideoPublicationStatus.UNPUBLISHED -> RoyalAlertBg
                                        else -> RoyalWarningBg
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = status.getLabel(currentLanguage),
                                        color = when (status) {
                                            VideoPublicationStatus.PUBLISHED -> RoyalSuccessText
                                            VideoPublicationStatus.UNPUBLISHED -> RoyalAlertText
                                            else -> RoyalWarningText
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    color = if (lesson.hasPlayableVideo()) RoyalSuccessBg else RoyalGreenSecondaryBg,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (lesson.hasPlayableVideo()) "Video Uploaded" else "Storyboard Only",
                                        color = if (lesson.hasPlayableVideo()) RoyalSuccessText else RoyalTextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(color = RoyalGreenSecondaryBg, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "${lesson.category().emoji} ${lesson.category().getTitle(currentLanguage)}",
                                        color = RoyalSoftGold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLessonEditorView(
    initialLesson: VideoLesson,
    language: AppLanguage,
    repo: VideoLearningRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var titleBn by remember { mutableStateOf(initialLesson.titleBn) }
    var titleHi by remember { mutableStateOf(initialLesson.titleHi) }
    var titleEn by remember { mutableStateOf(initialLesson.titleEn) }
    var summaryBn by remember { mutableStateOf(initialLesson.summaryBn) }
    var summaryHi by remember { mutableStateOf(initialLesson.summaryHi) }
    var summaryEn by remember { mutableStateOf(initialLesson.summaryEn) }
    var linkedCropId by remember { mutableStateOf(initialLesson.linkedCropId) }
    var selectedCategory by remember { mutableStateOf(initialLesson.category()) }
    var selectedStage by remember { mutableStateOf(initialLesson.stage()) }
    var videoUrl by remember { mutableStateOf(initialLesson.videoUrl) }
    var thumbnailUrl by remember { mutableStateOf(initialLesson.thumbnailUrl) }
    var audioUrlBn by remember { mutableStateOf(initialLesson.audioUrlBn) }
    var audioUrlHi by remember { mutableStateOf(initialLesson.audioUrlHi) }
    var audioUrlEn by remember { mutableStateOf(initialLesson.audioUrlEn) }
    var subtitleUrlBn by remember { mutableStateOf(initialLesson.subtitleUrlBn) }
    var subtitleUrlHi by remember { mutableStateOf(initialLesson.subtitleUrlHi) }
    var subtitleUrlEn by remember { mutableStateOf(initialLesson.subtitleUrlEn) }
    var isReviewedByExpert by remember { mutableStateOf(initialLesson.isReviewedByExpert) }
    var reviewedByName by remember { mutableStateOf(initialLesson.reviewedByName) }
    var lastReviewedDate by remember { mutableStateOf(initialLesson.lastReviewedDate) }
    var contentSource by remember { mutableStateOf(initialLesson.contentSource) }
    var status by remember { mutableStateOf(initialLesson.status()) }

    var isUploading by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    fun buildCurrentLesson(): VideoLesson = initialLesson.copy(
        categoryCode = selectedCategory.code,
        stageCode = selectedStage.code,
        linkedCropId = linkedCropId.trim(),
        titleBn = titleBn.trim(),
        titleHi = titleHi.trim(),
        titleEn = titleEn.trim(),
        summaryBn = summaryBn.trim(),
        summaryHi = summaryHi.trim(),
        summaryEn = summaryEn.trim(),
        videoUrl = videoUrl.trim(),
        thumbnailUrl = thumbnailUrl.trim(),
        audioUrlBn = audioUrlBn.trim(),
        audioUrlHi = audioUrlHi.trim(),
        audioUrlEn = audioUrlEn.trim(),
        subtitleUrlBn = subtitleUrlBn.trim(),
        subtitleUrlHi = subtitleUrlHi.trim(),
        subtitleUrlEn = subtitleUrlEn.trim(),
        isReviewedByExpert = isReviewedByExpert,
        reviewedByName = reviewedByName.trim(),
        lastReviewedDate = lastReviewedDate.trim(),
        contentSource = contentSource.trim(),
        publicationStatus = status.wireValue
    )

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            isUploading = true
            scope.launch {
                val res = repo.uploadAdminMediaFile(initialLesson.id, uri, "video", "video/mp4")
                isUploading = false
                res.onSuccess {
                    videoUrl = it.downloadUrl
                    statusBanner = "Video uploaded to Cloud Storage."
                }.onFailure {
                    statusBanner = it.localizedMessage ?: "Video upload failed"
                }
            }
        }
    }

    val thumbPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            isUploading = true
            scope.launch {
                val res = repo.uploadAdminThumbnail(initialLesson.id, uri)
                isUploading = false
                res.onSuccess {
                    thumbnailUrl = it.downloadUrl
                    statusBanner = "Thumbnail uploaded cleanly."
                }.onFailure {
                    statusBanner = it.localizedMessage ?: "Thumbnail upload failed"
                }
            }
        }
    }

    var pendingAudioLang by remember { mutableStateOf(AppLanguage.BENGALI) }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val slot = when (pendingAudioLang) {
                AppLanguage.BENGALI -> "audio_bn"
                AppLanguage.HINDI -> "audio_hi"
                AppLanguage.ENGLISH -> "audio_en"
            }
            isUploading = true
            scope.launch {
                val res = repo.uploadAdminMediaFile(initialLesson.id, uri, slot, "audio/mp4")
                isUploading = false
                res.onSuccess {
                    when (pendingAudioLang) {
                        AppLanguage.BENGALI -> audioUrlBn = it.downloadUrl
                        AppLanguage.HINDI -> audioUrlHi = it.downloadUrl
                        AppLanguage.ENGLISH -> audioUrlEn = it.downloadUrl
                    }
                    statusBanner = "${pendingAudioLang.nativeName} audio track uploaded."
                }.onFailure {
                    statusBanner = it.localizedMessage ?: "Audio upload failed"
                }
            }
        }
    }

    var pendingSubLang by remember { mutableStateOf(AppLanguage.BENGALI) }
    val subtitlePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val slot = when (pendingSubLang) {
                AppLanguage.BENGALI -> "sub_bn"
                AppLanguage.HINDI -> "sub_hi"
                AppLanguage.ENGLISH -> "sub_en"
            }
            isUploading = true
            scope.launch {
                val res = repo.uploadAdminMediaFile(initialLesson.id, uri, slot, "text/vtt")
                isUploading = false
                res.onSuccess {
                    when (pendingSubLang) {
                        AppLanguage.BENGALI -> subtitleUrlBn = it.downloadUrl
                        AppLanguage.HINDI -> subtitleUrlHi = it.downloadUrl
                        AppLanguage.ENGLISH -> subtitleUrlEn = it.downloadUrl
                    }
                    statusBanner = "${pendingSubLang.nativeName} subtitle file uploaded."
                }.onFailure {
                    statusBanner = it.localizedMessage ?: "Subtitle upload failed"
                }
            }
        }
    }

    BackHandler { onBack() }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = RoyalGreenPrimaryBg,
        topBar = {
            Surface(color = Color(0xFF064E3B), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit Lesson #${initialLesson.lessonNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showPreview = !showPreview },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("admin_video_preview_toggle")
                    ) {
                        Icon(Icons.Default.Preview, contentDescription = "Preview", tint = RoyalGold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RoyalGreenPrimaryBg),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isUploading) {
                    Surface(color = RoyalInfoBarColor, shape = RoundedCornerShape(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = RoyalGold, strokeWidth = 2.dp)
                            Text("Uploading file to Firebase Storage...", color = Color.White, fontSize = 12.5.sp)
                        }
                    }
                }

                if (statusBanner != null) {
                    Surface(
                        color = RoyalSuccessBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RoyalGold)
                    ) {
                        Text(
                            text = statusBanner.orEmpty(),
                            color = RoyalSuccessText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (showPreview) {
                    LessonVideoOrStoryboardPlayer(
                        lesson = buildCurrentLesson(),
                        language = language,
                        onPositionChanged = { _, _ -> }
                    )
                    HorizontalDivider(color = RoyalBorder)
                }

                // Media Upload Buttons Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "☁️ Media Uploads (Cloud Storage)",
                            color = RoyalSoftGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { videoPicker.launch("video/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_upload_video_btn")
                            ) {
                                Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload MP4", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    thumbPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                border = BorderStroke(1.dp, RoyalGold),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_upload_thumb_btn")
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Thumbnail", color = RoyalSoftGold, fontSize = 12.sp)
                            }
                        }

                        // Audio & Subtitle Uploaders per language
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AppLanguage.entries.forEach { lang ->
                                OutlinedButton(
                                    onClick = {
                                        pendingAudioLang = lang
                                        audioPicker.launch("audio/*")
                                    },
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(lang.code.uppercase(), color = RoyalTextPrimary, fontSize = 11.sp)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AppLanguage.entries.forEach { lang ->
                                OutlinedButton(
                                    onClick = {
                                        pendingSubLang = lang
                                        subtitlePicker.launch("text/*")
                                    },
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                ) {
                                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sub ${lang.code.uppercase()}", color = RoyalTextPrimary, fontSize = 11.sp)
                                }
                            }
                        }

                        EditorTextField("Playable Video HTTPS URL", videoUrl, { videoUrl = it })
                        EditorTextField("Thumbnail HTTPS URL", thumbnailUrl, { thumbnailUrl = it })
                    }
                }

                // Localized Text Fields Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🌐 Multilingual Metadata (BN / HI / EN)",
                            color = RoyalSoftGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        EditorTextField("Title (Bengali — বাংলা)", titleBn, { titleBn = it })
                        EditorTextField("Title (Hindi — हिन्दी)", titleHi, { titleHi = it })
                        EditorTextField("Title (English)", titleEn, { titleEn = it })
                        EditorTextField("Summary (Bengali)", summaryBn, { summaryBn = it })
                        EditorTextField("Summary (Hindi)", summaryHi, { summaryHi = it })
                        EditorTextField("Summary (English)", summaryEn, { summaryEn = it })
                        EditorTextField("Linked Crop ID (e.g. tomato, rice_paddy, potato)", linkedCropId, { linkedCropId = it })
                    }
                }

                // Category & Farming Stage Selectors
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Category (16 Options)", color = RoyalSoftGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(VideoCategory.entries, key = { it.code }) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text("${cat.emoji} ${cat.getTitle(language)}", fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGreenEmerald,
                                        selectedLabelColor = RoyalSoftGold,
                                        containerColor = RoyalGreenSecondaryBg,
                                        labelColor = RoyalTextSecondary
                                    )
                                )
                            }
                        }

                        Text("Farming Stage", color = RoyalSoftGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(FarmingStage.entries, key = { it.code }) { stage ->
                                FilterChip(
                                    selected = selectedStage == stage,
                                    onClick = { selectedStage = stage },
                                    label = { Text(stage.getTitle(language), fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGreenEmerald,
                                        selectedLabelColor = RoyalSoftGold,
                                        containerColor = RoyalGreenSecondaryBg,
                                        labelColor = RoyalTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                // Agricultural Accuracy & Publish Controls
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("✅ Agricultural Accuracy & Publishing", color = RoyalSoftGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Expert / Admin Accuracy Reviewed", color = RoyalTextPrimary, fontSize = 13.sp)
                            Switch(checked = isReviewedByExpert, onCheckedChange = { isReviewedByExpert = it })
                        }
                        EditorTextField("Reviewed By", reviewedByName, { reviewedByName = it })
                        EditorTextField("Last Reviewed Date (YYYY-MM-DD)", lastReviewedDate, { lastReviewedDate = it })
                        EditorTextField("Content Source", contentSource, { contentSource = it })

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val lesson = buildCurrentLesson()
                                        val res = repo.saveOrUpdateLesson(lesson)
                                        statusBanner = if (res.isSuccess) {
                                            "Saved lesson draft/updates."
                                        } else {
                                            res.exceptionOrNull()?.localizedMessage ?: "Save failed"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_video_save_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save", fontWeight = FontWeight.Bold)
                            }

                            val nextStatus = if (status == VideoPublicationStatus.PUBLISHED) {
                                VideoPublicationStatus.UNPUBLISHED
                            } else {
                                VideoPublicationStatus.PUBLISHED
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        val res = repo.setPublicationStatus(
                                            lesson = buildCurrentLesson(),
                                            newStatus = nextStatus,
                                            expertReviewed = isReviewedByExpert,
                                            reviewedBy = reviewedByName,
                                            reviewedDate = lastReviewedDate.ifBlank { "2026-11-26" }
                                        )
                                        res.onSuccess {
                                            status = it.status()
                                            Toast.makeText(context, "Status: ${status.wireValue}", Toast.LENGTH_SHORT).show()
                                        }.onFailure {
                                            statusBanner = it.localizedMessage ?: "Failed to change status"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (status == VideoPublicationStatus.PUBLISHED) RoyalAlertBg else RoyalGold,
                                    contentColor = if (status == VideoPublicationStatus.PUBLISHED) RoyalAlertText else Color(0xFF062D20)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_video_publish_btn")
                            ) {
                                Icon(Icons.Default.PublishedWithChanges, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (status == VideoPublicationStatus.PUBLISHED) "Unpublish" else "Publish",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val RoyalInfoBarColor = Color(0xFF0D3D47)

@Composable
private fun EditorTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp, color = RoyalTextSecondary) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = RoyalInputBg,
            unfocusedContainerColor = RoyalInputBg,
            focusedTextColor = RoyalTextPrimary,
            unfocusedTextColor = RoyalTextPrimary,
            focusedBorderColor = RoyalGold,
            unfocusedBorderColor = RoyalBorder
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
