package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.ForumComment
import com.example.data.model.ForumModerationStatus
import com.example.data.model.ForumPost
import com.example.data.model.ForumReport
import com.example.data.model.ForumReportReason
import com.example.data.model.ForumReportStatus
import com.example.data.model.ForumTopic
import com.example.data.model.ForumUserModeration
import com.example.data.model.UserModerationStatus
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID

private const val TAG = "ForumRepository"

enum class ForumOperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write")
}

fun handleForumFirestoreError(
    exception: Throwable,
    operationType: ForumOperationType,
    path: String?
): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val currentUser = try {
        FirebaseAuth.getInstance().currentUser
    } catch (_: Exception) {
        null
    }

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

/**
 * Provides trilingual translations for pre-seeded starter community discussions
 * while keeping farmer-authored posts rendered cleanly in their authored text.
 */
object ForumStarterTranslations {
    fun getLocalizedTitle(post: ForumPost, lang: AppLanguage): String = when (post.id) {
        "starter_post_tomato_blight" -> when (lang) {
            AppLanguage.BENGALI -> "শীতের কুয়াশায় টমেটোর পাতায় কালো দাগ — প্রাথমিক জৈব প্রতিকার কী?"
            AppLanguage.HINDI -> "सर्दियों के कोहरे में टमाटर की पत्तियों पर काले धब्बे — शुरुआती जैविक उपचार क्या है?"
            AppLanguage.ENGLISH -> "Dark concentric spots on Tomato leaves in winter fog — best early organic action?"
        }
        "starter_post_tea_mite" -> when (lang) {
            AppLanguage.BENGALI -> "উত্তরবঙ্গের চা বাগানে লাল মাকড় (Red Spider Mite) দমন অভিজ্ঞতা"
            AppLanguage.HINDI -> "उत्तर बंगाल के चाय बागानों में लाल मकड़ी (Red Spider Mite) नियंत्रण अनुभव"
            AppLanguage.ENGLISH -> "Managing Red Spider Mite outbreaks in North Bengal Tea sections"
        }
        "starter_post_paddy_water" -> when (lang) {
            AppLanguage.BENGALI -> "বোরো ধানে AWD পাইপ পদ্ধতিতে ৩০% সেচ খরচ কমিয়েছি"
            AppLanguage.HINDI -> "बोरो धान में AWD पाइप विधि से मैंने 30% सिंचाई लागत कम की"
            AppLanguage.ENGLISH -> "Saved 30% irrigation cost in Boro Paddy using AWD water tube method"
        }
        "starter_post_mustard_aphid" -> when (lang) {
            AppLanguage.BENGALI -> "সরষে ফুলে জাবপোকা (Aphid) — মৌমাছি রক্ষা করে কখন স্প্রে করবেন?"
            AppLanguage.HINDI -> "सरसों के फूलों में माहू (Aphid) — मधुमक्खियों को बचाकर छिड़काव कब करें?"
            AppLanguage.ENGLISH -> "Mustard aphids during flowering — timing sprays to protect honeybees"
        }
        "starter_post_mango_nursery" -> when (lang) {
            AppLanguage.BENGALI -> "উন্নত আম্রপালি ও ড্রাগন ফ্রুট কলম রোপণের গর্ত প্রস্তুতি টিপস"
            AppLanguage.HINDI -> "उन्नत आम्रपाली और ड्रैगन फ्रूट कलम रोपण के लिए गड्ढा तैयारी टिप्स"
            AppLanguage.ENGLISH -> "Soil pit preparation tips before planting Amrapali mango & dragon fruit saplings"
        }
        else -> post.title
    }

    fun getLocalizedContent(post: ForumPost, lang: AppLanguage): String = when (post.id) {
        "starter_post_tomato_blight" -> when (lang) {
            AppLanguage.BENGALI -> "ধূপগুড়িতে আমার ২ বিঘা টমেটো জমির নিচের পাতায় গোল কালো দাগ দেখা যাচ্ছে। এখনো ফল ধরার প্রথম পর্যায়। রাসায়নিকের আগে কপার অক্সিক্লোরাইড বা ট্রাইকোডার্মা কীভাবে ব্যবহার করা নিরাপদ হবে?"
            AppLanguage.HINDI -> "धूपगुड़ी में मेरे 2 बीघा टमाटर के खेत की निचली पत्तियों पर गोल काले धब्बे दिख रहे हैं। अभी फल लगने की शुरुआती अवस्था है। रासायनिक स्प्रे से पहले कॉपर ऑक्सीक्लोराइड या ट्राइकोडर्मा का उपयोग कैसे सुरक्षित रहेगा?"
            AppLanguage.ENGLISH -> "Seeing concentric dark spots on the lower leaves of my 2-bigha tomato plot in Dhupguri. Plants are at early fruiting stage. Should I prune infected lower leaves and spray Trichoderma or Copper Oxychloride first?"
        }
        "starter_post_tea_mite" -> when (lang) {
            AppLanguage.BENGALI -> "শীতের পর শুকনো আবহাওয়ায় চা গাছের পরিপক্ব পাতায় তামাটে ভাব আসছে। সকালবেলা পর্যাপ্ত জলের সঙ্গে নিম তেল ১৫০০ পিপিএম ও ছায়া গাছ নিয়ন্ত্রণ করলে প্রাকৃতিক শিকারি পোকা বাঁচে এবং পাতার মান ভালো থাকে।"
            AppLanguage.HINDI -> "सर्दियों के बाद सूखे मौसम में चाय की पत्तियों पर तांबे जैसा रंग दिख रहा है। सुबह भरपूर पानी के साथ नीम तेल 1500 PPM और छाया प्रबंधन करने से लाभदायक कीट सुरक्षित रहते हैं और पत्ती की गुणवत्ता बनी रहती है।"
            AppLanguage.ENGLISH -> "During dry spells, mature tea leaves show bronzing from Red Spider Mite. High-volume morning spot spraying of Neem oil 1500 ppm and shade regulation keeps Tea Board MRL safe while preserving natural predators."
        }
        "starter_post_paddy_water" -> when (lang) {
            AppLanguage.BENGALI -> "জমিতে ১২ ইঞ্চি ছিদ্রযুক্ত পিভিসি পাইপ বসিয়ে জলস্তর দেখছি। মাটির ১৫ সেমি নিচে জল নামলেই কেবল সেচ দিচ্ছি। এতে পাম্পের ডিজেল খরচ অনেক কমেছে এবং গাছের গোড়া শক্ত হয়েছে।"
            AppLanguage.HINDI -> "खेत में 12 इंच का छिद्रयुक्त पीवीसी पाइप लगाकर जल स्तर की जांच की। पानी मिट्टी से 15 सेमी नीचे जाने पर ही सिंचाई की। इससे डीजल खर्च काफी कम हुआ और जड़ें मजबूत रहीं।"
            AppLanguage.ENGLISH -> "Installed a perforated 12-inch PVC observation tube in the paddy field and irrigated only when water dropped 15 cm below the surface. Diesel pump costs dropped significantly and tillering improved."
        }
        "starter_post_mustard_aphid" -> when (lang) {
            AppLanguage.BENGALI -> "জমি থেকে হলুদ ফুল ফোটার সময় দিনের বেলায় কড়া কীটনাশক স্প্রে করলে পরাগায়ন কমে যায়। সবসময় বিকেল ৪টার পর স্প্রে করুন এবং বিঘায় ৮টি হলুদ আঠালো ফাঁদ ব্যবহার করুন।"
            AppLanguage.HINDI -> "सरसों में पीले फूल आते समय दोपहर में तेज कीटनाशक छिड़कने से परागण घटता है। हमेशा शाम 4 बजे के बाद छिड़काव करें और प्रति बीघा 8 पीले चिपचिपे ट्रैप लगाएं।"
            AppLanguage.ENGLISH -> "Spraying harsh insecticides during midday mustard bloom harms pollinators and lowers pod set. Place 8 yellow sticky traps per bigha and apply safe sprays only after 4 PM."
        }
        "starter_post_mango_nursery" -> when (lang) {
            AppLanguage.BENGALI -> "রোপণের ১৫ দিন আগে ২×২×২ ফুট গর্ত খুঁড়ে রোদে শুকিয়ে নিন। তারপর ১০ কেজি পচা গোবর সার, ২৫০ গ্রাম হাড়ের গুঁড়ো ও ৫০ গ্রাম ট্রাইকোডার্মা মিশিয়ে গর্ত ভরাট করলে চারা দ্রুত শিকড় ধরে।"
            AppLanguage.HINDI -> "रोपण से 15 दिन पहले 2×2×2 फीट का गड्ढा खोदकर धूप में सुखाएं। इसके बाद 10 किलो सड़ी गोबर खाद, 250 ग्राम बोन मील और 50 ग्राम ट्राइकोडर्मा मिलाकर भरने से पौधे की जड़ें तेजी से पकड़ती हैं।"
            AppLanguage.ENGLISH -> "Dig 2x2x2 ft pits 15 days before planting and sun-sterilize. Refill with 10 kg decomposed FYM, 250 g bone meal, and 50 g Trichoderma enriched compost for high survival rate."
        }
        else -> post.content
    }

