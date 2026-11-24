package com.example.ui.schemes

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppLanguage
import com.example.data.model.GovernmentScheme
import com.example.data.model.SchemeActivityFocus
import com.example.data.model.SchemeApplicationStatus
import com.example.data.model.SchemeCategory
import com.example.data.model.SchemeFarmerType
import com.example.data.model.SchemeGovLevel
import com.example.data.remote.GovernmentSchemesRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SchemeUiState {
    data object Loading : SchemeUiState
    data class Success(val schemes: List<GovernmentScheme>) : SchemeUiState
    data class Error(val message: String) : SchemeUiState
}

data class SchemeFilterState(
    val searchQuery: String = "",
    val govLevel: SchemeGovLevel? = null,
    val category: SchemeCategory? = null,
    val farmerType: SchemeFarmerType? = null,
    val activityFocus: SchemeActivityFocus? = null,
    val status: SchemeApplicationStatus? = null,
    val savedOnly: Boolean = false
) {
    val activeFilterCount: Int
        get() = listOfNotNull(govLevel, category, farmerType, activityFocus, status).size +
            (if (searchQuery.isNotBlank()) 1 else 0) +
            (if (savedOnly) 1 else 0)
}

class GovernmentSchemesViewModel(
    private val repository: GovernmentSchemesRepository,
    val currentAdminUid: String,
    val isAdmin: Boolean
) : ViewModel() {

    private val _filters = MutableStateFlow(SchemeFilterState())
    val filters: StateFlow<SchemeFilterState> = _filters.asStateFlow()

    private val _selectedSchemeId = MutableStateFlow<String?>(null)
    val selectedSchemeId: StateFlow<String?> = _selectedSchemeId.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    val bookmarkedIds: StateFlow<Set<String>> = repository.observeBookmarkedIds(currentAdminUid)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptySet()
        )

    val lastCachedAtMillis: StateFlow<Long?> = repository.observeLastCachedAtMillis()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    val allSchemesState: StateFlow<SchemeUiState> = repository.observeSchemes()
        .map<List<GovernmentScheme>, SchemeUiState> { SchemeUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing government schemes", rootError)
            emit(SchemeUiState.Error(rootError.message ?: "Failed to load government schemes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SchemeUiState.Loading
        )

    val cachedSchemesCount: StateFlow<Int> = allSchemesState
        .map { state -> (state as? SchemeUiState.Success)?.schemes?.size ?: 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )

    val filteredSchemes: StateFlow<List<GovernmentScheme>> = combine(
        allSchemesState,
        _filters,
        bookmarkedIds
    ) { state, filter, savedSet ->
        val raw = when (state) {
            is SchemeUiState.Success -> state.schemes
            else -> emptyList()
        }
        raw.filter { scheme ->
            val matchesQuery = scheme.matchesSearch(filter.searchQuery)
            val matchesGov = filter.govLevel == null || scheme.resolveGovLevel() == filter.govLevel
            val matchesCat = filter.category == null || filter.category in scheme.resolveAllCategories()
            val matchesFarmer = filter.farmerType == null ||
                filter.farmerType in scheme.resolveFarmerTypes() ||
                SchemeFarmerType.ALL_FARMERS in scheme.resolveFarmerTypes()
            val matchesActivity = filter.activityFocus == null ||
                filter.activityFocus == SchemeActivityFocus.ALL_ACTIVITIES ||
                scheme.resolveActivityFocus() == filter.activityFocus ||
                scheme.resolveActivityFocus() == SchemeActivityFocus.ALL_ACTIVITIES
            val matchesStatus = filter.status == null || scheme.resolveApplicationStatus() == filter.status
            val matchesSaved = !filter.savedOnly || scheme.id in savedSet

            matchesQuery && matchesGov && matchesCat && matchesFarmer && matchesActivity && matchesStatus && matchesSaved
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val selectedScheme: StateFlow<GovernmentScheme?> = combine(allSchemesState, _selectedSchemeId) { state, id ->
        if (id == null) null
        else (state as? SchemeUiState.Success)?.schemes?.firstOrNull { it.id == id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    fun setSearchQuery(q: String) {
        _filters.value = _filters.value.copy(searchQuery = q)
    }

    fun setGovLevel(level: SchemeGovLevel?) {
        _filters.value = _filters.value.copy(govLevel = level)
    }

    fun setCategory(category: SchemeCategory?) {
        _filters.value = _filters.value.copy(category = category)
    }

    fun setFarmerType(farmerType: SchemeFarmerType?) {
        _filters.value = _filters.value.copy(farmerType = farmerType)
    }

    fun setActivityFocus(activityFocus: SchemeActivityFocus?) {
        _filters.value = _filters.value.copy(activityFocus = activityFocus)
    }

    fun setStatus(status: SchemeApplicationStatus?) {
        _filters.value = _filters.value.copy(status = status)
    }

    fun setSavedOnly(savedOnly: Boolean) {
        _filters.value = _filters.value.copy(savedOnly = savedOnly)
    }

    fun toggleSavedOnly() {
        _filters.value = _filters.value.copy(savedOnly = !_filters.value.savedOnly)
    }

    fun clearFilters() {
        _filters.value = SchemeFilterState()
    }

    fun openSchemeDetail(schemeId: String?) {
        _selectedSchemeId.value = schemeId
    }

    fun toggleBookmark(
        schemeId: String,
        addedMessage: String? = null,
        removedMessage: String? = null
    ) {
        viewModelScope.launch {
            val nowBookmarked = repository.toggleBookmark(currentAdminUid, schemeId)
            val msg = if (nowBookmarked) addedMessage else removedMessage
            if (!msg.isNullOrBlank()) {
                _actionMessage.value = msg
            }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }

    fun openOfficialWebsite(context: Context, url: String, lang: AppLanguage): Boolean {
        return repository.openOfficialWebsite(context, url, lang)
    }

    fun saveAdminScheme(
        scheme: GovernmentScheme,
        isNew: Boolean,
        allowUrlOverwrite: Boolean,
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (!isAdmin) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            val result = repository.saveScheme(
                scheme = scheme,
                isNew = isNew,
                adminUid = currentAdminUid,
                allowUrlOverwrite = allowUrlOverwrite
            )
            result.onSuccess {
                _actionMessage.value = "Saved"
                onComplete(true)
            }.onFailure { err ->
                _actionMessage.value = err.message ?: "Failed to save scheme"
                onComplete(false)
            }
        }
    }

    fun updateAdminSchemeStatus(
        scheme: GovernmentScheme,
        newStatus: SchemeApplicationStatus,
        verifiedDate: String
    ) {
        if (!isAdmin) return
        viewModelScope.launch {
            val result = repository.updateSchemeStatus(
                scheme = scheme,
                newStatus = newStatus,
                verifiedDate = verifiedDate,
                adminUid = currentAdminUid
            )
            result.onFailure { err ->
                _actionMessage.value = err.message ?: "Status update failed"
            }
        }
    }

    companion object {
        private const val TAG = "GovtSchemesVM"
    }
}
