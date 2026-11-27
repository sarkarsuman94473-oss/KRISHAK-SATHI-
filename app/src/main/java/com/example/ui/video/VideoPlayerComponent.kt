package com.example.ui.video

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.VideoLesson
import com.example.data.model.VideoStoryboardScene
import com.example.data.model.formatMMSS
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalInfoBg
import com.example.ui.theme.RoyalInfoText
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import kotlinx.coroutines.delay

/**
 * Dual-mode Lesson Media Component:
 * 1) Real Jetpack Media3 ExoPlayer video stream when `lesson.hasPlayableVideo()` is true
 *    (play/pause, replay, progress seek, playback speed 0.75x-1.5x, subtitles, chapters, fullscreen).
 * 2) Honest Interactive Storyboard & Guided Breakdown when `lesson.hasPlayableVideo()` is false
 *    — explicitly labeled as a Storyboard Plan awaiting Admin video upload rather than faking playback.
 */
@Composable
fun LessonVideoOrStoryboardPlayer(
    lesson: VideoLesson,
    language: AppLanguage,
    initialPositionSeconds: Int = 0,
    onPositionChanged: (positionSeconds: Int, completed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (lesson.hasPlayableVideo()) {
        RealExoVideoPlayerSection(
            lesson = lesson,
            language = language,
            initialPositionSeconds = initialPositionSeconds,
            onPositionChanged = onPositionChanged,
            modifier = modifier
        )
    } else {
        InteractiveStoryboardLessonViewer(
            lesson = lesson,
            language = language,
            initialPositionSeconds = initialPositionSeconds,
            onPositionChanged = onPositionChanged,
            modifier = modifier
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun RealExoVideoPlayerSection(
    lesson: VideoLesson,
    language: AppLanguage,
    initialPositionSeconds: Int,
    onPositionChanged: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosSec by remember(lesson.id) { mutableIntStateOf(initialPositionSeconds.coerceAtLeast(0)) }
    var totalDurSec by remember(lesson.id) { mutableIntStateOf(lesson.durationSeconds.coerceAtLeast(1)) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSubtitles by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var playerErrorMsg by remember { mutableStateOf<String?>(null) }

    val exoPlayer = remember(lesson.id, lesson.videoUrl, language) {
        ExoPlayer.Builder(context).build().apply {
            val subtitleUrl = lesson.getSubtitleUrlForLanguage(language)
            val itemBuilder = MediaItem.Builder().setUri(lesson.videoUrl)
            if (subtitleUrl.startsWith("https://", ignoreCase = true)) {
                val subConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleUrl))
                    .setMimeType(
                        if (subtitleUrl.endsWith(".srt", ignoreCase = true)) {
                            MimeTypes.APPLICATION_SUBRIP
                        } else {
                            MimeTypes.TEXT_VTT
                        }
                    )
                    .setLanguage(language.code)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
                itemBuilder.setSubtitleConfigurations(listOf(subConfig))
            }
            setMediaItem(itemBuilder.build())
            prepare()
            if (initialPositionSeconds > 0) {
                seekTo(initialPositionSeconds * 1000L)
            }
            playWhenReady = false
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && exoPlayer.duration > 0L) {
                    totalDurSec = (exoPlayer.duration / 1000L).toInt().coerceAtLeast(1)
                }
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    onPositionChanged(totalDurSec, true)
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playerErrorMsg = error.localizedMessage ?: "Video stream failed to load"
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(playbackSpeed, exoPlayer) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    LaunchedEffect(exoPlayer, isPlaying) {
        while (true) {
            val pos = (exoPlayer.currentPosition / 1000L).toInt().coerceAtLeast(0)
            if (pos != currentPosSec) {
                currentPosSec = pos
                val done = totalDurSec > 0 && pos >= (totalDurSec * 0.92).toInt()
                onPositionChanged(pos, done)
            }
            delay(1000L)
        }
    }

    val activeScene = remember(lesson.scenes, currentPosSec) {
        lesson.scenes.lastOrNull { currentPosSec >= it.startSeconds } ?: lesson.scenes.firstOrNull()
    }

    @Composable
    fun PlayerSurfaceBox(boxModifier: Modifier, fullScreenMode: Boolean) {
        Box(
            modifier = boxModifier
                .background(Color.Black)
                .clip(RoundedCornerShape(if (fullScreenMode) 0.dp else 16.dp))
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view -> view.player = exoPlayer },
                modifier = Modifier.fillMaxSize()
            )
            // Localized closed-caption overlay from verified scene transcript
            if (showSubtitles && activeScene != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = activeScene.getSubtitle(language),
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            IconButton(
                onClick = { isFullscreen = !fullScreenMode },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .testTag(if (fullScreenMode) "video_exit_fullscreen_btn" else "video_fullscreen_btn")
            ) {
                Icon(
                    imageVector = if (fullScreenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = if (fullScreenMode) "Exit Fullscreen" else "Fullscreen",
                    tint = Color.White
                )
            }
        }
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                PlayerSurfaceBox(
                    boxModifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    fullScreenMode = true
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play or Pause",
                            tint = RoyalSoftGold
                        )
                    }
                    Text(
                        text = "${formatMMSS(currentPosSec)} / ${formatMMSS(totalDurSec)}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { isFullscreen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.BENGALI -> "ছোট পর্দা"
                                AppLanguage.HINDI -> "सामान्य स्क्रीन"
                                AppLanguage.ENGLISH -> "Exit Fullscreen"
                            }
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlayerSurfaceBox(
            boxModifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            fullScreenMode = false
        )

        if (playerErrorMsg != null) {
            Surface(
                color = RoyalWarningBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, RoyalGold)
            ) {
                Text(
                    text = playerErrorMsg.orEmpty(),
                    color = RoyalWarningText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Video Playback Controls Card
        Card(
            colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
            border = BorderStroke(1.dp, RoyalBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = currentPosSec.toFloat().coerceIn(0f, totalDurSec.toFloat()),
                    onValueChange = { newSec ->
                        currentPosSec = newSec.toInt()
                        exoPlayer.seekTo(newSec.toLong() * 1000L)
                    },
                    valueRange = 0f..totalDurSec.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = RoyalGold,
                        activeTrackColor = RoyalGreenEmerald,
                        inactiveTrackColor = RoyalBorder
                    ),
                    modifier = Modifier.testTag("video_progress_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("video_play_pause_btn")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    isPlaying && language == AppLanguage.BENGALI -> "বিরতি"
                                    isPlaying && language == AppLanguage.HINDI -> "रोकें"
                                    isPlaying -> "Pause"
                                    language == AppLanguage.BENGALI -> "চালান"
                                    language == AppLanguage.HINDI -> "चलाएं"
                                    else -> "Play"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                exoPlayer.seekTo(0L)
                                exoPlayer.play()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("video_replay_btn")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "Replay", tint = RoyalSoftGold)
                        }

                        IconButton(
                            onClick = { showSubtitles = !showSubtitles },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("video_subtitle_toggle_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClosedCaption,
                                contentDescription = "Subtitles",
                                tint = if (showSubtitles) RoyalGold else RoyalTextSecondary
                            )
                        }
                    }

                    Text(
                        text = "${formatMMSS(currentPosSec)} / ${formatMMSS(totalDurSec)}",
                        color = RoyalSoftGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Speed controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.BENGALI -> "গতি:"
                            AppLanguage.HINDI -> "गति:"
                            AppLanguage.ENGLISH -> "Speed:"
                        },
                        color = RoyalTextSecondary,
                        fontSize = 11.5.sp
                    )
                    listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                        FilterChip(
                            selected = playbackSpeed == speed,
                            onClick = { playbackSpeed = speed },
                            label = { Text("${speed}x", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGreenEmerald,
                                selectedLabelColor = RoyalSoftGold,
                                containerColor = RoyalGreenSecondaryBg,
                                labelColor = RoyalTextSecondary
                            ),
                            modifier = Modifier.heightIn(min = 36.dp)
                        )
                    }
                }
            }
        }

        // Chapter navigation
        if (lesson.scenes.isNotEmpty()) {
            SceneChapterSelectorRow(
                scenes = lesson.scenes,
                activeSceneNumber = activeScene?.sceneNumber ?: 1,
                language = language,
                onSelectScene = { scene ->
                    currentPosSec = scene.startSeconds
                    exoPlayer.seekTo(scene.startSeconds * 1000L)
                }
            )
        }
    }
}

