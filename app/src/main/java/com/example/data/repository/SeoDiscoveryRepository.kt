package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SeoArticleEntity
import com.example.data.local.entity.SeoConfigEntity
import com.example.data.local.entity.SeoReferralLogEntity
import com.example.data.model.AppLanguage
import com.example.data.model.BrandCapabilityRow
import com.example.data.model.FeatureAvailabilityStatus
import com.example.data.model.SeoArticleStatus
import com.example.data.model.SeoContentCategory
import com.example.data.model.SeoReferralSummary
import com.example.data.model.SeoTechnicalAuditResult
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val SEO_REPO_TAG = "SeoDiscoveryRepository"

class SeoDiscoveryRepository(
    context: Context,
    private val db: AppDatabase = AppDatabase.getInstance(context)
) {
    private val appContext = context.applicationContext
    private val dao = db.seoDiscoveryDao()

    companion object {
        const val LIVE_WEB_URL = "https://ais-pre-2x4wg44pdpamhvdlijirfv-948907126237.asia-southeast1.run.app"
        const val TAGLINE = "KRISHAK SATHI — Growing Together, Digitally."
        const val PLAY_STORE_STATUS = "Coming Soon"
        const val DEFAULT_LAST_UPDATED = "2026-11-18"
        private val SAFE_SLUG_REGEX = Regex("^[a-z0-9\\-_/]+$")

        fun sanitizeSlug(raw: String): String {
            return raw.trim()
                .lowercase()
                .replace(" ", "-")
                .replace(Regex("[^a-z0-9\\-_/]"), "")
                .trimStart('/')
                .ifBlank { "agri-guide" }
        }
    }

    private fun getFirestoreOrNull(): FirebaseFirestore? {
        return try {
            val dbId = appContext.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) FirebaseFirestore.getInstance(dbId) else null
        } catch (_: Exception) {
            null
        }
    }

    fun observePublishedArticles(): Flow<List<SeoArticleEntity>> = dao.observePublishedArticles()

    fun observeAllArticles(): Flow<List<SeoArticleEntity>> = dao.observeAllArticles()

    fun observeConfig(): Flow<SeoConfigEntity> = dao.observeConfig().map { it ?: defaultConfig() }

    fun observeReferralSummary(): Flow<SeoReferralSummary> = dao.observeReferralLogs().map { logs ->
        summarizeReferrals(logs)
    }

    fun defaultConfig(): SeoConfigEntity = SeoConfigEntity(
        id = "default",
        baseWebUrl = LIVE_WEB_URL,
        googleSearchConsoleToken = "",
        allowGooglebot = true,
        allowOaiSearchBot = true,
        allowGptBot = true,
        playStoreStatus = PLAY_STORE_STATUS,
        updatedByAdmin = "system",
        updatedAtMillis = System.currentTimeMillis()
    )

    suspend fun ensureSeeded() {
        if (dao.getConfigOnce() == null) {
            dao.upsertConfig(defaultConfig())
        }
        if (dao.countArticles() == 0) {
            dao.upsertArticles(defaultKnowledgeSeedArticles())
        }
    }

    fun brandCapabilities(): List<BrandCapabilityRow> = listOf(
        BrandCapabilityRow(
            nameEn = "Crop Master Library & Growth Stages",
            nameBn = "ফসল মাস্টার লাইব্রেরি ও চাষের ধাপ",
            nameHi = "फसल मास्टर लाइब्रेरी व खेती के चरण",
            status = FeatureAvailabilityStatus.LIVE,
            detailEn = "8+ flagship and 50+ regional crops with pests, nutrition & EN/BN/HI support.",
            detailBn = "বাংলা, হিন্দি ও ইংরেজিতে সম্পূর্ণ চাষ পদ্ধতি, রোগ ও পুষ্টি গাইড।",
            detailHi = "हिंदी, बांग्ला व अंग्रेज़ी में संपूर्ण खेती, रोग व पोषण जानकारी।"
        ),
        BrandCapabilityRow(
            nameEn = "KRISHAK AI Dual-Provider Assistant & Image Check",
            nameBn = "KRISHAK AI দ্বৈত-প্রোভাইডার ও রোগ শনাক্তকরণ",
            nameHi = "KRISHAK AI असिस्टेंट व फसल समस्या जांच",
            status = FeatureAvailabilityStatus.LIVE,
            detailEn = "Firebase AI Logic + curated ICAR/BCKV/UBKV/TRA citations and safe IPM guardrails.",
            detailBn = "গবেষণা-ভিত্তিক নির্ভরযোগ্য তথ্যসূত্র ও নিরাপদ IPM গাইডলাইনসহ AI সহায়তা।",
            detailHi = "ICAR/BCKV/UBKV संदर्भों और सुरक्षित IPM नियमों के साथ स्मार्ट AI सलाह।"
        ),
        BrandCapabilityRow(
            nameEn = "Government Farmer Schemes (Room Offline Cache)",
            nameBn = "সরকারি কৃষক প্রকল্প (অফলাইন ক্যাশসহ)",
            nameHi = "सरकारी किसान योजनाएं (ऑफ़लाइन कैश सहित)",
            status = FeatureAvailabilityStatus.LIVE,
            detailEn = "PM-KISAN, Krishak Bandhu, Bangla Shasya Bima & official portals stored offline.",
            detailBn = "কৃষকবন্ধু, পিএম-কিষাণ ও বাংলা শস্য বিমা তথ্য ইন্টারনেট ছাড়াই পড়া যায়।",
            detailHi = "पीएम-किसान, कृषक बंधु व फसल बीमा जानकारी कम नेटवर्क में भी उपलब्ध।"
        ),
        BrandCapabilityRow(
            nameEn = "Public Knowledge Hub, Crop SEO & Technical Markup",
            nameBn = "পাবলিক কৃষি জ্ঞান ভাণ্ডার ও SEO ওয়েব হাব",
            nameHi = "सार्वजनिक कृषि ज्ञान केंद्र एवं SEO वेब हब",
            status = FeatureAvailabilityStatus.LIVE,
            detailEn = "Multilingual EN/BN/HI guides, JSON-LD, hreflang, sitemap.xml & robots.txt.",
            detailBn = "বাংলা, হিন্দি ও ইংরেজি পাবলিক গাইড, JSON-LD ও সার্চ ইঞ্জিন সাইটম্যাপ।",
            detailHi = "बहुभाषी वेब गाइड, JSON-LD संरचना और खोज इंजन साइटमैप।"
        ),
        BrandCapabilityRow(
            nameEn = "Nursery Onboarding & Regional Soil Lab Sync",
            nameBn = "নার্সারি অংশীদারিত্ব ও আঞ্চলিক মাটি পরীক্ষাগার সংযোগ",
            nameHi = "नर्सरी पार्टनरशिप व क्षेत्रीय मिट्टी जांच सिंक",
            status = FeatureAvailabilityStatus.UNDER_DEVELOPMENT,
            detailEn = "Expanding North Bengal / Siliguri verified nursery and Soil Health Card workflows.",
            detailBn = "শিলিগুড়ি ও উত্তরবঙ্গের নার্সারি ও মাটি পরীক্ষা কেন্দ্র যুক্ত করার কাজ চলছে।",
            detailHi = "सिलीगुड़ी व उत्तर बंगाल नर्सरी नेटवर्क और मृदा कार्ड विस्तार प्रगति पर है।"
        ),
        BrandCapabilityRow(
            nameEn = "Google Play Store Public Android Listing",
            nameBn = "Google Play Store-এ পাবলিক অ্যাপ প্রকাশনা",
            nameHi = "Google Play Store सार्वजनिक रिलीज़",
            status = FeatureAvailabilityStatus.PLANNED,
            detailEn = "Coming Soon — Play Store link will be published strictly after store approval.",
            detailBn = "Coming Soon — চূড়ান্ত অনুমোদনের পরেই Play Store লিংক প্রকাশিত হবে।",
            detailHi = "Coming Soon — प्ले स्टोर स्वीकृति के बाद ही आधिकारिक लिंक जारी होगा।"
        )
    )

    suspend fun saveArticle(article: SeoArticleEntity, adminId: String): Result<SeoArticleEntity> {
        val cleanSlug = sanitizeSlug(article.slug)
        if (!SAFE_SLUG_REGEX.matches(cleanSlug)) {
            return Result.failure(IllegalArgumentException("Slug must contain only lowercase letters, digits, hyphens, or slashes."))
        }
        val cleanSourceUrl = article.sourceUrl.trim()
        if (!cleanSourceUrl.startsWith("https://", ignoreCase = true) || cleanSourceUrl.length < 12) {
            return Result.failure(IllegalArgumentException("Verified source URL must be a valid https:// institutional link."))
        }
        val baseUrl = (dao.getConfigOnce()?.baseWebUrl ?: LIVE_WEB_URL).trimEnd('/')
        val cleanCanonical = article.canonicalUrl.trim().ifBlank { "$baseUrl/knowledge/$cleanSlug.html" }
        val normalized = article.copy(
            slug = cleanSlug,
            sourceUrl = cleanSourceUrl,
            canonicalUrl = cleanCanonical,
            lastUpdatedDate = article.lastUpdatedDate.ifBlank { DEFAULT_LAST_UPDATED },
            reviewedByAdmin = adminId.ifBlank { article.reviewedByAdmin },
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertArticle(normalized)
        syncArticleToFirestoreBestEffort(normalized)
        return Result.success(normalized)
    }

    /**
     * AI-assisted draft creation NEVER auto-publishes; forces AI_DRAFT_PENDING_REVIEW
     * until an admin explicitly calls [approveAndPublishDraft].
     */
    suspend fun createAiAssistedDraft(
        titleEn: String,
        titleBn: String,
        titleHi: String,
        slug: String,
        category: SeoContentCategory,
        cropId: String,
        summaryEn: String,
        summaryBn: String,
        summaryHi: String,
        bodyEn: String,
        bodyBn: String,
        bodyHi: String,
        sourceName: String,
        sourceUrl: String
    ): Result<SeoArticleEntity> {
        val baseUrl = (dao.getConfigOnce()?.baseWebUrl ?: LIVE_WEB_URL).trimEnd('/')
        val cleanSlug = sanitizeSlug(slug)
        val draft = SeoArticleEntity(
            id = "seo_${UUID.randomUUID().toString().take(12)}",
            slug = cleanSlug,
            category = category.wireValue,
            cropId = cropId.trim().lowercase(),
            status = SeoArticleStatus.AI_DRAFT_PENDING_REVIEW.wireValue,
            titleEn = titleEn.trim(),
            titleBn = titleBn.trim().ifBlank { titleEn.trim() },
            titleHi = titleHi.trim().ifBlank { titleEn.trim() },
            metaDescriptionEn = summaryEn.trim().take(158),
            metaDescriptionBn = summaryBn.trim().ifBlank { summaryEn.trim() }.take(158),
            metaDescriptionHi = summaryHi.trim().ifBlank { summaryEn.trim() }.take(158),
            summaryEn = summaryEn.trim(),
            summaryBn = summaryBn.trim().ifBlank { summaryEn.trim() },
            summaryHi = summaryHi.trim().ifBlank { summaryEn.trim() },
            bodyEn = bodyEn.trim(),
            bodyBn = bodyBn.trim().ifBlank { bodyEn.trim() },
            bodyHi = bodyHi.trim().ifBlank { bodyEn.trim() },
            faqsSerialized = "",
            sourceName = sourceName.trim(),
            sourceUrl = sourceUrl.trim(),
            canonicalUrl = "$baseUrl/knowledge/$cleanSlug.html",
            lastUpdatedDate = DEFAULT_LAST_UPDATED,
            isAiAssisted = true,
            reviewedByAdmin = "",
            updatedAtMillis = System.currentTimeMillis()
        )
        return saveArticle(draft, adminId = "")
    }

    suspend fun approveAndPublishDraft(articleId: String, adminIdentifier: String): Result<SeoArticleEntity> {
        val existing = dao.getArticleById(articleId)
            ?: return Result.failure(IllegalArgumentException("Article not found."))
        if (adminIdentifier.isBlank()) {
            return Result.failure(IllegalStateException("Admin reviewer identity is required to publish."))
        }
        val updated = existing.copy(
            status = SeoArticleStatus.PUBLISHED.wireValue,
            reviewedByAdmin = adminIdentifier.trim(),
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertArticle(updated)
        syncArticleToFirestoreBestEffort(updated)
        return Result.success(updated)
    }

    suspend fun saveSeoConfig(config: SeoConfigEntity, adminIdentifier: String): Result<SeoConfigEntity> {
        val cleanBase = config.baseWebUrl.trim().trimEnd('/')
        if (!cleanBase.startsWith("https://", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Base web URL must start with https://"))
        }
        val clean = config.copy(
            id = "default",
            baseWebUrl = cleanBase,
            googleSearchConsoleToken = config.googleSearchConsoleToken.trim().take(128),
            playStoreStatus = PLAY_STORE_STATUS,
            updatedByAdmin = adminIdentifier.ifBlank { "admin" },
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertConfig(clean)
        syncConfigToFirestoreBestEffort(clean)
        return Result.success(clean)
    }

    suspend fun recordReferral(sourceBucket: String, targetSlug: String, language: AppLanguage) {
        // Strictly non-PII: only aggregate bucket + slug + language code
        val safeBucket = sourceBucket.trim().lowercase().replace(Regex("[^a-z0-9_\\-]"), "").ifBlank { "in_app" }
        val safeSlug = sanitizeSlug(targetSlug)
        val entry = SeoReferralLogEntity(
            id = "ref_${UUID.randomUUID().toString().take(14)}",
            sourceBucket = safeBucket,
            targetSlug = safeSlug,
            languageCode = language.code,
            timestampMillis = System.currentTimeMillis()
        )
        dao.insertReferralLog(entry)
    }

    fun summarizeReferrals(logs: List<SeoReferralLogEntity>): SeoReferralSummary {
        val bucketCounts = logs.groupingBy { it.sourceBucket }.eachCount()
        val topSlugs = logs.groupingBy { it.targetSlug }.eachCount()
            .entries.sortedByDescending { it.value }.take(6).map { it.key to it.value }
        return SeoReferralSummary(
            totalVisits = logs.size,
            countsByBucket = bucketCounts,
            topSlugs = topSlugs
        )
    }

    fun generateRobotsTxt(config: SeoConfigEntity): String {
        val base = config.baseWebUrl.trimEnd('/')
        return buildString {
            appendLine("# KRISHAK SATHI Public Agricultural Knowledge Hub & Crop Guides")
            if (config.allowGooglebot) {
                appendLine("User-agent: Googlebot")
                appendLine("Allow: /")
                appendLine("Allow: /bn/")
                appendLine("Allow: /hi/")
                appendLine("Allow: /brand/")
                appendLine("Allow: /knowledge/")
                appendLine("Allow: /crops/")
                appendLine("Allow: /sitemap.xml")
            } else {
                appendLine("User-agent: Googlebot")
            }
            appendProtectedDisallows(this)
            appendLine()
            if (config.allowOaiSearchBot) {
                appendLine("User-agent: OAI-SearchBot")
                appendLine("Allow: /")
                appendLine("Allow: /bn/")
                appendLine("Allow: /hi/")
                appendLine("Allow: /brand/")
                appendLine("Allow: /knowledge/")
                appendLine("Allow: /crops/")
                appendLine("Allow: /sitemap.xml")
            } else {
                appendLine("User-agent: OAI-SearchBot")
            }
            appendProtectedDisallows(this)
            appendLine()
            appendLine("# Separate GPTBot directive (kept independent from OAI-SearchBot)")
            appendLine("User-agent: GPTBot")
            if (config.allowGptBot) {
                appendLine("Allow: /knowledge/")
                appendLine("Allow: /crops/")
            } else {
                appendLine("Disallow: /")
            }
            appendProtectedDisallows(this)
            appendLine()
            appendLine("User-agent: *")
            appendLine("Allow: /")
            appendProtectedDisallows(this)
            appendLine()
            appendLine("Sitemap: $base/sitemap.xml")
        }
    }

    private fun appendProtectedDisallows(sb: StringBuilder) {
        sb.appendLine("Disallow: /admin")
        sb.appendLine("Disallow: /admin/")
        sb.appendLine("Disallow: /api/")
        sb.appendLine("Disallow: /auth/")
        sb.appendLine("Disallow: /firebase/")
        sb.appendLine("Disallow: /users/")
        sb.appendLine("Disallow: /private/")
    }

    fun generateSitemapXml(config: SeoConfigEntity, articles: List<SeoArticleEntity>): String {
        val base = config.baseWebUrl.trimEnd('/')
        val published = articles.filter { it.statusEnum() == SeoArticleStatus.PUBLISHED }
        val paths = linkedSetOf("/index.html", "/brand/index.html", "/knowledge/index.html")
        published.forEach { art ->
            val pathPart = if (art.cropId.isNotBlank()) "/crops/${art.cropId}.html" else "/knowledge/${art.slug}.html"
            paths.add(pathPart)
        }
        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">""")
            paths.forEach { p ->
                listOf("", "/bn", "/hi").forEach { langSeg ->
                    appendLine("  <url>")
                    appendLine("    <loc>$base$langSeg$p</loc>")
                    appendLine("    <lastmod>${DEFAULT_LAST_UPDATED}</lastmod>")
                    appendLine("    <xhtml:link rel=\"alternate\" hreflang=\"en\" href=\"$base$p\" />")
                    appendLine("    <xhtml:link rel=\"alternate\" hreflang=\"bn\" href=\"$base/bn$p\" />")
                    appendLine("    <xhtml:link rel=\"alternate\" hreflang=\"hi\" href=\"$base/hi$p\" />")
                    appendLine("    <xhtml:link rel=\"alternate\" hreflang=\"x-default\" href=\"$base$p\" />")
                    appendLine("  </url>")
                }
            }
            appendLine("</urlset>")
        }
    }

    fun runTechnicalAudit(config: SeoConfigEntity, articles: List<SeoArticleEntity>): SeoTechnicalAuditResult {
        val robots = generateRobotsTxt(config)
        val googleOk = robots.contains("User-agent: Googlebot\nAllow: /")
        val oaiOk = robots.contains("User-agent: OAI-SearchBot\nAllow: /")
        val gptSeparate = robots.indexOf("User-agent: GPTBot") > 0 &&
            robots.indexOf("User-agent: OAI-SearchBot") != robots.indexOf("User-agent: GPTBot")
        val protectedOk = listOf("/admin", "/api/", "/auth/", "/firebase/", "/users/", "/private/")
            .all { robots.contains("Disallow: $it") }
        val sitemap = generateSitemapXml(config, articles)
        val sitemapOk = sitemap.contains("<urlset") && sitemap.contains("hreflang=\"bn\"") && sitemap.contains("hreflang=\"hi\"")

        val published = articles.filter { it.statusEnum() == SeoArticleStatus.PUBLISHED }
        val pendingDrafts = articles.count { it.statusEnum() == SeoArticleStatus.AI_DRAFT_PENDING_REVIEW }
        val missingCitations = published.count { !it.sourceUrl.startsWith("https://") || it.sourceName.isBlank() }

        val notes = mutableListOf<String>()
        if (config.googleSearchConsoleToken.isBlank()) {
            notes.add("Google Search Console verification token not yet configured. Live Google indexing status requires verifying ownership in Google Search Console externally.")
        } else {
            notes.add("GSC meta verification token configured (${config.googleSearchConsoleToken.take(12)}...). Live crawl status must still be checked directly in Search Console.")
        }
        if (pendingDrafts > 0) {
            notes.add("$pendingDrafts AI-assisted draft(s) await manual admin review before publishing.")
        }
        notes.add("Google Play Store status remains honestly set to '${config.playStoreStatus}'.")

        return SeoTechnicalAuditResult(
            robotsTxtValid = googleOk && oaiOk && gptSeparate && protectedOk,
            googlebotAllowed = googleOk,
            oaiSearchBotAllowed = oaiOk,
            gptBotSeparated = gptSeparate,
            adminAndPrivateDisallowed = protectedOk,
            sitemapValid = sitemapOk,
            publishedCount = published.size,
            pendingReviewDrafts = pendingDrafts,
            missingCitationCount = missingCitations,
            notes = notes
        )
    }

    private suspend fun syncArticleToFirestoreBestEffort(article: SeoArticleEntity) {
        val uid = try { Firebase.auth.currentUser?.uid } catch (_: Exception) { null } ?: return
        val firestore = getFirestoreOrNull() ?: return
        try {
            val payload = hashMapOf(
                "slug" to article.slug,
                "category" to article.category,
                "cropId" to article.cropId,
                "status" to article.status,
                "titleEn" to article.titleEn,
                "titleBn" to article.titleBn,
                "titleHi" to article.titleHi,
                "summaryEn" to article.summaryEn,
                "summaryBn" to article.summaryBn,
                "summaryHi" to article.summaryHi,
                "sourceName" to article.sourceName,
                "sourceUrl" to article.sourceUrl,
                "canonicalUrl" to article.canonicalUrl,
                "lastUpdatedDate" to article.lastUpdatedDate,
                "isAiAssisted" to article.isAiAssisted,
                "reviewedBy" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("seo_articles").document(article.id)
                .set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(SEO_REPO_TAG, "Best-effort Firestore SEO article sync skipped/failed: ${e.message}")
        }
    }

    private suspend fun syncConfigToFirestoreBestEffort(config: SeoConfigEntity) {
        val uid = try { Firebase.auth.currentUser?.uid } catch (_: Exception) { null } ?: return
        val firestore = getFirestoreOrNull() ?: return
        try {
            val payload = hashMapOf(
                "baseWebUrl" to config.baseWebUrl,
                "googleSearchConsoleToken" to config.googleSearchConsoleToken,
                "allowGooglebot" to config.allowGooglebot,
                "allowOaiSearchBot" to config.allowOaiSearchBot,
                "allowGptBot" to config.allowGptBot,
                "playStoreStatus" to config.playStoreStatus,
                "updatedBy" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("seo_config").document("default")
                .set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(SEO_REPO_TAG, "Best-effort Firestore SEO config sync skipped/failed: ${e.message}")
        }
    }

    private fun defaultKnowledgeSeedArticles(): List<SeoArticleEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            SeoArticleEntity(
                id = "hub_crop_rice",
                slug = "rice",
                category = SeoContentCategory.CROP_GUIDE.wireValue,
                cropId = "rice",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Rice (Paddy) Cultivation, BPH & Blast IPM Guide",
                titleBn = "ধান চাষ, বাদামি শোষক পোকা ও ব্লাস্ট রোগ দমন গাইড",
                titleHi = "धान की खेती, भूरा फुदका व ब्लास्ट प्रबंधन गाइड",
                metaDescriptionEn = "ICAR & BCKV grounded rice nursery, SRI spacing, Leaf Color Chart nitrogen and BPH scouting guide.",
                metaDescriptionBn = "আমন ও বোরো ধানের বীজ শোধন, চারা রোপণ, বাদামি শোষক পোকা ও ব্লাস্ট রোগ প্রতিরোধ গাইড।",
                metaDescriptionHi = "खरीफ व बोरो धान नर्सरी, रोपाई दूरी, भूरा फुदका पहचान और आईसीएआर आधारित सुरक्षा।",
                summaryEn = "Transplant 21–25 day treated seedlings, apply Leaf Color Chart split nitrogen, and scout hill bases early for BPH.",
                summaryBn = "২১–২৫ দিনের সুস্থ চারা রোপণ, লিফ কালার চার্ট দেখে সার ও বাদামি শোষক পোকার গোড়া পরীক্ষা করুন।",
                summaryHi = "21–25 दिन की उपचारित पौध रोपें, लीफ कलर चार्ट से यूरिया दें और तने के नीचे भूरा फुदका जांचें।",
                bodyEn = "Select certified treated varieties (Swarna Sub-1, Shatabdi, Gobindobhog). Maintain alternate wetting and drying (AWD) in vegetative phase and 2–5 cm water during panicle initiation. Conserve natural spiders and mirid bugs; avoid excessive urea that invites Brown Planthopper and Sheath Blight. Always confirm exact CIBRC dosages on product labels before spraying.",
                bodyBn = "সার্টিফায়েড শোধন করা বীজ (স্বর্ণ, শতাব্দী, গোবিন্দভোগ) নির্বাচন করুন। কুশি ছাড়ার সময় পর্যাপ্ত জল-বাতাস চলাচল রাখুন ও শীষ আসার সময় ২–৫ সেমি জল রাখুন। অতিরিক্ত ইউরিয়া দিলে বাদামি শোষক ও খোলপচা বাড়ে। যেকোনো স্প্রে করার আগে CIBRC লেবেল ডোজ ও অপেক্ষাকাল দেখুন।",
                bodyHi = "प्रमाणित उपचारित बीज चुनें। कल्ले निकलते समय नियंत्रित पानी रखें और बाली निकलने पर 2–5 सेमी पानी बनाए रखें। अधिक यूरिया से भूरा फुदका व शीथ ब्लाइट बढ़ता है। किसी भी छिड़काव से पहले CIBRC लेबल खुराक की जांच करें।",
                faqsSerialized = "What is the best seedling age for Kharif paddy?::21 to 25 days for regular transplanting; 10 to 14 days for SRI.||Why do lower older paddy leaves turn uniformly yellow?::Uniform yellowing on older lower leaves typically indicates Nitrogen deficiency.",
                sourceName = "ICAR - National Rice Research Institute (NRRI) & BCKV",
                sourceUrl = "https://icar-nrri.in/",
                canonicalUrl = "$LIVE_WEB_URL/crops/rice.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now
            ),
            SeoArticleEntity(
                id = "hub_crop_potato",
                slug = "potato",
                category = SeoContentCategory.CROP_GUIDE.wireValue,
                cropId = "potato",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Potato Cultivation, Ridge Earthing-Up & Late Blight Care",
                titleBn = "আলু চাষ, মাটি ধরানো ও নাবী ধসা (Late Blight) প্রতিরোধ",
                titleHi = "आलू की खेती, मिट्टी चढ़ाना व पिछेती झुलसा बचाव",
                metaDescriptionEn = "CPRI & UBKV Rabi potato seed tuber care, ridge earthing-up at 25-30 days, and foggy Late Blight protection.",
                metaDescriptionBn = "পশ্চিমবঙ্গ ও উত্তরবঙ্গে আলুর বীজ শোধন, ২৫–৩০ দিনে মাটি ধরানো এবং কুয়াশায় নাবী ধসা নিয়ন্ত্রণ।",
                metaDescriptionHi = "रबी आलू बीज कंद उपचार, 25–30 दिन पर मिट्टी चढ़ाना व कोहरे में पिछेती झुलसा रोकथाम।",
                summaryEn = "Complete ridge earthing-up by 25–30 days to prevent green tubers and monitor foggy spells early for Late Blight.",
                summaryBn = "২৫–৩০ দিনে মাটি ধরিয়ে কন্দ সবুজ হওয়া রোধ করুন এবং কুয়াশায় নাবী ধসার জন্য নিয়মিত জমি পরিদর্শন করুন।",
                summaryHi = "25–30 दिन में मिट्टी चढ़ाकर कंद हरे होने से बचाएं और कोहरे में पिछेती झुलसा की नियमित जांच करें।",
                bodyEn = "Plant certified 35–45g seed tubers on 60 cm ridges at 20 cm spacing. Earth up ridges firmly 25–30 days after planting. Stop irrigation 10–12 days prior to harvest so skins cure against storage bruising. During cool overcast fog (10–16°C, >85% RH), inspect lower leaves for water-soaked Late Blight spots and consult KVK/BCKV or CIBRC labels for exact fungicide selection.",
                bodyBn = "৬০ সেমি দূরত্বে ভেলিতে ৩৫–৪৫ গ্রাম সুস্থ বীজ আলু বসান। রোপণের ২৫–৩০ দিনে মাটি ধরিয়ে দিন। আলু তোলার ১০–১২ দিন আগে সেচ বন্ধ করে খোসা শক্ত করুন। মেঘলা ও কুয়াশাচ্ছন্ন আবহাওয়ায় পাতার নিচে সাদা ছত্রাক দেখা গেলে দ্রুত KVK বা কৃষি দপ্তরের নির্দেশিত সুরক্ষা ব্যবস্থা নিন।",
                bodyHi = "60 सेमी मेड़ों पर 35–45 ग्राम स्वस्थ कंद लगाएं और 25–30 दिन में मिट्टी चढ़ाएं। खुदाई से 10–12 दिन पहले सिंचाई बंद करें। घने कोहरे में निचली पत्तियों पर काले धब्बे दिखने पर तुरंत कृषि विशेषज्ञ व CIBRC अनुमोदित उपाय अपनाएं।",
                faqsSerialized = "Why do harvested potatoes turn green?::Exposure to sunlight on eroded ridges triggers solanine greening; timely earthing-up stops this.||When should irrigation stop before digging potatoes?::10 to 12 days prior to harvest to cure skins.",
                sourceName = "ICAR - Central Potato Research Institute (CPRI)",
                sourceUrl = "https://cpri.icar.gov.in/",
                canonicalUrl = "$LIVE_WEB_URL/crops/potato.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 1000
            ),
            SeoArticleEntity(
                id = "hub_crop_tea",
                slug = "tea",
                category = SeoContentCategory.SILIGURI_NORTH_BENGAL.wireValue,
                cropId = "tea",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "North Bengal Small Tea Garden Management & TRA PPC Care",
                titleBn = "উত্তরবঙ্গ চা বাগান পরিচর্যা, ড্রেনেজ ও টি বোর্ড PPC সুরক্ষা",
                titleHi = "उत्तर बंगाल चाय बागान प्रबंधन व टी बोर्ड PPC गाइड",
                metaDescriptionEn = "Acidic pH 4.5-5.5 maintenance, Albizia shade canopy, Red Spider Mite & Tea Mosquito Bug control under TRA PPC.",
                metaDescriptionBn = "ডুয়ার্স ও তরাই চা বাগানের মাটির অম্লতা, ছায়া গাছ, লাল মাকড় ও টি মস্কিটো বাগ নিয়ন্ত্রণ।",
                metaDescriptionHi = "तराई व डुआर्स चाय बागानों में अम्लीय मिट्टी, छाया, लाल मकड़ी व टी मॉस्किटो बग प्रबंधन।",
                summaryEn = "Keep pH 4.5–5.5 with clean monsoon drains, shade trees, 7–10 day plucking cycles, and strict Tea Board PPC compliance.",
                summaryBn = "মাটির pH ৪.৫–৫.৫ ও নিকাশী নালা পরিষ্কার রাখুন এবং শুধুমাত্র টি বোর্ডের PPC অনুমোদিত সুরক্ষা মানুন।",
                summaryHi = "pH 4.5–5.5 व जल निकासी बनाए रखें और केवल टी बोर्ड PPC अनुमोदित सुरक्षा मानकों का पालन करें।",
                bodyEn = "Dooars and Terai small tea growers depend on acidic pH 4.5–5.5 and zero waterlogging. Establish Albizia shade trees to moderate heat and dust-driven Red Spider Mite flare-ups. Follow 'two leaves and a bud' every 7–10 days and consult Tea Board Plant Protection Code (PPC) before applying any acaricide or pesticide.",
                bodyBn = "তরাই ও ডুয়ার্সের ক্ষুদ্র চা বাগানে মাটির pH ৪.৫–৫.৫ ও সুষ্ঠু নিকাশী অত্যন্ত জরুরি। অ্যালবিজিয়া ছায়া গাছ লাল মাকড়ের প্রাদুর্ভাব কমায়। প্রতি ৭–১০ দিনে দুটি পাতা ও একটি কুঁড়ি তুলুন এবং টি বোর্ডের PPC তালিকা মেনেই স্প্রে করুন।",
                bodyHi = "चाय की गुणवत्ता के लिए pH 4.5–5.5, साफ नालियां और संतुलित छाया वृक्ष आवश्यक हैं। हर 7–10 दिन में दो पत्ती एक कली तोड़ें और केवल Tea Board PPC प्रमाणित उपचार चुनें।",
                faqsSerialized = "What is the best soil pH for tea?::Acidic well-drained soil with pH 4.5 to 5.5.||Where do tea growers verify allowed pesticides?::Tea Board of India Plant Protection Code (PPC) and CIBRC.",
                sourceName = "Tea Research Association (Tocklai / TRA) & Tea Board of India",
                sourceUrl = "https://www.tocklai.org/",
                canonicalUrl = "$LIVE_WEB_URL/crops/tea.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 2000
            ),
            SeoArticleEntity(
                id = "hub_crop_tomato",
                slug = "tomato",
                category = SeoContentCategory.DISEASE_PEST.wireValue,
                cropId = "tomato",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Tomato Leaf Curl Prevention, Staking & Fruit Borer IPM",
                titleBn = "টমেটো পাতা কোঁকড়ানো ভাইরাস, খুঁটি ও ফল ছিদ্রকারী দমন",
                titleHi = "टमाटर पत्ती मरोड़ वायरस, स्टेकिंग व फल छेदक प्रबंधन",
                metaDescriptionEn = "IIHR tomato raised-bed culture, 50-mesh net nursery for Whitefly, Marigold trap crop, and Blossom End Rot calcium care.",
                metaDescriptionBn = "নেট হাউস চারা তৈরি, গাঁদা ফাঁদ ফসল ও টমেটোর নিচের পচন (Blossom End Rot) রোধের পদ্ধতি।",
                metaDescriptionHi = "इंसेक्ट-नेट नर्सरी, गेंदा फंदा फसल और टमाटर के निचले सड़न की रोकथाम।",
                summaryEn = "Use 50-mesh netted nursery for Whitefly exclusion, plant border marigold trap rows, and maintain even soil moisture.",
                summaryBn = "৫০-মেশ নেটে সুস্থ চারা তৈরি করুন, প্রতি ১৫ সারিতে ১ সারি গাঁদা লাগান এবং নিয়মিত সেচ বজায় রাখুন।",
                summaryHi = "सफेद मक्खी रोकने हेतु नेट नर्सरी, गेंदा फंदा फसल व नियमित सिंचाई से उच्च उत्पादन लें।",
                bodyEn = "Raise seedlings inside 50-mesh insect nets to block Whitefly carrying Tomato Leaf Curl Virus. Stake plants on raised mulch beds, border plots with marigold trap rows against Fruit Borer, and maintain uniform moisture + calcium to avoid Blossom End Rot.",
                bodyBn = "সাদা মাছি রুখতে নেট হাউসে চারা তৈরি করুন। উঁচু বেডে খুঁটি দিয়ে গাছ বাঁধুন ও প্রতি ১৫ সারি টমেটোতে ১ সারি গাঁদা ফুল ফাঁদ ফসল হিসেবে লাগান।",
                bodyHi = "सफेद मक्खी से बचाव हेतु कीट-रोधी नेट में पौध उगाएं। बांस सहारे के साथ उठी मेड़ों पर रोपाई करें और हर 15 पंक्ति में एक पंक्ति गेंदा लगाएं।",
                faqsSerialized = "Why plant marigold near tomato?::African marigold lures Fruit Borer moths away from tomato fruits.||What causes black sunken rot at the bottom of tomatoes?::Calcium imbalance and irregular watering (Blossom End Rot).",
                sourceName = "ICAR - Indian Institute of Horticultural Research (IIHR)",
                sourceUrl = "https://www.iihr.res.in/",
                canonicalUrl = "$LIVE_WEB_URL/crops/tomato.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 3000
            ),
            SeoArticleEntity(
                id = "hub_crop_cabbage",
                slug = "cabbage",
                category = SeoContentCategory.ORGANIC_IPM.wireValue,
                cropId = "cabbage",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Cabbage Cultivation, Mustard Trap Crop & Boron Care",
                titleBn = "বাঁধাকপি চাষ, সরিষা ফাঁদ ফসল ও বোরন পুষ্টি গাইড",
                titleHi = "पत्तागोभी खेती, सरसों फंदा फसल व बोरॉन पोषण गाइड",
                metaDescriptionEn = "Compact cabbage head nutrition, Boron hollow stem prevention, and Mustard trap strip for Diamondback Moth IPM.",
                metaDescriptionBn = "বাঁধাকপিতে ডায়মন্ডব্যাক মথ দমনে সরিষা ফাঁদ ফসল, মাথা ফেটে যাওয়া রোধ ও বোরন ব্যবস্থাপনা।",
                metaDescriptionHi = "पत्तागोभी में डायमंडबैक मॉथ नियंत्रण के लिए सरसों पट्टी व सिर फटने से बचाव।",
                summaryEn = "Plant 2 rows of Indian Mustard every 25 rows of cabbage to trap Diamondback Moth and keep soil pH 6.0–6.8.",
                summaryBn = "প্রতি ২৫ সারি বাঁধাকপিতে ২ সারি সরিষা ফাঁদ ফসল হিসেবে লাগান ও ডাঁটা ফাঁপা রোধে বোরন নিশ্চিত করুন।",
                summaryHi = "हर 25 पंक्ति पत्तागोभी में 2 पंक्ति सरसों फंदा फसल लगाएं और सिर बंधते समय संतुलित सिंचाई रखें।",
                bodyEn = "Transplant hardened 25–30 day seedlings at 45 × 45 cm. Maintain even moisture to stop head splitting and supply Boron if hollow stems occur. Two rows of Indian Mustard every 25 cabbage rows effectively intercept Diamondback Moth adults.",
                bodyBn = "২৫–৩০ দিনের চারা ৪৫×৪৫ সেমিতে রোপণ করুন। বাঁধা গঠনের সময় হঠাৎ বেশি সেচ দেবেন না। ২৫ সারি অন্তর ২ সারি সরিষা লাগিয়ে পোকা দমন করুন।",
                bodyHi = "45×45 सेमी पर रोपाई करें। बोरॉन संतुलन से खोखले तने से बचें और 25 पंक्तियों में 2 पंक्ति सरसों लगाएं।",
                faqsSerialized = "Why do cabbage heads split in the field?::Heavy irrigation after dry soil causes rapid swelling and cracking.",
                sourceName = "ICAR - Indian Institute of Vegetable Research (IIVR)",
                sourceUrl = "https://iivr.icar.gov.in/",
                canonicalUrl = "$LIVE_WEB_URL/crops/cabbage.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 4000
            ),
            SeoArticleEntity(
                id = "hub_crop_mustard",
                slug = "mustard",
                category = SeoContentCategory.CROP_GUIDE.wireValue,
                cropId = "mustard",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Indian Mustard Cultivation, Sulphur & Honeybee-Safe Aphid IPM",
                titleBn = "সরিষা চাষ, সালফার পুষ্টি ও মৌমাছি-সুরক্ষিত জাবপোকা দমন",
                titleHi = "सरसों की खेती, सल्फर पोषण व मधुमक्खी-सुरक्षित माहू प्रबंधन",
                metaDescriptionEn = "Timely mid-Oct to early-Nov mustard sowing to beat Aphids, Sulphur for oil % and safe late-afternoon spray practices.",
                metaDescriptionBn = "সময়োচিত সরিষা বপন, তেল বাড়াতে সালফার সার ও মৌমাছির সুরক্ষা বজায় রেখে জাবপোকা নিয়ন্ত্রণ।",
                metaDescriptionHi = "समय से सरसों बुवाई, तेल बढ़ाने हेतु सल्फर और मधुमक्खी सुरक्षित माहू नियंत्रण।",
                summaryEn = "Sow early in Oct–Nov, include Sulphur for seed oil percentage, and never spray harsh chemicals during 10 AM–3 PM bee foraging.",
                summaryBn = "অক্টোবর–নভেম্বরে আগাম বীজ বুনুন, সালফার সার প্রয়োগ করুন এবং দুপুরের পর মৌমাছির সময়ে স্প্রে এড়িয়ে চলুন।",
                summaryHi = "अक्टूबर–नवंबर में समय से बुवाई करें, तेल प्रतिशत हेतु सल्फर दें और दोपहर में परागण के समय छिड़काव न करें।",
                bodyEn = "Early sowing helps mustard escape February aphid peaks. Apply balanced NPK + Sulphur and clip isolated early aphid twigs manually before sprays. Always protect honeybees during bloom.",
                bodyBn = "আগাম বপন করলে জাবপোকার প্রকোপ কমে। NPK-এর সাথে সালফার দিলে তেলের পরিমাণ বাড়ে। সকাল ১০টা–বিকেল ৩টার মধ্যে পরাগায়নের সময় স্প্রে করবেন না।",
                bodyHi = "समय पर बुवाई से माहू से बचाव होता है। सल्फर तेल की मात्रा बढ़ाता है। फूल अवस्था में दोपहर के समय छिड़काव न करें।",
                faqsSerialized = "Why add Sulphur to mustard?::Sulphur enhances oil synthesis in rapeseed-mustard grains.",
                sourceName = "ICAR - Directorate of Rapeseed-Mustard Research (DRMR)",
                sourceUrl = "https://www.drmr.res.in/",
                canonicalUrl = "$LIVE_WEB_URL/crops/mustard.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 5000
            ),
            SeoArticleEntity(
                id = "hub_crop_mango",
                slug = "mango",
                category = SeoContentCategory.NURSERY_SEEDS.wireValue,
                cropId = "mango",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Mango Orchard Canopy Pruning, Hopper & Fruit Fly Traps",
                titleBn = "আম বাগান পরিচর্যা, হপার ও মিথাইল ইউজিনল ফল মাছি ফাঁদ",
                titleHi = "आम बागान छंटाई, हॉपर व फल मक्खी फेरोमोन ट्रैप गाइड",
                metaDescriptionEn = "ICAR-CISH mango post-harvest pruning, Mealybug sticky tree bands, Mango Hopper care & Methyl Eugenol Fruit Fly traps.",
                metaDescriptionBn = "হিমসাগর ও আম্রপালি আম বাগানে ছাঁটাই, মিলিবাগ ব্যান্ড এবং মিথাইল ইউজিনল ফেরোমন ফাঁদ স্থাপন।",
                metaDescriptionHi = "आम बागानों में तुड़ाई के बाद छंटाई, मिलीबग पट्टी व मिथाइल यूजीनॉल ट्रैप उपयोग।",
                summaryEn = "Prune interior limbs post-harvest, wrap trunk bands against Mealybug, and install Methyl Eugenol pheromone traps before ripening.",
                summaryBn = "ফল তোলার পর ছাঁটাই করুন, কাণ্ডে আঠালো ব্যান্ড দিন এবং ফল পাকার আগে মিথাইল ইউজিনল ফাঁদ ঝোলান।",
                summaryHi = "तुड़ाई बाद छंटाई करें, तने पर पट्टी बांधें और फल पकने से पहले मिथाइल यूजीनॉल ट्रैप लगाएं।",
                bodyEn = "Sanitize orchard floor and prune dead interior wood after harvest. Avoid chemical spraying during full mango bloom to preserve natural pollinators. Hang Methyl Eugenol traps early to control Fruit Fly.",
                bodyBn = "ফল তোলার পর মরা ডাল ছাঁটাই ও পড়ে থাকা পচা আম নষ্ট করুন। পূর্ণ মুকুল অবস্থায় স্প্রে করবেন না। মিথাইল ইউজিনল ফাঁদ ফল মাছি নিয়ন্ত্রণে কার্যকর।",
                bodyHi = "तुड़ाई के बाद सूखी टहनियां छांटें। पूरे बौर पर छिड़काव से बचें और फल मक्खी रोकने हेतु समय पर फेरोमोन ट्रैप लगाएं।",
                faqsSerialized = "When should pheromone traps be hung in mango orchards?::From 6–8 weeks before expected fruit maturity.",
                sourceName = "ICAR - Central Institute for Subtropical Horticulture (CISH)",
                sourceUrl = "https://cish.icar.gov.in/",
                canonicalUrl = "$LIVE_WEB_URL/crops/mango.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 6000
            ),
            SeoArticleEntity(
                id = "hub_crop_chilli",
                slug = "chilli",
                category = SeoContentCategory.DISEASE_PEST.wireValue,
                cropId = "chilli",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Chilli Leaf Curl (Thrips vs Mite) & Maize Barrier IPM",
                titleBn = "লঙ্কার পাতা কোঁকড়ানো (থ্রিপস বনাম মাকড়) ও ভুট্টা বর্ডার গাইড",
                titleHi = "मिर्च पत्ता मरोड़ (थ्रिप्स बनाम माइट) व मक्का अवरोधक गाइड",
                metaDescriptionEn = "Distinguish Thrips upward boat-curl vs Mite downward rat-tail curl on chilli, plus raised-bed drainage & maize border crops.",
                metaDescriptionBn = "লঙ্কার পাতা ওপর দিকে (থ্রিপস) বনাম নিচের দিকে (মাকড়) কোঁকড়ানো চেনার সহজ উপায় ও উঁচু বেড চাষ।",
                metaDescriptionHi = "मिर्च में थ्रिप्स व माइट से पत्ता मुड़ने की पहचान और मक्का अवरोधक पंक्तियां।",
                summaryEn = "Upward boat curl = Thrips; Downward inverted curl = Yellow Mite. Grow 2 maize border rows and use well-drained raised beds.",
                summaryBn = "পাতা ওপর দিকে কোঁকড়ালে থ্রিপস আর নিচের দিকে উল্টে গেলে হলুদ মাকড়; উঁচু বেড ও চারপাশে ভুট্টা লাগান।",
                summaryHi = "ऊपर मुड़ना = थ्रिप्स, नीचे मुड़ना = पीली माइट; उठी मेड़ों पर खेती और चारों ओर मक्का लगाएं।",
                bodyEn = "Plant chilli on raised beds to avoid Phytophthora collar rot. Border plots with 2 rows of maize/sorghum to block incoming whiteflies and aphids. Hang both blue sticky traps (Thrips) and yellow sticky traps (Whitefly/Aphids).",
                bodyBn = "শিকড় পচা এড়াতে উঁচু বেডে লঙ্কা লাগান। চারপাশে ২ সারি ভুট্টা লাগিয়ে বাহক পোকা আটকান। নীল ও হলুদ আঠালো ফাঁদ ব্যবহার করুন।",
                bodyHi = "जड़ सड़न से बचने हेतु उठी मेड़ों पर रोपाई करें। खेत के चारों ओर मक्का की 2 पंक्तियां और नीले-पीले चिपचिपे ट्रैप लगाएं।",
                faqsSerialized = "How to distinguish chilli Thrips from Yellow Mite?::Thrips curl leaves upward like a boat; Yellow Mite curls leaves downward with long thin petioles.",
                sourceName = "ICAR - IIVR & Uttar Banga Krishi Viswavidyalaya (UBKV)",
                sourceUrl = "https://www.ubkv.ac.in/",
                canonicalUrl = "$LIVE_WEB_URL/crops/chilli.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 7000
            ),
            SeoArticleEntity(
                id = "hub_pesticide_safety",
                slug = "pesticide-safety-ipm",
                category = SeoContentCategory.PESTICIDE_SAFETY.wireValue,
                cropId = "",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Safe Pesticide Use, CIBRC Labels & Pre-Harvest Interval (PHI)",
                titleBn = "সুরক্ষিত কীটনাশক ব্যবহার, CIBRC লেবেল ও অপেক্ষাকাল (PHI)",
                titleHi = "सुरक्षित कीटनाशक उपयोग, CIBRC लेबल व प्रतीक्षा अवधि (PHI)",
                metaDescriptionEn = "Always verify official CIBRC product labels, color triangles, PPE gear, and waiting intervals before harvest.",
                metaDescriptionBn = "কোনো রাসায়নিক স্প্রের আগে CIBRC অনুমোদন, বোতলের রঙের ত্রিভুজ ও ফসল তোলার অপেক্ষাকাল মানুন।",
                metaDescriptionHi = "छिड़काव से पहले CIBRC पंजीकरण, लेबल खुराक, मास्क-दस्ताने व प्रतीक्षा अवधि का पालन करें।",
                summaryEn = "Zero guessed dosages: inspect CIBRC registered label, prioritize IPM & bio-controls first, and obey Waiting Period (PHI).",
                summaryBn = "অনুমিত ডোজ কখনো নয় — আগে IPM ও জৈব দমন করুন, প্রয়োজনে সরকারি লেবেল ও PHI মেনে স্প্রে করুন।",
                summaryHi = "अनुमानित खुराक से बचें — पहले जैविक/IPM अपनाएं, फिर CIBRC लेबल और PHI समय मानें।",
                bodyEn = "KRISHAK SATHI never invents chemical dosages. Farmers should always read the manufacturer's CIBRC-approved leaflet for active ingredient, target crop-pest pairing, exact dilution, and Pre-Harvest Interval (PHI), and consult local KVK/university agronomists when unsure.",
                bodyBn = "KRISHAK SATHI কখনোই মনগড়া রাসায়নিক ডোজ প্রদান করে না। সর্বদা প্যাকেটের CIBRC অনুমোদিত লেবেল পড়ে ডোজ ও অপেক্ষাকাল নিশ্চিত করুন এবং KVK বিজ্ঞানীর পরামর্শ নিন।",
                bodyHi = "KRISHAK SATHI कभी अनुमानित रासायनिक खुराक नहीं बताता। हमेशा बोतल पर CIBRC अनुमोदित मात्रा और प्रतीक्षा अवधि जांचें।",
                faqsSerialized = "Why does Pre-Harvest Interval (PHI) matter?::Harvesting too early after spraying leaves unsafe chemical residues on food crops.",
                sourceName = "Central Insecticides Board & Registration Committee (CIBRC)",
                sourceUrl = "https://cibrc.gov.in/",
                canonicalUrl = "$LIVE_WEB_URL/knowledge/pesticide-safety-ipm.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 8000
            ),
            SeoArticleEntity(
                id = "hub_soil_irrigation",
                slug = "soil-irrigation-north-bengal",
                category = SeoContentCategory.SOIL_NUTRIENTS.wireValue,
                cropId = "",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Soil Health Card, Acidic Soil & Critical Irrigation Stages",
                titleBn = "মাটির স্বাস্থ্য কার্ড, অম্লীয় মাটি ব্যবস্থাপনা ও সেচ সূচি",
                titleHi = "मृदा स्वास्थ्य कार्ड, अम्लीय मिट्टी सुधार व सिंचाई अनुसूची",
                metaDescriptionEn = "Science-backed soil pH testing, organic compost enrichment, and critical-stage irrigation scheduling for Indian farms.",
                metaDescriptionBn = "মাটি পরীক্ষা করে সুষম সার, অম্লীয় মাটিতে ডলোমাইট চুন এবং সঠিক সময়ে সেচ প্রয়োগ পদ্ধতি।",
                metaDescriptionHi = "मिट्टी परीक्षण आधारित उर्वरक प्रबंधन, जैविक खाद और फसल की मुख्य अवस्थाओं पर सिंचाई।",
                summaryEn = "Base NPK + Boron/Zinc on Soil Health Card tests, improve drainage on heavy soils, and irrigate critical growth stages.",
                summaryBn = "মাটির স্বাস্থ্য কার্ড অনুযায়ী NPK ও অনুখাদ্য দিন এবং জলাবদ্ধতা দূর করে সংকটকালীন পর্যায়ে সেচ দিন।",
                summaryHi = "मृदा स्वास्थ्य कार्ड अनुसार उर्वरक व सूक्ष्म पोषक तत्व दें और जल निकासी सुधारें।",
                bodyEn = "Healthy soils start with organic matter (FYM/vermicompost) and regular pH testing every 2–3 years. Sub-Himalayan North Bengal districts frequently exhibit acidic soil (pH 4.8–5.8) needing lab-guided dolomite lime for vegetables and pulses (never lime tea bushes).",
                bodyBn = "প্রতি ২–৩ বছর পর মাটি পরীক্ষা করুন। উত্তরবঙ্গের অম্লীয় মাটিতে সবজি ও ডাল চাষের আগে পরীক্ষাগারের পরামর্শমতো চুন ও জৈব সার মেশানো লাভজনক।",
                bodyHi = "हर 2–3 वर्ष में मिट्टी परीक्षण कराएं और गोबर खाद या वर्मीकम्पोस्ट से मिट्टी की संरचना सुधारें।",
                faqsSerialized = "Should farmers add lime to tea gardens?::No — tea prefers acidic soil (pH 4.5–5.5), whereas most vegetables prefer pH 6.0–7.0.",
                sourceName = "Soil Health Card - Dept. of Agriculture & Farmers Welfare",
                sourceUrl = "https://soilhealth.dac.gov.in/",
                canonicalUrl = "$LIVE_WEB_URL/knowledge/soil-irrigation-north-bengal.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 9000
            ),
            SeoArticleEntity(
                id = "hub_govt_schemes",
                slug = "government-schemes-west-bengal",
                category = SeoContentCategory.GOVT_SCHEMES.wireValue,
                cropId = "",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Central & West Bengal Government Farmer Schemes Directory",
                titleBn = "কেন্দ্রীয় ও পশ্চিমবঙ্গ সরকারি কৃষক সহায়তা প্রকল্প সমূহ",
                titleHi = "केंद्रीय व पश्चिम बंगाल सरकारी किसान योजना मार्गदर्शिका",
                metaDescriptionEn = "Official portals & eligibility overview for PM-KISAN, Krishak Bandhu, Bangla Shasya Bima, KCC & Soil Health Card.",
                metaDescriptionBn = "পিএম-কিষাণ, কৃষকবন্ধু, বাংলা শস্য বিমা ও কিষাণ ক্রেডিট কার্ডের অফিসিয়াল পোর্টাল তথ্য।",
                metaDescriptionHi = "पीएम-किसान, कृषक बंधु, बांग्ला शस्य बीमा और किसान क्रेडिट कार्ड के आधिकारिक पोर्टल।",
                summaryEn = "Direct verified .gov.in portals for income support, crop insurance, and institutional agri credit with offline cache in-app.",
                summaryBn = "অফিসিয়াল .gov.in লিংক ও আবেদনের প্রয়োজনীয় নথিপত্রের তালিকা এখন অ্যাপেই অফলাইন সেভ থাকে।",
                summaryHi = "पात्रता व आवेदन दस्तावेज़ों की जानकारी आधिकारिक .gov.in लिंक के साथ ऑफ़लाइन उपलब्ध।",
                bodyEn = "Check PM-KISAN (pmkisan.gov.in), Krishak Bandhu (krishakbandhu.wb.gov.in), and Bangla Shasya Bima (banglarshasyabima.net) inside KRISHAK SATHI Government Schemes (cached locally in Room for poor network areas). Always confirm live deadlines at your Block Agriculture Office.",
                bodyBn = "পিএম-কিষাণ ও কৃষকবন্ধু সংক্রান্ত সমস্ত তথ্য KRISHAK SATHI-র সরকারি প্রকল্প বিভাগে অফলাইনে সংরক্ষিত থাকে। চূড়ান্ত আবেদন .gov.in পোর্টাল বা ব্লক কৃষি দপ্তরে করুন।",
                bodyHi = "पीएम-किसान, कृषक बंधु व फसल बीमा विवरण ऐप के सरकारी योजना अनुभाग में ऑफ़लाइन भी उपलब्ध हैं।",
                faqsSerialized = "Can saved government schemes open offline?::Yes — KRISHAK SATHI caches schemes and bookmarks locally using Room.",
                sourceName = "Ministry of Agriculture & Farmers Welfare / Dept. of Agriculture WB",
                sourceUrl = "https://pmkisan.gov.in/",
                canonicalUrl = "$LIVE_WEB_URL/knowledge/government-schemes-west-bengal.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 10000
            ),
            SeoArticleEntity(
                id = "hub_siliguri_nb",
                slug = "siliguri-north-bengal-agri-services",
                category = SeoContentCategory.SILIGURI_NORTH_BENGAL.wireValue,
                cropId = "",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Siliguri & North Bengal Nurseries, Seeds & Agri Support Hub",
                titleBn = "শিলিগুড়ি ও উত্তরবঙ্গ কৃষি সেবা, নার্সারি ও বীজ সহায়িকা",
                titleHi = "सिलीगुड़ी व उत्तर बंगाल नर्सरी, बीज और कृषि सेवा केंद्र",
                metaDescriptionEn = "Regional farming intelligence, verified nurseries, fruit saplings, tea & vegetable guidance across Siliguri, Jalpaiguri & Dooars.",
                metaDescriptionBn = "শিলিগুড়ি, মাটিগাড়া, জলপাইগুড়ি ও ডুয়ার্স অঞ্চলের নার্সারি, চারা, বীজ ও মাণ্ডি দর সহায়িকা।",
                metaDescriptionHi = "सिलीगुड़ी, जलपाईगुड़ी, दार्जिलिंग व डुआर्स के किसानों हेतु नर्सरी पौध व मंडी जानकारी।",
                summaryEn = "Regional digital ecosystem linking Siliguri & North Bengal growers with nurseries, agri scientists, and wholesale mandi data.",
                summaryBn = "শিলিগুড়ি ও উত্তরবঙ্গের কৃষকদের সাথে বিশ্বস্ত নার্সারি, বিজ্ঞানী ও বাজার দরের ডিজিটাল সেতুবন্ধন।",
                summaryHi = "सिलीगुड़ी व उत्तर बंगाल के किसानों को प्रमाणित नर्सरी, विशेषज्ञों व मंडी दरों से जोड़ने वाला मंच।",
                bodyEn = "Designed around the diverse Terai, Dooars, and Hill belts of Siliguri, Jalpaiguri, Darjeeling, Kalimpong, and Cooch Behar, KRISHAK SATHI empowers local growers, nursery owners, and agricultural specialists to collaborate in Bengali, Hindi, and English.",
                bodyBn = "শিলিগুড়ি, জলপাইগুড়ি, দার্জিলিং ও কোচবিহারের বৈচিত্র্যময় কৃষি অঞ্চলের জন্য বাংলা, হিন্দি ও ইংরেজিতে পূর্ণাঙ্গ ডিজিটাল সহায়তা।",
                bodyHi = "सिलीगुड़ी, जलपाईगुड़ी, दार्जिलिंग व तराई क्षेत्र के किसानों व नर्सरियों के लिए बहुभाषी डिजिटल कृषि मंच।",
                faqsSerialized = "Does KRISHAK SATHI support nursery listings in North Bengal?::Yes — verified nurseries can register inside the Nursery Market & Discovery Hub.",
                sourceName = "Uttar Banga Krishi Viswavidyalaya (UBKV), Pundibari",
                sourceUrl = "https://www.ubkv.ac.in/",
                canonicalUrl = "$LIVE_WEB_URL/knowledge/siliguri-north-bengal-agri-services.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 11000
            ),
            SeoArticleEntity(
                id = "hub_machinery_storage",
                slug = "harvesting-storage-and-farm-tools",
                category = SeoContentCategory.MACHINERY_STORAGE.wireValue,
                cropId = "",
                status = SeoArticleStatus.PUBLISHED.wireValue,
                titleEn = "Safe Harvesting, Grain Drying, Cold Storage & Farm Machinery",
                titleBn = "সঠিক ফসল সংগ্রহ, শস্য শুকানো, হিমঘর ও কৃষি যন্ত্রপাতি গাইড",
                titleHi = "सुरक्षित कटाई, अनाज सुखाना, कोल्ड स्टोरेज व कृषि उपकरण गाइड",
                metaDescriptionEn = "Post-harvest moisture benchmarks (12-13% for paddy/mustard), curing potato skins, and safe sprayers/chaff-cutter maintenance.",
                metaDescriptionBn = "ধান ও সরিষা সংরক্ষণের সঠিক আর্দ্রতা, আলু সংরক্ষণে হিমঘর প্রস্তুতি ও স্প্রেয়ার-পাওয়ার টিলার রক্ষণাবেক্ষণ।",
                metaDescriptionHi = "अनाज भंडारण नमी मानक, आलू कंद क्योरिंग और स्प्रेयर व कृषि मशीनरी रखरखाव।",
                summaryEn = "Reduce 10–15% post-harvest loss by drying paddy/mustard to safe moisture and curing potatoes cleanly before storage.",
                summaryBn = "সঠিকভাবে রোদে শুকিয়ে ও আলু বাছাই করে সংরক্ষণ করলে ফসল কাটার পরের ক্ষতি উল্লেখযোগ্যভাবে কমে।",
                summaryHi = "कटाई के बाद उचित सुखाने, ग्रेडिंग और साफ भंडारण से 10–15% उपज नुकसान बचाएं।",
                bodyEn = "Always observe pesticide Pre-Harvest Intervals before cutting vegetables or plucking tea. Dry harvested rice and mustard cleanly off bare mud using tarpaulins down to safe storage moisture (~12–13% for paddy, ~8% for mustard). Calibrate knapsack sprayers with flat-fan or hollow-cone nozzles and clean filters with fresh water after every use.",
                bodyBn = "সবজি বা চা তোলার আগে অবশ্যই অপেক্ষাকাল মানুন। ত্রিপলের ওপর পরিষ্কারভাবে ধান ও সরিষা শুকিয়ে সংরক্ষণ করুন এবং প্রতিটি স্প্রের পর নজল ও ট্যাঙ্ক ভালোভাবে ধুয়ে রাখুন।",
                bodyHi = "कटाई से पहले प्रतीक्षा अवधि पूरा करें। पक्के फर्श या तिरपाल पर अनाज सुखाकर साफ बोरों में रखें और हर छिड़काव के बाद स्प्रेयर नोजल साफ करें।",
                faqsSerialized = "What moisture level prevents stored rice mold?::Sun-dry rice cleanly on tarpaulins down to approximately 12–13% grain moisture before bagging.",
                sourceName = "ICAR - Central Institute of Agricultural Engineering (CIAE)",
                sourceUrl = "https://ciae.icar.gov.in/",
                canonicalUrl = "$LIVE_WEB_URL/knowledge/index.html",
                lastUpdatedDate = DEFAULT_LAST_UPDATED,
                isAiAssisted = false,
                reviewedByAdmin = "agri_editorial",
                updatedAtMillis = now - 12000
            )
        )
    }
}
