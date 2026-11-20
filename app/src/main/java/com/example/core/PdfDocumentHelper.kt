package com.example.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PdfDocumentHelper {
    sealed interface PdfSource {
        data class BundledAsset(val assetFileName: String, val title: String) : PdfSource
        data class DocumentUri(val uri: Uri, val title: String) : PdfSource
    }

    suspend fun getPageCount(context: Context, source: PdfSource): Int = withContext(Dispatchers.IO) {
        openDescriptor(context, source)?.use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                renderer.pageCount
            }
        } ?: 0
    }

    suspend fun renderPage(
        context: Context,
        source: PdfSource,
        pageIndex: Int,
        targetWidthPx: Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            openDescriptor(context, source)?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null
                    renderer.openPage(pageIndex).use { page ->
                        val aspect = page.height.toFloat() / page.width.toFloat().coerceAtLeast(1f)
                        val width = targetWidthPx.coerceIn(480, 1440)
                        val height = (width * aspect).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun openDescriptor(context: Context, source: PdfSource): ParcelFileDescriptor? {
        return when (source) {
            is PdfSource.BundledAsset -> {
                val outFile = File(context.cacheDir, source.assetFileName)
                if (!outFile.exists()) {
                    context.assets.open(source.assetFileName).use { input ->
                        outFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                ParcelFileDescriptor.open(outFile, ParcelFileDescriptor.MODE_READ_ONLY)
            }
            is PdfSource.DocumentUri -> {
                context.contentResolver.openFileDescriptor(source.uri, "r")
            }
        }
    }
}
