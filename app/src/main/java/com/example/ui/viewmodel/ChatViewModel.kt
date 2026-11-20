package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AudioVoiceManager
import com.example.core.NotificationHelper
import com.example.data.model.CallSessionDoc
import com.example.data.model.DecryptedChatMessage
import com.example.data.model.PartnerRole
import com.example.data.repository.TwoMindsRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: TwoMindsRepository,
    private val appContext: Context,
    private val currentUserId: String,
    val activeRole: PartnerRole
) : ViewModel() {

    val voiceManager = AudioVoiceManager(appContext)
    private var typingClearJob: Job? = null
    private var lastKnownMessageIds = emptySet<String>()
    private var initializedMessages = false

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _playingMessageId = MutableStateFlow<String?>(null)
    val playingMessageId: StateFlow<String?> = _playingMessageId.asStateFlow()

    private val _errorBanner = MutableStateFlow<String?>(null)
    val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

    val messagesState: StateFlow<UiState<List<DecryptedChatMessage>>> = repository.observeMessages(currentUserId)
        .map<List<DecryptedChatMessage>, UiState<List<DecryptedChatMessage>>> { list ->
            UiState.Success(list)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w("ChatVM", "Error observing messages", root)
            emit(UiState.Error(root.message ?: "Failed to observe messages"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val callsState: StateFlow<UiState<List<CallSessionDoc>>> = repository.observeCalls(currentUserId)
        .map<List<CallSessionDoc>, UiState<List<CallSessionDoc>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            emit(UiState.Error(root.message ?: "Failed to observe calls"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    init {
        viewModelScope.launch {
            messagesState.collect { state ->
                if (state is UiState.Success) {
                    val msgs = state.data
                    val ids = msgs.map { it.id }.toSet()
                    if (initializedMessages) {
                        val newPartnerMsgs = msgs.filter {
                            it.id !in lastKnownMessageIds && it.senderUid != currentUserId && !it.deleted
                        }
                        newPartnerMsgs.lastOrNull()?.let { latest ->
                            val senderLabel = if (latest.senderRole == "ubaid") "Ubaid" else "Partner"
                            val preview = when (latest.messageType) {
                                "voice" -> "Voice note (${latest.durationSec ?: 1}s)"
                                "image" -> "Sent a photo"
                                "video" -> "Sent a video"
                                "file" -> "Sent file: ${latest.attachmentName ?: "Document"}"
                                "location" -> "Shared live location"
                                "sticker" -> "Sent a sticker ${latest.plaintext}"
                                else -> latest.plaintext
                            }
                            NotificationHelper.showMessageNotification(appContext, senderLabel, preview)
                        }
                    }
                    initializedMessages = true
                    lastKnownMessageIds = ids

                    // Automatically mark unread partner messages as read
                    msgs.filter { it.senderUid != currentUserId && currentUserId !in it.readByUids }
                        .forEach { unread ->
                            repository.markMessageRead(unread.id, currentUserId)
                        }
                }
            }
        }
    }

    fun onTypingChanged(text: String) {
        typingClearJob?.cancel()
        viewModelScope.launch {
            repository.updateMyMemberProfile(activeRole, mapOf("typingInChat" to text.isNotEmpty()))
        }
        if (text.isNotEmpty()) {
            typingClearJob = viewModelScope.launch {
                delay(2500L)
                repository.updateMyMemberProfile(activeRole, mapOf("typingInChat" to false))
            }
        }
    }

    fun sendMessage(
        memberUids: List<String>,
        plaintext: String,
        messageType: String = "text",
        attachmentName: String? = null,
        attachmentSizeBytes: Long? = null,
        durationSec: Int? = null,
        replyTo: DecryptedChatMessage? = null,
        disappearingSec: Long = 0L
    ) {
        if (plaintext.isBlank()) return
        typingClearJob?.cancel()
        viewModelScope.launch {
            repository.updateMyMemberProfile(activeRole, mapOf("typingInChat" to false))
            val res = repository.sendEncryptedMessage(
                senderRole = activeRole,
                memberUids = memberUids,
                plaintext = plaintext,
                messageType = messageType,
                attachmentName = attachmentName,
                attachmentSizeBytes = attachmentSizeBytes,
                durationSec = durationSec,
                replyToId = replyTo?.id,
                replyPreviewText = replyTo?.plaintext,
                disappearingSec = disappearingSec
            )
            if (res.isFailure) {
                _errorBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to send message"
            }
        }
    }

    fun editMessage(messageId: String, newText: String) {
        if (newText.isBlank()) return
        viewModelScope.launch {
            val res = repository.editEncryptedMessage(messageId, newText)
            if (res.isFailure) _errorBanner.value = res.exceptionOrNull()?.localizedMessage
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.markMessageDeleted(messageId)
        }
    }

    fun togglePin(message: DecryptedChatMessage) {
        viewModelScope.launch {
            repository.togglePinMessage(message.id, !message.pinned)
        }
    }

    fun react(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.reactToMessage(messageId, emoji)
        }
    }

    fun startVoiceRecording() {
        val started = voiceManager.startRecording()
        _isRecordingVoice.value = started
        if (!started) {
            _errorBanner.value = "Microphone unavailable on this device."
        }
    }

    fun stopAndSendVoiceRecording(memberUids: List<String>, disappearingSec: Long) {
        _isRecordingVoice.value = false
        val recorded = voiceManager.stopRecording() ?: return
        sendMessage(
            memberUids = memberUids,
            plaintext = recorded.audioBase64,
            messageType = "voice",
            attachmentName = "Voice Note",
            attachmentSizeBytes = recorded.sizeBytes,
            durationSec = recorded.durationSec,
            disappearingSec = disappearingSec
        )
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        voiceManager.stopRecordingAndDiscard()
    }

    fun togglePlayVoiceNote(msg: DecryptedChatMessage) {
        if (_playingMessageId.value == msg.id) {
            voiceManager.stopPlayback()
            _playingMessageId.value = null
        } else {
            _playingMessageId.value = msg.id
            voiceManager.playBase64Audio(msg.plaintext) {
                _playingMessageId.value = null
            }
        }
    }

    fun startCall(memberUids: List<String>, callType: String) {
        viewModelScope.launch {
            val res = repository.startCall(activeRole, memberUids, callType)
            if (res.isFailure) _errorBanner.value = res.exceptionOrNull()?.localizedMessage
        }
    }

    fun updateCallStatus(callId: String, status: String) {
        viewModelScope.launch {
            repository.updateCallStatus(callId, status)
        }
    }

    fun clearError() {
        _errorBanner.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.stopRecordingAndDiscard()
        voiceManager.stopPlayback()
    }
}
