package com.example.data.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.flow.Flow

@IgnoreExtraProperties
data class PromoBannerConfig(
    val id: String = "",
    val languageCode: String = "bn", // "bn", "hi", "en"
    val orderIndex: Int = 0,
    val isActive: Boolean = true,
    val title: String = "",
    val subtitle: String = "",
    val badgeText: String = "",
    val ctaLabel: String = "",
    val drawableResName: String = "",
    val imageUrl: String = "",
    val targetCardId: String = "",
    val isPoster: Boolean = false,
    val hideTextOverlay: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "promo_banners",
    indices = [Index(value = ["languageCode", "orderIndex"])]
)
data class PromoBannerEntity(
    @PrimaryKey
    val id: String,
    val languageCode: String,
    val orderIndex: Int,
    val isActive: Boolean,
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val ctaLabel: String,
    val drawableResName: String,
    val imageUrl: String,
    val targetCardId: String,
    val isPoster: Boolean,
    val hideTextOverlay: Boolean,
    val updatedAtMillis: Long
) {
    fun toModel(): PromoBannerConfig = PromoBannerConfig(
        id = id,
        languageCode = languageCode,
        orderIndex = orderIndex,
        isActive = isActive,
        title = title,
        subtitle = subtitle,
        badgeText = badgeText,
        ctaLabel = ctaLabel,
        drawableResName = drawableResName,
        imageUrl = imageUrl,
        targetCardId = targetCardId,
        isPoster = isPoster,
        hideTextOverlay = hideTextOverlay,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromModel(m: PromoBannerConfig): PromoBannerEntity = PromoBannerEntity(
            id = m.id,
            languageCode = m.languageCode,
            orderIndex = m.orderIndex,
            isActive = m.isActive,
            title = m.title,
            subtitle = m.subtitle,
            badgeText = m.badgeText,
            ctaLabel = m.ctaLabel,
            drawableResName = m.drawableResName,
            imageUrl = m.imageUrl,
            targetCardId = m.targetCardId,
            isPoster = m.isPoster,
            hideTextOverlay = m.hideTextOverlay,
            updatedAtMillis = m.updatedAtMillis
        )
    }
}

@Dao
interface PromoBannerDao {
    @Query("SELECT * FROM promo_banners WHERE languageCode = :languageCode AND isActive = 1 ORDER BY orderIndex ASC")
    fun observeActiveBannersForLanguage(languageCode: String): Flow<List<PromoBannerEntity>>

    @Query("SELECT * FROM promo_banners WHERE languageCode = :languageCode ORDER BY orderIndex ASC")
    fun observeAllBannersForLanguage(languageCode: String): Flow<List<PromoBannerEntity>>

    @Query("SELECT * FROM promo_banners ORDER BY languageCode ASC, orderIndex ASC")
    suspend fun getAllBannersOnce(): List<PromoBannerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBanner(entity: PromoBannerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBanners(entities: List<PromoBannerEntity>)

    @Query("DELETE FROM promo_banners WHERE id = :bannerId")
    suspend fun deleteBannerById(bannerId: String)
}
