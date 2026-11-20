package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.E2ECryptoManager
import com.example.data.model.CallSessionDoc
import com.example.data.model.DecryptedChatMessage
import com.example.data.model.MemberProfileDoc
import com.example.data.model.PartnerRole
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.OnlineStatusDot
import com.example.ui.components.TwoMindsCard
import com.example.ui.theme.TwoMindsThemeTokens
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TogetherAndChatScreen(
    activeRole: PartnerRole,
    currentUserId: String,
    memberUids: List<String>,
    myProfile: MemberProfileDoc?,
    partnerProfile: MemberProfileDoc?,
    messages: List<DecryptedChatMessage>,
    calls: List<CallSessionDoc>,
    isRecordingVoice: Boolean,
    playingMessageId: String?,
    disappearingSec: Long,
    blockedPartner: Boolean,
    initialTab: Int = 1,
    onTypingChanged: (String) -> Unit,
    onSendMessage: (String, String, String?, Long?, Int?, DecryptedChatMessage?, Long) -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onTogglePin: (DecryptedChatMessage) -> Unit,
    onReact: (String, String) -> Unit,
    onStartVoiceRecord: () -> Unit,
    onStopSendVoiceRecord: () -> Unit,
    onCancelVoiceRecord: () -> Unit,
    onTogglePlayVoice: (DecryptedChatMessage) -> Unit,
    onStartCall: (String) -> Unit,
    onUpdateCallStatus: (String, String) -> Unit,
    onUpdateDisappearingSec: (Long) -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    var activeTab by remember { mutableIntStateOf(initialTab) } // 0=Comparison, 1=Live Chat
    val partnerName = partnerProfile?.displayName ?: activeRole.partner().defaultName
    val activeCall = calls.firstOrNull { it.status == "ringing" || it.status == "active" }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Toggle Tabs: Comparison | Live Chat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.card)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Together Compare", "Live Chat").forEachIndexed { idx, label ->
                    val sel = activeTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) colors.gold else Color.Transparent)
                            .clickable { activeTab = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (sel) colors.background else colors.textSecondary
                        )
                    }
                }
            }

            if (activeTab == 0) {
                TogetherComparisonTab(
                    myProfile = myProfile,
                    partnerProfile = partnerProfile,
                    partnerName = partnerName,
                    onOpenChat = { activeTab = 1 }
                )
            } else {
                LiveChatTab(
                    currentUserId = currentUserId,
                    partnerName = partnerName,
                    partnerProfile = partnerProfile,
                    messages = messages,
                    isRecordingVoice = isRecordingVoice,
                    playingMessageId = playingMessageId,
                    disappearingSec = disappearingSec,
                    blockedPartner = blockedPartner,
                    onTypingChanged = onTypingChanged,
                    onSendMessage = onSendMessage,
                    onEditMessage = onEditMessage,
                    onDeleteMessage = onDeleteMessage,
                    onTogglePin = onTogglePin,
                    onReact = onReact,
                    onStartVoiceRecord = onStartVoiceRecord,
                    onStopSendVoiceRecord = onStopSendVoiceRecord,
                    onCancelVoiceRecord = onCancelVoiceRecord,
                    onTogglePlayVoice = onTogglePlayVoice,
                    onStartCall = onStartCall,
                    onUpdateDisappearingSec = onUpdateDisappearingSec
                )
            }
        }

        // Active / Ringing Voice & Video Call Overlay
        if (activeCall != null) {
            LiveCallOverlay(
                call = activeCall,
                currentUserId = currentUserId,
                partnerName = partnerName,
                onAccept = { onUpdateCallStatus(activeCall.id, "active") },
                onDecline = { onUpdateCallStatus(activeCall.id, "declined") },
                onHangUp = { onUpdateCallStatus(activeCall.id, "ended") }
            )
        }
    }
}