    fun getLocalizedComment(comment: ForumComment, lang: AppLanguage): String = when (comment.id) {
        "starter_cmt_1" -> when (lang) {
            AppLanguage.BENGALI -> "আক্রান্ত নিচের পাতাগুলো সাবধানে তুলে জমির বাইরে নষ্ট করুন, তারপর সকাল ৯টার আগে ট্রাইকোডার্মা ভিরিডি ৪ গ্রাম/লিটার বা কপার অক্সিক্লোরাইড ২.৫ গ্রাম/লিটার স্প্রে করুন।"
            AppLanguage.HINDI -> "संक्रमित निचली पत्तियों को सावधानी से तोड़कर खेत से बाहर नष्ट करें, फिर सुबह 9 बजे से पहले ट्राइकोडर्मा विरिडी 4 ग्राम/लीटर या कॉपर ऑक्सीक्लोराइड 2.5 ग्राम/लीटर का छिड़काव करें।"
            AppLanguage.ENGLISH -> "Carefully prune affected lower leaves and destroy outside the field, then spray Trichoderma viride 4 g/L or Copper Oxychloride 2.5 g/L before 9 AM."
        }
        "starter_cmt_2" -> when (lang) {
            AppLanguage.BENGALI -> "সেচের জল গাছের গোড়ায় জমতে দেবেন না এবং প্রয়োজনে প্রতি বিঘায় স্টিকার মিশিয়ে স্প্রে করলে ভালো ফল পাবেন।"
            AppLanguage.HINDI -> "सिंचाई का पानी तने के पास जमा न होने दें और बेहतर पकड़ के लिए स्प्रे में स्टीकर मिलाएं।"
            AppLanguage.ENGLISH -> "Avoid water stagnation at the root collar and mix a gentle spreader-sticker for uniform leaf coverage."
        }
        "starter_cmt_3" -> when (lang) {
            AppLanguage.BENGALI -> "খুব ভালো পরামর্শ! আমাদের বাগানের উত্তর ব্লকেও স্পট স্প্রে করে লাল মাকড় দ্রুত কমেছে।"
            AppLanguage.HINDI -> "बहुत उपयोगी अनुभव! हमारे बागान के उत्तरी ब्लॉक में भी स्पॉट स्प्रे से लाल मकड़ी तेजी से नियंत्रित हुई।"
            AppLanguage.ENGLISH -> "Great field insight! Spot-spraying affected borders early in our North section controlled spread within a week."
        }
        else -> comment.content
    }
}

