package com.example.data.repository

import android.content.Context
import android.os.Build
import com.example.R
import com.example.core.E2ECryptoManager
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TwoMindsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        const val WORKSPACE_ID = "two_minds_main"
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun workspaceRef() = db.collection("workspaces").document(WORKSPACE_ID)

    suspend fun ensureWorkspaceJoinedAndProfilesReady(role: PartnerRole): Result<List<String>> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val wsPath = "workspaces/$WORKSPACE_ID"
        return try {
            val wsRef = workspaceRef()
            var memberUids = listOf(uid)
            try {
                wsRef.update(
                    mapOf(
                        "memberUids" to FieldValue.arrayUnion(uid),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                val snap = wsRef.get().await()
                val existingMembers = (snap.get("memberUids") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: listOf(uid)
                memberUids = if (uid in existingMembers) existingMembers else existingMembers + uid
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    wsRef.set(
                        mapOf(
                            "workspaceName" to "Two Minds",
                            "memberUids" to listOf(uid),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    memberUids = listOf(uid)
                } else {
                    throw e
                }
            }

            // Ensure UserProfile document
            val userDocRef = db.collection("users").document(uid)
            val userSnap = userDocRef.get().await()
            val deviceLabel = "${Build.MANUFACTURER} ${Build.MODEL}".take(80)
            if (!userSnap.exists()) {
                userDocRef.set(
                    mapOf(
                        "userId" to uid,
                        "role" to role.id,
                        "displayName" to role.defaultName,
                        "bio" to "Building discipline, deen, and mastery every day.",
                        "themeMode" to "dark",
                        "notificationsEnabled" to true,
                        "disappearingMessagesSec" to 0L,
                        "blockedPartner" to false,
                        "deviceInfo" to deviceLabel,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                userDocRef.update(
                    mapOf(
                        "role" to role.id,
                        "deviceInfo" to deviceLabel,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Ensure my MemberProfile document
            val myMemberRef = wsRef.collection("memberProfiles").document(role.id)
            val memberSnap = myMemberRef.get().await()
            if (!memberSnap.exists()) {
                val defaultProfile = if (role == PartnerRole.ME) {
                    MemberProfileDoc(
                        ownerUid = uid,
                        role = "me",
                        displayName = "You (Partner)",
                        online = true,
                        lastSeenEpochMs = System.currentTimeMillis(),
                        currentBookTitle = "The Art of Thinking Clearly",
                        currentBookProgress = 68L,
                        activeSkillTrack = "UI/UX Design",
                        activeLanguage = "Turkish",
                        overallPercent = 78L
                    )
                } else {
                    MemberProfileDoc(
                        ownerUid = uid,
                        role = "ubaid",
                        displayName = "Ubaid",
                        online = true,
                        lastSeenEpochMs = System.currentTimeMillis(),
                        currentBookTitle = "Made to Stick",
                        currentBookProgress = 42L,
                        activeSkillTrack = "Digital Marketing",
                        activeLanguage = "Spanish",
                        overallPercent = 82L,
                        deenPercent = 85L,
                        readingPercent = 80L,
                        skillsPercent = 90L,
                        businessPercent = 85L,
                        fitnessPercent = 75L
                    )
                }
                myMemberRef.set(defaultProfile.toCreateMap()).await()
            } else if (memberSnap.getString("ownerUid") == uid) {
                myMemberRef.update(
                    mapOf(
                        "online" to true,
                        "lastSeenEpochMs" to System.currentTimeMillis(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            Result.success(memberUids)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, wsPath)
            Result.failure(e)
        }
    }

    fun observeUserProfile(uid: String): Flow<UserProfileDoc?> = flow {
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid).snapshots()
                .map { snap -> snap.toObject(UserProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observeWorkspace(uid: String): Flow<WorkspaceDoc?> = flow {
        val path = "workspaces/$WORKSPACE_ID"
        emitAll(
            workspaceRef().snapshots()
                .map { snap -> snap.toObject(WorkspaceDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observeMemberProfile(role: PartnerRole, uid: String): Flow<MemberProfileDoc?> = flow {
        val path = "workspaces/$WORKSPACE_ID/memberProfiles/${role.id}"
        emitAll(
            workspaceRef().collection("memberProfiles").document(role.id).snapshots()
                .map { snap -> snap.toObject(MemberProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    // Partner profile may not exist yet before partner signs in; emit null if NOT_FOUND
                    val fe = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fe?.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                        emit(null)
                    } else {
                        unwrapAndLog(error, OperationType.GET, path, uid)
                        throw error
                    }
                }
        )
    }

    suspend fun updateUserSettings(updates: Map<String, Any>): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            val payload = updates + ("updatedAt" to FieldValue.serverTimestamp())
            db.collection("users").document(uid).update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateMyMemberProfile(role: PartnerRole, updates: Map<String, Any>): Result<Unit> {
        val path = "workspaces/$WORKSPACE_ID/memberProfiles/${role.id}"
        return try {
            requireUserId()
            val payload = updates + ("updatedAt" to FieldValue.serverTimestamp())
            workspaceRef().collection("memberProfiles").document(role.id).update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- LIVE ENCRYPTED CHAT ---
    fun observeMessages(uid: String): Flow<List<DecryptedChatMessage>> = flow {
        val path = "workspaces/$WORKSPACE_ID/messages"
        emitAll(
            workspaceRef().collection("messages")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snapshot ->
                    val docs = snapshot.toObjects(ChatMessageDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                    val now = System.currentTimeMillis()
                    docs.filter { it.expiresAtEpochMs == null || it.expiresAtEpochMs == 0L || it.expiresAtEpochMs > now }
                        .map { doc ->
                            val plain = if (doc.deleted) {
                                "This message was deleted"
                            } else {
                                E2ECryptoManager.decrypt(doc.ciphertext, doc.ivBase64)
                            }
                            val replyPlain = if (!doc.replyPreviewCipher.isNullOrBlank() && !doc.replyPreviewIv.isNullOrBlank()) {
                                E2ECryptoManager.decrypt(doc.replyPreviewCipher, doc.replyPreviewIv)
                            } else null
                            DecryptedChatMessage(
                                id = doc.id,
                                senderUid = doc.senderUid,
                                senderRole = doc.senderRole,
                                plaintext = plain,
                                messageType = doc.messageType,
                                attachmentName = doc.attachmentName,
                                attachmentSizeBytes = doc.attachmentSizeBytes,
                                durationSec = doc.durationSec?.toInt(),
                                replyToId = doc.replyToId,
                                replyPreviewText = replyPlain,
                                edited = doc.edited,
                                deleted = doc.deleted,
                                pinned = doc.pinned,
                                reaction = doc.reaction,
                                readByUids = doc.readByUids,
                                expiresAtEpochMs = doc.expiresAtEpochMs,
                                createdAt = doc.createdAt
                            )
                        }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun sendEncryptedMessage(
        senderRole: PartnerRole,
        memberUids: List<String>,
        plaintext: String,
        messageType: String = "text",
        attachmentName: String? = null,
        attachmentSizeBytes: Long? = null,
        durationSec: Int? = null,
        replyToId: String? = null,
        replyPreviewText: String? = null,
        disappearingSec: Long = 0L
    ): Result<Unit> {
        var path = "workspaces/$WORKSPACE_ID/messages"
        return try {
            val uid = requireUserId()
            require(plaintext.isNotBlank()) { "Message content cannot be blank" }
            val enc = E2ECryptoManager.encrypt(plaintext)
            val replyEnc = replyPreviewText?.take(120)?.let { E2ECryptoManager.encrypt(it) }
            val expiresAt = if (disappearingSec > 0L) System.currentTimeMillis() + (disappearingSec * 1000L) else null
            val effectiveMembers = if (uid in memberUids) memberUids else (memberUids + uid).distinct()
            val docRef = workspaceRef().collection("messages").document()
            path = docRef.path
            val payload = mutableMapOf<String, Any?>(
                "senderUid" to uid,
                "senderRole" to senderRole.id,
                "memberUids" to effectiveMembers,
                "ciphertext" to enc.ciphertextBase64,
                "ivBase64" to enc.ivBase64,
                "messageType" to messageType,
                "attachmentName" to attachmentName,
                "attachmentSizeBytes" to attachmentSizeBytes,
                "durationSec" to durationSec?.toLong(),
                "replyToId" to replyToId,
                "replyPreviewCipher" to replyEnc?.ciphertextBase64,
                "replyPreviewIv" to replyEnc?.ivBase64,
                "edited" to false,
                "deleted" to false,
                "pinned" to false,
                "readByUids" to listOf(uid),
                "expiresAtEpochMs" to expiresAt,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun editEncryptedMessage(messageId: String, newPlaintext: String): Result<Unit> {
        val path = "workspaces/$WORKSPACE_ID/messages/$messageId"
        return try {
            requireUserId()
            require(newPlaintext.isNotBlank()) { "Edited message cannot be blank" }
            val enc = E2ECryptoManager.encrypt(newPlaintext)
            workspaceRef().collection("messages").document(messageId).update(
                mapOf(
                    "ciphertext" to enc.ciphertextBase64,
                    "ivBase64" to enc.ivBase64,
                    "edited" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markMessageDeleted(messageId: String): Result<Unit> {
        val path = "workspaces/$WORKSPACE_ID/messages/$messageId"
        return try {
            requireUserId()
            val enc = E2ECryptoManager.encrypt("Message deleted")
            workspaceRef().collection("messages").document(messageId).update(
                mapOf(
                    "ciphertext" to enc.ciphertextBase64,
                    "ivBase64" to enc.ivBase64,
                    "deleted" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun togglePinMessage(messageId: String, pinned: Boolean): Result<Unit> =
        updateMessageFields(messageId, mapOf("pinned" to pinned))

    suspend fun reactToMessage(messageId: String, emoji: String): Result<Unit> =
        updateMessageFields(messageId, mapOf("reaction" to emoji))

    suspend fun markMessageRead(messageId: String, uid: String): Result<Unit> =
        updateMessageFields(messageId, mapOf("readByUids" to FieldValue.arrayUnion(uid)))

    private suspend fun updateMessageFields(messageId: String, fields: Map<String, Any>): Result<Unit> {
        val path = "workspaces/$WORKSPACE_ID/messages/$messageId"
        return try {
            requireUserId()
            workspaceRef().collection("messages").document(messageId)
                .update(fields + ("updatedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- CALL SIGNALING ---
    fun observeCalls(uid: String): Flow<List<CallSessionDoc>> = flow {
        val path = "workspaces/$WORKSPACE_ID/calls"
        emitAll(
            workspaceRef().collection("calls")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CallSessionDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                        .take(5)
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun startCall(callerRole: PartnerRole, memberUids: List<String>, callType: String): Result<String> {
        var path = "workspaces/$WORKSPACE_ID/calls"
        return try {
            val uid = requireUserId()
            val effectiveMembers = if (uid in memberUids) memberUids else (memberUids + uid).distinct()
            val docRef = workspaceRef().collection("calls").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "callerUid" to uid,
                    "callerRole" to callerRole.id,
                    "memberUids" to effectiveMembers,
                    "callType" to callType,
                    "status" to "ringing",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateCallStatus(callId: String, status: String): Result<Unit> {
        val path = "workspaces/$WORKSPACE_ID/calls/$callId"
        return try {
            requireUserId()
            workspaceRef().collection("calls").document(callId).update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- BUSINESS & GOOGLE DRIVE ---
    fun observeBusinessItems(uid: String): Flow<List<BusinessItemDoc>> = flow {
        val path = "workspaces/$WORKSPACE_ID/businessItems"
        emitAll(
            workspaceRef().collection("businessItems")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(BusinessItemDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addBusinessItem(
        memberUids: List<String>,
        title: String,
        category: String,
        driveUrl: String?,
        driveType: String?,
        assigneeRole: String
    ): Result<Unit> {
        var path = "workspaces/$WORKSPACE_ID/businessItems"
        return try {
            val uid = requireUserId()
            require(title.isNotBlank()) { "Title is required" }
            val effectiveMembers = if (uid in memberUids) memberUids else (memberUids + uid).distinct()
            val docRef = workspaceRef().collection("businessItems").document()
            path = docRef.path
            val payload = mutableMapOf<String, Any?>(
                "createdByUid" to uid,
                "memberUids" to effectiveMembers,
                "title" to title.trim().take(160),
                "category" to category.take(60),
                "driveUrl" to driveUrl?.trim()?.takeIf { it.isNotEmpty() }?.take(500),
                "driveType" to driveType?.takeIf { it.isNotEmpty() },
                "assigneeRole" to assigneeRole,
                "completed" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleBusinessItem(itemId: String, completed: Boolean): Result<Unit> {
        val path = "workspaces/$WORKSPACE_ID/businessItems/$itemId"
        return try {
            requireUserId()
            workspaceRef().collection("businessItems").document(itemId).update(
                mapOf(
                    "completed" to completed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- BOOK NOTES & DISCUSSION ---
    fun observeBookNotes(uid: String): Flow<List<BookNoteDoc>> = flow {
        val path = "workspaces/$WORKSPACE_ID/bookNotes"
        emitAll(
            workspaceRef().collection("bookNotes")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(BookNoteDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addBookNote(
        authorRole: PartnerRole,
        memberUids: List<String>,
        bookId: String,
        content: String,
        pageNumber: Long? = null
    ): Result<Unit> {
        var path = "workspaces/$WORKSPACE_ID/bookNotes"
        return try {
            val uid = requireUserId()
            require(content.isNotBlank()) { "Note content cannot be empty" }
            val effectiveMembers = if (uid in memberUids) memberUids else (memberUids + uid).distinct()
            val docRef = workspaceRef().collection("bookNotes").document()
            path = docRef.path
            val payload = mutableMapOf<String, Any?>(
                "authorUid" to uid,
                "authorRole" to authorRole.id,
                "memberUids" to effectiveMembers,
                "bookId" to bookId,
                "content" to content.trim().take(2000),
                "pageNumber" to pageNumber,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- CUSTOM PDF BOOKS ---
    fun observeCustomBooks(uid: String): Flow<List<CustomBookDoc>> = flow {
        val path = "users/$uid/customBooks"
        emitAll(
            db.collection("users").document(uid).collection("customBooks")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(CustomBookDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addCustomBook(title: String, author: String, uriString: String, forRole: String, totalPages: Long): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("customBooks").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "title" to title.trim().take(160),
                    "author" to author.trim().ifEmpty { "PDF Document" }.take(120),
                    "uriString" to uriString.take(1000),
                    "forRole" to forRole,
                    "currentPage" to 0L,
                    "totalPages" to totalPages.coerceAtLeast(1L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- TRADING PORTFOLIO & ALERTS ---
    fun observeTrades(uid: String): Flow<List<TradeEntryDoc>> = flow {
        val path = "users/$uid/trades"
        emitAll(
            db.collection("users").document(uid).collection("trades")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(TradeEntryDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun placePaperTrade(symbol: String, side: String, quantity: Double, priceUsd: Double, notes: String?): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(quantity > 0 && priceUsd > 0) { "Invalid quantity or price" }
            val docRef = db.collection("users").document(uid).collection("trades").document()
            path = docRef.path
            val payload = mutableMapOf<String, Any?>(
                "userId" to uid,
                "symbol" to symbol,
                "side" to side,
                "quantity" to quantity,
                "priceUsd" to priceUsd,
                "totalUsd" to quantity * priceUsd,
                "notes" to notes?.takeIf { it.isNotBlank() }?.take(300),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observePriceAlerts(uid: String): Flow<List<PriceAlertDoc>> = flow {
        val path = "users/$uid/priceAlerts"
        emitAll(
            db.collection("users").document(uid).collection("priceAlerts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(PriceAlertDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun addPriceAlert(symbol: String, targetPrice: Double, condition: String): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(targetPrice > 0) { "Target price must be positive" }
            val docRef = db.collection("users").document(uid).collection("priceAlerts").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "symbol" to symbol,
                    "targetPrice" to targetPrice,
                    "condition" to condition,
                    "active" to true,
                    "triggered" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun markPriceAlertTriggered(alertId: String): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("priceAlerts").document(alertId)
            path = docRef.path
            docRef.update(
                mapOf(
                    "triggered" to true,
                    "active" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deletePriceAlert(alertId: String): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("priceAlerts").document(alertId)
            path = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    /**
     * Ensures documents created before Partner 2 joined have both memberUids so Partner 2 sees full history.
     */
    suspend fun syncHistoryMembership(memberUids: List<String>) {
        if (memberUids.size < 2) return
        val uid = auth.currentUser?.uid ?: return
        try {
            val msgs = workspaceRef().collection("messages")
                .whereEqualTo("senderUid", uid)
                .get().await()
            for (doc in msgs.documents) {
                val cur = (doc.get("memberUids") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (!cur.containsAll(memberUids)) {
                    doc.reference.update("memberUids", memberUids, "updatedAt", FieldValue.serverTimestamp()).await()
                }
            }
            val biz = workspaceRef().collection("businessItems")
                .whereEqualTo("createdByUid", uid)
                .get().await()
            for (doc in biz.documents) {
                val cur = (doc.get("memberUids") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (!cur.containsAll(memberUids)) {
                    doc.reference.update("memberUids", memberUids, "updatedAt", FieldValue.serverTimestamp()).await()
                }
            }
            val notes = workspaceRef().collection("bookNotes")
                .whereEqualTo("authorUid", uid)
                .get().await()
            for (doc in notes.documents) {
                val cur = (doc.get("memberUids") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (!cur.containsAll(memberUids)) {
                    doc.reference.update("memberUids", memberUids, "updatedAt", FieldValue.serverTimestamp()).await()
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun unwrapAndLog(error: Throwable, op: OperationType, path: String, expectedUid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }
}