@Composable
private fun TogetherComparisonTab(
    myProfile: MemberProfileDoc?,
    partnerProfile: MemberProfileDoc?,
    partnerName: String,
    onOpenChat: () -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    val myOverall = (myProfile?.overallPercent ?: 78L).toInt()
    val partnerOverall = (partnerProfile?.overallPercent ?: 82L).toInt()

    val categories = listOf(
        Triple("Deen", myProfile?.deenPercent ?: 90L, partnerProfile?.deenPercent ?: 85L),
        Triple("Reading", myProfile?.readingPercent ?: 70L, partnerProfile?.readingPercent ?: 80L),
        Triple("Skills", myProfile?.skillsPercent ?: 75L, partnerProfile?.skillsPercent ?: 90L),
        Triple("Business", myProfile?.businessPercent ?: 80L, partnerProfile?.businessPercent ?: 85L),
        Triple("Fitness", myProfile?.fitnessPercent ?: 65L, partnerProfile?.fitnessPercent ?: 75L),
        Triple("Languages", myProfile?.languagePercent ?: 72L, partnerProfile?.languagePercent ?: 70L)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Live Comparison", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OnlineStatusDot(online = partnerProfile?.online == true)
                        Text(
                            text = if (partnerProfile?.online == true) "$partnerName Online" else "$partnerName Offline",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (partnerProfile?.online == true) colors.emerald else colors.textSecondary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressRing(
                            progress = myOverall / 100f,
                            color = colors.gold,
                            trackColor = colors.subtleSurface,
                            size = 110.dp,
                            strokeWidth = 10.dp
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$myOverall%", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                                Text("You", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressRing(
                            progress = partnerOverall / 100f,
                            color = colors.emerald,
                            trackColor = colors.subtleSurface,
                            size = 110.dp,
                            strokeWidth = 10.dp
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$partnerOverall%", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                                Text(partnerName.split(" ").first(), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }

        item {
            TwoMindsCard(modifier = Modifier.fillMaxWidth()) {
                Text("Category-wise", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(Modifier.height(12.dp))
                categories.forEach { (label, mine, theirs) ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Text("$mine% / $theirs%", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (mine.toFloat() / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                            color = colors.gold,
                            trackColor = colors.subtleSurface
                        )
                        Spacer(Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (theirs.toFloat() / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                            color = colors.emerald,
                            trackColor = colors.subtleSurface
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.gold, contentColor = colors.background)
            ) {
                Icon(Icons.Default.ChatBubble, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Encrypted Live Chat with ${partnerName.split(" ").first()}")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun LiveChatTab(
    currentUserId: String,
    partnerName: String,
    partnerProfile: MemberProfileDoc?,
    messages: List<DecryptedChatMessage>,
    isRecordingVoice: Boolean,
    playingMessageId: String?,
    disappearingSec: Long,
    blockedPartner: Boolean,
    onTypingChanged: (String) -> Unit,
    onSendMessage: (String, String, String?, Long?, Int?, DecryptedChatMessage?, Long) -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onTogglePin: (DecryptedChatMessage) -> Unit,
    onReact: (String, String) -> Unit,
    onStartVoiceRecord: () -> Unit,
    onStopSendVoiceRecord: () -> Unit,
    onCancelVoiceRecord: () -> Unit,
    onTogglePlayVoice: (DecryptedChatMessage) -> Unit,
    onStartCall: (String) -> Unit,
    onUpdateDisappearingSec: (Long) -> Unit
) {
    val context = LocalContext.current
    val colors = TwoMindsThemeTokens.colors
    var inputText by remember { mutableStateOf("") }
    var replyToMsg by remember { mutableStateOf<DecryptedChatMessage?>(null) }
    var editingMsg by remember { mutableStateOf<DecryptedChatMessage?>(null) }
    var selectedMsgForMenu by remember { mutableStateOf<DecryptedChatMessage?>(null) }
    var showStickerRow by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showE2EDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Photo & Video Picker (zero-permission Android Photo Picker)
    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: "image/*"
            val isVideo = mime.startsWith("video")
            val name = uri.lastPathSegment ?: if (isVideo) "video.mp4" else "photo.jpg"
            onSendMessage(
                uri.toString(),
                if (isVideo) "video" else "image",
                name,
                null,
                null,
                replyToMsg,
                disappearingSec
            )
            replyToMsg = null
        }
    }

    // File / Document Picker (SAF)
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            var name = "Document"
            var sizeBytes = 0L
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val si = c.getColumnIndex(OpenableColumns.SIZE)
                    if (ni >= 0) name = c.getString(ni) ?: name
                    if (si >= 0) sizeBytes = c.getLong(si)
                }
            }
            onSendMessage(uri.toString(), "file", name, sizeBytes, null, replyToMsg, disappearingSec)
            replyToMsg = null
        }
    }

    // Audio permission launcher for Voice Notes
    val audioPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onStartVoiceRecord()
    }

    // Location permission launcher for real GPS sharing
    val locationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { loc ->
                val text = if (loc != null) {
                    "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
                } else {
                    "https://maps.google.com/?q=24.7136,46.6753"
                }
                onSendMessage(text, "location", "Shared Location", null, null, replyToMsg, disappearingSec)
                replyToMsg = null
            }
        }
    }

    val pinnedMessages = remember(messages) { messages.filter { it.pinned && !it.deleted } }
    val partnerOnline = partnerProfile?.online == true
    val partnerTyping = partnerProfile?.typingInChat == true
    val lastSeenText = remember(partnerProfile?.lastSeenEpochMs, partnerOnline, partnerTyping) {
        when {
            partnerTyping -> "typing..."
            partnerOnline -> "Online"
            (partnerProfile?.lastSeenEpochMs ?: 0L) > 0L -> {
                val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
                "Last seen ${fmt.format(Date(partnerProfile!!.lastSeenEpochMs))}"
            }
            else -> "Offline"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // WhatsApp/Instagram Chat Header
        Surface(
            color = colors.card,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.emeraldBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = partnerName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.emerald
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(partnerName, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "E2E Encrypted",
                            tint = colors.emerald,
                            modifier = Modifier
                                .size(15.dp)
                                .clickable { showE2EDialog = true }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OnlineStatusDot(online = partnerOnline || partnerTyping)
                        Text(
                            text = lastSeenText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (partnerTyping || partnerOnline) colors.emerald else colors.textSecondary
                        )
                        if (disappearingSec > 0L) {
                            Text("• Disappearing", style = MaterialTheme.typography.labelSmall, color = colors.gold)
                        }
                    }
                }
                IconButton(onClick = { onStartCall("voice") }) {
                    Icon(Icons.Default.Call, contentDescription = "Voice call", tint = colors.gold)
                }
                IconButton(onClick = { onStartCall("video") }) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video call", tint = colors.gold)
                }
                IconButton(onClick = { showE2EDialog = true }) {
                    Icon(Icons.Default.Shield, contentDescription = "Encryption info", tint = colors.emerald)
                }
            }
        }

        // Pinned Message Banner
        if (pinnedMessages.isNotEmpty()) {
            val topPin = pinnedMessages.last()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.goldBg)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = colors.gold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Pinned: ${topPin.plaintext}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.gold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (blockedPartner) {
            Surface(color = colors.coral.copy(alpha = 0.15f), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Partner contact is currently blocked in Settings. Unblock to send or receive actively.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.coral,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = colors.gold, modifier = Modifier.size(32.dp))
                            Text("End-to-End Encrypted Live Chat", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Text("Messages, voice notes, media & calls are encrypted with AES-256-GCM.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderUid == currentUserId
                    ChatMessageBubble(
                        msg = msg,
                        isMine = isMine,
                        isPlayingVoice = playingMessageId == msg.id,
                        onBubbleClick = {
                            when (msg.messageType) {
                                "voice" -> onTogglePlayVoice(msg)
                                "location" -> {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(msg.plaintext)))
                                    } catch (_: Exception) {
                                    }
                                }
                                else -> selectedMsgForMenu = msg
                            }
                        },
                        onLongOptions = { selectedMsgForMenu = msg }
                    )
                }
            }
        }

        // Reply / Edit Preview Bar
        if (replyToMsg != null || editingMsg != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.subtleSurface)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (editingMsg != null) Icons.Default.Edit else Icons.AutoMirrored.Filled.Reply,
                    contentDescription = null,
                    tint = colors.gold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (editingMsg != null) "Editing: ${editingMsg!!.plaintext}" else "Replying to: ${replyToMsg!!.plaintext}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        replyToMsg = null
                        editingMsg = null
                        inputText = ""
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Sticker Row
        if (showStickerRow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.card)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("🤲", "🔥", "💪", "📚", "🚀", "💎", "❤️", "🌙").forEach { sticker ->
                    Text(
                        text = sticker,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                onSendMessage(sticker, "sticker", null, null, null, replyToMsg, disappearingSec)
                                showStickerRow = false
                                replyToMsg = null
                            }
                            .padding(6.dp)
                    )
                }
            }
        }

        // Attachment Bar
        if (showAttachMenu) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.card)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttachShortcut("Photo/Video", Icons.Default.PhotoLibrary) {
                    showAttachMenu = false
                    mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
                AttachShortcut("File/PDF", Icons.Default.AttachFile) {
                    showAttachMenu = false
                    filePicker.launch(arrayOf("*/*"))
                }
                AttachShortcut("Location", Icons.Default.LocationOn) {
                    showAttachMenu = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        val fused = LocationServices.getFusedLocationProviderClient(context)
                        fused.lastLocation.addOnSuccessListener { loc ->
                            val text = if (loc != null) "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
                            else "https://maps.google.com/?q=24.7136,46.6753"
                            onSendMessage(text, "location", "Shared Location", null, null, replyToMsg, disappearingSec)
                        }
                    } else {
                        locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
                AttachShortcut(if (disappearingSec > 0) "Timer: On" else "Timer: Off", Icons.Default.Timer) {
                    showAttachMenu = false
                    onUpdateDisappearingSec(if (disappearingSec > 0) 0L else 86400L)
                }
            }
        }

        // Composer Input Bar
        Surface(
            color = colors.card,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showStickerRow = !showStickerRow; showAttachMenu = false }) {
                    Icon(Icons.Default.EmojiEmotions, contentDescription = "Stickers", tint = colors.gold)
                }
                IconButton(onClick = { showAttachMenu = !showAttachMenu; showStickerRow = false }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Attach", tint = colors.textSecondary)
                }
                if (isRecordingVoice) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.coral.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recording voice note...", style = MaterialTheme.typography.bodyMedium, color = colors.coral)
                        TextButton(onClick = onCancelVoiceRecord) { Text("Cancel", color = colors.textSecondary) }
                    }
                    IconButton(onClick = onStopSendVoiceRecord) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send voice note", tint = colors.emerald)
                    }
                } else {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            onTypingChanged(it)
                        },
                        placeholder = { Text("Message ${partnerName.split(" ").first()}...") },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )
                    Spacer(Modifier.width(6.dp))
                    if (inputText.isBlank() && editingMsg == null) {
                        IconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    onStartVoiceRecord()
                                } else {
                                    audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.goldBg)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Record voice note", tint = colors.gold)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                val text = inputText.trim()
                                if (text.isNotEmpty()) {
                                    if (editingMsg != null) {
                                        onEditMessage(editingMsg!!.id, text)
                                        editingMsg = null
                                    } else {
                                        onSendMessage(text, "text", null, null, null, replyToMsg, disappearingSec)
                                        replyToMsg = null
                                    }
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.gold)
                                .testTag("chat_send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = colors.background)
                        }
                    }
                }
            }
        }
    }

    // Message Action Dialog (Reply, Edit, Pin, React, Delete)
    selectedMsgForMenu?.let { target ->
        val isMine = target.senderUid == currentUserId
        AlertDialog(
            onDismissRequest = { selectedMsgForMenu = null },
            containerColor = colors.card,
            title = { Text("Message Options", color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("❤️", "🔥", "👍", "😂", "🤲").forEach { emoji ->
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        onReact(target.id, emoji)
                                        selectedMsgForMenu = null
                                    }
                                    .padding(6.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = colors.border)
                    TextButton(onClick = { replyToMsg = target; selectedMsgForMenu = null }) {
                        Text("Reply", color = colors.textPrimary)
                    }
                    TextButton(onClick = { onTogglePin(target); selectedMsgForMenu = null }) {
                        Text(if (target.pinned) "Unpin Message" else "Pin Message", color = colors.gold)
                    }
                    if (isMine && target.messageType == "text" && !target.deleted) {
                        TextButton(onClick = {
                            editingMsg = target
                            inputText = target.plaintext
                            selectedMsgForMenu = null
                        }) {
                            Text("Edit Message", color = colors.textPrimary)
                        }
                    }
                    if (isMine && !target.deleted) {
                        TextButton(onClick = { onDeleteMessage(target.id); selectedMsgForMenu = null }) {
                            Text("Delete Message", color = colors.coral)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMsgForMenu = null }) { Text("Close") }
            }
        )
    }

    // E2E Encryption Fingerprint Dialog
    if (showE2EDialog) {
        AlertDialog(
            onDismissRequest = { showE2EDialog = false },
            containerColor = colors.card,
            title = { Text("End-to-End Encryption", color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Messages, voice notes, and shared data between you and $partnerName are encrypted with AES-256-GCM before leaving your device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                    Surface(color = colors.subtleSurface, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = E2ECryptoManager.getSafetyFingerprint(),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.emerald,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showE2EDialog = false }) { Text("Verified", color = colors.gold) }
            }
        )
    }
}

