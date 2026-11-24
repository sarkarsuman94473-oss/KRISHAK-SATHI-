package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.media.ExifInterface
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Safely unwraps ContextWrapper chain to find host Activity (avoids ClassCastException).
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Normalizes Bengali (০-৯) and Devanagari (०-९) numerals to ASCII digits (0-9) and parses Double.
 */
fun String.normalizeDigits(): String {
    if (isEmpty()) return this
    val sb = StringBuilder(length)
    for (ch in this) {
        when (ch) {
            in '০'..'৯' -> sb.append(('0'.code + (ch.code - '০'.code)).toChar())
            in '०'..'९' -> sb.append(('0'.code + (ch.code - '०'.code)).toChar())
            '।' -> sb.append('.')
            ',' -> { /* strip thousands separators if any, keep trimmed later */ }
            else -> sb.append(ch)
        }
    }
    return sb.toString().trim()
}

fun String.toLocalizedDoubleOrNull(): Double? = normalizeDigits().toDoubleOrNull()

fun String.toLocalizedIntOrNull(): Int? = normalizeDigits().toIntOrNull()

/**
 * Safely starts an external Activity Intent without crashing when no handler is installed.
 */
fun safeStartActivity(
    context: Context,
    intent: Intent,
    fallbackToast: String? = null
): Boolean {
    return try {
        val launchIntent = if (context.findActivity() == null) {
            intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        } else {
            intent
        }
        context.startActivity(launchIntent)
        true
    } catch (_: Exception) {
        if (!fallbackToast.isNullOrBlank()) {
            Toast.makeText(context, fallbackToast, Toast.LENGTH_SHORT).show()
        }
        false
    }
}

/**
 * Loads a Bitmap safely from a content Uri with bounded dimensions (ARGB_8888 software bitmap),
 * automatically respects EXIF rotation, and properly closes streams to prevent OOM & FD leaks.
 */
fun loadSafeBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1280): Bitmap? {
    var boundsStream: InputStream? = null
    var decodeStream: InputStream? = null
    var exifStream: InputStream? = null
    return try {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        boundsStream = context.contentResolver.openInputStream(uri) ?: return null
        BitmapFactory.decodeStream(boundsStream, null, boundsOpts)
        boundsStream.close()
        boundsStream = null

        val origW = boundsOpts.outWidth
        val origH = boundsOpts.outHeight
        if (origW <= 0 || origH <= 0) return null

        var inSampleSize = 1
        while (origW / inSampleSize > maxDim * 2 || origH / inSampleSize > maxDim * 2) {
            inSampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        decodeStream = context.contentResolver.openInputStream(uri) ?: return null
        val decoded = BitmapFactory.decodeStream(decodeStream, null, decodeOpts) ?: return null
        decodeStream.close()
        decodeStream = null

        // Check EXIF orientation cleanly
        val orientation = try {
            exifStream = context.contentResolver.openInputStream(uri)
            if (exifStream != null) {
                ExifInterface(exifStream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } else {
                ExifInterface.ORIENTATION_NORMAL
            }
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        } finally {
            try { exifStream?.close() } catch (_: Exception) {}
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
        }

        val oriented = if (!matrix.isIdentity) {
            val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            if (rotated != decoded) decoded.recycle()
            rotated
        } else {
            decoded
        }

        scaleBitmapBounded(oriented, maxDim)
    } catch (_: OutOfMemoryError) {
        null
    } catch (_: Exception) {
        null
    } finally {
        try { boundsStream?.close() } catch (_: Exception) {}
        try { decodeStream?.close() } catch (_: Exception) {}
        try { exifStream?.close() } catch (_: Exception) {}
    }
}

/**
 * Scales a Bitmap down so neither width nor height exceeds [maxDim], preserving aspect ratio.
 */
fun scaleBitmapBounded(bitmap: Bitmap, maxDim: Int = 1280): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= maxDim && h <= maxDim) return bitmap
    val ratio = maxDim.toFloat() / max(w, h).toFloat()
    val newW = (w * ratio).roundToInt().coerceAtLeast(1)
    val newH = (h * ratio).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
}

@Composable
fun rememberScreenWidthDp(): Dp = LocalConfiguration.current.screenWidthDp.dp

@Composable
fun isCompactNarrowScreen(): Boolean = LocalConfiguration.current.screenWidthDp < 360

@Composable
fun isExpandedScreen(): Boolean = LocalConfiguration.current.screenWidthDp >= 600
