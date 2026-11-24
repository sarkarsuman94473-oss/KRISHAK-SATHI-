package com.example.ui.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.PromoBannerConfig
import com.example.data.repository.BannerRepository
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminBannerManagementScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { BannerRepository(context) }

    LaunchedEffect(Unit) {
        repo.ensureSeeded()
        repo.pullFromFirestore()
    }

    val langs = listOf(AppLanguage.BENGALI, AppLanguage.HINDI, AppLanguage.ENGLISH)
    var selectedLangIndex by remember { mutableIntStateOf(langs.indexOf(currentLanguage).coerceAtLeast(0)) }
    val managedLanguage = langs[selectedLangIndex]

    val banners by repo.observeAllBannersForLanguage(managedLanguage).collectAsState(initial = emptyList())
    var editingBanner by remember { mutableStateOf<PromoBannerConfig?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "হোম ব্যানার ম্যানেজার (ভাষা অনুযায়ী)"
                                    AppLanguage.HINDI -> "होम बैनर प्रबंधक (भाषा अनुसार)"
                                    AppLanguage.ENGLISH -> "Language Banner Manager (Admin)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "bn / hi / en strictly isolated pools",
                                fontSize = 11.sp,
                                color = RoyalTextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp).testTag("admin_banners_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = RoyalGreenSecondaryBg,
                        titleContentColor = RoyalTextPrimary
                    )
                )
                TabRow(
                    selectedTabIndex = selectedLangIndex,
                    containerColor = RoyalGreenSecondaryBg,
                    contentColor = RoyalGold
                ) {
                    Tab(
                        selected = selectedLangIndex == 0,
                        onClick = { selectedLangIndex = 0 },
                        text = { Text("বাংলা (bn)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedLangIndex == 1,
                        onClick = { selectedLangIndex = 1 },
                        text = { Text("हिन्दी (hi)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedLangIndex == 2,
                        onClick = { selectedLangIndex = 2 },
                        text = { Text("English (en)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .imePadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${managedLanguage.name} Pool (${banners.size} slides)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoyalSoftGold
                        )
                        Button(
                            onClick = {
                                editingBanner = PromoBannerConfig(
                                    id = "",
                                    languageCode = managedLanguage.code,
                                    orderIndex = (banners.maxOfOrNull { it.orderIndex } ?: -1) + 1,
                                    isActive = true,
                                    title = "",
                                    subtitle = "",
                                    badgeText = "",
                                    ctaLabel = when (managedLanguage) {
                                        AppLanguage.BENGALI -> "খুলুন"
                                        AppLanguage.HINDI -> "खोलें"
                                        AppLanguage.ENGLISH -> "Open"
                                    },
                                    drawableResName = "",
                                    imageUrl = "",
                                    targetCardId = "machinery",
                                    isPoster = true,
                                    hideTextOverlay = false
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_add_banner_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add ${managedLanguage.code.uppercase()} Banner", fontSize = 12.sp)
                        }
                    }
                }

                itemsIndexed(banners, key = { _, b -> b.id }) { idx, banner ->
                    val drawableRes = BannerRepository.resolveDrawableRes(banner.drawableResName)
                    val isCustom = !banner.id.startsWith("bn_") && !banner.id.startsWith("hi_") && !banner.id.startsWith("en_banner_")
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (banner.isActive) RoyalBorder else RoyalBorder.copy(alpha = 0.4f)),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 88.dp, height = 50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RoyalGreenSecondaryBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        banner.imageUrl.isNotBlank() -> {
                                            SubcomposeAsyncImage(
                                                model = banner.imageUrl,
                                                contentDescription = banner.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        drawableRes != null -> {
                                            Image(
                                                painter = painterResource(drawableRes),
                                                contentDescription = banner.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        else -> {
                                            Text("EN Card", fontSize = 10.sp, color = RoyalSoftGold)
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "#${idx + 1} ${banner.title.ifBlank { banner.id }}",
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalTextPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Route: ${banner.targetCardId.ifBlank { "poster" }} • Overlay hidden: ${banner.hideTextOverlay}",
                                        fontSize = 11.sp,
                                        color = RoyalTextSecondary
                                    )
                                }

                                Switch(
                                    checked = banner.isActive,
                                    onCheckedChange = { checked ->
                                        scope.launch {
                                            repo.toggleBannerActive(banner, checked)
                                        }
                                    }
                                )
                            }

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    enabled = idx > 0,
                                    onClick = {
                                        scope.launch {
                                            repo.swapBannerOrder(banner, banners[idx - 1])
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = RoyalSoftGold)
                                }
                                IconButton(
                                    enabled = idx < banners.lastIndex,
                                    onClick = {
                                        scope.launch {
                                            repo.swapBannerOrder(banner, banners[idx + 1])
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = RoyalSoftGold)
                                }
                                OutlinedButton(
                                    onClick = { editingBanner = banner },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Replace / Edit", fontSize = 11.sp)
                                }
                                if (isCustom) {
                                    IconButton(
                                        onClick = {
                                            scope.launch { repo.deleteCustomBanner(banner.id) }
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Banner", tint = RoyalWarningText)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingBanner?.let { target ->
        var title by remember(target.id) { mutableStateOf(target.title) }
        var subtitle by remember(target.id) { mutableStateOf(target.subtitle) }
        var badge by remember(target.id) { mutableStateOf(target.badgeText) }
        var cta by remember(target.id) { mutableStateOf(target.ctaLabel) }
        var imageUrl by remember(target.id) { mutableStateOf(target.imageUrl) }
        var route by remember(target.id) { mutableStateOf(target.targetCardId) }
        var hideOverlay by remember(target.id) { mutableStateOf(target.hideTextOverlay) }

        val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) imageUrl = uri.toString()
        }

        AlertDialog(
            onDismissRequest = { editingBanner = null },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalTextPrimary,
            textContentColor = RoyalTextPrimary,
            title = {
                Text(
                    if (target.id.isBlank()) "Add ${target.languageCode.uppercase()} Banner" else "Edit ${target.id}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Banner Title *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = badge, onValueChange = { badge = it }, label = { Text("Badge Text") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cta, onValueChange = { cta = it }, label = { Text("Button Label") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("Target Card ID (ai, machinery, nursery, market, empty)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL (https:// or Picked URI)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedButton(
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Compressed Banner Image")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hideOverlay, onCheckedChange = { hideOverlay = it })
                        Text("Artwork already has text (hide bottom overlay)", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = repo.saveBanner(
                                target.copy(
                                    title = title.ifBlank { "KRISHAK SATHI" },
                                    subtitle = subtitle,
                                    badgeText = badge,
                                    ctaLabel = cta,
                                    imageUrl = imageUrl,
                                    targetCardId = route.trim(),
                                    hideTextOverlay = hideOverlay
                                )
                            )
                            Toast.makeText(
                                context,
                                if (res.isSuccess) "Banner updated for ${target.languageCode}" else "Failed to save banner",
                                Toast.LENGTH_SHORT
                            ).show()
                            editingBanner = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) {
                    Text("Save Banner")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBanner = null }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }
}
