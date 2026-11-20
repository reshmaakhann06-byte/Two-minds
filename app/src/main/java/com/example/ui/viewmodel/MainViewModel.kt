package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppPreferences
import com.example.data.model.*
import com.example.data.repository.TwoMindsRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel(
    private val repository: TwoMindsRepository,
    private val prefs: AppPreferences,
    val currentUserId: String,
    val activeRole: PartnerRole
) : ViewModel() {

    private val partnerRole = activeRole.partner()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val userProfileState: StateFlow<UiState<UserProfileDoc?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfileDoc?, UiState<UserProfileDoc?>> { UiState.Success(it) }
        .catch { e -> emitError(e, "userProfile") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val workspaceState: StateFlow<UiState<WorkspaceDoc?>> = repository.observeWorkspace(currentUserId)
        .map<WorkspaceDoc?, UiState<WorkspaceDoc?>> { UiState.Success(it) }
        .catch { e -> emitError(e, "workspace") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val myProfileState: StateFlow<UiState<MemberProfileDoc?>> = repository.observeMemberProfile(activeRole, currentUserId)
        .map<MemberProfileDoc?, UiState<MemberProfileDoc?>> { UiState.Success(it) }
        .catch { e -> emitError(e, "myProfile") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val partnerProfileState: StateFlow<UiState<MemberProfileDoc?>> = repository.observeMemberProfile(partnerRole, currentUserId)
        .map<MemberProfileDoc?, UiState<MemberProfileDoc?>> { UiState.Success(it) }
        .catch { e -> emitError(e, "partnerProfile") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val businessItemsState: StateFlow<UiState<List<BusinessItemDoc>>> = repository.observeBusinessItems(currentUserId)
        .map<List<BusinessItemDoc>, UiState<List<BusinessItemDoc>>> { UiState.Success(it) }
        .catch { e -> emitError(e, "businessItems") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val bookNotesState: StateFlow<UiState<List<BookNoteDoc>>> = repository.observeBookNotes(currentUserId)
        .map<List<BookNoteDoc>, UiState<List<BookNoteDoc>>> { UiState.Success(it) }
        .catch { e -> emitError(e, "bookNotes") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val customBooksState: StateFlow<UiState<List<CustomBookDoc>>> = repository.observeCustomBooks(currentUserId)
        .map<List<CustomBookDoc>, UiState<List<CustomBookDoc>>> { UiState.Success(it) }
        .catch { e -> emitError(e, "customBooks") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    init {
        viewModelScope.launch {
            val res = repository.ensureWorkspaceJoinedAndProfilesReady(activeRole)
            if (res.isFailure) {
                _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Sync warning"
            }
        }
        viewModelScope.launch {
            workspaceState.collect { state ->
                if (state is UiState.Success) {
                    val members = state.data?.memberUids.orEmpty()
                    if (members.size >= 2) {
                        repository.syncHistoryMembership(members)
                    }
                }
            }
        }
    }

    private fun memberUidsOrSelf(): List<String> {
        val ws = (workspaceState.value as? UiState.Success)?.data
        val list = ws?.memberUids.orEmpty()
        return if (currentUserId in list) list else (list + currentUserId).distinct()
    }

    private fun currentMyProfile(): MemberProfileDoc? = (myProfileState.value as? UiState.Success)?.data

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
            repository.updateUserSettings(mapOf("themeMode" to mode))
        }
    }

    fun setOnlinePresence(online: Boolean) {
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf(
                    "online" to online,
                    "lastSeenEpochMs" to System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleChecklist(index: Int) {
        val profile = currentMyProfile() ?: return
        val updated = profile.checklistStates.toMutableList()
        while (updated.size <= index) updated.add(false)
        updated[index] = !updated[index]
        val doneCount = updated.count { it }.toLong()
        val totalCount = updated.size.toLong().coerceAtLeast(1L)
        val overall = computeOverall(profile.copy(checklistStates = updated))
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf(
                    "checklistStates" to updated,
                    "tasksDone" to doneCount,
                    "tasksTotal" to totalCount,
                    "overallPercent" to overall
                )
            )
        }
    }

    fun toggleNamaz(index: Int) {
        val profile = currentMyProfile() ?: return
        val updated = profile.namazStates.toMutableList()
        while (updated.size <= index) updated.add(false)
        updated[index] = !updated[index]
        val deenPct = computeDeenPercent(updated, profile.sunnatStates)
        val overall = computeOverall(profile.copy(namazStates = updated, deenPercent = deenPct))
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf(
                    "namazStates" to updated,
                    "deenPercent" to deenPct,
                    "overallPercent" to overall
                )
            )
        }
    }

    fun toggleSunnat(index: Int) {
        val profile = currentMyProfile() ?: return
        val updated = profile.sunnatStates.toMutableList()
        while (updated.size <= index) updated.add(false)
        updated[index] = !updated[index]
        val deenPct = computeDeenPercent(profile.namazStates, updated)
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf(
                    "sunnatStates" to updated,
                    "deenPercent" to deenPct
                )
            )
        }
    }

    fun incrementTasbeeh() {
        val profile = currentMyProfile() ?: return
        val next = profile.tasbeehCount + 1L
        viewModelScope.launch {
            repository.updateMyMemberProfile(activeRole, mapOf("tasbeehCount" to next))
        }
    }

    fun resetTasbeeh() {
        viewModelScope.launch {
            repository.updateMyMemberProfile(activeRole, mapOf("tasbeehCount" to 0L))
        }
    }

    fun selectTasbeehPreset(label: String, target: Int) {
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf(
                    "tasbeehLabel" to label,
                    "tasbeehTarget" to target.toLong(),
                    "tasbeehCount" to 0L
                )
            )
        }
    }

    fun updateQuranProgress(surah: String, addPages: Long, addMinutes: Long) {
        val profile = currentMyProfile() ?: return
        val newPage = (profile.quranPage + addPages).coerceIn(0L, profile.quranTotalPages)
        val newMinutes = (profile.quranMinutes + addMinutes).coerceAtMost(1440L)
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf(
                    "currentSurah" to surah,
                    "quranPage" to newPage,
                    "quranMinutes" to newMinutes
                )
            )
        }
    }

    fun updateReadingProgress(bookTitle: String, addPages: Long) {
        val profile = currentMyProfile() ?: return
        val newDaily = (profile.dailyPagesRead + addPages).coerceIn(0L, 500L)
        val newBookPct = (profile.currentBookProgress + (addPages * 2L)).coerceIn(0L, 100L)
        val readingPct = ((newDaily.toFloat() / profile.dailyPagesTarget.coerceAtLeast(1L)) * 100f)
            .toLong().coerceIn(0L, 100L)
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf(
                    "currentBookTitle" to bookTitle,
                    "dailyPagesRead" to newDaily,
                    "currentBookProgress" to newBookPct,
                    "readingPercent" to readingPct
                )
            )
        }
    }

    fun addBookNote(bookId: String, content: String, pageNumber: Long?) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val res = repository.addBookNote(activeRole, memberUidsOrSelf(), bookId, content, pageNumber)
            if (res.isFailure) _statusBanner.value = res.exceptionOrNull()?.localizedMessage
        }
    }

    fun addCustomBook(title: String, author: String, uriString: String, forRole: String, totalPages: Long) {
        viewModelScope.launch {
            val res = repository.addCustomBook(title, author, uriString, forRole, totalPages)
            if (res.isFailure) _statusBanner.value = res.exceptionOrNull()?.localizedMessage
        }
    }

    fun updateWaterGlasses(delta: Long) {
        val profile = currentMyProfile() ?: return
        val next = (profile.waterGlasses + delta).coerceIn(0L, 20L)
        val fitnessPct = computeFitnessPercent(next, profile.sleepHours, profile.workoutCompleted)
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf("waterGlasses" to next, "fitnessPercent" to fitnessPct)
            )
        }
    }

    fun updateSleepHours(hours: Double) {
        val profile = currentMyProfile() ?: return
        val clamped = hours.coerceIn(0.0, 16.0)
        val fitnessPct = computeFitnessPercent(profile.waterGlasses, clamped, profile.workoutCompleted)
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf("sleepHours" to clamped, "fitnessPercent" to fitnessPct)
            )
        }
    }

    fun toggleWorkoutCompleted() {
        val profile = currentMyProfile() ?: return
        val next = !profile.workoutCompleted
        val fitnessPct = computeFitnessPercent(profile.waterGlasses, profile.sleepHours, next)
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf("workoutCompleted" to next, "fitnessPercent" to fitnessPct)
            )
        }
    }

    fun boostSkillProgress(deltaPercent: Long) {
        val profile = currentMyProfile() ?: return
        val next = (profile.skillsPercent + deltaPercent).coerceIn(0L, 100L)
        viewModelScope.launch {
            repository.updateMyMemberProfile(activeRole, mapOf("skillsPercent" to next))
        }
    }

    fun markLanguageWordLearned() {
        val profile = currentMyProfile() ?: return
        val nextWords = profile.languageWordsLearned + 1L
        val nextPct = (profile.languagePercent + 4L).coerceIn(0L, 100L)
        viewModelScope.launch {
            repository.updateMyMemberProfile(
                activeRole,
                mapOf("languageWordsLearned" to nextWords, "languagePercent" to nextPct)
            )
        }
    }

    fun addBusinessItem(title: String, category: String, driveUrl: String?, driveType: String?, assignee: String) {
        viewModelScope.launch {
            val res = repository.addBusinessItem(memberUidsOrSelf(), title, category, driveUrl, driveType, assignee)
            if (res.isFailure) _statusBanner.value = res.exceptionOrNull()?.localizedMessage
        }
    }

    fun toggleBusinessItem(item: BusinessItemDoc) {
        viewModelScope.launch {
            repository.toggleBusinessItem(item.id, !item.completed)
            val profile = currentMyProfile() ?: return@launch
            val nextPct = (profile.businessPercent + if (!item.completed) 5L else -5L).coerceIn(0L, 100L)
            repository.updateMyMemberProfile(activeRole, mapOf("businessPercent" to nextPct))
        }
    }

    fun updateProfileCustomization(displayName: String, bio: String) {
        val cleanName = displayName.trim().take(60)
        val cleanBio = bio.trim().take(240)
        if (cleanName.isBlank()) return
        viewModelScope.launch {
            repository.updateUserSettings(mapOf("displayName" to cleanName, "bio" to cleanBio))
            repository.updateMyMemberProfile(activeRole, mapOf("displayName" to cleanName, "bio" to cleanBio))
        }
    }

    fun updateNotificationAndPrivacy(notificationsEnabled: Boolean, disappearingSec: Long, blockedPartner: Boolean) {
        viewModelScope.launch {
            repository.updateUserSettings(
                mapOf(
                    "notificationsEnabled" to notificationsEnabled,
                    "disappearingMessagesSec" to disappearingSec,
                    "blockedPartner" to blockedPartner
                )
            )
        }
    }

    private fun computeDeenPercent(namaz: List<Boolean>, sunnat: List<Boolean>): Long {
        val nScore = if (namaz.isEmpty()) 0f else (namaz.count { it }.toFloat() / namaz.size) * 70f
        val sScore = if (sunnat.isEmpty()) 0f else (sunnat.count { it }.toFloat() / sunnat.size) * 30f
        return (nScore + sScore).toLong().coerceIn(0L, 100L)
    }

    private fun computeFitnessPercent(water: Long, sleep: Double, workoutDone: Boolean): Long {
        val w = (water.coerceAtMost(8L) / 8f) * 35f
        val s = ((sleep.coerceAtMost(8.0) / 8.0) * 25f).toFloat()
        val ex = if (workoutDone) 40f else 0f
        return (w + s + ex).toLong().coerceIn(0L, 100L)
    }

    private fun computeOverall(p: MemberProfileDoc): Long {
        val checkPct = if (p.checklistStates.isEmpty()) 0L else (p.checklistStates.count { it } * 100L) / p.checklistStates.size
        return ((p.deenPercent + p.readingPercent + p.skillsPercent + p.businessPercent + p.fitnessPercent + checkPct) / 6L)
            .coerceIn(0L, 100L)
    }

    private suspend fun <T> FlowCollector<UiState<T>>.emitError(error: Throwable, tag: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w("MainViewModel", "Error observing $tag", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load $tag"))
    }
}
