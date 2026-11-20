package com.example.core

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File

class AudioVoiceManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var outputFile: File? = null
    private var startTimeMs: Long = 0L

    fun startRecording(): Boolean {
        return try {
            stopRecordingAndDiscard()
            val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            outputFile = file
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioSamplingRate(16000)
            mr.setAudioEncodingBitRate(32000)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            startTimeMs = System.currentTimeMillis()
            true
        } catch (_: Exception) {
            stopRecordingAndDiscard()
            false
        }
    }

    data class RecordedVoiceNote(
        val audioBase64: String,
        val durationSec: Int,
        val sizeBytes: Long
    )

    fun stopRecording(): RecordedVoiceNote? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            val file = outputFile ?: return null
            if (!file.exists()) return null
            val bytes = file.readBytes()
            val durationSec = ((System.currentTimeMillis() - startTimeMs) / 1000L).toInt().coerceAtLeast(1)
            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            file.delete()
            outputFile = null
            RecordedVoiceNote(
                audioBase64 = encoded,
                durationSec = durationSec,
                sizeBytes = bytes.size.toLong()
            )
        } catch (_: Exception) {
            stopRecordingAndDiscard()
            null
        }
    }

    fun stopRecordingAndDiscard() {
        try {
            recorder?.release()
        } catch (_: Exception) {
        }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }

    fun playBase64Audio(base64Audio: String, onComplete: () -> Unit) {
        try {
            stopPlayback()
            val bytes = Base64.decode(base64Audio, Base64.NO_WRAP)
            val tempFile = File(context.cacheDir, "play_${System.currentTimeMillis()}.m4a")
            tempFile.writeBytes(bytes)
            val mp = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    tempFile.delete()
                    stopPlayback()
                    onComplete()
                }
                start()
            }
            player = mp
        } catch (_: Exception) {
            onComplete()
        }
    }

    fun stopPlayback() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
    }
}