class ForumRepository(
    private val firestoreProvider: () -> FirebaseFirestore?,
    private val appContext: Context,
    private val authProvider: () -> FirebaseAuth? = {
        try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
    }
) {
    constructor(context: Context) : this(
        firestoreProvider = {
            try {
                val appCtx = context.applicationContext
                FirebaseFirestore.getInstance(appCtx.getString(R.string.firestore_database_id))
            } catch (e: Exception) {
                Log.w(TAG, "Firestore instance unavailable offline: ${e.message}")
                null
            }
        },
        appContext = context.applicationContext
    )

    private val prefs = appContext.getSharedPreferences("krishak_sathi_community_forum_v1", Context.MODE_PRIVATE)

    private val _localPosts = MutableStateFlow<List<ForumPost>>(emptyList())
    private val _localCommentsByPost = MutableStateFlow<Map<String, List<ForumComment>>>(emptyMap())
    private val _localReports = MutableStateFlow<List<ForumReport>>(emptyList())
    private val _localUserModerations = MutableStateFlow<Map<String, ForumUserModeration>>(emptyMap())
    private val _localLikedPostIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        loadFromPrefs()
    }

    private fun toTimestamp(millis: Long): Timestamp {
        val sec = millis / 1000L
        val nanos = ((millis % 1000L) * 1_000_000L).toInt().coerceAtLeast(0)
        return Timestamp(sec, nanos)
    }

    private fun fromTimestamp(ts: Timestamp?): Long = ts?.toDate()?.time ?: System.currentTimeMillis()

    private fun loadFromPrefs() {
        val rawPosts = prefs.getString("posts_json", null)
        if (!rawPosts.isNullOrBlank()) {
            _localPosts.value = parsePostsJson(rawPosts)
        } else {
            val now = System.currentTimeMillis()
            val seeds = listOf(
                ForumPost(
                    id = "starter_post_tomato_blight",
                    authorId = "starter_farmer_ramesh",
                    authorName = "রামেশ মণ্ডল / Ramesh Mandal",
                    authorRole = "FARMER",
                    title = ForumStarterTranslations.getLocalizedTitle(ForumPost(id = "starter_post_tomato_blight"), AppLanguage.ENGLISH),
                    content = ForumStarterTranslations.getLocalizedContent(ForumPost(id = "starter_post_tomato_blight"), AppLanguage.ENGLISH),
                    topic = ForumTopic.PEST_DISEASE.code,
                    cropId = "tomato",
                    cropName = "Tomato",
                    likesCount = 14L,
                    commentsCount = 2L,
                    reportCount = 0L,
                    moderationStatus = ForumModerationStatus.APPROVED.code,
                    pinned = false,
                    createdAt = toTimestamp(now - 3600_000L * 4),
                    updatedAt = toTimestamp(now - 3600_000L * 4)
                ),
                ForumPost(
                    id = "starter_post_tea_mite",
                    authorId = "starter_expert_ananya",
                    authorName = "ডঃ অনন্যা সেন / Dr. Ananya Sen",
                    authorRole = "EXPERT",
                    title = ForumStarterTranslations.getLocalizedTitle(ForumPost(id = "starter_post_tea_mite"), AppLanguage.ENGLISH),
                    content = ForumStarterTranslations.getLocalizedContent(ForumPost(id = "starter_post_tea_mite"), AppLanguage.ENGLISH),
                    topic = ForumTopic.CROP_CARE.code,
                    cropId = "tea",
                    cropName = "Tea",
                    likesCount = 28L,
                    commentsCount = 1L,
                    reportCount = 0L,
                    moderationStatus = ForumModerationStatus.APPROVED.code,
                    pinned = true,
                    createdAt = toTimestamp(now - 3600_000L * 18),
                    updatedAt = toTimestamp(now - 3600_000L * 18)
                ),
                ForumPost(
                    id = "starter_post_paddy_water",
                    authorId = "starter_farmer_bikash",
                    authorName = "বিকাশ রায় / Bikash Roy",
                    authorRole = "FARMER",
                    title = ForumStarterTranslations.getLocalizedTitle(ForumPost(id = "starter_post_paddy_water"), AppLanguage.ENGLISH),
                    content = ForumStarterTranslations.getLocalizedContent(ForumPost(id = "starter_post_paddy_water"), AppLanguage.ENGLISH),
                    topic = ForumTopic.IRRIGATION_SOIL.code,
                    cropId = "rice_paddy",
                    cropName = "Rice / Paddy",
                    likesCount = 21L,
                    commentsCount = 0L,
                    reportCount = 0L,
                    moderationStatus = ForumModerationStatus.APPROVED.code,
                    pinned = false,
                    createdAt = toTimestamp(now - 86400_000L * 1),
                    updatedAt = toTimestamp(now - 86400_000L * 1)
                ),
                ForumPost(
                    id = "starter_post_mustard_aphid",
                    authorId = "starter_farmer_tapas",
                    authorName = "তাপস দাস / Tapas Das",
                    authorRole = "FARMER",
                    title = ForumStarterTranslations.getLocalizedTitle(ForumPost(id = "starter_post_mustard_aphid"), AppLanguage.ENGLISH),
                    content = ForumStarterTranslations.getLocalizedContent(ForumPost(id = "starter_post_mustard_aphid"), AppLanguage.ENGLISH),
                    topic = ForumTopic.TIPS_SUCCESS.code,
                    cropId = "mustard",
                    cropName = "Mustard",
                    likesCount = 11L,
                    commentsCount = 0L,
                    reportCount = 0L,
                    moderationStatus = ForumModerationStatus.APPROVED.code,
                    pinned = false,
                    createdAt = toTimestamp(now - 86400_000L * 2),
                    updatedAt = toTimestamp(now - 86400_000L * 2)
                ),
                ForumPost(
                    id = "starter_post_mango_nursery",
                    authorId = "starter_nursery_rabin",
                    authorName = "উত্তরবঙ্গ গ্রিন নার্সারি / North Bengal Green Nursery",
                    authorRole = "NURSERY_OWNER",
                    title = ForumStarterTranslations.getLocalizedTitle(ForumPost(id = "starter_post_mango_nursery"), AppLanguage.ENGLISH),
                    content = ForumStarterTranslations.getLocalizedContent(ForumPost(id = "starter_post_mango_nursery"), AppLanguage.ENGLISH),
                    topic = ForumTopic.SEEDS_NURSERY.code,
                    cropId = "mango",
                    cropName = "Mango",
                    likesCount = 19L,
                    commentsCount = 0L,
                    reportCount = 0L,
                    moderationStatus = ForumModerationStatus.APPROVED.code,
                    pinned = false,
                    createdAt = toTimestamp(now - 86400_000L * 3),
                    updatedAt = toTimestamp(now - 86400_000L * 3)
                )
            )
            savePostsLocal(seeds)
        }

        val rawComments = prefs.getString("comments_json", null)
        if (!rawComments.isNullOrBlank()) {
            _localCommentsByPost.value = parseCommentsJson(rawComments)
        } else {
            val now = System.currentTimeMillis()
            val seedComments = mapOf(
                "starter_post_tomato_blight" to listOf(
                    ForumComment(
                        id = "starter_cmt_1",
                        postId = "starter_post_tomato_blight",
                        authorId = "starter_expert_ananya",
                        authorName = "ডঃ অনন্যা সেন / Dr. Ananya Sen",
                        authorRole = "EXPERT",
                        content = ForumStarterTranslations.getLocalizedComment(ForumComment(id = "starter_cmt_1"), AppLanguage.ENGLISH),
                        moderationStatus = ForumModerationStatus.APPROVED.code,
                        createdAt = toTimestamp(now - 3600_000L * 3),
                        updatedAt = toTimestamp(now - 3600_000L * 3)
                    ),
                    ForumComment(
                        id = "starter_cmt_2",
                        postId = "starter_post_tomato_blight",
                        authorId = "starter_farmer_bikash",
                        authorName = "বিকাশ রায় / Bikash Roy",
                        authorRole = "FARMER",
                        content = ForumStarterTranslations.getLocalizedComment(ForumComment(id = "starter_cmt_2"), AppLanguage.ENGLISH),
                        moderationStatus = ForumModerationStatus.APPROVED.code,
                        createdAt = toTimestamp(now - 3600_000L * 2),
                        updatedAt = toTimestamp(now - 3600_000L * 2)
                    )
                ),
                "starter_post_tea_mite" to listOf(
                    ForumComment(
                        id = "starter_cmt_3",
                        postId = "starter_post_tea_mite",
                        authorId = "starter_farmer_ramesh",
                        authorName = "রামেশ মণ্ডল / Ramesh Mandal",
                        authorRole = "FARMER",
                        content = ForumStarterTranslations.getLocalizedComment(ForumComment(id = "starter_cmt_3"), AppLanguage.ENGLISH),
                        moderationStatus = ForumModerationStatus.APPROVED.code,
                        createdAt = toTimestamp(now - 3600_000L * 10),
                        updatedAt = toTimestamp(now - 3600_000L * 10)
                    )
                )
            )
            saveCommentsLocal(seedComments)
        }

        val rawReports = prefs.getString("reports_json", null)
        if (!rawReports.isNullOrBlank()) {
            _localReports.value = parseReportsJson(rawReports)
        }

        val rawMods = prefs.getString("user_mods_json", null)
        if (!rawMods.isNullOrBlank()) {
            _localUserModerations.value = parseUserModerationJson(rawMods)
        }

        val likedSet = prefs.getStringSet("liked_post_ids", emptySet()) ?: emptySet()
        _localLikedPostIds.value = likedSet.toSet()
    }

    private fun savePostsLocal(posts: List<ForumPost>) {
        val sorted = sortPosts(posts)
        _localPosts.value = sorted
        val arr = JSONArray()
        sorted.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("authorId", p.authorId)
                put("authorName", p.authorName)
                put("authorRole", p.authorRole)
                put("title", p.title)
                put("content", p.content)
                put("topic", p.topic)
                put("cropId", p.cropId)
                put("cropName", p.cropName)
                put("likesCount", p.likesCount)
                put("commentsCount", p.commentsCount)
                put("reportCount", p.reportCount)
                put("moderationStatus", p.moderationStatus)
                put("pinned", p.pinned)
                put("moderationReason", p.moderationReason)
                put("createdAtMillis", fromTimestamp(p.createdAt))
                put("updatedAtMillis", fromTimestamp(p.updatedAt))
            })
        }
        prefs.edit().putString("posts_json", arr.toString()).apply()
    }

    private fun saveCommentsLocal(map: Map<String, List<ForumComment>>) {
        _localCommentsByPost.value = map
        val root = JSONObject()
        map.forEach { (postId, list) ->
            val arr = JSONArray()
            list.forEach { c ->
                arr.put(JSONObject().apply {
                    put("id", c.id)
                    put("postId", c.postId)
                    put("authorId", c.authorId)
                    put("authorName", c.authorName)
                    put("authorRole", c.authorRole)
                    put("content", c.content)
                    put("moderationStatus", c.moderationStatus)
                    put("reportCount", c.reportCount)
                    put("moderationReason", c.moderationReason)
                    put("createdAtMillis", fromTimestamp(c.createdAt))
                    put("updatedAtMillis", fromTimestamp(c.updatedAt))
                })
            }
            root.put(postId, arr)
        }
        prefs.edit().putString("comments_json", root.toString()).apply()
    }

    private fun saveReportsLocal(reports: List<ForumReport>) {
        val sorted = reports.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        _localReports.value = sorted
        val arr = JSONArray()
        sorted.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("targetType", r.targetType)
                put("postId", r.postId)
                put("commentId", r.commentId)
                put("targetAuthorId", r.targetAuthorId)
                put("targetAuthorName", r.targetAuthorName)
                put("targetPreview", r.targetPreview)
                put("reporterId", r.reporterId)
                put("reporterName", r.reporterName)
                put("reason", r.reason)
                put("details", r.details)
                put("status", r.status)
                put("adminActionNote", r.adminActionNote)
                put("createdAtMillis", fromTimestamp(r.createdAt))
                put("updatedAtMillis", fromTimestamp(r.updatedAt))
            })
        }
        prefs.edit().putString("reports_json", arr.toString()).apply()
    }

    private fun saveUserModerationsLocal(map: Map<String, ForumUserModeration>) {
        _localUserModerations.value = map
        val arr = JSONArray()
        map.values.forEach { m ->
            arr.put(JSONObject().apply {
                put("userId", m.userId)
                put("userName", m.userName)
                put("status", m.status)
                put("warningCount", m.warningCount)
                put("moderationReason", m.moderationReason)
                put("mutedUntilMillis", m.mutedUntilMillis)
                put("updatedByAdmin", m.updatedByAdmin)
                put("createdAtMillis", fromTimestamp(m.createdAt))
                put("updatedAtMillis", fromTimestamp(m.updatedAt))
            })
        }
        prefs.edit().putString("user_mods_json", arr.toString()).apply()
    }

    private fun saveLikedPostIdsLocal(ids: Set<String>) {
        _localLikedPostIds.value = ids
        prefs.edit().putStringSet("liked_post_ids", ids).apply()
    }

    private fun sortPosts(posts: List<ForumPost>): List<ForumPost> {
        return posts.sortedWith(
            compareByDescending<ForumPost> { it.pinned }
                .thenByDescending { it.createdAt?.seconds ?: 0L }
                .thenByDescending { it.createdAt?.nanoseconds ?: 0 }
        )
    }

    private fun activeFirebaseUid(): String? = authProvider()?.currentUser?.uid

    private fun activeDb(): FirebaseFirestore? = firestoreProvider()

    fun observePosts(isAdmin: Boolean): Flow<List<ForumPost>> {
        val db = activeDb()
        val fbUid = activeFirebaseUid()
        return if (db != null && fbUid != null) {
            val query = if (isAdmin) {
                db.collection("forum_posts")
            } else {
                db.collection("forum_posts")
                    .whereIn("moderationStatus", listOf(ForumModerationStatus.APPROVED.code, ForumModerationStatus.FLAGGED.code))
            }
            val remoteFlow = query.snapshots()
                .map { snap ->
                    snap.toObjects(ForumPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    handleForumFirestoreError(e, ForumOperationType.LIST, "forum_posts")
                    emit(emptyList())
                }
            combine(remoteFlow, _localPosts) { remote, local ->
                val byId = LinkedHashMap<String, ForumPost>()
                local.forEach { byId[it.id] = it }
                remote.forEach { byId[it.id] = it }
                val merged = byId.values.filter { post ->
                    isAdmin || post.moderationStatus != ForumModerationStatus.HIDDEN.code || post.authorId == fbUid
                }
                sortPosts(merged)
            }
        } else {
            _localPosts.map { list ->
                sortPosts(
                    list.filter { post ->
                        isAdmin || post.moderationStatus != ForumModerationStatus.HIDDEN.code
                    }
                )
            }
        }
    }

    fun observeComments(postId: String, isAdmin: Boolean): Flow<List<ForumComment>> {
        val db = activeDb()
        val fbUid = activeFirebaseUid()
        val localFlow = _localCommentsByPost.map { map ->
            (map[postId] ?: emptyList()).filter { c ->
                isAdmin || c.moderationStatus != ForumModerationStatus.HIDDEN.code
            }.sortedBy { it.createdAt ?: Timestamp(0, 0) }
        }
        return if (db != null && fbUid != null && !postId.startsWith("starter_")) {
            val baseCol = db.collection("forum_posts").document(postId).collection("comments")
            val query = if (isAdmin) {
                baseCol
            } else {
                baseCol.whereIn("moderationStatus", listOf(ForumModerationStatus.APPROVED.code, ForumModerationStatus.FLAGGED.code))
            }
            val remoteFlow = query.snapshots()
                .map { snap ->
                    snap.toObjects(ForumComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    handleForumFirestoreError(e, ForumOperationType.LIST, "forum_posts/$postId/comments")
                    emit(emptyList())
                }
            combine(remoteFlow, localFlow) { remote, local ->
                val byId = LinkedHashMap<String, ForumComment>()
                local.forEach { byId[it.id] = it }
                remote.forEach { byId[it.id] = it }
                byId.values
                    .filter { isAdmin || it.moderationStatus != ForumModerationStatus.HIDDEN.code || it.authorId == fbUid }
                    .sortedBy { it.createdAt ?: Timestamp(0, 0) }
            }
        } else {
            localFlow
        }
    }

    fun observeLikedPostIds(): Flow<Set<String>> = _localLikedPostIds

    fun observeReports(isAdmin: Boolean, userId: String): Flow<List<ForumReport>> {
        val db = activeDb()
        val fbUid = activeFirebaseUid()
        return if (db != null && fbUid != null && isAdmin) {
            val remoteFlow = db.collection("forum_reports")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ForumReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    handleForumFirestoreError(e, ForumOperationType.LIST, "forum_reports")
                    emit(emptyList())
                }
            combine(remoteFlow, _localReports) { remote, local ->
                val byId = LinkedHashMap<String, ForumReport>()
                local.forEach { byId[it.id] = it }
                remote.forEach { byId[it.id] = it }
                byId.values.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
        } else {
            _localReports.map { reports ->
                if (isAdmin) reports else reports.filter { it.reporterId == userId }
            }
        }
    }

    fun observeAllUserModerations(isAdmin: Boolean): Flow<List<ForumUserModeration>> {
        val db = activeDb()
        val fbUid = activeFirebaseUid()
        return if (db != null && fbUid != null && isAdmin) {
            val remoteFlow = db.collection("forum_user_moderation")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ForumUserModeration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { e ->
                    handleForumFirestoreError(e, ForumOperationType.LIST, "forum_user_moderation")
                    emit(emptyList())
                }
            combine(remoteFlow, _localUserModerations) { remote, local ->
                val byId = LinkedHashMap<String, ForumUserModeration>()
                local.values.forEach { byId[it.userId] = it }
                remote.forEach { byId[it.userId] = it }
                byId.values.sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            }
        } else {
            _localUserModerations.map { it.values.toList() }
        }
    }

    fun observeUserModeration(userId: String): Flow<ForumUserModeration?> {
        val db = activeDb()
        val fbUid = activeFirebaseUid()
        return if (db != null && fbUid != null && fbUid == userId) {
            val remoteFlow = db.collection("forum_user_moderation").document(userId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(ForumUserModeration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { e ->
                    handleForumFirestoreError(e, ForumOperationType.GET, "forum_user_moderation/$userId")
                    emit(null)
                }
            combine(remoteFlow, _localUserModerations) { remote, local ->
                remote ?: local[userId]
            }
        } else {
            _localUserModerations.map { it[userId] }
        }
    }

    private fun checkUserCanParticipate(userId: String): ForumUserModeration? {
        val record = _localUserModerations.value[userId]
        return if (record != null && record.isCurrentlyRestricted()) record else null
    }

    suspend fun createPost(
        authorId: String,
        authorName: String,
        authorRole: String,
        title: String,
        content: String,
        topic: ForumTopic,
        cropId: String,
        cropName: String
    ): Result<ForumPost> {
        return try {
            val restriction = checkUserCanParticipate(authorId)
            if (restriction != null) {
                return Result.failure(IllegalStateException("MODERATION_RESTRICTED:${restriction.status}:${restriction.moderationReason}"))
            }

            val cleanTitle = title.trim()
            val cleanContent = content.trim()
            require(cleanTitle.length in 3..160) { "Post title must be between 3 and 160 characters." }
            require(cleanContent.length in 5..3000) { "Post content must be between 5 and 3000 characters." }

            val db = activeDb()
            val fbUid = activeFirebaseUid()
            val postId = if (db != null && fbUid != null) {
                db.collection("forum_posts").document().id
            } else {
                "post_${UUID.randomUUID().toString().take(10)}"
            }

            val effectiveAuthorId = fbUid ?: authorId
            val nowTs = Timestamp.now()
            val newPost = ForumPost(
                id = postId,
                authorId = effectiveAuthorId,
                authorName = authorName.ifBlank { "Farmer" }.take(100),
                authorRole = authorRole.ifBlank { "FARMER" }.take(30),
                title = cleanTitle,
                content = cleanContent,
                topic = topic.code,
                cropId = cropId.ifBlank { "general" }.take(80),
                cropName = cropName.ifBlank { "General Farming" }.take(120),
                likesCount = 0L,
                commentsCount = 0L,
                reportCount = 0L,
                moderationStatus = ForumModerationStatus.APPROVED.code,
                pinned = false,
                moderationReason = "",
                createdAt = nowTs,
                updatedAt = nowTs
            )

            if (db != null && fbUid != null) {
                db.collection("forum_posts").document(postId)
                    .set(newPost.toFirestoreCreateMap())
                    .await()
            }

            savePostsLocal(listOf(newPost) + _localPosts.value.filterNot { it.id == postId })
            Result.success(newPost)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.CREATE, "forum_posts")
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String, requesterId: String, isAdmin: Boolean): Result<Unit> {
        return try {
            val target = _localPosts.value.firstOrNull { it.id == postId }
            if (target != null && target.authorId != requesterId && !isAdmin) {
                return Result.failure(SecurityException("Only author or admin can delete this post."))
            }

            val db = activeDb()
            val fbUid = activeFirebaseUid()
            if (db != null && fbUid != null && !postId.startsWith("starter_")) {
                db.collection("forum_posts").document(postId).delete().await()
            }

            savePostsLocal(_localPosts.value.filterNot { it.id == postId })
            val updatedComments = _localCommentsByPost.value.toMutableMap()
            updatedComments.remove(postId)
            saveCommentsLocal(updatedComments)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.DELETE, "forum_posts/$postId")
            Result.failure(e)
        }
    }

    suspend fun toggleLike(postId: String, userId: String): Result<Boolean> {
        return try {
            val currentLikes = _localLikedPostIds.value.toMutableSet()
            val currentlyLiked = postId in currentLikes
            val delta = if (currentlyLiked) -1L else 1L

            val db = activeDb()
            val fbUid = activeFirebaseUid()
            if (db != null && fbUid != null && !postId.startsWith("starter_")) {
                val likeRef = db.collection("forum_posts").document(postId).collection("likes").document(fbUid)
                val postRef = db.collection("forum_posts").document(postId)
                if (currentlyLiked) {
                    likeRef.delete().await()
                } else {
                    likeRef.set(mapOf("userId" to fbUid, "createdAt" to FieldValue.serverTimestamp())).await()
                }
                postRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(delta),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (currentlyLiked) currentLikes.remove(postId) else currentLikes.add(postId)
            saveLikedPostIdsLocal(currentLikes)

            val updatedPosts = _localPosts.value.map { post ->
                if (post.id == postId) {
                    post.copy(likesCount = (post.likesCount + delta).coerceAtLeast(0L))
                } else post
            }
            savePostsLocal(updatedPosts)
            Result.success(!currentlyLiked)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.UPDATE, "forum_posts/$postId")
            Result.failure(e)
        }
    }

    suspend fun addComment(
        postId: String,
        authorId: String,
        authorName: String,
        authorRole: String,
        content: String
    ): Result<ForumComment> {
        return try {
            val restriction = checkUserCanParticipate(authorId)
            if (restriction != null) {
                return Result.failure(IllegalStateException("MODERATION_RESTRICTED:${restriction.status}:${restriction.moderationReason}"))
            }

            val cleanContent = content.trim()
            require(cleanContent.length in 1..1500) { "Comment cannot be empty or exceed 1500 characters." }

            val db = activeDb()
            val fbUid = activeFirebaseUid()
            val commentId = if (db != null && fbUid != null && !postId.startsWith("starter_")) {
                db.collection("forum_posts").document(postId).collection("comments").document().id
            } else {
                "cmt_${UUID.randomUUID().toString().take(10)}"
            }

            val effectiveAuthorId = fbUid ?: authorId
            val nowTs = Timestamp.now()
            val comment = ForumComment(
                id = commentId,
                postId = postId,
                authorId = effectiveAuthorId,
                authorName = authorName.ifBlank { "Farmer" }.take(100),
                authorRole = authorRole.ifBlank { "FARMER" }.take(30),
                content = cleanContent,
                moderationStatus = ForumModerationStatus.APPROVED.code,
                reportCount = 0L,
                moderationReason = "",
                createdAt = nowTs,
                updatedAt = nowTs
            )

            if (db != null && fbUid != null && !postId.startsWith("starter_")) {
                val postRef = db.collection("forum_posts").document(postId)
                postRef.collection("comments").document(commentId)
                    .set(comment.toFirestoreCreateMap())
                    .await()
                postRef.update(
                    mapOf(
                        "commentsCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val currentMap = _localCommentsByPost.value.toMutableMap()
            val existingList = currentMap[postId].orEmpty().filterNot { it.id == commentId }
            currentMap[postId] = existingList + comment
            saveCommentsLocal(currentMap)

            savePostsLocal(_localPosts.value.map { post ->
                if (post.id == postId) post.copy(commentsCount = post.commentsCount + 1L) else post
            })
            Result.success(comment)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.CREATE, "forum_posts/$postId/comments")
            Result.failure(e)
        }
    }

    suspend fun deleteComment(
        postId: String,
        commentId: String,
        requesterId: String,
        isAdmin: Boolean
    ): Result<Unit> {
        return try {
            val currentMap = _localCommentsByPost.value.toMutableMap()
            val existing = currentMap[postId].orEmpty()
            val target = existing.firstOrNull { it.id == commentId }
            if (target != null && target.authorId != requesterId && !isAdmin) {
                return Result.failure(SecurityException("Only author or admin can delete this comment."))
            }

            val db = activeDb()
            val fbUid = activeFirebaseUid()
            if (db != null && fbUid != null && !postId.startsWith("starter_") && !commentId.startsWith("starter_")) {
                val postRef = db.collection("forum_posts").document(postId)
                postRef.collection("comments").document(commentId).delete().await()
                postRef.update(
                    mapOf(
                        "commentsCount" to FieldValue.increment(-1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            currentMap[postId] = existing.filterNot { it.id == commentId }
            saveCommentsLocal(currentMap)
            savePostsLocal(_localPosts.value.map { post ->
                if (post.id == postId) post.copy(commentsCount = (post.commentsCount - 1L).coerceAtLeast(0L)) else post
            })
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.DELETE, "forum_posts/$postId/comments/$commentId")
            Result.failure(e)
        }
    }

    suspend fun submitReport(
        targetType: String,
        postId: String,
        commentId: String = "",
        targetAuthorId: String,
        targetAuthorName: String,
        targetPreview: String,
        reporterId: String,
        reporterName: String,
        reason: ForumReportReason,
        details: String
    ): Result<ForumReport> {
        return try {
            val db = activeDb()
            val fbUid = activeFirebaseUid()
            val reportId = if (db != null && fbUid != null) {
                db.collection("forum_reports").document().id
            } else {
                "rep_${UUID.randomUUID().toString().take(10)}"
            }

            val effectiveReporterId = fbUid ?: reporterId
            val nowTs = Timestamp.now()
            val report = ForumReport(
                id = reportId,
                targetType = targetType,
                postId = postId,
                commentId = commentId,
                targetAuthorId = targetAuthorId.ifBlank { "unknown_user" },
                targetAuthorName = targetAuthorName.ifBlank { "Farmer" }.take(100),
                targetPreview = targetPreview.trim().take(280),
                reporterId = effectiveReporterId,
                reporterName = reporterName.ifBlank { "Farmer" }.take(100),
                reason = reason.code,
                details = details.trim().take(500),
                status = ForumReportStatus.PENDING.code,
                adminActionNote = "",
                createdAt = nowTs,
                updatedAt = nowTs
            )

            if (db != null && fbUid != null) {
                db.collection("forum_reports").document(reportId)
                    .set(report.toFirestoreCreateMap())
                    .await()
                if (!postId.startsWith("starter_")) {
                    if (targetType == "POST") {
                        db.collection("forum_posts").document(postId).update(
                            mapOf(
                                "reportCount" to FieldValue.increment(1L),
                                "moderationStatus" to ForumModerationStatus.FLAGGED.code,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    } else if (commentId.isNotBlank() && !commentId.startsWith("starter_")) {
                        db.collection("forum_posts").document(postId)
                            .collection("comments").document(commentId)
                            .update(
                                mapOf(
                                    "reportCount" to FieldValue.increment(1L),
                                    "moderationStatus" to ForumModerationStatus.FLAGGED.code,
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                            ).await()
                    }
                }
            }

            saveReportsLocal(listOf(report) + _localReports.value)
            if (targetType == "POST") {
                savePostsLocal(_localPosts.value.map { p ->
                    if (p.id == postId) {
                        val nextStatus = if (p.moderationStatus == ForumModerationStatus.HIDDEN.code) p.moderationStatus else ForumModerationStatus.FLAGGED.code
                        p.copy(reportCount = p.reportCount + 1L, moderationStatus = nextStatus)
                    } else p
                })
            } else if (commentId.isNotBlank()) {
                val map = _localCommentsByPost.value.toMutableMap()
                map[postId] = map[postId].orEmpty().map { c ->
                    if (c.id == commentId) {
                        val nextStatus = if (c.moderationStatus == ForumModerationStatus.HIDDEN.code) c.moderationStatus else ForumModerationStatus.FLAGGED.code
                        c.copy(reportCount = c.reportCount + 1L, moderationStatus = nextStatus)
                    } else c
                }
                saveCommentsLocal(map)
            }
            Result.success(report)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.CREATE, "forum_reports")
            Result.failure(e)
        }
    }

    // =========================================================================
    // ADMIN MODERATION OPERATIONS
    // =========================================================================

    suspend fun adminPinPost(postId: String, pinned: Boolean): Result<Unit> {
        return try {
            val db = activeDb()
            val fbUid = activeFirebaseUid()
            if (db != null && fbUid != null && !postId.startsWith("starter_")) {
                db.collection("forum_posts").document(postId).update(
                    mapOf(
                        "pinned" to pinned,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            savePostsLocal(_localPosts.value.map { if (it.id == postId) it.copy(pinned = pinned) else it })
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.UPDATE, "forum_posts/$postId")
            Result.failure(e)
        }
    }

    suspend fun adminUpdatePostModeration(
        postId: String,
        status: ForumModerationStatus,
        reason: String
    ): Result<Unit> {
        return try {
            val cleanReason = reason.trim().take(300)
            val db = activeDb()
            val fbUid = activeFirebaseUid()
            if (db != null && fbUid != null && !postId.startsWith("starter_")) {
                db.collection("forum_posts").document(postId).update(
                    mapOf(
                        "moderationStatus" to status.code,
                        "moderationReason" to cleanReason,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            savePostsLocal(_localPosts.value.map {
                if (it.id == postId) it.copy(moderationStatus = status.code, moderationReason = cleanReason) else it
            })
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.UPDATE, "forum_posts/$postId")
            Result.failure(e)
        }
    }

    suspend fun adminUpdateCommentModeration(
        postId: String,
        commentId: String,
        status: ForumModerationStatus,
        reason: String
    ): Result<Unit> {
        return try {
            val cleanReason = reason.trim().take(300)
            val db = activeDb()
            val fbUid = activeFirebaseUid()
            if (db != null && fbUid != null && !postId.startsWith("starter_") && !commentId.startsWith("starter_")) {
                db.collection("forum_posts").document(postId)
                    .collection("comments").document(commentId)
                    .update(
                        mapOf(
                            "moderationStatus" to status.code,
                            "moderationReason" to cleanReason,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            }
            val map = _localCommentsByPost.value.toMutableMap()
            map[postId] = map[postId].orEmpty().map {
                if (it.id == commentId) it.copy(moderationStatus = status.code, moderationReason = cleanReason) else it
            }
            saveCommentsLocal(map)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.UPDATE, "forum_posts/$postId/comments/$commentId")
            Result.failure(e)
        }
    }

    suspend fun adminResolveReport(
        reportId: String,
        status: ForumReportStatus,
        actionNote: String
    ): Result<Unit> {
        return try {
            val cleanNote = actionNote.trim().take(300)
            val db = activeDb()
            val fbUid = activeFirebaseUid()
            if (db != null && fbUid != null) {
                db.collection("forum_reports").document(reportId).update(
                    mapOf(
                        "status" to status.code,
                        "adminActionNote" to cleanNote,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            saveReportsLocal(_localReports.value.map {
                if (it.id == reportId) it.copy(status = status.code, adminActionNote = cleanNote) else it
            })
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.UPDATE, "forum_reports/$reportId")
            Result.failure(e)
        }
    }

    suspend fun adminSetUserBehaviorStatus(
        targetUserId: String,
        targetUserName: String,
        newStatus: UserModerationStatus,
        reason: String,
        muteDurationHours: Long = 0L,
        adminId: String
    ): Result<ForumUserModeration> {
        return try {
            val existing = _localUserModerations.value[targetUserId]
            val nextWarnings = when (newStatus) {
                UserModerationStatus.WARNED, UserModerationStatus.MUTED, UserModerationStatus.BANNED ->
                    (existing?.warningCount ?: 0L) + 1L
                UserModerationStatus.ACTIVE -> existing?.warningCount ?: 0L
            }
            val mutedUntil = if (newStatus == UserModerationStatus.MUTED && muteDurationHours > 0L) {
                System.currentTimeMillis() + muteDurationHours * 3600_000L
            } else 0L

            val nowTs = Timestamp.now()
            val updatedRecord = ForumUserModeration(
                id = targetUserId,
                userId = targetUserId,
                userName = targetUserName.ifBlank { existing?.userName ?: "Farmer" }.take(100),
                status = newStatus.code,
                warningCount = nextWarnings.coerceAtMost(100L),
                moderationReason = reason.trim().take(300),
                mutedUntilMillis = mutedUntil,
                updatedByAdmin = adminId.take(128),
                createdAt = existing?.createdAt ?: nowTs,
                updatedAt = nowTs
            )

            val db = activeDb()
            val fbUid = activeFirebaseUid()
            if (db != null && fbUid != null) {
                val docRef = db.collection("forum_user_moderation").document(targetUserId)
                val snap = docRef.get().await()
                if (snap.exists()) {
                    docRef.update(
                        mapOf(
                            "userName" to updatedRecord.userName,
                            "status" to updatedRecord.status,
                            "warningCount" to updatedRecord.warningCount,
                            "moderationReason" to updatedRecord.moderationReason,
                            "mutedUntilMillis" to updatedRecord.mutedUntilMillis,
                            "updatedByAdmin" to updatedRecord.updatedByAdmin,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } else {
                    docRef.set(updatedRecord.toFirestoreWriteMap(isNew = true)).await()
                }
            }

            val nextMap = _localUserModerations.value.toMutableMap()
            nextMap[targetUserId] = updatedRecord
            saveUserModerationsLocal(nextMap)
            Result.success(updatedRecord)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleForumFirestoreError(e, ForumOperationType.WRITE, "forum_user_moderation/$targetUserId")
            Result.failure(e)
        }
    }

    private fun parsePostsJson(raw: String): List<ForumPost> {
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        ForumPost(
                            id = o.optString("id"),
                            authorId = o.optString("authorId"),
                            authorName = o.optString("authorName"),
                            authorRole = o.optString("authorRole", "FARMER"),
                            title = o.optString("title"),
                            content = o.optString("content"),
                            topic = o.optString("topic", ForumTopic.CROP_CARE.code),
                            cropId = o.optString("cropId", "general"),
                            cropName = o.optString("cropName", "General Farming"),
                            likesCount = o.optLong("likesCount", 0L),
                            commentsCount = o.optLong("commentsCount", 0L),
                            reportCount = o.optLong("reportCount", 0L),
                            moderationStatus = o.optString("moderationStatus", ForumModerationStatus.APPROVED.code),
                            pinned = o.optBoolean("pinned", false),
                            moderationReason = o.optString("moderationReason", ""),
                            createdAt = toTimestamp(o.optLong("createdAtMillis", System.currentTimeMillis())),
                            updatedAt = toTimestamp(o.optLong("updatedAtMillis", System.currentTimeMillis()))
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing posts json", e)
            emptyList()
        }
    }

    private fun parseCommentsJson(raw: String): Map<String, List<ForumComment>> {
        return try {
            val root = JSONObject(raw)
            val result = mutableMapOf<String, List<ForumComment>>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val postId = keys.next()
                val arr = root.getJSONArray(postId)
                val list = buildList {
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        add(
                            ForumComment(
                                id = o.optString("id"),
                                postId = o.optString("postId", postId),
                                authorId = o.optString("authorId"),
                                authorName = o.optString("authorName"),
                                authorRole = o.optString("authorRole", "FARMER"),
                                content = o.optString("content"),
                                moderationStatus = o.optString("moderationStatus", ForumModerationStatus.APPROVED.code),
                                reportCount = o.optLong("reportCount", 0L),
                                moderationReason = o.optString("moderationReason", ""),
                                createdAt = toTimestamp(o.optLong("createdAtMillis", System.currentTimeMillis())),
                                updatedAt = toTimestamp(o.optLong("updatedAtMillis", System.currentTimeMillis()))
                            )
                        )
                    }
                }
                result[postId] = list
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing comments json", e)
            emptyMap()
        }
    }

    private fun parseReportsJson(raw: String): List<ForumReport> {
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        ForumReport(
                            id = o.optString("id"),
                            targetType = o.optString("targetType", "POST"),
                            postId = o.optString("postId"),
                            commentId = o.optString("commentId", ""),
                            targetAuthorId = o.optString("targetAuthorId"),
                            targetAuthorName = o.optString("targetAuthorName"),
                            targetPreview = o.optString("targetPreview", ""),
                            reporterId = o.optString("reporterId"),
                            reporterName = o.optString("reporterName"),
                            reason = o.optString("reason", ForumReportReason.SPAM.code),
                            details = o.optString("details", ""),
                            status = o.optString("status", ForumReportStatus.PENDING.code),
                            adminActionNote = o.optString("adminActionNote", ""),
                            createdAt = toTimestamp(o.optLong("createdAtMillis", System.currentTimeMillis())),
                            updatedAt = toTimestamp(o.optLong("updatedAtMillis", System.currentTimeMillis()))
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing reports json", e)
            emptyList()
        }
    }

    private fun parseUserModerationJson(raw: String): Map<String, ForumUserModeration> {
        return try {
            val arr = JSONArray(raw)
            val result = mutableMapOf<String, ForumUserModeration>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val uid = o.optString("userId")
                if (uid.isNotBlank()) {
                    result[uid] = ForumUserModeration(
                        id = uid,
                        userId = uid,
                        userName = o.optString("userName"),
                        status = o.optString("status", UserModerationStatus.ACTIVE.code),
                        warningCount = o.optLong("warningCount", 0L),
                        moderationReason = o.optString("moderationReason", ""),
                        mutedUntilMillis = o.optLong("mutedUntilMillis", 0L),
                        updatedByAdmin = o.optString("updatedByAdmin", ""),
                        createdAt = toTimestamp(o.optLong("createdAtMillis", System.currentTimeMillis())),
                        updatedAt = toTimestamp(o.optLong("updatedAtMillis", System.currentTimeMillis()))
                    )
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing user moderation json", e)
            emptyMap()
        }
    }
}