/**
 * Honest Interactive Storyboard & Guided Breakdown Viewer shown when no video file has been uploaded yet.
 * Clearly distinguishes storyboard planning from real video playback — never spoofs a fake video player.
 */
@Composable
private fun InteractiveStoryboardLessonViewer(
    lesson: VideoLesson,
    language: AppLanguage,
    initialPositionSeconds: Int,
    onPositionChanged: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scenes = lesson.scenes
    var activeSceneIndex by remember(lesson.id, initialPositionSeconds) {
        val idx = scenes.indexOfLast { initialPositionSeconds >= it.startSeconds }.coerceAtLeast(0)
        mutableIntStateOf(idx)
    }
    var showCaptions by remember { mutableStateOf(true) }

    val currentScene = scenes.getOrNull(activeSceneIndex)

    LaunchedEffect(activeSceneIndex, lesson.id) {
        if (scenes.isNotEmpty() && currentScene != null) {
            val done = activeSceneIndex == scenes.lastIndex
            onPositionChanged(currentScene.endSeconds, done)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Honest Status Banner
        Surface(
            color = RoyalInfoBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("storyboard_honest_notice")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = RoyalSoftGold,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (language) {
                            AppLanguage.BENGALI -> "ইন্টারেক্টিভ স্টোরিবোর্ড ও অ্যানিমেশন গাইড (ভিডিও ফাইল আপলোড অপেক্ষমাণ)"
                            AppLanguage.HINDI -> "इंटरैक्टिव स्टोरीबोर्ड और चरणबद्ध गाइड (वीडियो अपलोड लंबित)"
                            AppLanguage.ENGLISH -> "Interactive Lesson Storyboard & Script Plan (Video File Pending Admin Upload)"
                        },
                        color = RoyalSoftGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = when (language) {
                            AppLanguage.BENGALI -> "অ্যাডমিন প্রকৃত ভিডিও আপলোড না করা পর্যন্ত ধাপে ধাপে দৃশ্য, ডায়াগ্রাম ও বাংলা-হিন্দি-ইংরেজি পাঠ পড়তে পারবেন।"
                            AppLanguage.HINDI -> "एडमिन द्वारा वास्तविक वीडियो अपलोड होने तक आप चरण-दर-चरण दृश्य, डायग्राम और तीनों भाषाओं में स्क्रिप्ट देख सकते हैं।"
                            AppLanguage.ENGLISH -> "Review the verified scene-by-scene 3D animation cues, diagrams, and BN/HI/EN script while video encoding is pending."
                        },
                        color = RoyalInfoText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Visual Reference Preview Card (with thumbnail + scene overlay — explicitly NOT a fake play overlay)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
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
                            listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.78f)
                            )
                        )
                    )
            )

            // Scene number & timestamp badge
            if (currentScene != null) {
                Surface(
                    color = RoyalGreenPrimaryBg.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, RoyalGold),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.BENGALI -> "দৃশ্য ${currentScene.sceneNumber}/${scenes.size} • ${currentScene.formattedTimestampRange()}"
                            AppLanguage.HINDI -> "दृश्य ${currentScene.sceneNumber}/${scenes.size} • ${currentScene.formattedTimestampRange()}"
                            AppLanguage.ENGLISH -> "Scene ${currentScene.sceneNumber}/${scenes.size} • ${currentScene.formattedTimestampRange()}"
                        },
                        color = RoyalSoftGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Visual cue bottom overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currentScene.getTitle(language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showCaptions) {
                        Text(
                            text = currentScene.getSubtitle(language),
                            color = Color(0xFFE9F5E5),
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Scene Step Controls & Narration Card
        if (currentScene != null) {
            AnimatedContent(
                targetState = currentScene,
                label = "storyboard_scene_transition"
            ) { scene ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("storyboard_scene_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${scene.sceneNumber}. ${scene.getTitle(language)}",
                                color = RoyalSoftGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showCaptions = !showCaptions },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Toggle Captions",
                                    tint = if (showCaptions) RoyalGold else RoyalTextSecondary
                                )
                            }
                        }

                        // Narration script
                        Text(
                            text = scene.getNarration(language),
                            color = RoyalTextPrimary,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        )

                        // Diagram & Visual Animation Spec
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, RoyalBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🎬 ${scene.visualDirection}",
                                    color = RoyalTextSecondary,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    text = "✨ ${scene.animationDirection}",
                                    color = RoyalTextSecondary,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    text = "📐 ${scene.diagramCue}",
                                    color = RoyalSuccessText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        // Prev / Next scene buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { if (activeSceneIndex > 0) activeSceneIndex-- },
                                enabled = activeSceneIndex > 0,
                                border = BorderStroke(1.dp, RoyalBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("storyboard_prev_scene_btn")
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.BENGALI -> "আগের দৃশ্য"
                                        AppLanguage.HINDI -> "पिछला दृश्य"
                                        AppLanguage.ENGLISH -> "Previous"
                                    }
                                )
                            }

                            Button(
                                onClick = {
                                    if (activeSceneIndex < scenes.lastIndex) {
                                        activeSceneIndex++
                                    } else {
                                        onPositionChanged(lesson.durationSeconds, true)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("storyboard_next_scene_btn")
                            ) {
                                Text(
                                    text = if (activeSceneIndex < scenes.lastIndex) {
                                        when (language) {
                                            AppLanguage.BENGALI -> "পরের দৃশ্য"
                                            AppLanguage.HINDI -> "अगला दृश्य"
                                            AppLanguage.ENGLISH -> "Next Scene"
                                        }
                                    } else {
                                        when (language) {
                                            AppLanguage.BENGALI -> "পাঠ সম্পূর্ণ ✓"
                                            AppLanguage.HINDI -> "पाठ पूर्ण ✓"
                                            AppLanguage.ENGLISH -> "Finish Lesson ✓"
                                        }
                                    }
                                )
                                if (activeSceneIndex < scenes.lastIndex) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.SkipNext, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (scenes.isNotEmpty()) {
            SceneChapterSelectorRow(
                scenes = scenes,
                activeSceneNumber = currentScene?.sceneNumber ?: 1,
                language = language,
                onSelectScene = { scene ->
                    val idx = scenes.indexOfFirst { it.sceneNumber == scene.sceneNumber }
                    if (idx >= 0) activeSceneIndex = idx
                }
            )
        }
    }
}

@Composable
private fun SceneChapterSelectorRow(
    scenes: List<VideoStoryboardScene>,
    activeSceneNumber: Int,
    language: AppLanguage,
    onSelectScene: (VideoStoryboardScene) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = when (language) {
                AppLanguage.BENGALI -> "অধ্যায় অনুযায়ী দেখুন (Chapters)"
                AppLanguage.HINDI -> "अध्याय के अनुसार देखें (Chapters)"
                AppLanguage.ENGLISH -> "Lesson Chapters & Timestamps"
            },
            color = RoyalSoftGold,
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp
        )
        LazyRow(
            contentPadding = PaddingValues(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(scenes, key = { _, s -> s.sceneNumber }) { _, scene ->
                val selected = scene.sceneNumber == activeSceneNumber
                Surface(
                    color = if (selected) RoyalGreenEmerald else RoyalGreenSecondaryBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (selected) RoyalGold else RoyalBorder),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onSelectScene(scene) }
                        .testTag("lesson_chapter_${scene.sceneNumber}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${scene.sceneNumber}. ${scene.getTitle(language)}",
                            color = if (selected) Color.White else RoyalTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = scene.formattedTimestampRange(),
                            color = if (selected) RoyalSoftGold else RoyalTextSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }
        }
    }
}