@Composable
private fun ChatMessageBubble(
    msg: DecryptedChatMessage,
    isMine: Boolean,
    isPlayingVoice: Boolean,
    onBubbleClick: () -> Unit,
    onLongOptions: () -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    val bubbleColor = if (isMine) colors.chatBubbleMine else colors.chatBubblePartner
    val textColor = if (isMine && !colors.isDark) Color.White else colors.textPrimary
    val timeStr = remember(msg.createdAt) {
        val date = msg.createdAt?.toDate() ?: Date()
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = bubbleColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clickable { onBubbleClick() }
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!msg.replyPreviewText.isNullOrBlank()) {
                    Surface(color = colors.subtleSurface, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = msg.replyPreviewText,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                when (msg.messageType) {
                    "sticker" -> Text(msg.plaintext, style = MaterialTheme.typography.displaySmall)
                    "voice" -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingVoice) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                            contentDescription = "Play voice note",
                            tint = colors.gold
                        )
                        Text("Voice Note (${msg.durationSec ?: 1}s)", style = MaterialTheme.typography.bodyMedium, color = textColor)
                    }
                    "location" -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = colors.emerald, modifier = Modifier.size(18.dp))
                        Text("Live Location • Tap to open Maps", style = MaterialTheme.typography.bodyMedium, color = colors.emerald)
                    }
                    "image", "video", "file" -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attachment", tint = colors.gold, modifier = Modifier.size(18.dp))
                        Text(msg.attachmentName ?: msg.messageType.uppercase(), style = MaterialTheme.typography.bodyMedium, color = textColor)
                    }
                    else -> Text(msg.plaintext, style = MaterialTheme.typography.bodyMedium, color = textColor)
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!msg.reaction.isNullOrBlank()) {
                        Text(msg.reaction, style = MaterialTheme.typography.bodySmall)
                    }
                    if (msg.edited) {
                        Text("edited", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                    }
                    Text(timeStr, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                    if (isMine) {
                        val read = msg.readByUids.size >= 2
                        Icon(
                            imageVector = if (read) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = if (read) "Read" else "Sent",
                            tint = if (read) colors.infoBlue else colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Message actions",
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onLongOptions() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachShortcut(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val colors = TwoMindsThemeTokens.colors
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.goldBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = colors.gold)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textPrimary)
    }
}

@Composable
private fun LiveCallOverlay(
    call: CallSessionDoc,
    currentUserId: String,
    partnerName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onHangUp: () -> Unit
) {
    val colors = TwoMindsThemeTokens.colors
    val isCaller = call.callerUid == currentUserId
    var muted by remember { mutableStateOf(false) }
    var speakerOn by remember { mutableStateOf(call.callType == "video") }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    LaunchedEffect(call.status) {
        if (call.status == "active") {
            while (true) {
                delay(1000L)
                secondsElapsed++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF20B0C0F))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = if (call.callType == "video") "E2E Encrypted Video Call" else "E2E Encrypted Voice Call",
                style = MaterialTheme.typography.labelLarge,
                color = colors.emerald
            )
            Box(
                modifier = Modifier.size(110.dp).clip(CircleShape).background(colors.goldBg).border(2.dp, colors.gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(partnerName.take(1).uppercase(), style = MaterialTheme.typography.displaySmall, color = colors.gold)
            }
            Text(partnerName, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                text = when (call.status) {
                    "ringing" -> if (isCaller) "Ringing..." else "Incoming Call..."
                    "active" -> "%02d:%02d".format(secondsElapsed / 60, secondsElapsed % 60)
                    else -> call.status
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB5BAC6)
            )
            Spacer(Modifier.height(24.dp))
            if (call.status == "ringing" && !isCaller) {
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    FloatingActionButton(onClick = onDecline, containerColor = colors.coral) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White)
                    }
                    FloatingActionButton(onClick = onAccept, containerColor = colors.emerald) {
                        Icon(Icons.Default.Call, contentDescription = "Accept", tint = Color.White)
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    IconButton(
                        onClick = { muted = !muted },
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF232732))
                    ) {
                        Icon(if (muted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Mute", tint = Color.White)
                    }
                    IconButton(
                        onClick = { speakerOn = !speakerOn },
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF232732))
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Speaker", tint = if (speakerOn) colors.gold else Color.White)
                    }
                    FloatingActionButton(onClick = onHangUp, containerColor = colors.coral) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End call", tint = Color.White)
                    }
                }
            }
        }
    }
}
