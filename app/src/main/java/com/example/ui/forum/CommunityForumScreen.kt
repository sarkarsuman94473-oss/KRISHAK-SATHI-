package com.example.ui.forum

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.datasource.CropMasterDatabase
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.ForumComment
import com.example.data.model.ForumModerationStatus
import com.example.data.model.ForumPost
import com.example.data.model.ForumReportStatus
import com.example.data.model.ForumTopic
import com.example.data.model.ForumUserModeration
import com.example.data.model.UserModerationStatus
import com.example.data.remote.ForumRepository
import com.example.data.remote.ForumStarterTranslations
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenHighlight
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import com.example.ui.theme.royalOutlinedTextFieldColors
import com.example.util.L10n
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

private data class ReportTargetState(
    val targetType: String,
    val postId: String,
    val commentId: String,
    val authorId: String,
    val authorName: String,
    val preview: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunityForumScreen(
    userId: String,
    userName: String,
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val session = remember(context) { UserSessionManager.getSession(context) }
    val isAdmin = remember(context, session) { UserSessionManager.isAdmin(context) }
    val userRole = remember(session, isAdmin) {
        if (isAdmin) UserSession.ROLE_ADMIN else (session?.role ?: UserSession.ROLE_FARMER)
    }

    val forumViewModel: ForumViewModel = viewModel(
        key = "${userId}_$isAdmin",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
                val dbProvider: () -> FirebaseFirestore? = {
                    try {
                        FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                    } catch (_: Exception) {
                        null
                    }
                }
                ForumViewModel(
                    repository = ForumRepository(dbProvider, app),
                    currentUserId = userId,
                    currentUserName = userName,
                    currentUserRole = userRole,
                    isAdmin = isAdmin
                )
            }
        }
    )

    val postsState by forumViewModel.postsState.collectAsStateWithLifecycle()
    val likedIds by forumViewModel.likedPostIds.collectAsStateWithLifecycle()
    val myModeration by forumViewModel.myModeration.collectAsStateWithLifecycle()
    val reports by forumViewModel.reports.collectAsStateWithLifecycle()
    val userModerations by forumViewModel.userModerations.collectAsStateWithLifecycle()
    val selectedPostId by forumViewModel.selectedPostId.collectAsStateWithLifecycle()
    val selectedComments by forumViewModel.commentsForSelectedPost.collectAsStateWithLifecycle()
    val actionMessage by forumViewModel.actionMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            forumViewModel.clearMessage()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTopicFilter by remember { mutableStateOf<ForumTopic?>(null) }
    var selectedCropFilterId by remember { mutableStateOf("") }
    var selectedCropFilterLabel by remember { mutableStateOf("") }
    var feedFilter by remember { mutableStateOf(ForumFeedFilter.ALL) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showCropFilterDialog by remember { mutableStateOf(false) }
    var showModerationPanel by remember { mutableStateOf(false) }
    var reportTarget by remember { mutableStateOf<ReportTargetState?>(null) }

    val allPosts = when (val s = postsState) {
        is ForumUiState.Success -> s.data
        else -> emptyList()
    }
    val selectedPost = remember(allPosts, selectedPostId) {
        allPosts.firstOrNull { it.id == selectedPostId }
    }

    if (showModerationPanel && isAdmin) {
        ForumModerationPanel(
            currentLanguage = currentLanguage,
            posts = allPosts,
            reports = reports,
            userModerations = userModerations,
            onBack = { showModerationPanel = false },
            onHideOrRestorePost = { postId, status, reason ->
                forumViewModel.adminSetPostStatus(postId, status, reason)
            },
            onPinOrUnpinPost = { postId, pinned ->
                forumViewModel.adminTogglePin(postId, pinned)
            },
            onDeletePost = { postId ->
                forumViewModel.deletePost(postId)
            },
            onResolveReport = { repId, status, note ->
                forumViewModel.adminResolveReport(repId, status, note)
            },
            onModerateUser = { targetId, targetName, status, reason, muteHours ->
                forumViewModel.adminModerateUser(targetId, targetName, status, reason, muteHours)
            }
        )
        return
    }

    if (selectedPost != null) {
        ForumPostDetailScreen(
            post = selectedPost,
            comments = selectedComments,
            currentLanguage = currentLanguage,
            currentUserId = userId,
            isAdmin = isAdmin,
            isLiked = selectedPost.id in likedIds,
            myModeration = myModeration,
            snackbarHostState = snackbarHostState,
            onBack = { forumViewModel.selectPost(null) },
            onToggleLike = { forumViewModel.toggleLike(selectedPost.id) },
            onDeletePost = {
                forumViewModel.deletePost(selectedPost.id) { forumViewModel.selectPost(null) }
            },
            onReportPost = {
                reportTarget = ReportTargetState(
                    targetType = "POST",
                    postId = selectedPost.id,
                    commentId = "",
                    authorId = selectedPost.authorId,
                    authorName = selectedPost.authorName,
                    preview = ForumStarterTranslations.getLocalizedTitle(selectedPost, currentLanguage)
                )
            },
            onSendComment = { text, onDone ->
                forumViewModel.addComment(
                    postId = selectedPost.id,
                    content = text,
                    successMsg = L10n.t("forum_comment_added", currentLanguage),
                    restrictedMsg = L10n.t("forum_restricted_action_error", currentLanguage),
                    onSuccess = onDone
                )
            },
            onDeleteComment = { cmtId ->
                forumViewModel.deleteComment(selectedPost.id, cmtId)
            },
            onReportComment = { comment ->
                reportTarget = ReportTargetState(
                    targetType = "COMMENT",
                    postId = selectedPost.id,
                    commentId = comment.id,
                    authorId = comment.authorId,
                    authorName = comment.authorName,
                    preview = ForumStarterTranslations.getLocalizedComment(comment, currentLanguage)
                )
            },
            onAdminTogglePin = { forumViewModel.adminTogglePin(selectedPost.id, !selectedPost.pinned) },
            onAdminToggleHide = {
                val next = if (selectedPost.moderationStatus == ForumModerationStatus.HIDDEN.code) {
                    ForumModerationStatus.APPROVED
                } else ForumModerationStatus.HIDDEN
                forumViewModel.adminSetPostStatus(selectedPost.id, next, L10n.t("forum_mod_reason_default", currentLanguage))
            }
        )
    } else {
        val pendingReportCount = remember(reports) {
            reports.count { it.status == ForumReportStatus.PENDING.code }
        }
        val filteredPosts = remember(
            allPosts,
            searchQuery,
            selectedTopicFilter,
            selectedCropFilterId,
            feedFilter,
            currentLanguage,
            userId
        ) {
            allPosts.filter { post ->
                val matchesFeed = when (feedFilter) {
                    ForumFeedFilter.ALL -> true
                    ForumFeedFilter.PINNED -> post.pinned
                    ForumFeedFilter.MY_POSTS -> post.authorId == userId
                }
                val matchesTopic = selectedTopicFilter == null || post.topic == selectedTopicFilter?.code
                val matchesCrop = selectedCropFilterId.isBlank() || post.cropId.equals(selectedCropFilterId, ignoreCase = true)
                val localizedTitle = ForumStarterTranslations.getLocalizedTitle(post, currentLanguage)
                val localizedBody = ForumStarterTranslations.getLocalizedContent(post, currentLanguage)
                val localizedCrop = resolveCropDisplayName(post.cropId, post.cropName, currentLanguage)
                val matchesSearch = searchQuery.isBlank() ||
                    localizedTitle.contains(searchQuery, ignoreCase = true) ||
                    localizedBody.contains(searchQuery, ignoreCase = true) ||
                    post.authorName.contains(searchQuery, ignoreCase = true) ||
                    localizedCrop.contains(searchQuery, ignoreCase = true)

                matchesFeed && matchesTopic && matchesCrop && matchesSearch
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = RoyalGreenPrimaryBg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Column {
                            Text(
                                text = L10n.t("forum_title", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RoyalTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = L10n.t("forum_subtitle", currentLanguage),
                                fontSize = 11.sp,
                                color = RoyalTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("forum_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = L10n.t("btn_back", currentLanguage),
                                tint = RoyalSoftGold
                            )
                        }
                    },
                    actions = {
                        if (isAdmin) {
                            Surface(
                                color = if (pendingReportCount > 0) RoyalAlertBg else RoyalGreenCardBg,
                                border = BorderStroke(1.dp, RoyalGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .heightIn(min = 48.dp)
                                    .clickable { showModerationPanel = true }
                                    .testTag("forum_moderation_open_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = L10n.t("forum_moderation_btn", currentLanguage),
                                        tint = RoyalSoftGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (pendingReportCount > 0) {
                                            "${L10n.t("forum_moderation_btn", currentLanguage)} ($pendingReportCount)"
                                        } else {
                                            L10n.t("forum_moderation_btn", currentLanguage)
                                        },
                                        color = RoyalSoftGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalGreenSecondaryBg)
                )
            },
            floatingActionButton = {
                val isRestricted = myModeration?.isCurrentlyRestricted() == true
                if (!isRestricted) {
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = RoyalGold,
                        contentColor = RoyalGreenPrimaryBg,
                        modifier = Modifier.testTag("forum_new_post_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = L10n.t("forum_new_post_btn", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 880.dp),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // User Behavior Notice Banner if Warned / Muted / Banned
                    myModeration?.let { mod ->
                        val status = UserModerationStatus.fromCode(mod.status)
                        if (status != UserModerationStatus.ACTIVE) {
                            item {
                                UserModerationNoticeBanner(mod, status, currentLanguage)
                            }
                        }
                    }

                    // Search Bar
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(L10n.t("forum_search_hint", currentLanguage)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalGold) },
                            singleLine = true,
                            colors = royalOutlinedTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forum_search_input")
                        )
                    }

                    // Feed View Pills + Crop Filter Picker Button
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val feedOptions = listOf(
                                ForumFeedFilter.ALL to L10n.t("forum_filter_all", currentLanguage),
                                ForumFeedFilter.PINNED to L10n.t("forum_filter_pinned", currentLanguage),
                                ForumFeedFilter.MY_POSTS to L10n.t("forum_filter_my_posts", currentLanguage)
                            )
                            feedOptions.forEach { (option, label) ->
                                val selected = feedFilter == option
                                FilterChip(
                                    selected = selected,
                                    onClick = { feedFilter = option },
                                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGreenEmerald,
                                        selectedLabelColor = RoyalSoftGold,
                                        containerColor = RoyalGreenCardBg,
                                        labelColor = RoyalTextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = RoyalBorder,
                                        selectedBorderColor = RoyalGold
                                    ),
                                    modifier = Modifier.testTag("forum_feed_filter_${option.name}")
                                )
                            }

                            FilterChip(
                                selected = selectedCropFilterId.isNotBlank(),
                                onClick = { showCropFilterDialog = true },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = if (selectedCropFilterId.isNotBlank()) RoyalSoftGold else RoyalGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (selectedCropFilterId.isBlank()) {
                                            L10n.t("forum_all_crops", currentLanguage)
                                        } else {
                                            selectedCropFilterLabel
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGreenEmerald,
                                    selectedLabelColor = RoyalSoftGold,
                                    containerColor = RoyalGreenCardBg,
                                    labelColor = RoyalSoftGold
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCropFilterId.isNotBlank(),
                                    borderColor = RoyalGold.copy(alpha = 0.5f),
                                    selectedBorderColor = RoyalGold
                                ),
                                modifier = Modifier.testTag("forum_crop_filter_btn")
                            )
                        }
                    }

                    // Topic Filter Strip
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                val selected = selectedTopicFilter == null
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedTopicFilter = null },
                                    label = {
                                        Text(
                                            text = L10n.t("forum_all_topics", currentLanguage),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGold,
                                        selectedLabelColor = RoyalGreenPrimaryBg,
                                        containerColor = RoyalGreenSecondaryBg,
                                        labelColor = RoyalTextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = RoyalBorder,
                                        selectedBorderColor = RoyalGold
                                    ),
                                    modifier = Modifier.testTag("forum_topic_chip_ALL")
                                )
                            }
                            items(ForumTopic.values()) { topic ->
                                val selected = selectedTopicFilter == topic
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedTopicFilter = if (selected) null else topic },
                                    label = {
                                        Text(
                                            text = topic.getLabel(currentLanguage),
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalGold,
                                        selectedLabelColor = RoyalGreenPrimaryBg,
                                        containerColor = RoyalGreenSecondaryBg,
                                        labelColor = RoyalTextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = RoyalBorder,
                                        selectedBorderColor = RoyalGold
                                    ),
                                    modifier = Modifier.testTag("forum_topic_chip_${topic.code}")
                                )
                            }
                        }
                    }

                    // Feed Body
                    when (postsState) {
                        ForumUiState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = RoyalGold)
                                }
                            }
                        }
                        is ForumUiState.Error -> {
                            item {
                                Text(
                                    text = (postsState as ForumUiState.Error).message,
                                    color = RoyalAlertText,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        is ForumUiState.Success -> {
                            if (filteredPosts.isEmpty()) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Forum,
                                                contentDescription = null,
                                                tint = RoyalGold,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Text(
                                                text = L10n.t("forum_empty_title", currentLanguage),
                                                color = RoyalTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = L10n.t("forum_empty_desc", currentLanguage),
                                                color = RoyalTextSecondary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(filteredPosts, key = { it.id }) { post ->
                                    ForumPostCard(
                                        post = post,
                                        currentLanguage = currentLanguage,
                                        currentUserId = userId,
                                        isAdmin = isAdmin,
                                        isLiked = post.id in likedIds,
                                        onOpenDetail = { forumViewModel.selectPost(post.id) },
                                        onToggleLike = { forumViewModel.toggleLike(post.id) },
                                        onReport = {
                                            reportTarget = ReportTargetState(
                                                targetType = "POST",
                                                postId = post.id,
                                                commentId = "",
                                                authorId = post.authorId,
                                                authorName = post.authorName,
                                                preview = ForumStarterTranslations.getLocalizedTitle(post, currentLanguage)
                                            )
                                        },
                                        onDelete = { forumViewModel.deletePost(post.id) }
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateForumPostDialog(
            currentLanguage = currentLanguage,
            onDismiss = { showCreateDialog = false },
            onPublish = { title, body, topic, cropId, cropName ->
                forumViewModel.createPost(
                    title = title,
                    content = body,
                    topic = topic,
                    cropId = cropId,
                    cropName = cropName,
                    successMsg = L10n.t("forum_post_created", currentLanguage),
                    restrictedMsg = L10n.t("forum_restricted_action_error", currentLanguage),
                    onSuccess = { showCreateDialog = false }
                )
            }
        )
    }

    if (showCropFilterDialog) {
        ForumCropSelectorDialog(
            currentLanguage = currentLanguage,
            includeAllOption = true,
            onDismiss = { showCropFilterDialog = false },
            onSelectCrop = { cropId, cropLabel ->
                selectedCropFilterId = cropId
                selectedCropFilterLabel = cropLabel
                showCropFilterDialog = false
            }
        )
    }

    reportTarget?.let { target ->
        ReportContentDialog(
            currentLanguage = currentLanguage,
            onDismiss = { reportTarget = null },
            onSubmit = { reason, details ->
                forumViewModel.submitReport(
                    targetType = target.targetType,
                    postId = target.postId,
                    commentId = target.commentId,
                    targetAuthorId = target.authorId,
                    targetAuthorName = target.authorName,
                    targetPreview = target.preview,
                    reason = reason,
                    details = details,
                    sentMsg = L10n.t("forum_report_sent", currentLanguage),
                    onDone = { reportTarget = null }
                )
            }
        )
    }
}

@Composable
private fun UserModerationNoticeBanner(
    moderation: ForumUserModeration,
    status: UserModerationStatus,
    currentLanguage: AppLanguage
) {
    val isWarned = status == UserModerationStatus.WARNED
    val container = if (isWarned) RoyalWarningBg else RoyalAlertBg
    val tint = if (isWarned) RoyalWarningText else RoyalAlertText
    val text = when (status) {
        UserModerationStatus.WARNED -> L10n.t("forum_user_warned_banner", currentLanguage)
        UserModerationStatus.MUTED -> L10n.t("forum_user_muted_banner", currentLanguage)
        UserModerationStatus.BANNED -> L10n.t("forum_user_banned_banner", currentLanguage)
        UserModerationStatus.ACTIVE -> ""
    }

    Surface(
        color = container,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forum_user_moderation_banner")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = text, color = tint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            if (moderation.moderationReason.isNotBlank()) {
                Text(
                    text = moderation.moderationReason,
                    color = RoyalTextPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ForumPostCard(
    post: ForumPost,
    currentLanguage: AppLanguage,
    currentUserId: String,
    isAdmin: Boolean,
    isLiked: Boolean,
    onOpenDetail: () -> Unit,
    onToggleLike: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit
) {
    val title = ForumStarterTranslations.getLocalizedTitle(post, currentLanguage)
    val content = ForumStarterTranslations.getLocalizedContent(post, currentLanguage)
    val topicLabel = ForumTopic.fromCode(post.topic).getLabel(currentLanguage)
    val cropLabel = resolveCropDisplayName(post.cropId, post.cropName, currentLanguage)
    val canDelete = isAdmin || post.authorId == currentUserId

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, if (post.pinned) RoyalGold else RoyalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() }
            .testTag("forum_post_card_${post.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (post.pinned) {
                    Surface(
                        color = RoyalGold,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = RoyalGreenPrimaryBg, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = L10n.t("forum_pinned_badge", currentLanguage),
                                color = RoyalGreenPrimaryBg,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Surface(
                    color = RoyalGreenEmerald.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = topicLabel,
                        color = RoyalSoftGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    color = RoyalGreenSecondaryBg,
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = cropLabel,
                        color = RoyalTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (post.moderationStatus != ForumModerationStatus.APPROVED.code && (isAdmin || post.authorId == currentUserId)) {
                    Surface(color = RoyalAlertBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = ForumModerationStatus.fromCode(post.moderationStatus).getLabel(currentLanguage),
                            color = RoyalAlertText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Text(
                text = title,
                color = RoyalTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )

            Text(
                text = content,
                color = RoyalTextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${post.authorName} • ${formatForumTime(post.createdAt)}",
                color = RoyalTextSecondary,
                fontSize = 11.sp
            )

            HorizontalDivider(thickness = 1.dp, color = RoyalBorder.copy(alpha = 0.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isLiked) RoyalGreenEmerald else RoyalGreenSecondaryBg,
                        border = BorderStroke(1.dp, if (isLiked) RoyalGold else RoyalBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable { onToggleLike() }
                            .testTag("forum_like_btn_${post.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                contentDescription = L10n.t("forum_likes_label", currentLanguage),
                                tint = if (isLiked) RoyalSoftGold else RoyalTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${post.likesCount}",
                                color = if (isLiked) RoyalSoftGold else RoyalTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        color = RoyalGreenSecondaryBg,
                        border = BorderStroke(1.dp, RoyalBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable { onOpenDetail() }
                            .testTag("forum_comment_btn_${post.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = L10n.t("forum_comments_label", currentLanguage),
                                tint = RoyalGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${post.commentsCount} ${L10n.t("forum_comments_label", currentLanguage)}",
                                color = RoyalTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onReport,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("forum_report_post_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = L10n.t("forum_report_btn", currentLanguage),
                            tint = RoyalTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("forum_delete_post_btn_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = L10n.t("forum_delete_post", currentLanguage),
                                tint = RoyalAlertText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForumPostDetailScreen(
    post: ForumPost,
    comments: List<ForumComment>,
    currentLanguage: AppLanguage,
    currentUserId: String,
    isAdmin: Boolean,
    isLiked: Boolean,
    myModeration: ForumUserModeration?,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onToggleLike: () -> Unit,
    onDeletePost: () -> Unit,
    onReportPost: () -> Unit,
    onSendComment: (String, () -> Unit) -> Unit,
    onDeleteComment: (String) -> Unit,
    onReportComment: (ForumComment) -> Unit,
    onAdminTogglePin: () -> Unit,
    onAdminToggleHide: () -> Unit
) {
    BackHandler { onBack() }

    var replyDraft by remember { mutableStateOf("") }
    val isRestricted = myModeration?.isCurrentlyRestricted() == true

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = RoyalGreenPrimaryBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = resolveCropDisplayName(post.cropId, post.cropName, currentLanguage),
                        color = RoyalSoftGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("forum_detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = L10n.t("btn_back", currentLanguage),
                            tint = RoyalSoftGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalGreenSecondaryBg)
            )
        },
        bottomBar = {
            Surface(
                color = RoyalGreenSecondaryBg,
                border = BorderStroke(1.dp, RoyalBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                if (isRestricted) {
                    Text(
                        text = L10n.t("forum_restricted_action_error", currentLanguage),
                        color = RoyalAlertText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = replyDraft,
                            onValueChange = { if (it.length <= 1500) replyDraft = it },
                            placeholder = { Text(L10n.t("forum_comment_hint", currentLanguage)) },
                            maxLines = 3,
                            colors = royalOutlinedTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("forum_reply_input")
                        )
                        Button(
                            onClick = {
                                val text = replyDraft.trim()
                                if (text.isNotEmpty()) {
                                    onSendComment(text) { replyDraft = "" }
                                }
                            },
                            enabled = replyDraft.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalGold,
                                contentColor = RoyalGreenPrimaryBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .heightIn(min = 50.dp)
                                .testTag("forum_send_reply_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = L10n.t("forum_send_comment", currentLanguage)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("forum_detail_list"),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                    border = BorderStroke(1.dp, if (post.pinned) RoyalGold else RoyalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = ForumTopic.fromCode(post.topic).getLabel(currentLanguage),
                            color = RoyalSoftGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = ForumStarterTranslations.getLocalizedTitle(post, currentLanguage),
                            color = RoyalTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 23.sp
                        )
                        Text(
                            text = ForumStarterTranslations.getLocalizedContent(post, currentLanguage),
                            color = RoyalTextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )
                        Text(
                            text = "${post.authorName} • ${formatForumTime(post.createdAt)}",
                            color = RoyalTextSecondary,
                            fontSize = 12.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onToggleLike,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLiked) RoyalGreenEmerald else RoyalGreenSecondaryBg,
                                    contentColor = RoyalSoftGold
                                ),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${L10n.t("forum_likes_label", currentLanguage)} (${post.likesCount})")
                            }
                            IconButton(onClick = onReportPost, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Flag, contentDescription = L10n.t("forum_report_btn", currentLanguage), tint = RoyalTextSecondary)
                            }
                            if (isAdmin || post.authorId == currentUserId) {
                                IconButton(onClick = onDeletePost, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = L10n.t("forum_delete_post", currentLanguage), tint = RoyalAlertText)
                                }
                            }
                        }

                        if (isAdmin) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onAdminTogglePin,
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenSecondaryBg, contentColor = RoyalSoftGold),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(if (post.pinned) L10n.t("forum_mod_unpin", currentLanguage) else L10n.t("forum_mod_pin", currentLanguage), fontSize = 12.sp)
                                }
                                Button(
                                    onClick = onAdminToggleHide,
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalAlertBg, contentColor = RoyalAlertText),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        if (post.moderationStatus == ForumModerationStatus.HIDDEN.code) L10n.t("forum_mod_restore", currentLanguage)
                                        else L10n.t("forum_mod_hide", currentLanguage),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "${L10n.t("forum_comments_label", currentLanguage)} (${comments.size})",
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (comments.isEmpty()) {
                item {
                    Text(
                        text = L10n.t("forum_no_comments", currentLanguage),
                        color = RoyalTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(comments, key = { it.id }) { comment ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
                        border = BorderStroke(1.dp, RoyalBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forum_comment_card_${comment.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .background(RoyalGreenEmerald, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = comment.authorName.firstOrNull()?.uppercase() ?: "F",
                                            color = RoyalSoftGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = comment.authorName,
                                            color = RoyalTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = formatForumTime(comment.createdAt),
                                            color = RoyalTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Row {
                                    IconButton(
                                        onClick = { onReportComment(comment) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Flag,
                                            contentDescription = L10n.t("forum_report_btn", currentLanguage),
                                            tint = RoyalTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    if (isAdmin || comment.authorId == currentUserId) {
                                        IconButton(
                                            onClick = { onDeleteComment(comment.id) },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("forum_delete_comment_btn_${comment.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = L10n.t("forum_delete_comment", currentLanguage),
                                                tint = RoyalAlertText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = ForumStarterTranslations.getLocalizedComment(comment, currentLanguage),
                                color = RoyalTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun resolveCropDisplayName(cropId: String, fallbackName: String, lang: AppLanguage): String {
    if (cropId == "general") return L10n.t("forum_general_crop", lang)
    val match = CropMasterDatabase.getCropById(cropId)
    return match?.getName(lang) ?: fallbackName
}

private fun formatForumTime(ts: Timestamp?): String {
    val date = ts?.toDate() ?: return ""
    return SimpleDateFormat("dd MMM, HH:mm", Locale.ENGLISH).format(date)
}
