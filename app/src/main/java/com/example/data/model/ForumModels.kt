package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ForumTopic(val code: String) {
    CROP_CARE("CROP_CARE"),
    PEST_DISEASE("PEST_DISEASE"),
    IRRIGATION_SOIL("IRRIGATION_SOIL"),
    SEEDS_NURSERY("SEEDS_NURSERY"),
    MARKET_SCHEMES("MARKET_SCHEMES"),
    TIPS_SUCCESS("TIPS_SUCCESS");

    fun getLabel(lang: AppLanguage): String = when (this) {
        CROP_CARE -> when (lang) {
            AppLanguage.BENGALI -> "ফসলের পরিচর্যা"
            AppLanguage.HINDI -> "फसल देखभाल"
            AppLanguage.ENGLISH -> "Crop Care"
        }
        PEST_DISEASE -> when (lang) {
            AppLanguage.BENGALI -> "রোগ ও পোকামাকড়"
            AppLanguage.HINDI -> "रोग एवं कीट"
            AppLanguage.ENGLISH -> "Pest & Disease"
        }
        IRRIGATION_SOIL -> when (lang) {
            AppLanguage.BENGALI -> "সেচ ও মাটি স্বাস্থ্য"
            AppLanguage.HINDI -> "सिंचाई व मिट्टी"
            AppLanguage.ENGLISH -> "Irrigation & Soil"
        }
        SEEDS_NURSERY -> when (lang) {
            AppLanguage.BENGALI -> "বীজ ও নার্সারি"
            AppLanguage.HINDI -> "बीज व नर्सरी"
            AppLanguage.ENGLISH -> "Seeds & Nursery"
        }
        MARKET_SCHEMES -> when (lang) {
            AppLanguage.BENGALI -> "বাজার ও প্রকল্প"
            AppLanguage.HINDI -> "बाजार व योजनाएं"
            AppLanguage.ENGLISH -> "Market & Schemes"
        }
        TIPS_SUCCESS -> when (lang) {
            AppLanguage.BENGALI -> "টিপস ও সাফল্যের গল্প"
            AppLanguage.HINDI -> "सुझाव व सफलता"
            AppLanguage.ENGLISH -> "Tips & Stories"
        }
    }

    companion object {
        fun fromCode(code: String): ForumTopic =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: CROP_CARE
    }
}

enum class ForumModerationStatus(val code: String) {
    APPROVED("APPROVED"),
    FLAGGED("FLAGGED"),
    HIDDEN("HIDDEN");

    fun getLabel(lang: AppLanguage): String = when (this) {
        APPROVED -> when (lang) {
            AppLanguage.BENGALI -> "অনুমোদিত"
            AppLanguage.HINDI -> "स्वीकृत"
            AppLanguage.ENGLISH -> "Approved"
        }
        FLAGGED -> when (lang) {
            AppLanguage.BENGALI -> "রিপোর্টকৃত"
            AppLanguage.HINDI -> "फ्लैग किया गया"
            AppLanguage.ENGLISH -> "Flagged"
        }
        HIDDEN -> when (lang) {
            AppLanguage.BENGALI -> "লুকানো হয়েছে"
            AppLanguage.HINDI -> "छिपाया गया"
            AppLanguage.ENGLISH -> "Hidden"
        }
    }

    companion object {
        fun fromCode(code: String): ForumModerationStatus =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: APPROVED
    }
}

enum class ForumReportReason(val code: String) {
    SPAM("SPAM"),
    MISINFORMATION("MISINFORMATION"),
    ABUSIVE("ABUSIVE"),
    WRONG_CATEGORY("WRONG_CATEGORY"),
    OTHER("OTHER");

    fun getLabel(lang: AppLanguage): String = when (this) {
        SPAM -> when (lang) {
            AppLanguage.BENGALI -> "স্প্যাম বা অপ্রাসঙ্গিক বিজ্ঞাপন"
            AppLanguage.HINDI -> "स्पैम या अवांछित प्रचार"
            AppLanguage.ENGLISH -> "Spam / Promotion"
        }
        MISINFORMATION -> when (lang) {
            AppLanguage.BENGALI -> "ভুল বা ক্ষতিকর কৃষি তথ্য"
            AppLanguage.HINDI -> "गलत या हानिकारक कृषि जानकारी"
            AppLanguage.ENGLISH -> "Unsafe / Misinformation"
        }
        ABUSIVE -> when (lang) {
            AppLanguage.BENGALI -> "অশালীন বা অপমানজনক ভাষা"
            AppLanguage.HINDI -> "अपमानजनक या अनुचित भाषा"
            AppLanguage.ENGLISH -> "Abusive Language"
        }
        WRONG_CATEGORY -> when (lang) {
            AppLanguage.BENGALI -> "ভুল ফসল বা বিভাগ নির্বাচন"
            AppLanguage.HINDI -> "गलत फसल या श्रेणी"
            AppLanguage.ENGLISH -> "Wrong Crop / Topic"
        }
        OTHER -> when (lang) {
            AppLanguage.BENGALI -> "অন্যান্য নিয়ম লঙ্ঘন"
            AppLanguage.HINDI -> "अन्य नियम उल्लंघन"
            AppLanguage.ENGLISH -> "Other Violation"
        }
    }

    companion object {
        fun fromCode(code: String): ForumReportReason =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: OTHER
    }
}

enum class ForumReportStatus(val code: String) {
    PENDING("PENDING"),
    RESOLVED("RESOLVED"),
    DISMISSED("DISMISSED");

