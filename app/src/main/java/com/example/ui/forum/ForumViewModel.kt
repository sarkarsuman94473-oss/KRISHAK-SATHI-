package com.example.ui.forum

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ForumComment
import com.example.data.model.ForumModerationStatus
import com.example.data.model.ForumPost
import com.example.data.model.ForumReport
import com.example.data.model.ForumReportReason
import com.example.data.model.ForumReportStatus
import com.example.data.model.ForumTopic
import com.example.data.model.ForumUserModeration
import com.example.data.model.UserModerationStatus
import com.example.data.remote.ForumRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ForumUiState<out T> {
    data object Loading : ForumUiState<Nothing>
    data class Success<T>(val data: T) : ForumUiState<T>
    data class Error(val message: String) : ForumUiState<Nothing>
}

enum class ForumFeedFilter {
    ALL,
    PINNED,
    MY_POSTS
}

@OptIn(ExperimentalCoroutinesApi::class)
class ForumViewModel(
    private val repository: ForumRepository,
    val currentUserId: String,
    val currentUserName: String,
    val currentUserRole: String,
    val isAdmin: Boolean
) : ViewModel() {

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId: StateFlow<String?> = _selectedPostId.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    val postsState: StateFlow<ForumUiState<List<ForumPost>>> = repository.observePosts(isAdmin)
        .map<List<ForumPost>, ForumUiState<List<ForumPost>>> { ForumUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing forum posts", rootError)
            emit(ForumUiState.Error(rootError.message ?: "Failed to load discussions"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ForumUiState.Loading
        )

    val likedPostIds: StateFlow<Set<String>> = repository.observeLikedPostIds()
        .catch { emit(emptySet()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptySet()
        )

    val myModeration: StateFlow<ForumUserModeration?> = repository.observeUserModeration(currentUserId)
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    val reports: StateFlow<List<ForumReport>> = repository.observeReports(isAdmin, currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val userModerations: StateFlow<List<ForumUserModeration>> = repository.observeAllUserModerations(isAdmin)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val commentsForSelectedPost: StateFlow<List<ForumComment>> = _selectedPostId
        .flatMapLatest { postId ->
            if (postId == null) flowOf(emptyList())
            else repository.observeComments(postId, isAdmin)
                .catch { error ->
                    Log.w(TAG, "Error observing comments", error)
                    emit(emptyList())
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    fun selectPost(postId: String?) {
        _selectedPostId.value = postId
    }

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun createPost(
        title: String,
        content: String,
        topic: ForumTopic,
        cropId: String,
        cropName: String,
        successMsg: String,
        restrictedMsg: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createPost(
                authorId = currentUserId,
                authorName = currentUserName,
                authorRole = currentUserRole,
                title = title,
                content = content,
                topic = topic,
                cropId = cropId,
                cropName = cropName
            )
            result.onSuccess {
                _actionMessage.value = successMsg
                onSuccess()
            }.onFailure { err ->
                val msg = err.message.orEmpty()
                _actionMessage.value = if (msg.startsWith("MODERATION_RESTRICTED")) {
                    restrictedMsg
                } else {
                    err.localizedMessage ?: "Failed to publish post"
                }
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId, currentUserId)
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Could not update like"
                }
        }
    }

    fun deletePost(postId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePost(postId, currentUserId, isAdmin)
                .onSuccess { onDeleted() }
                .onFailure { err -> _actionMessage.value = err.localizedMessage }
        }
    }

    fun addComment(
        postId: String,
        content: String,
        successMsg: String,
        restrictedMsg: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addComment(
                postId = postId,
                authorId = currentUserId,
                authorName = currentUserName,
                authorRole = currentUserRole,
                content = content
            ).onSuccess {
                _actionMessage.value = successMsg
                onSuccess()
            }.onFailure { err ->
                val msg = err.message.orEmpty()
                _actionMessage.value = if (msg.startsWith("MODERATION_RESTRICTED")) {
                    restrictedMsg
                } else {
                    err.localizedMessage ?: "Could not post reply"
                }
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(postId, commentId, currentUserId, isAdmin)
                .onFailure { err -> _actionMessage.value = err.localizedMessage }
        }
    }

    fun submitReport(
        targetType: String,
        postId: String,
        commentId: String = "",
        targetAuthorId: String,
        targetAuthorName: String,
        targetPreview: String,
        reason: ForumReportReason,
        details: String,
        sentMsg: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repository.submitReport(
                targetType = targetType,
                postId = postId,
                commentId = commentId,
                targetAuthorId = targetAuthorId,
                targetAuthorName = targetAuthorName,
                targetPreview = targetPreview,
                reporterId = currentUserId,
                reporterName = currentUserName,
                reason = reason,
                details = details
            ).onSuccess {
                _actionMessage.value = sentMsg
                onDone()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to submit report"
            }
        }
    }

    fun adminTogglePin(postId: String, pinned: Boolean) {
        if (!isAdmin) return
        viewModelScope.launch {
            repository.adminPinPost(postId, pinned)
                .onFailure { err -> _actionMessage.value = err.localizedMessage }
        }
    }

    fun adminSetPostStatus(postId: String, status: ForumModerationStatus, reason: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            repository.adminUpdatePostModeration(postId, status, reason)
                .onFailure { err -> _actionMessage.value = err.localizedMessage }
        }
    }

    fun adminSetCommentStatus(postId: String, commentId: String, status: ForumModerationStatus, reason: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            repository.adminUpdateCommentModeration(postId, commentId, status, reason)
                .onFailure { err -> _actionMessage.value = err.localizedMessage }
        }
    }

    fun adminResolveReport(reportId: String, status: ForumReportStatus, actionNote: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            repository.adminResolveReport(reportId, status, actionNote)
                .onFailure { err -> _actionMessage.value = err.localizedMessage }
        }
    }

    fun adminModerateUser(
        targetUserId: String,
        targetUserName: String,
        newStatus: UserModerationStatus,
        reason: String,
        muteHours: Long = 0L
    ) {
        if (!isAdmin) return
        viewModelScope.launch {
            repository.adminSetUserBehaviorStatus(
                targetUserId = targetUserId,
                targetUserName = targetUserName,
                newStatus = newStatus,
                reason = reason,
                muteDurationHours = muteHours,
                adminId = currentUserId
            ).onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to update user status"
            }
        }
    }

    private companion object {
        const val TAG = "ForumVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
