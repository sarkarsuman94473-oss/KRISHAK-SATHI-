package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.PromoBannerConfig
import com.example.data.model.PromoBannerEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Manages Language-Specific Home Promotional Banners stored locally in Room and synced
 * with Cloud Firestore (`promo_banners` collection).
 *
 * Strictly isolates Bengali (`bn`), Hindi (`hi`), and English (`en`) banner pools so
 * banners from different languages are never mixed.
 */
class BannerRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val dao = database.promoBannerDao()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w("BannerRepository", "Firestore unavailable: ${e.message}")
            null
        }
    }

    fun observeActiveBanners(language: AppLanguage): Flow<List<PromoBannerConfig>> =
        dao.observeActiveBannersForLanguage(language.code).map { list ->
            list.map { it.toModel() }
        }

    fun observeAllBannersForLanguage(language: AppLanguage): Flow<List<PromoBannerConfig>> =
        dao.observeAllBannersForLanguage(language.code).map { list ->
            list.map { it.toModel() }
        }

    suspend fun ensureSeeded() {
        val existing = dao.getAllBannersOnce().associateBy { it.id }
        val missing = defaultSeedBanners.filter { !existing.containsKey(it.id) }
        if (missing.isNotEmpty()) {
            dao.upsertBanners(missing.map { PromoBannerEntity.fromModel(it) })
        }
    }

    suspend fun pullFromFirestore(): Int {
        val db = firestore ?: return 0
        val user = try { Firebase.auth.currentUser } catch (_: Exception) { null }
        if (user == null) return 0
        return try {
            val snap = db.collection("promo_banners").get().await()
            val remote = snap.documents.mapNotNull { d ->
                val lang = d.getString("languageCode")?.lowercase()?.trim().orEmpty()
                if (lang !in listOf("bn", "hi", "en")) return@mapNotNull null
                PromoBannerConfig(
                    id = d.id,
                    languageCode = lang,
                    orderIndex = (d.getLong("orderIndex") ?: 0L).toInt(),
                    isActive = d.getBoolean("isActive") ?: true,
                    title = d.getString("title") ?: "",
                    subtitle = d.getString("subtitle") ?: "",
                    badgeText = d.getString("badgeText") ?: "",
                    ctaLabel = d.getString("ctaLabel") ?: "",
                    drawableResName = d.getString("drawableResName") ?: "",
                    imageUrl = d.getString("imageUrl") ?: "",
                    targetCardId = d.getString("targetCardId") ?: "",
                    isPoster = d.getBoolean("isPoster") ?: false,
                    hideTextOverlay = d.getBoolean("hideTextOverlay") ?: false,
                    updatedAtMillis = System.currentTimeMillis()
                )
            }
            if (remote.isNotEmpty()) {
                dao.upsertBanners(remote.map { PromoBannerEntity.fromModel(it) })
            }
            remote.size
        } catch (e: Exception) {
            Log.w("BannerRepository", "Pull promo_banners failed: ${e.message}")
            0
        }
    }

    suspend fun saveBanner(banner: PromoBannerConfig): Result<Unit> {
        return try {
            val cleanLang = banner.languageCode.lowercase().trim()
            require(cleanLang in listOf("bn", "hi", "en")) { "Unsupported languageCode: $cleanLang" }
            val cleanId = banner.id.ifBlank { "${cleanLang}_custom_${System.currentTimeMillis()}" }
            val cleaned = banner.copy(
                id = cleanId,
                languageCode = cleanLang,
                title = banner.title.trim().take(160),
                subtitle = banner.subtitle.trim().take(240),
                badgeText = banner.badgeText.trim().take(60),
                ctaLabel = banner.ctaLabel.trim().take(40),
                imageUrl = banner.imageUrl.trim().take(1000),
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertBanner(PromoBannerEntity.fromModel(cleaned))
            pushBannerToFirestore(cleaned)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleBannerActive(banner: PromoBannerConfig, active: Boolean): Result<Unit> =
        saveBanner(banner.copy(isActive = active))

    suspend fun swapBannerOrder(first: PromoBannerConfig, second: PromoBannerConfig): Result<Unit> {
        return try {
            val updatedFirst = first.copy(orderIndex = second.orderIndex, updatedAtMillis = System.currentTimeMillis())
            val updatedSecond = second.copy(orderIndex = first.orderIndex, updatedAtMillis = System.currentTimeMillis())
            dao.upsertBanners(listOf(PromoBannerEntity.fromModel(updatedFirst), PromoBannerEntity.fromModel(updatedSecond)))
            pushBannerToFirestore(updatedFirst)
            pushBannerToFirestore(updatedSecond)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomBanner(bannerId: String): Result<Unit> {
        return try {
            dao.deleteBannerById(bannerId)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("promo_banners")?.document(bannerId)?.delete()?.await()
                }
            } catch (_: Exception) {}
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun pushBannerToFirestore(banner: PromoBannerConfig) {
        try {
            val db = firestore ?: return
            val user = Firebase.auth.currentUser ?: return
            if (user.uid.isBlank()) return
            val payload = hashMapOf(
                "languageCode" to banner.languageCode,
                "orderIndex" to banner.orderIndex,
                "isActive" to banner.isActive,
                "title" to banner.title,
                "subtitle" to banner.subtitle,
                "badgeText" to banner.badgeText,
                "ctaLabel" to banner.ctaLabel,
                "drawableResName" to banner.drawableResName,
                "imageUrl" to banner.imageUrl,
                "targetCardId" to banner.targetCardId,
                "isPoster" to banner.isPoster,
                "hideTextOverlay" to banner.hideTextOverlay,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("promo_banners").document(banner.id).set(payload).await()
        } catch (e: Exception) {
            Log.w("BannerRepository", "Firestore banner sync deferred: ${e.message}")
        }
    }

    companion object {
        val defaultSeedBanners: List<PromoBannerConfig> = listOf(
            // =========================================================================
            // BENGALI (bn) — Exact existing 5 banner slides & order preserved strictly
            // =========================================================================
            PromoBannerConfig(
                id = "bn_main_poster",
                languageCode = "bn",
                orderIndex = 0,
                isActive = true,
                title = "KRISHAK SATHI — কৃষকের পাশে, সবসময়",
                subtitle = "স্মার্ট কৃষির নতুন সঙ্গী | এআই ও আধুনিক কৃষি সেবা",
                badgeText = "⭐ অফিশিয়াল পোস্টার",
                ctaLabel = "খুলুন",
                drawableResName = "img_promo_poster",
                targetCardId = "",
                isPoster = true,
                hideTextOverlay = false
            ),
            PromoBannerConfig(
                id = "bn_ai_gemini",
                languageCode = "bn",
                orderIndex = 1,
                isActive = true,
                title = "কৃষক Assistant AI — Google Gemini যুক্ত!",
                subtitle = "যে ফসলের প্রশ্ন করবেন, নির্ভুল ও তাৎক্ষণিক সমাধান",
                badgeText = "🤖 AI + Gemini",
                ctaLabel = "খুলুন",
                drawableResName = "img_promo_ai_gemini_1793678721697",
                targetCardId = "ai",
                isPoster = false,
                hideTextOverlay = false
            ),
            PromoBannerConfig(
                id = "bn_disease_detect",
                languageCode = "bn",
                orderIndex = 2,
                isActive = true,
                title = "ফসলের রোগ ও পোকা নির্ণয়",
                subtitle = "ক্যামেরা দিয়ে ছবি তুলুন, ১ ক্লিকেই চিকিৎসা নির্দেশিকা",
                badgeText = "📷 ক্যামেরা নির্ণয়",
                ctaLabel = "খুলুন",
                drawableResName = "img_promo_disease_detect_1793678742155",
                targetCardId = "problem",
                isPoster = false,
                hideTextOverlay = false
            ),
            PromoBannerConfig(
                id = "bn_marketplace",
                languageCode = "bn",
                orderIndex = 3,
                isActive = true,
                title = "কৃষিপণ্য সরাসরি বিক্রি ও ক্রয় করুন",
                subtitle = "সঠিক দামে সরাসরি পাইকারি ও খুচরা বাজারে বিক্রির সুযোগ",
                badgeText = "💰 বাজার ও বিক্রয়",
                ctaLabel = "খুলুন",
                drawableResName = "img_promo_marketplace_1793678763421",
                targetCardId = "market",
                isPoster = false,
                hideTextOverlay = false
            ),
            PromoBannerConfig(
                id = "bn_main_banner",
                languageCode = "bn",
                orderIndex = 4,
                isActive = true,
                title = "কৃষকের উন্নতিতে আমাদের অঙ্গীকার",
                subtitle = "চারা, বীজ, সার, কীটনাশক ও বিশেষজ্ঞ পরামর্শ এক অ্যাপে",
                badgeText = "🌱 স্মার্ট কৃষি",
                ctaLabel = "খুলুন",
                drawableResName = "img_promo_main_banner_1793678699888",
                targetCardId = "crops",
                isPoster = false,
                hideTextOverlay = false
            ),

            // =========================================================================
            // HINDI (hi) — Strictly the 3 uploaded Hindi banner visuals
            // =========================================================================
            PromoBannerConfig(
                id = "hi_banner_1",
                languageCode = "hi",
                orderIndex = 0,
                isActive = true,
                title = "कृषक साथी — स्मार्ट खेती समाधान",
                subtitle = "AI सलाहकार, नर्सरी डायरेक्टरी, कृषि बाजार व मौसम अपडेट",
                badgeText = "🌱 कृषक साथी",
                ctaLabel = "खोलें",
                drawableResName = "img_banner_hi_1",
                targetCardId = "ai",
                isPoster = true,
                hideTextOverlay = true
            ),
            PromoBannerConfig(
                id = "hi_banner_2",
                languageCode = "hi",
                orderIndex = 1,
                isActive = true,
                title = "कृषक साथी — स्मार्ट खेती, समृद्ध किसान",
                subtitle = "सही जानकारी सही समय पर — फसल, खाद, नर्सरी व मंडी भाव",
                badgeText = "🌾 स्मार्ट खेती",
                ctaLabel = "खोलें",
                drawableResName = "img_banner_hi_2",
                targetCardId = "nursery",
                isPoster = true,
                hideTextOverlay = true
            ),
            PromoBannerConfig(
                id = "hi_banner_3",
                languageCode = "hi",
                orderIndex = 2,
                isActive = true,
                title = "कृषक साथी — खेती की हर समस्या का एक समाधान",
                subtitle = "AI फसल सलाह, नर्सरी, कृषि यंत्र व सेवाएं एवं विशेषज्ञ मदद",
                badgeText = "🚜 कृषि सेवाएं",
                ctaLabel = "खोलें",
                drawableResName = "img_banner_hi_3",
                targetCardId = "machinery",
                isPoster = true,
                hideTextOverlay = true
            ),

            // =========================================================================
            // ENGLISH (en) — Pure English banner configuration (no BN/HI artwork mixed)
            // =========================================================================
            PromoBannerConfig(
                id = "en_banner_ai",
                languageCode = "en",
                orderIndex = 0,
                isActive = true,
                title = "KRISHAK SATHI — Smart AI Crop Companion",
                subtitle = "Instant crop diagnosis, CIBRC safe dosage & regional weather alerts",
                badgeText = "✨ Gemini Crop AI",
                ctaLabel = "Ask AI",
                drawableResName = "",
                targetCardId = "ai",
                isPoster = false,
                hideTextOverlay = false
            ),
            PromoBannerConfig(
                id = "en_banner_machinery",
                languageCode = "en",
                orderIndex = 1,
                isActive = true,
                title = "Agricultural Machinery, Rentals & Services",
                subtitle = "Tractors, sprayers, solar pumps, harvesters & Siliguri dealers",
                badgeText = "🚜 Farm Machinery Hub",
                ctaLabel = "Explore",
                drawableResName = "",
                targetCardId = "machinery",
                isPoster = false,
                hideTextOverlay = false
            ),
            PromoBannerConfig(
                id = "en_banner_nursery",
                languageCode = "en",
                orderIndex = 2,
                isActive = true,
                title = "Plant Kingdom & 22-District Nursery Directory",
                subtitle = "124+ Himalayan, indoor, fruit & medicinal guides + transparent map",
                badgeText = "🌿 Botanical Library",
                ctaLabel = "Browse",
                drawableResName = "",
                targetCardId = "nursery",
                isPoster = false,
                hideTextOverlay = false
            )
        )

        fun resolveDrawableRes(resName: String): Int? = when (resName.trim()) {
            "img_promo_poster" -> R.drawable.img_promo_poster
            "img_promo_ai_gemini_1793678721697" -> R.drawable.img_promo_ai_gemini_1793678721697
            "img_promo_disease_detect_1793678742155" -> R.drawable.img_promo_disease_detect_1793678742155
            "img_promo_marketplace_1793678763421" -> R.drawable.img_promo_marketplace_1793678763421
            "img_promo_main_banner_1793678699888" -> R.drawable.img_promo_main_banner_1793678699888
            "img_banner_hi_1" -> R.drawable.img_banner_hi_1
            "img_banner_hi_2" -> R.drawable.img_banner_hi_2
            "img_banner_hi_3" -> R.drawable.img_banner_hi_3
            else -> null
        }
    }
}
