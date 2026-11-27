package com.example.ui.video

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.FarmerVideoProgress
import com.example.data.model.FarmingStage
import com.example.data.model.VideoCategory
import com.example.data.model.VideoLesson
import com.example.data.model.formatMMSS
import com.example.data.remote.VideoLearningRepository
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoLearningCentreScreen(
    currentLanguage: AppLanguage,
    userId: String,
    onBack: () -> Unit,
    onOpenAdminVideoManager: () -> Unit = {},
    onOpenCropGuide: (cropId: String) -> Unit = {},
    onAskAiAboutLesson: (lesson: VideoLesson, currentSeconds: Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val repo = remember(context) { VideoLearningRepository(context) }
    val isAdmin = remember(context, userId) { UserSessionManager.isAdmin(context) }
    val coroutineScope = rememberCoroutineScope()

    val lessons by repo.observeLessons(includeUnpublished = false)
        .collectAsState(initial = repo.getCachedLessons(includeUnpublished = false))
    val progressMap by repo.observeUserProgress(userId)
        .collectAsState(initial = repo.getLocalUserProgressMap(userId))

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<VideoCategory?>(null) }
    var selectedStage by remember { mutableStateOf<FarmingStage?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var activeLesson by remember { mutableStateOf<VideoLesson?>(null) }

    // Keep activeLesson updated if cloud/cache refreshes
    LaunchedEffect(lessons, activeLesson?.id) {
        val currentId = activeLesson?.id ?: return@LaunchedEffect
        lessons.firstOrNull { it.id == currentId }?.let { activeLesson = it }
    }

    val filteredLessons = remember(lessons, searchQuery, selectedCategory, selectedStage, favoritesOnly, progressMap) {
        lessons.filter { lesson ->
            val catOk = selectedCategory == null || lesson.category() == selectedCategory
            val stageOk = selectedStage == null || lesson.stage() == selectedStage
            val favOk = !favoritesOnly || (progressMap[lesson.id]?.isFavorite == true)
            val queryOk = lesson.matchesSearch(searchQuery)
            catOk && stageOk && favOk && queryOk
        }
    }

    val continueWatchingLessons = remember(lessons, progressMap) {
        lessons.filter { lesson ->
            val p = progressMap[lesson.id]
            p != null && p.lastPositionSeconds > 0 && !p.completed
        }.sortedByDescending { progressMap[it.id]?.updatedAtMillis ?: 0L }
    }

    BackHandler {
        if (activeLesson != null) {
            activeLesson = null
        } else {
            onBack()
        }
    }

    val currentLesson = activeLesson
    if (currentLesson != null) {
        VideoLessonDetailView(
            lesson = currentLesson,
            language = currentLanguage,
            progress = progressMap[currentLesson.id],
            onBack = { activeLesson = null },
            onToggleFavorite = {
                coroutineScope.launch {
                    repo.toggleFavorite(userId, currentLesson, currentLanguage)
                }
            },
            onSaveProgress = { posSec, done ->
                coroutineScope.launch {
                    repo.saveProgress(
                        userId = userId,
                        lessonId = currentLesson.id,
                        positionSeconds = posSec,
                        totalDurationSeconds = currentLesson.durationSeconds,
                        completed = done,
                        language = currentLanguage
                    )
                }
            },
            onOpenCropGuide = {
                if (currentLesson.linkedCropId.isNotBlank()) {
                    onOpenCropGuide(currentLesson.linkedCropId)
                }
            },
            onAskAi = { posSec ->
                onAskAiAboutLesson(currentLesson, posSec)
            }
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
                            .testTag("video_learning_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🎬 কৃষি শিক্ষা — অ্যানিমেটেড ভিডিও গাইড"
                                AppLanguage.HINDI -> "🎬 कृषि शिक्षा — एनिमेटेड वीडियो गाइड"
                                AppLanguage.ENGLISH -> "🎬 Krishi Shiksha — Video Learning Centre"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "হাতে-কলমে চাষের ধাপ, ১৬টি বিভাগ ও বহুভাষিক পাঠ"
                                AppLanguage.HINDI -> "व्यावहारिक खेती के चरण, 16 श्रेणियां व बहुभाषी पाठ"
                                AppLanguage.ENGLISH -> "Practical farming lessons • 16 Categories • BN / HI / EN"
                            },
                            color = Color(0xFFC5D8C4),
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { favoritesOnly = !favoritesOnly },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("video_favorites_filter_btn")
                    ) {
                        Icon(
                            imageVector = if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (favoritesOnly) RoyalGold else Color.White
                        )
                    }
                    if (isAdmin) {
                        IconButton(
                            onClick = onOpenAdminVideoManager,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("video_open_admin_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Video Manager",
                                tint = RoyalGold
                            )
                        }
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
                    .widthIn(max = 880.dp),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Box
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = RoyalSoftGold)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = RoyalTextSecondary)
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ফসল বা বিষয় খুঁজুন (যেমন: টমেটো, ধান, মাটি, সেচ)..."
                                    AppLanguage.HINDI -> "फसल या विषय खोजें (जैसे: टमाटर, धान, मिट्टी, सिंचाई)..."
                                    AppLanguage.ENGLISH -> "Search crop or topic (e.g. tomato, paddy, soil, pest)..."
                                },
                                color = RoyalTextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RoyalInputBg,
                            unfocusedContainerColor = RoyalInputBg,
                            focusedTextColor = RoyalTextPrimary,
                            unfocusedTextColor = RoyalTextPrimary,
                            focusedBorderColor = RoyalGold,
                            unfocusedBorderColor = RoyalBorder
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_search_input")
                    )
                }

                // Continue Watching Shelf
                if (continueWatchingLessons.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "▶️ দেখা চালিয়ে যান (Continue Watching)"
                                    AppLanguage.HINDI -> "▶️ देखना जारी रखें (Continue Watching)"
                                    AppLanguage.ENGLISH -> "▶️ Continue Learning"
                                },
                                color = RoyalSoftGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(continueWatchingLessons, key = { "cw_${it.id}" }) { lesson ->
                                    val prog = progressMap[lesson.id]
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .width(235.dp)
                                            .clickable { activeLesson = lesson }
                                            .testTag("continue_card_${lesson.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = lesson.getTitle(currentLanguage),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            LinearProgressIndicator(
                                                progress = { prog?.progressFraction() ?: 0f },
                                                color = RoyalGold,
                                                trackColor = RoyalBorder,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                            )
                                            Text(
                                                text = "${formatMMSS(prog?.lastPositionSeconds ?: 0)} / ${lesson.formattedDuration()}",
                                                color = RoyalSoftGold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Farming Stage Filter Strip
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "চাষের ধাপ অনুযায়ী বাছাই"
                                AppLanguage.HINDI -> "खेती के चरण के अनुसार चुनें"
                                AppLanguage.ENGLISH -> "Filter by Farming Stage"
                            },
                            color = RoyalTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedStage == null,
                                    onClick = { selectedStage = null },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "সব ধাপ"
                                                AppLanguage.HINDI -> "सभी चरण"
                                                AppLanguage.ENGLISH -> "All Stages"
                                            },
                                            fontSize = 11.5.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGreenEmerald,
                                        selectedLabelColor = RoyalSoftGold,
                                        containerColor = RoyalGreenSecondaryBg,
                                        labelColor = RoyalTextSecondary
                                    ),
                                    modifier = Modifier.heightIn(min = 40.dp)
                                )
                            }
                            items(FarmingStage.entries, key = { it.code }) { stage ->
                                FilterChip(
                                    selected = selectedStage == stage,
                                    onClick = {
                                        selectedStage = if (selectedStage == stage) null else stage
                                    },
                                    label = { Text(stage.getTitle(currentLanguage), fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGreenEmerald,
                                        selectedLabelColor = RoyalSoftGold,
                                        containerColor = RoyalGreenSecondaryBg,
                                        labelColor = RoyalTextSecondary
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .testTag("video_stage_${stage.code}")
                                )
                            }
                        }
                    }
                }

                // 16 Video Learning Categories Strip
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "১৬টি কৃষি শিক্ষা বিভাগ"
                                AppLanguage.HINDI -> "16 कृषि शिक्षा श्रेणियां"
                                AppLanguage.ENGLISH -> "16 Learning Categories"
                            },
                            color = RoyalSoftGold,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "✨ সব বিভাগ"
                                                AppLanguage.HINDI -> "✨ सभी श्रेणियां"
                                                AppLanguage.ENGLISH -> "✨ All Categories"
                                            },
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGreenEmerald,
                                        selectedLabelColor = RoyalSoftGold,
                                        containerColor = RoyalGreenSecondaryBg,
                                        labelColor = RoyalTextSecondary
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 42.dp)
                                        .testTag("video_cat_ALL")
                                )
                            }
                            items(VideoCategory.entries, key = { it.code }) { cat ->
                                val selected = selectedCategory == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = if (selected) null else cat },
                                    label = {
                                        Text("${cat.emoji} ${cat.getTitle(currentLanguage)}", fontSize = 12.sp)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGreenEmerald,
                                        selectedLabelColor = RoyalSoftGold,
                                        containerColor = RoyalGreenSecondaryBg,
                                        labelColor = RoyalTextSecondary
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 42.dp)
                                        .testTag("video_cat_${cat.code}")
                                )
                            }
                        }
                    }
                }

                if (filteredLessons.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                            border = BorderStroke(1.dp, RoyalBorder),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "এই বিভাগে বা অনুসন্ধানে কোনো পাঠ পাওয়া যায়নি"
                                        AppLanguage.HINDI -> "इस श्रेणी या खोज में कोई पाठ नहीं मिला"
                                        AppLanguage.ENGLISH -> "No lessons match the current filter"
                                    },
                                    color = RoyalTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                OutlinedButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedCategory = null
                                        selectedStage = null
                                        favoritesOnly = false
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সব ফিল্টার মুছুন"
                                            AppLanguage.HINDI -> "फ़िल्टर रीसेट करें"
                                            AppLanguage.ENGLISH -> "Reset Filters"
                                        },
                                        color = RoyalSoftGold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredLessons, key = { it.id }) { lesson ->
                        VideoLessonListCard(
                            lesson = lesson,
                            language = currentLanguage,
                            progress = progressMap[lesson.id],
                            onClick = { activeLesson = lesson },
                            onToggleFavorite = {
                                coroutineScope.launch {
                                    repo.toggleFavorite(userId, lesson, currentLanguage)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoLessonListCard(
    lesson: VideoLesson,
    language: AppLanguage,
    progress: FarmerVideoProgress?,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val isPlayable = lesson.hasPlayableVideo()
    val isFav = progress?.isFavorite == true
    val isDone = progress?.completed == true

    Card(
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, if (isDone) RoyalGold.copy(alpha = 0.7f) else RoyalBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("video_lesson_card_${lesson.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(RoyalGreenSecondaryBg)
            ) {
                if (lesson.thumbnailUrl.startsWith("https://", ignoreCase = true)) {
                    AsyncImage(
                        model = lesson.thumbnailUrl,
                        contentDescription = lesson.getTitle(language),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = lesson.fallbackDrawableRes()),
                        contentDescription = lesson.getTitle(language),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )

                // Honest badge: Playable Video vs Storyboard Breakdown
                Surface(
                    color = if (isPlayable) Color(0xFF15803D) else Color(0xFF064E3B).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayable) Icons.Default.PlayCircle else Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = RoyalSoftGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isPlayable) {
                                when (language) {
                                    AppLanguage.BENGALI -> "ভিডিও চালু"
                                    AppLanguage.HINDI -> "वीडियो उपलब्ध"
                                    AppLanguage.ENGLISH -> "Playable Video"
                                }
                            } else {
                                when (language) {
                                    AppLanguage.BENGALI -> "স্টোরিবোর্ড ও গাইড (${lesson.scenes.size} দৃশ্য)"
                                    AppLanguage.HINDI -> "स्टोरीबोर्ड गाइड (${lesson.scenes.size} दृश्य)"
                                    AppLanguage.ENGLISH -> "Storyboard Guide (${lesson.scenes.size} Scenes)"
                                }
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Favorite button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("video_fav_btn_${lesson.id}")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark Favorite",
                        tint = if (isFav) RoyalGold else Color.White
                    )
                }

                // Bottom duration + completion pill
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = Color.Black.copy(alpha = 0.72f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "${lesson.category().emoji} ${lesson.category().getTitle(language)}",
                            color = RoyalSoftGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(color = Color.Black.copy(alpha = 0.78f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = lesson.formattedDuration(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${lesson.lessonNumber}. ${lesson.getTitle(language)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = RoyalSuccessText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = lesson.getSummary(language),
                    color = RoyalTextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    lesson.getCropTags(language).take(3).forEach { tag ->
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, RoyalBorder)
                        ) {
                            Text(
                                text = tag,
                                color = Color(0xFFC5D8C4),
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (lesson.isReviewedByExpert) {
                        Surface(
                            color = RoyalSuccessBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = RoyalSuccessText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = when (language) {
                                        AppLanguage.BENGALI -> "যাচাইকৃত গাইড"
                                        AppLanguage.HINDI -> "सत्यापित पाठ"
                                        AppLanguage.ENGLISH -> "Reviewed"
                                    },
                                    color = RoyalSuccessText,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (progress != null && progress.lastPositionSeconds > 0 && !progress.completed) {
                    LinearProgressIndicator(
                        progress = { progress.progressFraction() },
                        color = RoyalGold,
                        trackColor = RoyalBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoLessonDetailView(
    lesson: VideoLesson,
    language: AppLanguage,
    progress: FarmerVideoProgress?,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSaveProgress: (positionSeconds: Int, completed: Boolean) -> Unit,
    onOpenCropGuide: () -> Unit,
    onAskAi: (currentSeconds: Int) -> Unit
) {
    var activePositionSec by remember(lesson.id) {
        mutableIntStateOf(progress?.lastPositionSeconds ?: 0)
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
                            .testTag("video_detail_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.getTitle(language),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${lesson.category().emoji} ${lesson.category().getTitle(language)} • ${lesson.formattedDuration()}",
                            color = RoyalSoftGold,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("video_detail_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (progress?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (progress?.isFavorite == true) RoyalGold else Color.White
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 880.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LessonVideoOrStoryboardPlayer(
                    lesson = lesson,
                    language = language,
                    initialPositionSeconds = progress?.lastPositionSeconds ?: 0,
                    onPositionChanged = { sec, completed ->
                        activePositionSec = sec
                        onSaveProgress(sec, completed)
                    }
                )

                // Honest Multi-Language Audio & Subtitle Track Status
                Surface(
                    color = RoyalGreenCardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val hasAudio = lesson.hasSeparateAudioTrack(language)
                        val audioLabel = if (hasAudio) {
                            when (language) {
                                AppLanguage.BENGALI -> "অডিও ট্র্যাক (${language.nativeName}): আপলোড করা আছে"
                                AppLanguage.HINDI -> "ऑडियो ट्रैक (${language.nativeName}): उपलब्ध"
                                AppLanguage.ENGLISH -> "Narration Audio (${language.nativeName}): Available"
                            }
                        } else {
                            when (language) {
                                AppLanguage.BENGALI -> "অডিও ট্র্যাক (${language.nativeName}): আলাদা ভয়েস ট্র্যাক এখনো আপলোড হয়নি; সম্পূর্ণ বাংলা পাঠ্য ও সাবটাইটেল নিচে রয়েছে"
                                AppLanguage.HINDI -> "ऑडियो ट्रैक (${language.nativeName}): अलग ऑडियो अभी अपलोड नहीं है; पूरी स्क्रिप्ट व सबटाइटल उपलब्ध हैं"
                                AppLanguage.ENGLISH -> "Audio Narration (${language.nativeName}): Separate voice file not uploaded yet; full localized script & subtitles provided"
                            }
                        }
                        Text(
                            text = "🔊 $audioLabel",
                            color = RoyalTextPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "📚 ${lesson.contentSource} • ${lesson.lastReviewedDate.ifBlank { "Reviewed" }}",
                            color = RoyalSoftGold,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Objective & Action Buttons (Ask AI + Open Crop Guidance)
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.BENGALI -> "🎯 শেখার উদ্দেশ্য"
                                AppLanguage.HINDI -> "🎯 सीखने का उद्देश्य"
                                AppLanguage.ENGLISH -> "🎯 Learning Objective"
                            },
                            color = RoyalSoftGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = lesson.getObjective(language),
                            color = RoyalTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        HorizontalDivider(color = RoyalBorder)

                        Button(
                            onClick = { onAskAi(activePositionSec) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalGreenEmerald,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("video_ask_ai_btn")
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.BENGALI -> "এই পাঠ সম্পর্কে কৃষক AI-কে জিজ্ঞাসা করুন"
                                    AppLanguage.HINDI -> "इस वीडियो पाठ के बारे में कृषक AI से पूछें"
                                    AppLanguage.ENGLISH -> "Ask Krishak AI About This Lesson"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (lesson.linkedCropId.isNotBlank()) {
                            OutlinedButton(
                                onClick = onOpenCropGuide,
                                border = BorderStroke(1.dp, RoyalGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("video_open_crop_guide_btn")
                            ) {
                                Icon(Icons.Default.Spa, contentDescription = null, tint = RoyalSoftGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.BENGALI -> "সম্পর্কিত ফসল নির্দেশিকা দেখুন"
                                        AppLanguage.HINDI -> "संबंधित फसल गाइड देखें"
                                        AppLanguage.ENGLISH -> "Open Related Crop Guidance"
                                    },
                                    color = RoyalSoftGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Agricultural Safety & Accuracy Disclaimer
                Surface(
                    color = RoyalWarningBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = RoyalGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = lesson.getSafetyDisclaimer(language),
                            color = RoyalWarningText,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
