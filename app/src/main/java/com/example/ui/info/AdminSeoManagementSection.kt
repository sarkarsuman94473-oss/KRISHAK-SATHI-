package com.example.ui.info

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SeoArticleEntity
import com.example.data.model.AppLanguage
import com.example.data.model.SeoArticleStatus
import com.example.data.model.SeoContentCategory
import com.example.data.model.SeoReferralSummary
import com.example.data.model.SeoTechnicalAuditResult
import com.example.data.repository.SeoDiscoveryRepository
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenHighlight
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.royalOutlinedTextFieldColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun AdminSeoManagementSection(
    currentLanguage: AppLanguage,
    adminIdentifier: String = "krishak_admin"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) { SeoDiscoveryRepository(context) }

    LaunchedEffect(Unit) {
        repository.ensureSeeded()
    }

    val articles by repository.observeAllArticles().collectAsStateWithLifecycle(initialValue = emptyList())
    val seoConfig by repository.observeConfig().collectAsStateWithLifecycle(initialValue = repository.defaultConfig())
    val referralSummary by repository.observeReferralSummary().collectAsStateWithLifecycle(
        initialValue = SeoReferralSummary(0, emptyMap(), emptyList())
    )

    var subTab by remember { mutableIntStateOf(0) }

    // Article / Draft Composer states
    var draftTitleEn by remember { mutableStateOf("") }
    var draftTitleBn by remember { mutableStateOf("") }
    var draftSlug by remember { mutableStateOf("") }
    var draftCropId by remember { mutableStateOf("") }
    var draftSummaryEn by remember { mutableStateOf("") }
    var draftSourceName by remember { mutableStateOf("ICAR / BCKV / UBKV Verified Advisory") }
    var draftSourceUrl by remember { mutableStateOf("https://icar.org.in/") }

    // Technical SEO fields
    var baseWebUrl by remember(seoConfig.baseWebUrl) { mutableStateOf(seoConfig.baseWebUrl) }
    var gscToken by remember(seoConfig.googleSearchConsoleToken) { mutableStateOf(seoConfig.googleSearchConsoleToken) }
    var allowGooglebot by remember(seoConfig.allowGooglebot) { mutableStateOf(seoConfig.allowGooglebot) }
    var allowOaiSearchBot by remember(seoConfig.allowOaiSearchBot) { mutableStateOf(seoConfig.allowOaiSearchBot) }
    var allowGptBot by remember(seoConfig.allowGptBot) { mutableStateOf(seoConfig.allowGptBot) }
    var auditResult by remember { mutableStateOf<SeoTechnicalAuditResult?>(null) }
    val auditCardRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(auditResult) {
        if (auditResult != null) {
            auditCardRequester.bringIntoView()
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().testTag("admin_seo_section")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "SEO, পাবলিক জ্ঞান ও AI ডিসকভারি কন্ট্রোল"
                            AppLanguage.HINDI -> "SEO, सार्वजनिक ज्ञान व AI डिस्कवरी नियंत्रण"
                            AppLanguage.ENGLISH -> "SEO, Public Knowledge & AI Search Discovery"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RoyalTextPrimary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "আর্টিকেল, AI খসড়া অনুমোদন, robots.txt, sitemap ও GSC"
                            AppLanguage.HINDI -> "लेख, AI ड्राफ्ट समीक्षा, robots.txt, sitemap व GSC"
                            AppLanguage.ENGLISH -> "Manage articles, AI draft review gating, robots.txt, sitemap & GSC"
                        },
                        fontSize = 11.5.sp,
                        color = RoyalTextSecondary
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to (if (currentLanguage == AppLanguage.BENGALI) "আর্টিকেল ও খসড়া" else if (currentLanguage == AppLanguage.HINDI) "लेख व ड्राफ्ट" else "Articles & Drafts"),
                    1 to (if (currentLanguage == AppLanguage.BENGALI) "ফসল SEO" else if (currentLanguage == AppLanguage.HINDI) "फसल SEO" else "Crop SEO"),
                    2 to "Robots / Sitemap",
                    3 to (if (currentLanguage == AppLanguage.BENGALI) "রেফারেল পরিসংখ্যান" else if (currentLanguage == AppLanguage.HINDI) "रेफरल विश्लेषण" else "Analytics")
                ).forEach { (idx, label) ->
                    val tag = when (idx) {
                        0 -> "admin_seo_tab_articles"
                        1 -> "admin_seo_tab_crops"
                        2 -> "admin_seo_tab_technical"
                        else -> "admin_seo_tab_analytics"
                    }
                    FilterChip(
                        selected = subTab == idx,
                        onClick = { subTab = idx },
                        modifier = Modifier.testTag(tag),
                        label = { Text(label, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreenPrimary,
                            selectedLabelColor = RoyalSoftGold
                        )
                    )
                }
            }

            when (subTab) {
                0 -> {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "নতুন AI-সহায়তা খসড়া তৈরি (প্রকাশের আগে অ্যাডমিন অনুমোদন আবশ্যক)"
                            AppLanguage.HINDI -> "नया AI-सहायता ड्राफ्ट बनाएं (प्रकाशन से पहले एडमिन स्वीकृति आवश्यक)"
                            AppLanguage.ENGLISH -> "Create AI-Assisted Draft (Strictly Held Pending Review Until Approved)"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = RoyalSoftGold
                    )

                    OutlinedTextField(
                        value = draftTitleEn,
                        onValueChange = { draftTitleEn = it },
                        label = { Text("Article Title (English)") },
                        colors = royalOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_seo_draft_title_input")
                    )
                    OutlinedTextField(
                        value = draftTitleBn,
                        onValueChange = { draftTitleBn = it },
                        label = { Text("বাংলা শিরোনাম (Bengali Title)") },
                        colors = royalOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftSlug,
                        onValueChange = { draftSlug = it },
                        label = { Text("URL Slug (e.g. winter-irrigation-guide)") },
                        colors = royalOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_seo_draft_slug_input")
                    )
                    OutlinedTextField(
                        value = draftSummaryEn,
                        onValueChange = { draftSummaryEn = it },
                        label = { Text("SEO Summary / Meta Description (<=158 chars)") },
                        colors = royalOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftSourceName,
                        onValueChange = { draftSourceName = it },
                        label = { Text("Trusted Institution Name (ICAR/BCKV/UBKV/TRA)") },
                        colors = royalOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftSourceUrl,
                        onValueChange = { draftSourceUrl = it },
                        label = { Text("Verified HTTPS Source URL") },
                        colors = royalOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (draftTitleEn.isBlank() || draftSlug.isBlank() || draftSummaryEn.isBlank()) {
                                Toast.makeText(context, "Enter title, slug, and summary", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                val res = repository.createAiAssistedDraft(
                                    titleEn = draftTitleEn,
                                    titleBn = draftTitleBn,
                                    titleHi = draftTitleEn,
                                    slug = draftSlug,
                                    category = SeoContentCategory.ORGANIC_IPM,
                                    cropId = draftCropId,
                                    summaryEn = draftSummaryEn,
                                    summaryBn = draftSummaryEn,
                                    summaryHi = draftSummaryEn,
                                    bodyEn = draftSummaryEn,
                                    bodyBn = draftSummaryEn,
                                    bodyHi = draftSummaryEn,
                                    sourceName = draftSourceName,
                                    sourceUrl = draftSourceUrl
                                )
                                res.onSuccess {
                                    draftTitleEn = ""
                                    draftTitleBn = ""
                                    draftSlug = ""
                                    draftSummaryEn = ""
                                    Toast.makeText(context, "AI draft saved as AI_DRAFT_PENDING_REVIEW", Toast.LENGTH_SHORT).show()
                                }.onFailure { err ->
                                    Toast.makeText(context, err.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("admin_seo_create_draft_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save AI Draft (Pending Review)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Articles (${articles.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RoyalSoftGold
                    )

                    articles.take(8).forEach { art ->
                        AdminSeoArticleRow(
                            article = art,
                            onApprovePublish = {
                                scope.launch {
                                    repository.approveAndPublishDraft(art.id, adminIdentifier)
                                        .onSuccess { Toast.makeText(context, "Published ${it.slug}", Toast.LENGTH_SHORT).show() }
                                }
                            }
                        )
                    }
                }

                1 -> {
                    val cropArticles = articles.filter { it.cropId.isNotBlank() }
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ফসল ভিত্তিক SEO মেটা ও হালনাগাদ তারিখ (${cropArticles.size})"
                            AppLanguage.HINDI -> "फसल SEO मेटा व अंतिम अपडेट (${cropArticles.size})"
                            AppLanguage.ENGLISH -> "Crop SEO Metadata & Institutional Citations (${cropArticles.size})"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = RoyalSoftGold
                    )
                    cropArticles.forEach { art ->
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RoyalBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${art.cropId.uppercase()} — ${art.title(currentLanguage)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalTextPrimary)
                                Text("Canonical: ${art.canonicalUrl}", fontSize = 11.sp, color = RoyalTextSecondary)
                                Text("Source: ${art.sourceName} | Updated: ${art.lastUpdatedDate}", fontSize = 11.sp, color = RoyalSoftGold)
                            }
                        }
                    }
                }

                2 -> {
                    OutlinedTextField(
                        value = baseWebUrl,
                        onValueChange = { baseWebUrl = it },
                        label = { Text("Public Knowledge Hub Base HTTPS URL") },
                        colors = royalOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = gscToken,
                        onValueChange = { gscToken = it },
                        label = { Text("Google Search Console Verification Meta Token (Optional)") },
                        colors = royalOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_seo_gsc_token_input")
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Allow Googlebot (Public Knowledge & Crops)", fontSize = 12.5.sp, color = RoyalTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = allowGooglebot, onCheckedChange = { allowGooglebot = it })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Allow OAI-SearchBot (ChatGPT Search Discovery)", fontSize = 12.5.sp, color = RoyalTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = allowOaiSearchBot, onCheckedChange = { allowOaiSearchBot = it })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Allow GPTBot (Separate Training Directive)", fontSize = 12.5.sp, color = RoyalTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = allowGptBot, onCheckedChange = { allowGptBot = it })
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val updated = seoConfig.copy(
                                        baseWebUrl = baseWebUrl,
                                        googleSearchConsoleToken = gscToken,
                                        allowGooglebot = allowGooglebot,
                                        allowOaiSearchBot = allowOaiSearchBot,
                                        allowGptBot = allowGptBot
                                    )
                                    repository.saveSeoConfig(updated, adminIdentifier)
                                        .onSuccess {
                                            auditResult = repository.runTechnicalAudit(it, articles)
                                            Toast.makeText(context, "SEO Crawler & GSC config saved", Toast.LENGTH_SHORT).show()
                                        }
                                        .onFailure { err ->
                                            Toast.makeText(context, err.message ?: "Invalid URL", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_seo_save_config_btn")
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save SEO Config", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                val current = seoConfig.copy(
                                    baseWebUrl = baseWebUrl,
                                    googleSearchConsoleToken = gscToken,
                                    allowGooglebot = allowGooglebot,
                                    allowOaiSearchBot = allowOaiSearchBot,
                                    allowGptBot = allowGptBot
                                )
                                auditResult = repository.runTechnicalAudit(current, articles)
                            },
                            border = BorderStroke(1.dp, RoyalGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalSoftGold),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_seo_run_audit_btn")
                        ) {
                            Text("Validate Robots & Sitemap", fontSize = 12.sp)
                        }
                    }

                    auditResult?.let { audit ->
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RoyalBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(auditCardRequester)
                                .testTag("admin_seo_audit_result_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "robots.txt: ${if (audit.robotsTxtValid) "VALID" else "NEEDS REVIEW"} | sitemap.xml: ${if (audit.sitemapValid) "VALID" else "NEEDS REVIEW"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (audit.robotsTxtValid && audit.sitemapValid) RoyalGreenHighlight else RoyalSoftGold
                                )
                                Text(
                                    text = "Published: ${audit.publishedCount} | Pending AI Drafts: ${audit.pendingReviewDrafts} | Protected /admin & /users blocked: ${audit.adminAndPrivateDisallowed}",
                                    fontSize = 11.5.sp,
                                    color = RoyalTextPrimary
                                )
                                audit.notes.forEach { n ->
                                    Text("• $n", fontSize = 11.sp, color = RoyalTextSecondary)
                                }
                            }
                        }
                    }
                }

                else -> {
                    Surface(
                        color = RoyalGreenSecondaryBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Privacy-Safe Referral & Discovery Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalTextPrimary)
                            }
                            Text(
                                text = "Total logged visits (zero PII stored): ${referralSummary.totalVisits}",
                                fontSize = 12.sp,
                                color = RoyalSoftGold
                            )
                            if (referralSummary.countsByBucket.isEmpty()) {
                                Text("No local referrals recorded yet.", fontSize = 12.sp, color = RoyalTextSecondary)
                            } else {
                                referralSummary.countsByBucket.forEach { (bucket, count) ->
                                    Text("• $bucket: $count", fontSize = 12.sp, color = RoyalTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSeoArticleRow(
    article: SeoArticleEntity,
    onApprovePublish: () -> Unit
) {
    val isPendingReview = article.statusEnum() == SeoArticleStatus.AI_DRAFT_PENDING_REVIEW
    Surface(
        color = RoyalGreenSecondaryBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isPendingReview) RoyalGold else RoyalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.titleEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = RoyalTextPrimary,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Surface(
                    color = if (isPendingReview) RoyalGold.copy(alpha = 0.2f) else RoyalGreenHighlight.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = article.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPendingReview) RoyalSoftGold else RoyalGreenHighlight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text("Slug: /knowledge/${article.slug}.html | Source: ${article.sourceName}", fontSize = 11.sp, color = RoyalTextSecondary)
            if (isPendingReview) {
                Button(
                    onClick = onApprovePublish,
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_seo_approve_draft_${article.id}")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Approve & Publish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
