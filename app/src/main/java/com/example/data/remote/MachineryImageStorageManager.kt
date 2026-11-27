package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

data class UploadedEquipmentPhoto(
    val storagePath: String,
    val downloadUrl: String,
    val byteSize: Int
)

/**
 * Handles client-side lossless-aspect compression and Firebase Storage uploads for
 * seller/admin machinery photos (`machinery_images/{machineId}/{photoId}.jpg`).
 *
 * Only performs network uploads when called interactively by an authenticated user
 * with a non-blank UID; never runs during automated tests or offline reference seeding.
 */
object MachineryImageStorageManager {

    private const val MAX_EDGE_PX = 1280
    private const val JPEG_QUALITY = 85

    suspend fun compressUriToJpeg(
        context: Context,
        uri: Uri,
        maxEdgePx: Int = MAX_EDGE_PX,
        quality: Int = JPEG_QUALITY
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            val origW = boundsOpts.outWidth
            val origH = boundsOpts.outHeight
            require(origW > 0 && origH > 0) { "Invalid image stream" }

            var sampleSize = 1
            val longest = maxOf(origW, origH)
            while (longest / sampleSize > maxEdgePx * 2) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: error("Could not decode selected photo")

            val scaled = if (maxOf(decoded.width, decoded.height) > maxEdgePx) {
                val ratio = maxEdgePx.toFloat() / maxOf(decoded.width, decoded.height).toFloat()
                val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
                    if (it !== decoded) decoded.recycle()
                }
            } else {
                decoded
            }

            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            scaled.recycle()
            Result.success(out.toByteArray())
        } catch (e: Exception) {
            Log.w("MachineryStorage", "Image compression failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun uploadEquipmentPhoto(
        context: Context,
        machineId: String,
        uri: Uri
    ): Result<UploadedEquipmentPhoto> {
        val currentUser = try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        }
        if (currentUser == null || currentUser.uid.isBlank()) {
            return Result.failure(IllegalStateException("Google Sign-In required before uploading to Firebase Storage"))
        }

        val compressed = compressUriToJpeg(context, uri).getOrElse {
            return Result.failure(it)
        }

        return try {
            val cleanMachineId = machineId.ifBlank { "draft_${currentUser.uid}" }
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val storagePath = "machinery_images/$cleanMachineId/${UUID.randomUUID()}.jpg"
            val ref = Firebase.storage.reference.child(storagePath)
            ref.putBytes(compressed).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(
                UploadedEquipmentPhoto(
                    storagePath = storagePath,
                    downloadUrl = url,
                    byteSize = compressed.size
                )
            )
        } catch (e: Exception) {
            Log.w("MachineryStorage", "Firebase Storage upload deferred/failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun uploadNurseryProductPhoto(
        context: Context,
        sellerUserId: String,
        uri: Uri
    ): Result<UploadedEquipmentPhoto> {
        val currentUser = try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        }
        if (currentUser == null || currentUser.uid.isBlank()) {
            return Result.failure(IllegalStateException("Google Sign-In required before uploading nursery photos to cloud storage"))
        }

        val compressed = compressUriToJpeg(context, uri).getOrElse {
            return Result.failure(it)
        }

        return try {
            val cleanSellerId = sellerUserId.ifBlank { currentUser.uid }
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val storagePath = "nursery_images/$cleanSellerId/${UUID.randomUUID()}.jpg"
            val ref = Firebase.storage.reference.child(storagePath)
            ref.putBytes(compressed).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(
                UploadedEquipmentPhoto(
                    storagePath = storagePath,
                    downloadUrl = url,
                    byteSize = compressed.size
                )
            )
        } catch (e: Exception) {
            Log.w("MachineryStorage", "Nursery Firebase Storage upload failed: ${e.message}")
            Result.failure(e)
        }
    }
}
