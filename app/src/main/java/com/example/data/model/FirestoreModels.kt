package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class PartnerRole(val id: String, val defaultName: String) {
    ME("me", "You (Partner)"),
    UBAID("ubaid", "Ubaid");

    fun partner(): PartnerRole = if (this == ME) UBAID else ME

    companion object {
        fun fromId(raw: String?): PartnerRole? = entries.firstOrNull { it.id == raw }
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "unassigned",
    val displayName: String = "Two Minds Partner",
    val bio: String = "Building discipline, deen, and mastery every day.",
    val themeMode: String = "dark",
    val notificationsEnabled: Boolean = true,
    val disappearingMessagesSec: Long = 0L,
    val blockedPartner: Boolean = false,
    val deviceInfo: String = "Android Device",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WorkspaceDoc(
    @DocumentId val id: String = "",
    val workspaceName: String = "Two Minds",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MemberProfileDoc(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val role: String = "me",
    val displayName: String = "Partner",
    val bio: String = "Grow Together",
    val online: Boolean = false,
    val typingInChat: Boolean = false,
    val lastSeenEpochMs: Long = 0L,
    val tasksDone: Long = 5L,
    val tasksTotal: Long = 7L,
    val quranMinutes: Long = 45L,
    val quranPage: Long = 112L,
    val quranTotalPages: Long = 172L,
    val currentSurah: String = "Surah Al-Baqarah",
    val workoutCompleted: Boolean = true,
    val overallPercent: Long = 78L,
    val deenPercent: Long = 90L,
    val readingPercent: Long = 70L,
    val skillsPercent: Long = 75L,
    val businessPercent: Long = 80L,
    val fitnessPercent: Long = 65L,
    val languagePercent: Long = 72L,
    val tasbeehLabel: String = "SubhanAllah",
    val tasbeehCount: Long = 78L,
    val tasbeehTarget: Long = 100L,
    val waterGlasses: Long = 5L,
    val sleepHours: Double = 7.5,
    val currentBookTitle: String = "The Art of Thinking Clearly",
    val currentBookProgress: Long = 68L,
    val dailyPagesRead: Long = 12L,
    val dailyPagesTarget: Long = 20L,
    val activeSkillTrack: String = "UI/UX Design",
    val activeLanguage: String = "Turkish",
    val languageWordsLearned: Long = 140L,
    val namazStates: List<Boolean> = listOf(true, true, true, false, false),
    val checklistStates: List<Boolean> = listOf(true, true, true, true, true, false, false),
    val sunnatStates: List<Boolean> = listOf(true, true, true, false, false, false),
    val weeklyScores: List<Long> = listOf(58L, 64L, 69L, 74L, 71L, 78L, 82L),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "ownerUid" to ownerUid,
        "role" to role,
        "displayName" to displayName,
        "bio" to bio,
        "online" to online,
        "typingInChat" to typingInChat,
        "lastSeenEpochMs" to lastSeenEpochMs,
        "tasksDone" to tasksDone,
        "tasksTotal" to tasksTotal,
        "quranMinutes" to quranMinutes,
        "quranPage" to quranPage,
        "quranTotalPages" to quranTotalPages,
        "currentSurah" to currentSurah,
        "workoutCompleted" to workoutCompleted,
        "overallPercent" to overallPercent,
        "deenPercent" to deenPercent,
        "readingPercent" to readingPercent,
        "skillsPercent" to skillsPercent,
        "businessPercent" to businessPercent,
        "fitnessPercent" to fitnessPercent,
        "languagePercent" to languagePercent,
        "tasbeehLabel" to tasbeehLabel,
        "tasbeehCount" to tasbeehCount,
        "tasbeehTarget" to tasbeehTarget,
        "waterGlasses" to waterGlasses,
        "sleepHours" to sleepHours,
        "currentBookTitle" to currentBookTitle,
        "currentBookProgress" to currentBookProgress,
        "dailyPagesRead" to dailyPagesRead,
        "dailyPagesTarget" to dailyPagesTarget,
        "activeSkillTrack" to activeSkillTrack,
        "activeLanguage" to activeLanguage,
        "languageWordsLearned" to languageWordsLearned,
        "namazStates" to namazStates,
        "checklistStates" to checklistStates,
        "sunnatStates" to sunnatStates,
        "weeklyScores" to weeklyScores,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class ChatMessageDoc(
    @DocumentId val id: String = "",
    val senderUid: String = "",
    val senderRole: String = "me",
    val memberUids: List<String> = emptyList(),
    val ciphertext: String = "",
    val ivBase64: String = "",
    val messageType: String = "text",
    val attachmentName: String? = null,
    val attachmentSizeBytes: Long? = null,
    val durationSec: Long? = null,
    val replyToId: String? = null,
    val replyPreviewCipher: String? = null,
    val replyPreviewIv: String? = null,
    val edited: Boolean = false,
    val deleted: Boolean = false,
    val pinned: Boolean = false,
    val reaction: String? = null,
    val readByUids: List<String> = emptyList(),
    val expiresAtEpochMs: Long? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class DecryptedChatMessage(
    val id: String,
    val senderUid: String,
    val senderRole: String,
    val plaintext: String,
    val messageType: String,
    val attachmentName: String?,
    val attachmentSizeBytes: Long?,
    val durationSec: Int?,
    val replyToId: String?,
    val replyPreviewText: String?,
    val edited: Boolean,
    val deleted: Boolean,
    val pinned: Boolean,
    val reaction: String?,
    val readByUids: List<String>,
    val expiresAtEpochMs: Long?,
    val createdAt: Timestamp?
)

@Keep
@IgnoreExtraProperties
data class CallSessionDoc(
    @DocumentId val id: String = "",
    val callerUid: String = "",
    val callerRole: String = "me",
    val memberUids: List<String> = emptyList(),
    val callType: String = "voice",
    val status: String = "ended",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BusinessItemDoc(
    @DocumentId val id: String = "",
    val createdByUid: String = "",
    val memberUids: List<String> = emptyList(),
    val title: String = "",
    val category: String = "Operations",
    val driveUrl: String? = null,
    val driveType: String? = null,
    val assigneeRole: String = "both",
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookNoteDoc(
    @DocumentId val id: String = "",
    val authorUid: String = "",
    val authorRole: String = "me",
    val memberUids: List<String> = emptyList(),
    val bookId: String = "thinking_clearly",
    val content: String = "",
    val pageNumber: Long? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TradeEntryDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val symbol: String = "BTCUSDT",
    val side: String = "BUY",
    val quantity: Double = 0.0,
    val priceUsd: Double = 0.0,
    val totalUsd: Double = 0.0,
    val notes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PriceAlertDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val symbol: String = "BTCUSDT",
    val targetPrice: Double = 0.0,
    val condition: String = "above",
    val active: Boolean = true,
    val triggered: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CustomBookDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val author: String = "",
    val uriString: String = "",
    val forRole: String = "both",
    val currentPage: Long = 0L,
    val totalPages: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