    fun getLabel(lang: AppLanguage): String = when (this) {
        PENDING -> when (lang) {
            AppLanguage.BENGALI -> "পর্যালোচনাধীন"
            AppLanguage.HINDI -> "लंबित"
            AppLanguage.ENGLISH -> "Pending"
        }
        RESOLVED -> when (lang) {
            AppLanguage.BENGALI -> "সমাধানকৃত"
            AppLanguage.HINDI -> "निस्तारित"
            AppLanguage.ENGLISH -> "Resolved"
        }
        DISMISSED -> when (lang) {
            AppLanguage.BENGALI -> "খারিজ"
            AppLanguage.HINDI -> "खारिज"
            AppLanguage.ENGLISH -> "Dismissed"
        }
    }

    companion object {
        fun fromCode(code: String): ForumReportStatus =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: PENDING
    }
}

enum class UserModerationStatus(val code: String) {
    ACTIVE("ACTIVE"),
    WARNED("WARNED"),
    MUTED("MUTED"),
    BANNED("BANNED");

    fun getLabel(lang: AppLanguage): String = when (this) {
        ACTIVE -> when (lang) {
            AppLanguage.BENGALI -> "সক্রিয়"
            AppLanguage.HINDI -> "सक्रिय"
            AppLanguage.ENGLISH -> "Active"
        }
        WARNED -> when (lang) {
            AppLanguage.BENGALI -> "সতর্কীকৃত"
            AppLanguage.HINDI -> "चेतावनी दी गई"
            AppLanguage.ENGLISH -> "Warned"
        }
        MUTED -> when (lang) {
            AppLanguage.BENGALI -> "সাময়িক নীরব (Muted)"
            AppLanguage.HINDI -> "अस्थायी मूक (Muted)"
            AppLanguage.ENGLISH -> "Muted"
        }
        BANNED -> when (lang) {
            AppLanguage.BENGALI -> "নিষিদ্ধ (Banned)"
            AppLanguage.HINDI -> "प्रतिबंधित (Banned)"
            AppLanguage.ENGLISH -> "Banned"
        }
    }

    companion object {
        fun fromCode(code: String): UserModerationStatus =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ACTIVE
    }
}

@Keep
@IgnoreExtraProperties
data class ForumPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "FARMER",
    val title: String = "",
    val content: String = "",
    val topic: String = ForumTopic.CROP_CARE.code,
    val cropId: String = "general",
    val cropName: String = "General Farming",
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val reportCount: Long = 0L,
    val moderationStatus: String = ForumModerationStatus.APPROVED.code,
    val pinned: Boolean = false,
    val moderationReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toFirestoreCreateMap(): Map<String, Any> = mapOf(
        "authorId" to authorId,
        "authorName" to authorName,
        "authorRole" to authorRole,
        "title" to title,
        "content" to content,
        "topic" to topic,
        "cropId" to cropId,
        "cropName" to cropName,
        "likesCount" to likesCount,
        "commentsCount" to commentsCount,
        "reportCount" to reportCount,
        "moderationStatus" to moderationStatus,
        "pinned" to pinned,
        "moderationReason" to moderationReason,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class ForumComment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "FARMER",
    val content: String = "",
    val moderationStatus: String = ForumModerationStatus.APPROVED.code,
    val reportCount: Long = 0L,
    val moderationReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toFirestoreCreateMap(): Map<String, Any> = mapOf(
        "postId" to postId,
        "authorId" to authorId,
        "authorName" to authorName,
        "authorRole" to authorRole,
        "content" to content,
        "moderationStatus" to moderationStatus,
        "reportCount" to reportCount,
        "moderationReason" to moderationReason,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class ForumReport(
    @DocumentId val id: String = "",
    val targetType: String = "POST", // POST or COMMENT
    val postId: String = "",
    val commentId: String = "",
    val targetAuthorId: String = "",
    val targetAuthorName: String = "",
    val targetPreview: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reason: String = ForumReportReason.SPAM.code,
    val details: String = "",
    val status: String = ForumReportStatus.PENDING.code,
    val adminActionNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toFirestoreCreateMap(): Map<String, Any> = mapOf(
        "targetType" to targetType,
        "postId" to postId,
        "commentId" to commentId,
        "targetAuthorId" to targetAuthorId,
        "targetAuthorName" to targetAuthorName,
        "targetPreview" to targetPreview,
        "reporterId" to reporterId,
        "reporterName" to reporterName,
        "reason" to reason,
        "details" to details,
        "status" to status,
        "adminActionNote" to adminActionNote,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class ForumUserModeration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val status: String = UserModerationStatus.ACTIVE.code,
    val warningCount: Long = 0L,
    val moderationReason: String = "",
    val mutedUntilMillis: Long = 0L,
    val updatedByAdmin: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isCurrentlyRestricted(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return when (UserModerationStatus.fromCode(status)) {
            UserModerationStatus.BANNED -> true
            UserModerationStatus.MUTED -> mutedUntilMillis == 0L || nowMillis < mutedUntilMillis
            else -> false
        }
    }

    fun toFirestoreWriteMap(isNew: Boolean): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "userName" to userName,
            "status" to status,
            "warningCount" to warningCount,
            "moderationReason" to moderationReason,
            "mutedUntilMillis" to mutedUntilMillis,
            "updatedByAdmin" to updatedByAdmin,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isNew) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }
}
