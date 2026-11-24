package com.example.ui.machinery

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.MachineryCategory
import com.example.data.repository.MachineryRepository
import com.example.ui.theme.MachineryCardBadgeBg
import com.example.ui.theme.MachineryCardBadgeText
import com.example.ui.theme.MachineryCardBodyInk
import com.example.ui.theme.MachineryCardBorder
import com.example.ui.theme.MachineryCardTitleInk
import com.example.ui.theme.MachineryCardWarnBg
import com.example.ui.theme.MachineryCardWarnText
import com.example.ui.theme.MachineryCardWhite
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.royalOutlinedTextFieldColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MachineryManufacturersScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    repository: MachineryRepository? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val repo = remember(repository) { repository ?: MachineryRepository(context) }

    LaunchedEffect(repo) { repo.ensureSeeded() }

    val manufacturers by repo.observeAllManufacturers().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filtered = remember(manufacturers, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        manufacturers.filter { oem ->
            val matchCat = selectedCategory == null ||
                oem.supportedCategoriesCsv.contains(selectedCategory!!, ignoreCase = true)
            val matchQuery = q.isBlank() ||
                oem.nameEn.lowercase().contains(q) ||
                oem.nameBn.lowercase().contains(q) ||
                oem.nameHi.lowercase().contains(q) ||
                oem.focusSummaryEn.lowercase().contains(q) ||
                oem.countryOrOrigin.lowercase().contains(q)
            matchCat && matchQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("machinery_manufacturers_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalGreenSecondaryBg,
                    titleContentColor = RoyalTextPrimary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("machinery_manufacturers_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RoyalTextPrimary)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "যাচাইকৃত কৃষি যন্ত্র প্রস্তুতকারক (${manufacturers.size})"
                                AppLanguage.HINDI -> "सत्यापित कृषि यंत्र निर्माता (${manufacturers.size})"
                                AppLanguage.ENGLISH -> "Verified OEM Manufacturers (${manufacturers.size})"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "অফিসিয়াল ওয়েবসাইট, ডিলার লোকেটর ও সার্ভিস"
                                AppLanguage.HINDI -> "आधिकारिक वेबसाइट, डीलर लोकेटर व सर्विस"
                                AppLanguage.ENGLISH -> "Official Portals, Dealer Locators & Helplines"
                            },
                            fontSize = 10.sp,
                            color = RoyalSoftGold,
                            maxLines = 1
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RoyalGreenPrimaryBg)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().widthIn(max = 880.dp),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        color = MachineryCardWarnBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MachineryCardWarnText.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MachineryCardWarnText, modifier = Modifier.size(16.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "শুধুমাত্র অফিসিয়াল তথ্য লিঙ্ক — কোনো ব্যবসায়িক অংশীদারিত্ব দাবি করা হয়নি।"
                                    AppLanguage.HINDI -> "केवल आधिकारिक जानकारी लिंक — कोई व्यावसायिक साझेदारी दावा नहीं।"
                                    AppLanguage.ENGLISH -> "Official info links only — verify local authorised dealers directly on each manufacturer's portal."
                                },
                                fontSize = 11.sp,
                                color = MachineryCardWarnText
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        placeholder = { Text("Search Mahindra, VST, Honda, Stihl, Garuda, Kisankraft...") },
                        singleLine = true,
                        colors = royalOutlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("machinery_manufacturers_search")
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All (${manufacturers.size})", fontSize = 11.sp) }
                            )
                        }
                        items(MachineryCategory.values().toList(), key = { it.code }) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat.code,
                                onClick = { selectedCategory = if (selectedCategory == cat.code) null else cat.code },
                                label = { Text("${cat.emoji} ${cat.getDisplayName(currentLanguage)}", fontSize = 10.sp) }
                            )
                        }
                    }
                }

                items(filtered, key = { it.id }) { oem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
                        border = BorderStroke(1.dp, MachineryCardBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("oem_card_${oem.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(oem.getDisplayName(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MachineryCardTitleInk)
                                    Text("Origin: ${oem.countryOrOrigin}", fontSize = 11.sp, color = MachineryCardBodyInk)
                                }
                                Surface(color = MachineryCardBadgeBg, shape = RoundedCornerShape(6.dp)) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = MachineryCardBadgeText, modifier = Modifier.size(11.dp))
                                        Text("Official Link", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MachineryCardBadgeText)
                                    }
                                }
                            }

                            if (oem.focusSummaryEn.isNotBlank()) {
                                Text(oem.focusSummaryEn, fontSize = 11.sp, lineHeight = 16.sp, color = MachineryCardBodyInk)
                            }
                            if (oem.customerCareHelpline.isNotBlank()) {
                                Text("Helpline: ${oem.customerCareHelpline}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MachineryCardBadgeText)
                            }

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (oem.officialPortalUrl.startsWith("https://")) {
                                    OutlinedButton(
                                        onClick = {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(oem.officialPortalUrl)))
                                            }.onFailure {
                                                Toast.makeText(context, "Unable to open URL", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = MachineryCardBadgeText)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Official Portal", fontSize = 11.sp, color = MachineryCardBadgeText)
                                    }
                                }
                                if (oem.dealerLocatorUrl.startsWith("https://")) {
                                    OutlinedButton(
                                        onClick = {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(oem.dealerLocatorUrl)))
                                            }
                                        },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MachineryCardBadgeText)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Dealer Locator", fontSize = 11.sp, color = MachineryCardBadgeText)
                                    }
                                }
                                if (oem.servicePortalUrl.startsWith("https://")) {
                                    OutlinedButton(
                                        onClick = {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(oem.servicePortalUrl)))
                                            }
                                        },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp), tint = MachineryCardBadgeText)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Service", fontSize = 11.sp, color = MachineryCardBadgeText)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
