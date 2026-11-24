package com.example.ui.nursery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.StarterNurseryDatabase
import com.example.data.model.ItemStockStatus
import com.example.data.model.NurseryBusiness
import com.example.data.model.NurseryCatalogueItem
import com.example.data.model.PlantCategory
import com.example.data.remote.NurseryApplicationRepository
import com.example.util.safeStartActivity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NurseryPublicProfileScreen(
    nurseryId: String,
    applicationRepository: NurseryApplicationRepository,
    currentLanguage: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.BENGALI,
    onNavigateBack: () -> Unit,
    onRequestQuote: (nurseryId: String, nurseryName: String, itemName: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val applications by applicationRepository.allApplicationsFlow.collectAsState(initial = emptyList())

    // Locate nursery from StarterNurseryDatabase or applications
    val nursery: NurseryBusiness? = remember(nurseryId, applications) {
        val fromStatic = StarterNurseryDatabase.starterNurseries.find { it.id == nurseryId }
        if (fromStatic != null) return@remember fromStatic

        // Try to match approved applications
        val app = applications.find { it.id == nurseryId || it.userId == nurseryId }
        if (app != null) {
            NurseryBusiness(
                id = app.id,
                nameBn = app.nurseryNameBn.ifBlank { app.nurseryNameEn },
                nameEn = app.nurseryNameEn.ifBlank { app.nurseryNameBn },
                ownerName = app.getDisplayOwnerName(currentLanguage),
                phone = app.businessPhone.ifBlank { app.mobileNumber },
                whatsapp = app.whatsappNumber.ifBlank { app.mobileNumber },
                addressBn = app.getDisplayFullAddress(currentLanguage),
                city = app.cityOrTown,
                district = app.district,
                pinCode = app.pinCode,
                isVerified = app.status == "APPROVED",
                verificationStatus = if (app.status == "APPROVED") "VERIFIED" else "PENDING",
                isWholesale = true,
                isRetail = true,
                categories = listOf(PlantCategory.FRUITS, PlantCategory.VEGETABLE_SAPLINGS),
                rating = 4.9,
                reviewCount = 18,
                minimumWholesaleValue = 1000.0,
                deliveryAvailable = app.isDeliveryAvailable,
                badgeText = when (currentLanguage) {
                    com.example.data.model.AppLanguage.BENGALI -> "কৃষক সাথী যাচাইকৃত নার্সারি"
                    com.example.data.model.AppLanguage.HINDI -> "कृषक साथी सत्यापित नर्सरी"
                    com.example.data.model.AppLanguage.ENGLISH -> "Krishak Sathi Verified Nursery"
                },
                catalogue = listOf(
                    NurseryCatalogueItem("app_item_1", "উচ্চফলনশীল কলম চারা", "High-Yield Grafted", PlantCategory.FRUITS, "১ম শ্রেণী কলম", 90.0, 55.0, 50, "চারা", ItemStockStatus.IN_STOCK, "🌱"),
                    NurseryCatalogueItem("app_item_2", "সবজি চারা ট্রে প্যাক", "Vegetable Seedlings Tray", PlantCategory.VEGETABLE_SAPLINGS, "প্রোট্রে রেডি চারা", 3.0, 1.8, 200, "চারা", ItemStockStatus.IN_STOCK, "🥬")
                )
            )
        } else {
            StarterNurseryDatabase.starterNurseries.firstOrNull()
        }
    }

    if (nursery == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                when (currentLanguage) {
                    com.example.data.model.AppLanguage.BENGALI -> "নার্সারি পাওয়া যায়নি"
                    com.example.data.model.AppLanguage.HINDI -> "नर्सरी नहीं मिली"
                    com.example.data.model.AppLanguage.ENGLISH -> "Nursery not found"
                }
            )
        }
        return
    }

    var selectedCategoryFilter by remember { mutableStateOf<PlantCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val displayedItems = remember(nursery.catalogue, selectedCategoryFilter, searchQuery, currentLanguage) {
        nursery.catalogue.filter { item ->
            val matchCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter
            val matchQuery = searchQuery.isBlank() ||
                    item.nameBn.contains(searchQuery, ignoreCase = true) ||
                    item.nameEn.contains(searchQuery, ignoreCase = true) ||
                    item.getName(currentLanguage).contains(searchQuery, ignoreCase = true) ||
                    item.varietyBn.contains(searchQuery, ignoreCase = true) ||
                    item.getVariety(currentLanguage).contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Column {
                        Text(
                            text = nursery.getDisplayName(currentLanguage),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${nursery.district}, ${nursery.city}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp).testTag("nursery_profile_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            shareNursery(context, nursery, currentLanguage)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Storefront Hero Banner Card
            item {
                NurseryStorefrontHeader(nursery = nursery, currentLanguage = currentLanguage)
            }

            // Direct Communication & Navigation Buttons
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            callNursery(context, nursery.phone, currentLanguage)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("nursery_call_action_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "কল করুন"
                                com.example.data.model.AppLanguage.HINDI -> "कॉल करें"
                                com.example.data.model.AppLanguage.ENGLISH -> "Call Now"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = {
                            chatWhatsapp(context, nursery.whatsapp, nursery.getDisplayName(currentLanguage), currentLanguage)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("nursery_whatsapp_action_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Whatsapp, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "হোয়াটসঅ্যাপ"
                                com.example.data.model.AppLanguage.HINDI -> "व्हाट्सएप"
                                com.example.data.model.AppLanguage.ENGLISH -> "WhatsApp"
                            },
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            openMapDirections(context, nursery, currentLanguage)
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 48.dp)
                            .testTag("nursery_map_action_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "ম্যাপ"
                                com.example.data.model.AppLanguage.HINDI -> "मानचित्र"
                                com.example.data.model.AppLanguage.ENGLISH -> "Map"
                            },
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Zero-Commission Direct Promise Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Handshake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    com.example.data.model.AppLanguage.BENGALI -> "০% কমিশন • সরাসরি কৃষক ও নার্সারির চুক্তি"
                                    com.example.data.model.AppLanguage.HINDI -> "0% कमीशन • सीधा किसान व नर्सरी अनुबंध"
                                    com.example.data.model.AppLanguage.ENGLISH -> "0% Commission • Direct Farmer & Nursery Deal"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    com.example.data.model.AppLanguage.BENGALI -> "কোনো মধ্যস্বত্বভোগী নেই। সরাসরি কল বা হোয়াটসঅ্যাপ করে সঠিক চারা ও উপযুক্ত দরদামে কেনাকাটা করুন।"
                                    com.example.data.model.AppLanguage.HINDI -> "कोई बिचौलिया नहीं। सीधे कॉल या व्हाट्सएप कर सही पौध व उचित मूल्य पर खरीदारी करें।"
                                    com.example.data.model.AppLanguage.ENGLISH -> "No middlemen. Call or WhatsApp directly for verified saplings and transparent pricing."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
                            )
                        }
                    }
                }
            }

            // Address & Verification Info Card
            item {
                NurseryDetailsCard(nursery = nursery, currentLanguage = currentLanguage)
            }

            // Catalogue Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "চারা ও ফসলের ক্যাটালগ (${displayedItems.size})"
                            com.example.data.model.AppLanguage.HINDI -> "पौध व फसल कैटलॉग (${displayedItems.size})"
                            com.example.data.model.AppLanguage.ENGLISH -> "Plant & Crop Catalogue (${displayedItems.size})"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "তাজা স্টক তালিকা"
                            com.example.data.model.AppLanguage.HINDI -> "ताज़ा स्टॉक सूची"
                            com.example.data.model.AppLanguage.ENGLISH -> "Fresh Stock List"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar for Catalogue
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "এই নার্সারির চারা বা জাত খুঁজুন..."
                            com.example.data.model.AppLanguage.HINDI -> "इस नर्सरी के पौधे या किस्में खोजें..."
                            com.example.data.model.AppLanguage.ENGLISH -> "Search plants or varieties from this nursery..."
                        })
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        com.example.data.model.AppLanguage.BENGALI -> "সব চারা"
                                        com.example.data.model.AppLanguage.HINDI -> "सभी पौधे"
                                        com.example.data.model.AppLanguage.ENGLISH -> "All Plants"
                                    }
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    items(PlantCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            },
                            label = { Text("${cat.emoji} ${cat.getDisplayName(currentLanguage)}") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Catalogue Items List
            if (displayedItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "কোনো চারা পাওয়া যায়নি। অন্যান্য বিভাগ নির্বাচন করুন।"
                                com.example.data.model.AppLanguage.HINDI -> "कोई पौधा नहीं मिला। अन्य श्रेणी चुनें।"
                                com.example.data.model.AppLanguage.ENGLISH -> "No plants found. Please select another category."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(displayedItems, key = { it.id }) { item ->
                    CatalogueItemCard(
                        item = item,
                        currentLanguage = currentLanguage,
                        onCall = { callNursery(context, nursery.phone, currentLanguage) },
                        onWhatsapp = {
                            val msg = when (currentLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "নমস্কার, আমি কৃষক সাথী অ্যাপ থেকে দেখছি। আপনার '${item.getName(currentLanguage)}' (${item.getVariety(currentLanguage)}) চারাটি সম্পর্কে বিস্তারিত জানতে ও অর্ডার করতে চাই।"
                                com.example.data.model.AppLanguage.HINDI -> "नमस्ते, मैं कृषक साथी ऐप से देख रहा हूँ। आपके '${item.getName(currentLanguage)}' (${item.getVariety(currentLanguage)}) पौधे के बारे में जानकारी व ऑर्डर करना चाहता हूँ।"
                                com.example.data.model.AppLanguage.ENGLISH -> "Hello, viewing from Krishak Sathi app. I want details and wish to order '${item.getName(currentLanguage)}' (${item.getVariety(currentLanguage)})."
                            }
                            sendWhatsappDirect(context, nursery.whatsapp, msg, currentLanguage)
                        },
                        onRequestQuote = {
                            onRequestQuote(nursery.id, nursery.getDisplayName(currentLanguage), item.getName(currentLanguage))
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
        }
    }
}

@Composable
private fun NurseryStorefrontHeader(
    nursery: NurseryBusiness,
    currentLanguage: com.example.data.model.AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verified Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = nursery.getBadge(currentLanguage),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = com.example.ui.theme.RoyalGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${nursery.rating} (${nursery.reviewCount})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nursery Name
            Text(
                text = nursery.getDisplayName(currentLanguage),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (nursery.nameEn.isNotBlank() && nursery.nameEn != nursery.getDisplayName(currentLanguage)) {
                Text(
                    text = nursery.nameEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Owner info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (currentLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "মালিক/পরিচালক: ${nursery.getOwner(currentLanguage)}"
                        com.example.data.model.AppLanguage.HINDI -> "मालिक/संचालक: ${nursery.getOwner(currentLanguage)}"
                        com.example.data.model.AppLanguage.ENGLISH -> "Owner / Director: ${nursery.getOwner(currentLanguage)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NurseryDetailsCard(
    nursery: NurseryBusiness,
    currentLanguage: com.example.data.model.AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    com.example.data.model.AppLanguage.BENGALI -> "ঠিকানা ও বাণিজ্য তথ্য"
                    com.example.data.model.AppLanguage.HINDI -> "पता व व्यापार जानकारी"
                    com.example.data.model.AppLanguage.ENGLISH -> "Address & Trade Information"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Location
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${nursery.getAddress(currentLanguage)}, ${nursery.city}, ${nursery.district} - ${nursery.pinCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Delivery info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = if (nursery.deliveryAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (nursery.deliveryAvailable) {
                        when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "হোম/ফিল্ড ডেলিভারি সুবিধা আছে (নির্দিষ্ট এলাকায়)"
                            com.example.data.model.AppLanguage.HINDI -> "होम/फील्ड डिलीवरी उपलब्ध (निर्धारित क्षेत्र में)"
                            com.example.data.model.AppLanguage.ENGLISH -> "Home / Field Delivery Available (Select Areas)"
                        }
                    } else {
                        when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "শুধুমাত্র নার্সারি থেকে সংগ্রহ"
                            com.example.data.model.AppLanguage.HINDI -> "केवल नर्सरी से पिकअप"
                            com.example.data.model.AppLanguage.ENGLISH -> "Nursery Pickup Only"
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Wholesale condition
            if (nursery.isWholesale) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "পাইকারি সর্বনিম্ন অর্ডার: ₹${nursery.minimumWholesaleValue.toInt()} বা বেশি"
                            com.example.data.model.AppLanguage.HINDI -> "न्यूनतम थोक ऑर्डर: ₹${nursery.minimumWholesaleValue.toInt()} या अधिक"
                            com.example.data.model.AppLanguage.ENGLISH -> "Min Wholesale Order: ₹${nursery.minimumWholesaleValue.toInt()} or more"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogueItemCard(
    item: NurseryCatalogueItem,
    currentLanguage: com.example.data.model.AppLanguage,
    onCall: () -> Unit,
    onWhatsapp: () -> Unit,
    onRequestQuote: () -> Unit
) {
    val unit = item.getUnit(currentLanguage)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.iconEmoji,
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = item.getName(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.getVariety(currentLanguage)} • ${item.category.getDisplayName(currentLanguage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = Color(item.stockStatus.colorHex).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.stockStatus.getDisplayName(currentLanguage),
                        color = Color(item.stockStatus.colorHex),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Pricing details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "খুচরা মূল্য"
                            com.example.data.model.AppLanguage.HINDI -> "खुदरा मूल्य"
                            com.example.data.model.AppLanguage.ENGLISH -> "Retail Price"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${item.priceRetail.toInt()} / $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "পাইকারি দর (মিনিমাম ${item.minWholesaleQty} $unit)"
                            com.example.data.model.AppLanguage.HINDI -> "थोक दर (न्यूनतम ${item.minWholesaleQty} $unit)"
                            com.example.data.model.AppLanguage.ENGLISH -> "Bulk Rate (Min ${item.minWholesaleQty} $unit)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${item.priceWholesale.toInt()} / $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Action row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "কল"
                            com.example.data.model.AppLanguage.HINDI -> "कॉल"
                            com.example.data.model.AppLanguage.ENGLISH -> "Call"
                        },
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onWhatsapp,
                    modifier = Modifier
                        .weight(1.3f)
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Whatsapp, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "অর্ডার করুন"
                            com.example.data.model.AppLanguage.HINDI -> "ऑर्डर करें"
                            com.example.data.model.AppLanguage.ENGLISH -> "Order Now"
                        },
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(
                    onClick = onRequestQuote,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(
                        when (currentLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "দরদাম চান"
                            com.example.data.model.AppLanguage.HINDI -> "भाव पूछें"
                            com.example.data.model.AppLanguage.ENGLISH -> "Get Quote"
                        },
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun callNursery(context: Context, phone: String, currentLanguage: com.example.data.model.AppLanguage) {
    val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
    val fallback = when (currentLanguage) {
        com.example.data.model.AppLanguage.BENGALI -> "কল করার অ্যাপ পাওয়া যায়নি"
        com.example.data.model.AppLanguage.HINDI -> "कॉल करने के लिए ऐप नहीं मिला"
        com.example.data.model.AppLanguage.ENGLISH -> "No dialer app found"
    }
    safeStartActivity(context, intent, fallback)
}

private fun chatWhatsapp(
    context: Context,
    whatsapp: String,
    nurseryName: String,
    currentLanguage: com.example.data.model.AppLanguage
) {
    val message = when (currentLanguage) {
        com.example.data.model.AppLanguage.BENGALI -> "নমস্কার, আমি কৃষক সাথী অ্যাপে আপনার '$nurseryName' সম্পর্কে জেনে যোগাযোগ করছি। চারা ও অর্ডার সম্পর্কে জানতে চাই।"
        com.example.data.model.AppLanguage.HINDI -> "नमस्ते, मैंने कृषक साथी ऐप पर '$nurseryName' देखा। पौधों और ऑर्डर के बारे में जानकारी चाहिए।"
        com.example.data.model.AppLanguage.ENGLISH -> "Hello, contacting '$nurseryName' via Krishak Sathi app regarding saplings and ordering."
    }
    sendWhatsappDirect(context, whatsapp, message, currentLanguage)
}

private fun sendWhatsappDirect(
    context: Context,
    phone: String,
    message: String,
    currentLanguage: com.example.data.model.AppLanguage
) {
    var cleanPhone = phone.replace(Regex("[^0-9]"), "")
    if (cleanPhone.length == 10) cleanPhone = "91$cleanPhone"
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    val fallback = when (currentLanguage) {
        com.example.data.model.AppLanguage.BENGALI -> "WhatsApp খোলা সম্ভব হয়নি"
        com.example.data.model.AppLanguage.HINDI -> "WhatsApp नहीं खोला जा सका"
        com.example.data.model.AppLanguage.ENGLISH -> "Unable to open WhatsApp"
    }
    safeStartActivity(context, intent, fallback)
}

private fun openMapDirections(
    context: Context,
    nursery: NurseryBusiness,
    currentLanguage: com.example.data.model.AppLanguage
) {
    val query = Uri.encode("${nursery.nameEn.ifBlank { nursery.nameBn }}, ${nursery.city}, ${nursery.district}")
    val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    val fallback = when (currentLanguage) {
        com.example.data.model.AppLanguage.BENGALI -> "ম্যাপ খোলা সম্ভব হয়নি"
        com.example.data.model.AppLanguage.HINDI -> "मैप नहीं खोला जा सका"
        com.example.data.model.AppLanguage.ENGLISH -> "Unable to open maps"
    }
    if (!safeStartActivity(context, mapIntent)) {
        val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
        safeStartActivity(context, Intent(Intent.ACTION_VIEW, browserUri), fallback)
    }
}

private fun shareNursery(
    context: Context,
    nursery: NurseryBusiness,
    currentLanguage: com.example.data.model.AppLanguage
) {
    val text = when (currentLanguage) {
        com.example.data.model.AppLanguage.BENGALI -> """
            🌱 ${nursery.nameBn} (${nursery.district})
            যাচাইকৃত নার্সারি • তাজা কলম ও ফসলের চারা
            যোগাযোগ: ${nursery.phone}
            কৃষক সাথী - স্মার্ট এগ্রিকালচার প্ল্যাটফর্ম
        """.trimIndent()
        com.example.data.model.AppLanguage.HINDI -> """
            🌱 ${nursery.getDisplayName(currentLanguage)} (${nursery.district})
            सत्यापित नर्सरी • ताज़ा कलम व फसल पौधे
            संपर्क: ${nursery.phone}
            कृषक साथी - स्मार्ट एग्रीकल्चर प्लेटफॉर्म
        """.trimIndent()
        com.example.data.model.AppLanguage.ENGLISH -> """
            🌱 ${nursery.getDisplayName(currentLanguage)} (${nursery.district})
            Verified Nursery • Fresh Grafts & Crop Saplings
            Contact: ${nursery.phone}
            KRISHAK SATHI - Smart Agriculture Platform
        """.trimIndent()
    }
    val chooserTitle = when (currentLanguage) {
        com.example.data.model.AppLanguage.BENGALI -> "নার্সারি শেয়ার করুন"
        com.example.data.model.AppLanguage.HINDI -> "नर्सरी साझा करें"
        com.example.data.model.AppLanguage.ENGLISH -> "Share Nursery"
    }
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val fallback = when (currentLanguage) {
        com.example.data.model.AppLanguage.BENGALI -> "শেয়ার করার অ্যাপ পাওয়া যায়নি"
        com.example.data.model.AppLanguage.HINDI -> "शेयर करने के लिए ऐप नहीं मिला"
        com.example.data.model.AppLanguage.ENGLISH -> "No app available to share"
    }
    safeStartActivity(context, Intent.createChooser(sendIntent, chooserTitle), fallback)
}
