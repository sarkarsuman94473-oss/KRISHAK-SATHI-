package com.example.ui.nursery

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.datasource.PlantKingdomCatalog
import com.example.data.local.session.UserSessionManager
import com.example.data.model.*
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.remote.NurseryProductRepository
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.machinery.MachinerySellerPanel
import com.example.util.L10n
import com.example.util.LanguageManager
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedDoubleOrNull
import com.example.util.toLocalizedIntOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurserySellerDashboardScreen(
    userId: String,
    currentLanguage: AppLanguage,
    repository: NurseryApplicationRepository,
    onBackToHome: () -> Unit,
    onOpenPublicStore: (NurseryApplication) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToHome() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val productRepo = remember { NurseryProductRepository(context) }

    val applications by repository.allApplicationsFlow.collectAsState(initial = emptyList())
    val myNursery = remember(applications, userId) {
        applications.firstOrNull { it.userId == userId } ?: applications.firstOrNull()
    }

    val trustRepo = remember(context) { TrustAndSafetyRepository(context) }
    val suspendedSellerIds by trustRepo.observeSuspendedSellerIds().collectAsState(initial = emptySet())
    val isSellerSuspended = remember(suspendedSellerIds, userId, myNursery) {
        (userId in suspendedSellerIds) || (myNursery?.id in suspendedSellerIds)
    }
    var acceptedSellerAgreement by remember {
        mutableStateOf(UserSessionManager.hasAcceptedSellerAgreement(context))
    }

    // Role-based security check
    val isAdmin = remember { UserSessionManager.isAdmin(context) }
    val isOwner = remember(myNursery) {
        UserSessionManager.isNurseryOwner(context) || isAdmin || myNursery?.status == "APPROVED"
    }

    if (!isOwner) {
        // Security gate for unapproved sellers
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            L10n.t("seller_dashboard", currentLanguage),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackToHome, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 540.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিক্রেতা ড্যাশবোর্ড কেবল অনুমোদিত নার্সারির জন্য সংরক্ষিত"
                                AppLanguage.HINDI -> "विक्रेता डैशबोर्ड केवल स्वीकृत नर्सरियों के लिए आरक्षित है"
                                AppLanguage.ENGLISH -> "Seller Dashboard is reserved for approved nurseries only"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "আপনার নার্সারি নিবন্ধন এখনো অনুমোদিত হয়নি অথবা আবেদন জমা দেননি। নার্সারি মালিক হিসেবে যুক্ত হতে আবেদন করুন।"
                                AppLanguage.HINDI -> "आपकी नर्सरी अभी स्वीकृत नहीं हुई है या आपने आवेदन जमा नहीं किया है। नर्सरी मालिक के रूप में जुड़ने के लिए आवेदन करें।"
                                AppLanguage.ENGLISH -> "Your nursery registration is not yet approved or submitted. Apply to join as a Nursery Owner."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onBackToHome,
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্রোফাইল থেকে আবেদন করুন"
                                    AppLanguage.HINDI -> "प्रोफ़ाइल से आवेदन करें"
                                    AppLanguage.ENGLISH -> "Apply from Profile"
                                }
                            )
                        }
                    }
                }
            }
        }
        return
    }

    // Products, Orders & Real Metrics State
    val sellerProducts by productRepo.observeProductsBySeller(userId).collectAsState(initial = emptyList())
    val sellerOrders by productRepo.observeOrdersForSeller(userId).collectAsState(initial = emptyList())
    val metrics by productRepo.getMetricsForSeller(userId).collectAsState(initial = SellerDashboardMetrics())

    // 9 Navigation Tabs:
    // 0: Overview, 1: My Nursery, 2: Add Product, 3: My Products, 4: Inventory, 5: Orders, 6: Sales History, 7: Notifications, 8: Settings
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state for product edit / delete
    var productToEdit by remember { mutableStateOf<NurseryProduct?>(null) }
    var productToDelete by remember { mutableStateOf<NurseryProduct?>(null) }
    var quickStockUpdateProduct by remember { mutableStateOf<NurseryProduct?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBackToHome,
                                modifier = Modifier.size(48.dp).testTag("seller_dashboard_back_btn")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = L10n.t("seller_dashboard", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.BENGALI) {
                                        myNursery?.nurseryNameBn?.ifBlank { myNursery.nurseryNameEn } ?: "উত্তরবঙ্গ গ্রিন নার্সারি"
                                    } else {
                                        myNursery?.nurseryNameEn?.ifBlank { myNursery.nurseryNameBn } ?: "North Bengal Green Nursery"
                                    },
                                    fontSize = 12.sp,
                                    color = com.example.ui.theme.RoyalTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Switch to Farmer Mode Button
                        Button(
                            onClick = onBackToHome,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.RoyalGold,
                                contentColor = com.example.ui.theme.RoyalGreenPrimaryBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("seller_switch_to_farmer_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = L10n.t("seller_farmer_mode", currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 9 Dashboard Navigation Tabs (Scrollable)
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        val tabs = listOf(
                            0 to L10n.t("seller_overview", currentLanguage),
                            1 to L10n.t("seller_my_nursery", currentLanguage),
                            2 to L10n.t("seller_add_product", currentLanguage),
                            3 to "${L10n.t("seller_my_products", currentLanguage)} (${sellerProducts.size})",
                            4 to L10n.t("seller_inventory", currentLanguage),
                            5 to "${L10n.t("seller_orders", currentLanguage)} (${sellerOrders.size})",
                            6 to L10n.t("seller_sales_history", currentLanguage),
                            7 to L10n.t("seller_notifications", currentLanguage),
                            8 to L10n.t("seller_settings", currentLanguage),
                            9 to when (currentLanguage) {
                                AppLanguage.BENGALI -> "যন্ত্র ও ভাড়া"
                                AppLanguage.HINDI -> "मशीनरी व किराया"
                                AppLanguage.ENGLISH -> "Machinery & Rentals"
                            }
                        )

                        tabs.forEach { (index, title) ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.75f)
                                    )
                                }
                            )
                        }
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .imePadding()
        ) {
            if (isSellerSuspended) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("seller_suspended_banner"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "⛔ অ্যাডমিন ট্রাস্ট টিম আপনার বিক্রেতা অ্যাকাউন্ট স্থগিত করেছে। নতুন পণ্য প্রকাশ সাময়িকভাবে বন্ধ আছে।"
                            AppLanguage.HINDI -> "⛔ एडमिन ट्रस्ट टीम द्वारा आपका विक्रेता खाता निलंबित है। नए उत्पाद प्रकाशन अस्थायी रूप से बंद हैं।"
                            AppLanguage.ENGLISH -> "⛔ Your seller account is currently suspended by Admin Trust & Moderation. Publishing new listings is disabled."
                        },
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (!acceptedSellerAgreement && !isAdmin) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("seller_agreement_banner"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "📜 বাধ্যতামূলক বিক্রেতা আইনি চুক্তি (${UserSessionManager.CURRENT_SELLER_AGREEMENT_VERSION}): সঠিক মূল্য, চারা/যন্ত্রের আসল তথ্য এবং কোনো প্রতারণা না করার শর্ত গ্রহণ করুন।"
                                AppLanguage.HINDI -> "📜 अनिवार्य विक्रेता कानूनी समझौता (${UserSessionManager.CURRENT_SELLER_AGREEMENT_VERSION}): सटीक कीमत, वास्तविक विवरण और धोखाधड़ी-मुक्त नीति स्वीकार करें।"
                                AppLanguage.ENGLISH -> "📜 Mandatory Seller Agreement (${UserSessionManager.CURRENT_SELLER_AGREEMENT_VERSION}): Confirm honest pricing, authentic product disclosures, and zero fraud before publishing."
                            },
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = {
                                UserSessionManager.recordSellerAgreementAcceptance(context)
                                acceptedSellerAgreement = true
                            },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("accept_seller_agreement_btn")
                        ) {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বিক্রেতা শর্তাবলী গ্রহণ করুন"
                                    AppLanguage.HINDI -> "विक्रेता शर्तें स्वीकार करें"
                                    AppLanguage.ENGLISH -> "Accept Seller Terms"
                                }
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> SellerOverviewTab(
                    metrics = metrics,
                    nursery = myNursery,
                    recentOrders = sellerOrders.take(3),
                    currentLanguage = currentLanguage,
                    onNavigateTab = { selectedTab = it },
                    onOpenPublicStore = {
                        if (myNursery != null) onOpenPublicStore(myNursery)
                    }
                )
                1 -> SellerMyNurseryTab(
                    nursery = myNursery,
                    currentLanguage = currentLanguage,
                    onOpenPublicStore = {
                        if (myNursery != null) onOpenPublicStore(myNursery)
                    }
                )
                2 -> {
                    if (isSellerSuspended || (!acceptedSellerAgreement && !isAdmin)) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "নতুন পণ্য যোগ করার আগে উপরের অ্যাকাউন্ট অবস্থা ও বিক্রেতা চুক্তি সম্পন্ন করুন।"
                                    AppLanguage.HINDI -> "नया उत्पाद जोड़ने से पहले ऊपर दी गई खाता स्थिति और विक्रेता समझौता पूरा करें।"
                                    AppLanguage.ENGLISH -> "Please resolve the account status or accept Seller Terms above before adding new products."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        SellerAddProductTab(
                            sellerUserId = userId,
                            nursery = myNursery,
                            currentLanguage = currentLanguage,
                            productRepo = productRepo,
                            onProductAdded = {
                                Toast.makeText(context, L10n.t("toast_product_saved", currentLanguage), Toast.LENGTH_SHORT).show()
                                selectedTab = 3 // Move to My Products list
                            }
                        )
                    }
                }
                3 -> SellerMyProductsTab(
                    products = sellerProducts,
                    currentLanguage = currentLanguage,
                    onEditProduct = { productToEdit = it },
                    onDeleteProduct = { productToDelete = it },
                    onQuickUpdate = { quickStockUpdateProduct = it },
                    onToggleStock = { prod, isOutOfStock ->
                        coroutineScope.launch {
                            productRepo.toggleStockStatus(prod.id, userId, isOutOfStock)
                        }
                    },
                    onNavigateToAddProduct = { selectedTab = 2 }
                )
                4 -> SellerInventoryTab(
                    products = sellerProducts,
                    currentLanguage = currentLanguage,
                    onQuickRestock = { prod -> quickStockUpdateProduct = prod },
                    onToggleStock = { prod, isOutOfStock ->
                        coroutineScope.launch {
                            productRepo.toggleStockStatus(prod.id, userId, isOutOfStock)
                        }
                    }
                )
                5 -> SellerOrdersTab(
                    orders = sellerOrders,
                    currentLanguage = currentLanguage,
                    onAcceptOrder = { ord ->
                        coroutineScope.launch {
                            productRepo.acceptOrder(ord.id, userId)
                            Toast.makeText(context, L10n.t("toast_order_accepted", currentLanguage), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCompleteOrder = { ord ->
                        coroutineScope.launch {
                            productRepo.completeOrder(ord.id, userId)
                            Toast.makeText(context, L10n.t("toast_order_completed", currentLanguage), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCancelOrder = { ord ->
                        coroutineScope.launch {
                            val cancelReason = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিক্রেতা কর্তৃক বাতিল"
                                AppLanguage.HINDI -> "विक्रेता द्वारा रद्द"
                                AppLanguage.ENGLISH -> "Cancelled by Seller"
                            }
                            productRepo.cancelOrderAndRestoreStock(ord.id, cancelReason, currentLanguage)
                            Toast.makeText(context, L10n.t("toast_order_cancelled", currentLanguage), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                6 -> SellerSalesHistoryTab(
                    orders = sellerOrders,
                    metrics = metrics,
                    currentLanguage = currentLanguage
                )
                7 -> SellerNotificationsTab(
                    orders = sellerOrders,
                    products = sellerProducts,
                    currentLanguage = currentLanguage
                )
                8 -> SellerSettingsTab(
                    nursery = myNursery,
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    onBackToHome = onBackToHome
                )
                9 -> MachinerySellerPanel(
                    userId = userId,
                    businessName = myNursery?.getDisplayName(currentLanguage) ?: "KRISHAK SATHI Partner",
                    locality = myNursery?.cityOrTown ?: "Siliguri",
                    phone = myNursery?.businessPhone?.ifBlank { myNursery.mobileNumber },
                    currentLanguage = currentLanguage
                )
            }
        }
        }
    }

    // Edit Product Dialog
    productToEdit?.let { prod ->
        SellerEditProductDialog(
            product = prod,
            currentLanguage = currentLanguage,
            onDismiss = { productToEdit = null },
            onSave = { updated ->
                coroutineScope.launch {
                    val res = productRepo.updateProduct(updated, userId, currentLanguage)
                    if (res.isSuccess) {
                        Toast.makeText(context, L10n.t("toast_product_saved", currentLanguage), Toast.LENGTH_SHORT).show()
                        productToEdit = null
                    } else {
                        val fallbackErr = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ত্রুটি হয়েছে"
                            AppLanguage.HINDI -> "त्रुटि हुई"
                            AppLanguage.ENGLISH -> "An error occurred"
                        }
                        Toast.makeText(context, res.exceptionOrNull()?.message ?: fallbackErr, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Delete Product Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(L10n.t("btn_delete_product", currentLanguage)) },
            text = {
                    val dispProdName = prod.getDisplayName(currentLanguage)
                    Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "আপনি কি নিশ্চিত যে '$dispProdName' তালিকা থেকে মুছে ফেলতে চান?"
                        AppLanguage.HINDI -> "क्या आप वाकई '$dispProdName' को सूची से हटाना चाहते हैं?"
                        AppLanguage.ENGLISH -> "Are you sure you want to remove '$dispProdName' from your catalogue?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            productRepo.deleteProduct(prod.id, userId, currentLanguage)
                            Toast.makeText(context, L10n.t("toast_product_deleted", currentLanguage), Toast.LENGTH_SHORT).show()
                            productToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(L10n.t("btn_delete_product", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(L10n.t("btn_cancel", currentLanguage))
                }
            }
        )
    }

    // Quick Stock / Price Update Dialog
    quickStockUpdateProduct?.let { prod ->
        var editPrice by remember { mutableStateOf(prod.regularPrice.toInt().toString()) }
        var editDiscount by remember { mutableStateOf(prod.discountPrice?.toInt()?.toString() ?: "") }
        var editStock by remember { mutableStateOf(prod.availableStock.toString()) }

        AlertDialog(
            onDismissRequest = { quickStockUpdateProduct = null },
            title = { Text(L10n.t("btn_quick_update_stock", currentLanguage)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = prod.getDisplayName(currentLanguage),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text(L10n.t("field_regular_price", currentLanguage)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDiscount,
                        onValueChange = { editDiscount = it },
                        label = { Text(L10n.t("field_discount_price", currentLanguage)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editStock,
                        onValueChange = { editStock = it },
                        label = { Text(L10n.t("field_available_stock", currentLanguage)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val regP = editPrice.toLocalizedDoubleOrNull() ?: prod.regularPrice
                        val discP = editDiscount.toLocalizedDoubleOrNull()
                        val stk = editStock.toLocalizedIntOrNull() ?: prod.availableStock
                        coroutineScope.launch {
                            try {
                                productRepo.updatePriceAndStock(prod.id, userId, regP, discP, stk)
                                Toast.makeText(context, L10n.t("toast_stock_updated", currentLanguage), Toast.LENGTH_SHORT).show()
                                quickStockUpdateProduct = null
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(L10n.t("btn_confirm", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { quickStockUpdateProduct = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(L10n.t("btn_cancel", currentLanguage))
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// TAB 0: SELLER OVERVIEW
// -----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SellerOverviewTab(
    metrics: SellerDashboardMetrics,
    nursery: NurseryApplication?,
    recentOrders: List<NurseryOrder>,
    currentLanguage: AppLanguage,
    onNavigateTab: (Int) -> Unit,
    onOpenPublicStore: () -> Unit
) {
    val isNarrow = isCompactNarrowScreen()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome Storefront Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                            Text(
                                text = L10n.t("seller_verified_badge", currentLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.BENGALI) {
                                nursery?.nurseryNameBn?.ifBlank { nursery.nurseryNameEn } ?: "উত্তরবঙ্গ গ্রিন নার্সারি"
                            } else {
                                nursery?.nurseryNameEn?.ifBlank { nursery.nurseryNameBn } ?: "North Bengal Green Nursery"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${nursery?.district ?: "Jalpaiguri"}, ${nursery?.state ?: "West Bengal"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenPublicStore,
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            L10n.t("btn_view_seller_profile", currentLanguage),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Section: Analytics Cards (Real database data)
        item {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "📊 রিয়েল-টাইম ব্যবসায়িক অ্যানালিটিক্স"
                    AppLanguage.HINDI -> "📊 रीयल-टाइम व्यवसाय विश्लेषण"
                    AppLanguage.ENGLISH -> "📊 Real-Time Business Analytics"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = if (isNarrow) 2 else 3
            ) {
                AnalyticsStatCard(
                    title = L10n.t("analytics_total_products", currentLanguage),
                    value = metrics.totalProducts.toString(),
                    icon = Icons.Default.Inventory2,
                    color = com.example.ui.theme.RoyalInfoText,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsStatCard(
                    title = L10n.t("analytics_active_products", currentLanguage),
                    value = metrics.activeProducts.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = com.example.ui.theme.RoyalGreenHighlight,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsStatCard(
                    title = L10n.t("analytics_out_of_stock", currentLanguage),
                    value = metrics.outOfStockProducts.toString(),
                    icon = Icons.Default.Warning,
                    color = com.example.ui.theme.RoyalAlertText,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsStatCard(
                    title = L10n.t("analytics_new_orders", currentLanguage),
                    value = metrics.newOrdersCount.toString(),
                    icon = Icons.Default.ShoppingCart,
                    color = com.example.ui.theme.RoyalSoftGold,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsStatCard(
                    title = L10n.t("analytics_completed_orders", currentLanguage),
                    value = metrics.completedOrdersCount.toString(),
                    icon = Icons.Default.TaskAlt,
                    color = com.example.ui.theme.RoyalSuccessText,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsStatCard(
                    title = L10n.t("analytics_total_sales", currentLanguage),
                    value = "₹${metrics.totalSalesAmount.toInt()}",
                    icon = Icons.Default.CurrencyRupee,
                    color = com.example.ui.theme.RoyalGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Action Shortcuts
        item {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "⚡ দ্রুত পদক্ষেপ (Quick Actions)"
                    AppLanguage.HINDI -> "⚡ त्वरित कार्य (Quick Actions)"
                    AppLanguage.ENGLISH -> "⚡ Quick Actions"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigateTab(2) }, // Add Product
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(L10n.t("seller_add_product", currentLanguage), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                OutlinedButton(
                    onClick = { onNavigateTab(5) }, // Orders
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ListAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(L10n.t("seller_orders", currentLanguage), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        // Recent Orders Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "📦 সাম্প্রতিক অর্ডারসমূহ"
                        AppLanguage.HINDI -> "📦 हाल के ऑर्डर"
                        AppLanguage.ENGLISH -> "📦 Recent Orders"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateTab(5) }) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "সব দেখুন"
                            AppLanguage.HINDI -> "सभी देखें"
                            AppLanguage.ENGLISH -> "View All"
                        }
                    )
                }
            }
        }

        if (recentOrders.isEmpty()) {
            item {
                Text(
                    text = L10n.t("empty_no_orders", currentLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recentOrders, key = { it.id }) { ord ->
                OrderSummaryCard(order = ord, currentLanguage = currentLanguage)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 1: MY NURSERY PROFILE
// -----------------------------------------------------------------------------
@Composable
private fun SellerMyNurseryTab(
    nursery: NurseryApplication?,
    currentLanguage: AppLanguage,
    onOpenPublicStore: () -> Unit
) {
    if (nursery == null) return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সরকারি লাইসেন্স যাচাইকৃত নার্সারি"
                                    AppLanguage.HINDI -> "लाइसेंस सत्यापित नर्सरी"
                                    AppLanguage.ENGLISH -> "Verified Licensed Nursery"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "প্রতিষ্ঠা: ${nursery.establishmentYear.ifBlank { "২০১২" }}"
                            AppLanguage.HINDI -> "स्थापना: ${nursery.establishmentYear.ifBlank { "2012" }}"
                            AppLanguage.ENGLISH -> "Est.: ${nursery.establishmentYear.ifBlank { "2012" }}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = nursery.getDisplayName(currentLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (currentLanguage == AppLanguage.BENGALI && nursery.nurseryNameEn.isNotBlank()) {
                    Text(
                        text = nursery.nurseryNameEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                DetailRow(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "মালিকের নাম:"
                        AppLanguage.HINDI -> "मालिक का नाम:"
                        AppLanguage.ENGLISH -> "Owner Name:"
                    },
                    value = nursery.getDisplayOwnerName(currentLanguage)
                )
                DetailRow(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "মোবাইল নম্বর:"
                        AppLanguage.HINDI -> "मोबाइल नंबर:"
                        AppLanguage.ENGLISH -> "Mobile Number:"
                    },
                    value = nursery.businessPhone.ifBlank { nursery.mobileNumber }
                )
                DetailRow(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "হোয়াটসঅ্যাপ:"
                        AppLanguage.HINDI -> "व्हाट्सएप:"
                        AppLanguage.ENGLISH -> "WhatsApp:"
                    },
                    value = nursery.whatsappNumber.ifBlank { nursery.mobileNumber }
                )
                DetailRow(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ঠিকানা:"
                        AppLanguage.HINDI -> "पता:"
                        AppLanguage.ENGLISH -> "Address:"
                    },
                    value = "${nursery.getDisplayFullAddress(currentLanguage)}, ${nursery.cityOrTown}, ${nursery.district} - ${nursery.pinCode}"
                )
                DetailRow(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ডেলিভারি সুবিধা:"
                        AppLanguage.HINDI -> "डिलीवरी सुविधा:"
                        AppLanguage.ENGLISH -> "Delivery:"
                    },
                    value = if (nursery.isDeliveryAvailable) {
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "হ্যাঁ (সর্বোচ্চ ${nursery.deliveryRadiusKm} কিমি)"
                            AppLanguage.HINDI -> "हाँ (अधिकतम ${nursery.deliveryRadiusKm} किमी)"
                            AppLanguage.ENGLISH -> "Yes (Up to ${nursery.deliveryRadiusKm} km)"
                        }
                    } else {
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "শুধুমাত্র নার্সারি থেকে সংগ্রহ"
                            AppLanguage.HINDI -> "केवल नर्सरी से पिकअप"
                            AppLanguage.ENGLISH -> "Nursery Pickup Only"
                        }
                    }
                )
                DetailRow(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ট্রেড লাইসেন্স:"
                        AppLanguage.HINDI -> "ट्रेड लाइसेंस:"
                        AppLanguage.ENGLISH -> "Trade License:"
                    },
                    value = nursery.tradeLicenseNumber.ifBlank { "TL/DARJ/2018/8892" }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onOpenPublicStore,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(L10n.t("btn_view_seller_profile", currentLanguage))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 2: ADD PRODUCT FORM (All 10 Categories & 12 Fields)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SellerAddProductTab(
    sellerUserId: String,
    nursery: NurseryApplication?,
    currentLanguage: AppLanguage,
    productRepo: NurseryProductRepository,
    onProductAdded: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isNarrow = isCompactNarrowScreen()

    var productName by remember { mutableStateOf("") }
    var linkedPlantId by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NurseryProductCategory.FRUIT_PLANTS) }
    var description by remember { mutableStateOf("") }
    var regularPrice by remember { mutableStateOf("") }
    var discountPrice by remember { mutableStateOf("") }
    var availableStock by remember { mutableStateOf("25") }
    var plantSize by remember {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "১-২ ফুট (মাঝারি)"
                AppLanguage.HINDI -> "1-2 फीट (मध्यम)"
                AppLanguage.ENGLISH -> "1-2 ft (Medium)"
            }
        )
    }
    var plantType by remember {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "কলম চারা (Grafted)"
                AppLanguage.HINDI -> "कलमी पौधा (Grafted)"
                AppLanguage.ENGLISH -> "Grafted Sapling"
            }
        )
    }
    var careInstructions by remember {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "পর্যাপ্ত রোদে রাখুন। প্রতিদিন সকালে হালকা পানি দিন।"
                AppLanguage.HINDI -> "पर्याप्त धूप में रखें। रोज़ सुबह हल्का पानी दें।"
                AppLanguage.ENGLISH -> "Keep in adequate sunlight. Water lightly every morning."
            }
        )
    }
    var deliveryAvailable by remember { mutableStateOf(true) }
    var deliveryRadius by remember { mutableStateOf("30") }
    var location by remember {
        mutableStateOf(
            nursery?.cityOrTown ?: when (currentLanguage) {
                AppLanguage.BENGALI -> "শিলিগুড়ি"
                AppLanguage.HINDI -> "सिलीगुड़ी"
                AppLanguage.ENGLISH -> "Siliguri"
            }
        )
    }

    // Multi-Image URLs / Keys + Android Photo Picker (Zero Broad Storage Permission)
    val imagesList = remember { mutableStateListOf<String>() }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imagesList.add(uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = L10n.t("form_add_product_title", currentLanguage),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Optional Plant Kingdom Species Linker
        Text(
            text = when (currentLanguage) {
                AppLanguage.BENGALI -> "উদ্ভিদ ভাণ্ডার থেকে প্রজাতি লিংক করুন (ঐচ্ছিক)"
                AppLanguage.HINDI -> "पौधा विश्वकोश से प्रजाति जोड़ें (वैकल्पिक)"
                AppLanguage.ENGLISH -> "Link Plant Kingdom Species (Optional)"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PlantKingdomCatalog.allPlants.take(15), key = { it.plantId }) { pk ->
                val selected = linkedPlantId == pk.plantId
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) {
                            linkedPlantId = ""
                        } else {
                            linkedPlantId = pk.plantId
                            if (productName.isBlank()) productName = pk.getDisplayName(currentLanguage)
                            if (description.isBlank()) description = pk.getDisplayDescription(currentLanguage)
                            careInstructions = pk.getDisplayCareInstructions(currentLanguage)
                        }
                    },
                    label = { Text(pk.getDisplayName(currentLanguage), fontSize = 11.sp) }
                )
            }
        }

        // 1. Product Name
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text(L10n.t("field_product_name", currentLanguage)) },
            placeholder = { Text(L10n.t("field_product_name_hint", currentLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // 2. Product Category Selector (All 10 required)
        Text(
            text = L10n.t("field_product_category", currentLanguage),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(NurseryProductCategory.values()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text("${cat.emoji} ${cat.getDisplayName(currentLanguage)}", fontSize = 12.sp) }
                )
            }
        }

        // 3. Multiple Product Images Manager
        Text(
            text = L10n.t("field_product_images", currentLanguage),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            imagesList.forEachIndexed { idx, imgName ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ছবি ${idx + 1}"
                                AppLanguage.HINDI -> "फ़ोटो ${idx + 1}"
                                AppLanguage.ENGLISH -> "Photo ${idx + 1}"
                            },
                            fontSize = 12.sp
                        )
                        IconButton(
                            onClick = { imagesList.removeAt(idx) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            TextButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(L10n.t("btn_add_another_image", currentLanguage), fontSize = 12.sp)
            }
        }

        // 4. Product Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(L10n.t("field_product_desc", currentLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )

        // 5. Pricing Row (Regular & Discount Price)
        if (isNarrow) {
            OutlinedTextField(
                value = regularPrice,
                onValueChange = { regularPrice = it },
                label = { Text(L10n.t("field_regular_price", currentLanguage)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = discountPrice,
                onValueChange = { discountPrice = it },
                label = { Text(L10n.t("field_discount_price", currentLanguage)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = regularPrice,
                    onValueChange = { regularPrice = it },
                    label = { Text(L10n.t("field_regular_price", currentLanguage)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = discountPrice,
                    onValueChange = { discountPrice = it },
                    label = { Text(L10n.t("field_discount_price", currentLanguage)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 6. Available Stock
        OutlinedTextField(
            value = availableStock,
            onValueChange = { availableStock = it },
            label = { Text(L10n.t("field_available_stock", currentLanguage)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // 7. Plant Size & Type Row
        if (isNarrow) {
            OutlinedTextField(
                value = plantSize,
                onValueChange = { plantSize = it },
                label = { Text(L10n.t("field_plant_size", currentLanguage)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = plantType,
                onValueChange = { plantType = it },
                label = { Text(L10n.t("field_plant_type", currentLanguage)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = plantSize,
                    onValueChange = { plantSize = it },
                    label = { Text(L10n.t("field_plant_size", currentLanguage)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = plantType,
                    onValueChange = { plantType = it },
                    label = { Text(L10n.t("field_plant_type", currentLanguage)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 8. Plant Care Instructions
        OutlinedTextField(
            value = careInstructions,
            onValueChange = { careInstructions = it },
            label = { Text(L10n.t("field_care_instructions", currentLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp)
        )

        // 9. Delivery Availability Switch & Radius
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = L10n.t("field_delivery_available", currentLanguage),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = deliveryAvailable, onCheckedChange = { deliveryAvailable = it })
        }

        if (deliveryAvailable) {
            OutlinedTextField(
                value = deliveryRadius,
                onValueChange = { deliveryRadius = it },
                label = { Text(L10n.t("field_delivery_radius", currentLanguage)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 10. Product Location
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(L10n.t("field_product_location", currentLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Save Button with Validation
        Button(
            onClick = {
                val regP = regularPrice.toLocalizedDoubleOrNull()
                val stockInt = availableStock.toLocalizedIntOrNull()
                if (productName.isBlank()) {
                    Toast.makeText(context, L10n.t("error_required_fields", currentLanguage), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (regP == null || regP <= 0) {
                    Toast.makeText(context, L10n.t("error_invalid_price", currentLanguage), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val newProd = NurseryProduct(
                    nurseryId = nursery?.id ?: "app_nurs_default",
                    nurseryName = if (currentLanguage == AppLanguage.BENGALI) {
                        nursery?.nurseryNameBn?.ifBlank { nursery.nurseryNameEn } ?: "উত্তরবঙ্গ নার্সারি"
                    } else {
                        nursery?.nurseryNameEn?.ifBlank { nursery.nurseryNameBn } ?: "North Bengal Nursery"
                    },
                    sellerUserId = sellerUserId,
                    productName = productName.trim(),
                    category = selectedCategory.code,
                    images = imagesList.toList(),
                    description = description.trim(),
                    regularPrice = regP,
                    discountPrice = discountPrice.toLocalizedDoubleOrNull(),
                    availableStock = stockInt ?: 10,
                    plantSize = plantSize.trim(),
                    plantType = plantType.trim(),
                    plantCareInstructions = careInstructions.trim(),
                    deliveryAvailable = deliveryAvailable,
                    deliveryRadiusKm = deliveryRadius.toLocalizedIntOrNull() ?: 30,
                    productLocation = location.trim(),
                    isApproved = true,
                    status = "ACTIVE",
                    linkedPlantId = linkedPlantId
                )

                val partnerApproved = UserSessionManager.isAdmin(context) ||
                    UserSessionManager.isNurseryOwner(context) ||
                    nursery?.status.equals("APPROVED", ignoreCase = true)

                coroutineScope.launch {
                    try {
                        val res = productRepo.addProduct(
                            product = newProd,
                            sellerUserId = sellerUserId,
                            lang = currentLanguage,
                            isApprovedPartner = partnerApproved
                        )
                        if (res.isSuccess) {
                            onProductAdded()
                        } else {
                            val fallbackErr = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ত্রুটি হয়েছে"
                                AppLanguage.HINDI -> "त्रुटि हुई"
                                AppLanguage.ENGLISH -> "An error occurred"
                            }
                            Toast.makeText(context, res.exceptionOrNull()?.message ?: fallbackErr, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("submit_product_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(L10n.t("btn_save_product", currentLanguage), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// -----------------------------------------------------------------------------
// TAB 3: MY PRODUCTS LIST & CRUD ACTIONS
// -----------------------------------------------------------------------------
@Composable
private fun SellerMyProductsTab(
    products: List<NurseryProduct>,
    currentLanguage: AppLanguage,
    onEditProduct: (NurseryProduct) -> Unit,
    onDeleteProduct: (NurseryProduct) -> Unit,
    onQuickUpdate: (NurseryProduct) -> Unit,
    onToggleStock: (NurseryProduct, Boolean) -> Unit,
    onNavigateToAddProduct: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCatFilter by remember { mutableStateOf<String?>(null) }

    val filtered = remember(products, searchQuery, selectedCatFilter) {
        products.filter { p ->
            val matchCat = selectedCatFilter == null || p.category.equals(selectedCatFilter, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() ||
                    p.productName.contains(searchQuery, ignoreCase = true) ||
                    p.plantType.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Add Button Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(L10n.t("btn_search", currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Button(
                    onClick = onNavigateToAddProduct,
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(L10n.t("seller_add_product", currentLanguage), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedCatFilter == null,
                        onClick = { selectedCatFilter = null },
                        label = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সব (${products.size})"
                                    AppLanguage.HINDI -> "सभी (${products.size})"
                                    AppLanguage.ENGLISH -> "All (${products.size})"
                                },
                                fontSize = 12.sp
                            )
                        }
                    )
                }
                items(NurseryProductCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCatFilter == cat.code,
                        onClick = { selectedCatFilter = if (selectedCatFilter == cat.code) null else cat.code },
                        label = { Text("${cat.emoji} ${cat.getDisplayName(currentLanguage)}", fontSize = 12.sp) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(L10n.t("empty_no_products", currentLanguage), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(filtered, key = { it.id }) { prod ->
                SellerProductCard(
                    product = prod,
                    currentLanguage = currentLanguage,
                    onEdit = { onEditProduct(prod) },
                    onDelete = { onDeleteProduct(prod) },
                    onQuickUpdate = { onQuickUpdate(prod) },
                    onToggleStock = { isOut -> onToggleStock(prod, isOut) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SellerProductCard(
    product: NurseryProduct,
    currentLanguage: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickUpdate: () -> Unit,
    onToggleStock: (Boolean) -> Unit
) {
    val categoryEnum = NurseryProductCategory.fromCode(product.category)
    val isInStock = product.status == "ACTIVE" && product.freeStock > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = categoryEnum.emoji, fontSize = 28.sp)
                    Column {
                        Text(
                            text = product.getDisplayName(currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${categoryEnum.getDisplayName(currentLanguage)} • ${product.getDisplayType(currentLanguage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (isInStock) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isInStock) "${L10n.t("status_active", currentLanguage)} (${product.freeStock})" else L10n.t("status_out_of_stock", currentLanguage),
                        color = if (isInStock) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "₹${product.effectivePrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.discountPrice != null && product.discountPrice < product.regularPrice) {
                        Text(
                            text = "₹${product.regularPrice.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "মজুত: ${product.availableStock} (সংরক্ষিত: ${product.reservedStock})"
                        AppLanguage.HINDI -> "स्टॉक: ${product.availableStock} (आरक्षित: ${product.reservedStock})"
                        AppLanguage.ENGLISH -> "Stock: ${product.availableStock} (Reserved: ${product.reservedStock})"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onEdit, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(L10n.t("btn_edit_product", currentLanguage), fontSize = 12.sp)
                    }
                    TextButton(onClick = onQuickUpdate, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "মূল্য/স্টক"
                                AppLanguage.HINDI -> "कीमत/स्टॉक"
                                AppLanguage.ENGLISH -> "Price/Stock"
                            },
                            fontSize = 12.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { onToggleStock(isInStock) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = if (isInStock) L10n.t("btn_mark_out_of_stock", currentLanguage) else L10n.t("btn_mark_in_stock", currentLanguage),
                            fontSize = 11.sp,
                            color = if (isInStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 4: INVENTORY TRACKING & RESTOCK
// -----------------------------------------------------------------------------
@Composable
private fun SellerInventoryTab(
    products: List<NurseryProduct>,
    currentLanguage: AppLanguage,
    onQuickRestock: (NurseryProduct) -> Unit,
    onToggleStock: (NurseryProduct, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "নিরাপদ স্টক ট্র্যাকিং ও প্রিভেনশন"
                                AppLanguage.HINDI -> "सुरक्षित स्टॉक ट्रैकिंग व प्रबंधन"
                                AppLanguage.ENGLISH -> "Smart Stock Tracking & Prevention"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "গ্রাহক অর্ডার গ্রহণ করলে স্টক স্বয়ংক্রিয়ভাবে সংরক্ষিত বা কমে যায়। অর্ডার বাতিল হলে স্টক পুনরুদ্ধার হয়।"
                                AppLanguage.HINDI -> "ग्राहक ऑर्डर स्वीकार करने पर स्टॉक अपने आप आरक्षित या कम हो जाता है। ऑर्डर रद्द होने पर स्टॉक वापस जुड़ जाता है।"
                                AppLanguage.ENGLISH -> "Stock is automatically reserved when an order is accepted and restored if cancelled."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(products, key = { it.id }) { prod ->
            val isOut = prod.freeStock == 0

            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.getDisplayName(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "মোট স্টক: ${prod.availableStock}"
                                    AppLanguage.HINDI -> "कुल स्टॉक: ${prod.availableStock}"
                                    AppLanguage.ENGLISH -> "Total: ${prod.availableStock}"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সংরক্ষিত: ${prod.reservedStock}"
                                    AppLanguage.HINDI -> "आरक्षित: ${prod.reservedStock}"
                                    AppLanguage.ENGLISH -> "Reserved: ${prod.reservedStock}"
                                },
                                fontSize = 12.sp,
                                color = com.example.ui.theme.RoyalSoftGold
                            )
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "মুক্ত: ${prod.freeStock}"
                                    AppLanguage.HINDI -> "उपलब्ध: ${prod.freeStock}"
                                    AppLanguage.ENGLISH -> "Free: ${prod.freeStock}"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { onQuickRestock(prod) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "+ স্টক যোগ"
                                    AppLanguage.HINDI -> "+ स्टॉक जोड़ें"
                                    AppLanguage.ENGLISH -> "+ Restock"
                                },
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { onToggleStock(prod, !isOut) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                if (isOut) Icons.Default.CheckCircle else Icons.Default.Block,
                                contentDescription = if (isOut) L10n.t("btn_mark_in_stock", currentLanguage) else L10n.t("btn_mark_out_of_stock", currentLanguage),
                                tint = if (isOut) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 5: ORDERS & ORDER WORKFLOW (New, Accepted, Completed, Cancelled)
// -----------------------------------------------------------------------------
@Composable
private fun SellerOrdersTab(
    orders: List<NurseryOrder>,
    currentLanguage: AppLanguage,
    onAcceptOrder: (NurseryOrder) -> Unit,
    onCompleteOrder: (NurseryOrder) -> Unit,
    onCancelOrder: (NurseryOrder) -> Unit
) {
    var selectedOrderFilter by remember { mutableStateOf("ALL") }

    val filtered = remember(orders, selectedOrderFilter) {
        when (selectedOrderFilter) {
            "ALL" -> orders
            else -> orders.filter { it.status.equals(selectedOrderFilter, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val filters = listOf(
                    "ALL" to when (currentLanguage) {
                        AppLanguage.BENGALI -> "সবগুলো (${orders.size})"
                        AppLanguage.HINDI -> "सभी (${orders.size})"
                        AppLanguage.ENGLISH -> "All (${orders.size})"
                    },
                    "NEW" to L10n.t("order_tab_new", currentLanguage),
                    "ACCEPTED" to L10n.t("order_tab_accepted", currentLanguage),
                    "COMPLETED" to L10n.t("order_tab_completed", currentLanguage),
                    "CANCELLED" to L10n.t("order_tab_cancelled", currentLanguage)
                )
                items(filters) { (code, label) ->
                    FilterChip(
                        selected = selectedOrderFilter == code,
                        onClick = { selectedOrderFilter = code },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(L10n.t("empty_no_orders", currentLanguage), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(filtered, key = { it.id }) { ord ->
                SellerOrderFullCard(
                    order = ord,
                    currentLanguage = currentLanguage,
                    onAccept = { onAcceptOrder(ord) },
                    onComplete = { onCompleteOrder(ord) },
                    onCancel = { onCancelOrder(ord) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SellerOrderFullCard(
    order: NurseryOrder,
    currentLanguage: AppLanguage,
    onAccept: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val (statusBg, statusFg, statusText) = when (order.status) {
        "NEW" -> Triple(com.example.ui.theme.RoyalWarningBg, com.example.ui.theme.RoyalSoftGold, when (currentLanguage) {
            AppLanguage.BENGALI -> "নতুন অর্ডার"
            AppLanguage.HINDI -> "नया ऑर्डर"
            AppLanguage.ENGLISH -> "New Order"
        })
        "ACCEPTED" -> Triple(com.example.ui.theme.RoyalInfoBg, com.example.ui.theme.RoyalInfoText, when (currentLanguage) {
            AppLanguage.BENGALI -> "গৃহীত ও প্রস্তুত"
            AppLanguage.HINDI -> "स्वीकृत व तैयार"
            AppLanguage.ENGLISH -> "Accepted"
        })
        "COMPLETED" -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, when (currentLanguage) {
            AppLanguage.BENGALI -> "সম্পন্ন"
            AppLanguage.HINDI -> "पूर्ण"
            AppLanguage.ENGLISH -> "Completed"
        })
        "CANCELLED" -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, when (currentLanguage) {
            AppLanguage.BENGALI -> "বাতিল"
            AppLanguage.HINDI -> "रद्द"
            AppLanguage.ENGLISH -> "Cancelled"
        })
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, order.status)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(color = statusBg, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = statusText,
                        color = statusFg,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            val dispCustomer = com.example.data.datasource.NurseryTranslations.getOrderCustomerName(order, currentLanguage)
            val dispAddress = com.example.data.datasource.NurseryTranslations.getOrderAddress(order, currentLanguage)
            Text("${L10n.t("order_buyer_label", currentLanguage)} $dispCustomer (${order.customerPhone})", fontSize = 13.sp)
            Text("${L10n.t("order_address_label", currentLanguage)} $dispAddress", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider()

            // Items
            order.items.forEach { itm ->
                val dispItemName = com.example.data.datasource.NurseryTranslations.getOrderItemName(itm, currentLanguage)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$dispItemName x ${itm.quantity}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("₹${itm.totalPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "মোট মূল্য (${L10n.t("order_payment_cod", currentLanguage)}):"
                        AppLanguage.HINDI -> "कुल राशि (${L10n.t("order_payment_cod", currentLanguage)}):"
                        AppLanguage.ENGLISH -> "Total (${L10n.t("order_payment_cod", currentLanguage)}):"
                    },
                    fontWeight = FontWeight.Bold
                )
                Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            }

            if (order.status == "ACCEPTED") {
                Text(
                    text = L10n.t("order_stock_reserved_notice", currentLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Action Buttons based on status
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.status == "NEW") {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(L10n.t("btn_accept_order", currentLanguage), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (order.status == "ACCEPTED") {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.RoyalGreenEmerald, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(L10n.t("btn_complete_order", currentLanguage), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (order.status in listOf("NEW", "ACCEPTED")) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(0.8f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(L10n.t("btn_cancel_order", currentLanguage), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 6: SALES HISTORY
// -----------------------------------------------------------------------------
@Composable
private fun SellerSalesHistoryTab(
    orders: List<NurseryOrder>,
    metrics: SellerDashboardMetrics,
    currentLanguage: AppLanguage
) {
    val completedOrders = orders.filter { it.status in listOf("COMPLETED", "ACCEPTED") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "মোট সংগৃহীত বিক্রয় ও রাজস্ব"
                            AppLanguage.HINDI -> "कुल बिक्री और राजस्व"
                            AppLanguage.ENGLISH -> "Total Sales & Revenue"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text("₹${metrics.totalSalesAmount.toInt()}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "সম্পন্ন অর্ডার সংখ্যা: ${metrics.completedOrdersCount} টি"
                            AppLanguage.HINDI -> "पूर्ण ऑर्डर: ${metrics.completedOrdersCount}"
                            AppLanguage.ENGLISH -> "Completed Orders: ${metrics.completedOrdersCount}"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Text(
                when (currentLanguage) {
                    AppLanguage.BENGALI -> "বিক্রয় ইনভয়েস বিবরণী"
                    AppLanguage.HINDI -> "बिक्री चालान विवरण"
                    AppLanguage.ENGLISH -> "Sales Invoice History"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(completedOrders, key = { it.id }) { ord ->
            OrderSummaryCard(order = ord, currentLanguage = currentLanguage)
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 7: NOTIFICATIONS
// -----------------------------------------------------------------------------
@Composable
private fun SellerNotificationsTab(
    orders: List<NurseryOrder>,
    products: List<NurseryProduct>,
    currentLanguage: AppLanguage
) {
    val lowStock = products.filter { it.freeStock in 0..5 }
    val newOrders = orders.filter { it.status == "NEW" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                when (currentLanguage) {
                    AppLanguage.BENGALI -> "🔔 ব্যবসা ও অর্ডার বিজ্ঞপ্তি"
                    AppLanguage.HINDI -> "🔔 व्यवसाय व ऑर्डर सूचनाएं"
                    AppLanguage.ENGLISH -> "🔔 Business & Order Notifications"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (newOrders.isNotEmpty()) {
            item {
                NotificationBanner(
                    title = when (currentLanguage) {
                        AppLanguage.BENGALI -> "${newOrders.size} টি নতুন অর্ডার অপেক্ষমাণ আছে!"
                        AppLanguage.HINDI -> "${newOrders.size} नए ऑर्डर प्रतीक्षा में हैं!"
                        AppLanguage.ENGLISH -> "${newOrders.size} new order(s) pending!"
                    },
                    desc = when (currentLanguage) {
                        AppLanguage.BENGALI -> "গ্রাহকের অর্ডার গ্রহণ করে দ্রুত প্রস্তুত করুন।"
                        AppLanguage.HINDI -> "ग्राहक का ऑर्डर स्वीकार करें और जल्दी तैयार करें।"
                        AppLanguage.ENGLISH -> "Accept customer orders and prepare for dispatch."
                    },
                    icon = Icons.Default.ShoppingCart,
                    color = com.example.ui.theme.RoyalSoftGold,
                    bg = com.example.ui.theme.RoyalWarningBg
                )
            }
        }

        if (lowStock.isNotEmpty()) {
            item {
                val firstItemName = lowStock.firstOrNull()?.getDisplayName(currentLanguage) ?: ""
                NotificationBanner(
                    title = when (currentLanguage) {
                        AppLanguage.BENGALI -> "স্টক সতর্কবার্তা (${lowStock.size} টি পণ্য)"
                        AppLanguage.HINDI -> "स्टॉक चेतावनी (${lowStock.size} उत्पाद)"
                        AppLanguage.ENGLISH -> "Low Stock Alert (${lowStock.size} items)"
                    },
                    desc = when (currentLanguage) {
                        AppLanguage.BENGALI -> "$firstItemName সহ কয়েকটি পণ্যের স্টক কমে এসেছে। দ্রুত রিস্টক করুন।"
                        AppLanguage.HINDI -> "$firstItemName सहित कुछ उत्पादों का स्टॉक कम है। जल्द ही रीस्टॉक करें।"
                        AppLanguage.ENGLISH -> "$firstItemName and others are running low. Restock soon."
                    },
                    icon = Icons.Default.Warning,
                    color = com.example.ui.theme.RoyalAlertText,
                    bg = com.example.ui.theme.RoyalAlertBg
                )
            }
        }

        item {
            NotificationBanner(
                title = when (currentLanguage) {
                    AppLanguage.BENGALI -> "নার্সারি স্টোর যাচাইকৃত ও সক্রিয়"
                    AppLanguage.HINDI -> "नर्सरी स्टोर सत्यापित व सक्रिय है"
                    AppLanguage.ENGLISH -> "Nursery Store Verified & Active"
                },
                desc = when (currentLanguage) {
                    AppLanguage.BENGALI -> "আপনার নার্সারি কৃষক সাথী পাবলিক মার্কেটপ্লেসে সফলভাবে দৃশ্যমান রয়েছে।"
                    AppLanguage.HINDI -> "आपकी नर्सरी कृषक साथी पब्लिक मार्केटप्लेस पर सफलतापूर्वक लाइव है।"
                    AppLanguage.ENGLISH -> "Your nursery is live and visible on the KRISHAK SATHI public marketplace."
                },
                icon = Icons.Default.Verified,
                color = com.example.ui.theme.RoyalSuccessText,
                bg = com.example.ui.theme.RoyalSuccessBg
            )
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 8: SETTINGS & GLOBAL MULTILINGUAL SWITCH
// -----------------------------------------------------------------------------
@Composable
private fun SellerSettingsTab(
    nursery: NurseryApplication?,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var isStoreActive by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "⚙️ নার্সারি সেটিংস ও ভাষা"
                AppLanguage.HINDI -> "⚙️ नर्सरी सेटिंग्स व भाषा"
                AppLanguage.ENGLISH -> "⚙️ Nursery Settings & Language"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Store Visibility
        Card(
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "পাবলিক স্টোর সক্রিয়তা"
                            AppLanguage.HINDI -> "पब्लिक स्टोर दृश्यता"
                            AppLanguage.ENGLISH -> "Public Store Visibility"
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "মার্কেটপ্লেসে গ্রাহকদের কাছে নার্সারি দেখানো হবে"
                            AppLanguage.HINDI -> "मार्केटप्लेस में ग्राहकों को नर्सरी दिखाई जाएगी"
                            AppLanguage.ENGLISH -> "Show your nursery to buyers in the marketplace"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = isStoreActive, onCheckedChange = { isStoreActive = it })
            }
        }

        // Language Switcher Card (App-wide)
        Card(
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(L10n.t("settings_language_title", currentLanguage), fontWeight = FontWeight.Bold)
                Text(L10n.t("settings_language_desc", currentLanguage), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Button(
                            onClick = {
                                LanguageManager.saveLanguage(context, lang)
                                onLanguageChange(lang)
                                val toastText = when (lang) {
                                    AppLanguage.BENGALI -> "${lang.nativeName} নির্বাচিত হয়েছে"
                                    AppLanguage.HINDI -> "${lang.nativeName} चुनी गई"
                                    AppLanguage.ENGLISH -> "${lang.nativeName} selected"
                                }
                                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Text(
                                lang.nativeName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(L10n.t("seller_switch_to_farmer", currentLanguage))
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun AnalyticsStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun OrderSummaryCard(order: NurseryOrder, currentLanguage: AppLanguage) {
    Card(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dispCustomer = com.example.data.datasource.NurseryTranslations.getOrderCustomerName(order, currentLanguage)
            Column(modifier = Modifier.weight(1f)) {
                Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "$dispCustomer • ${order.items.size} টি পণ্য"
                        AppLanguage.HINDI -> "$dispCustomer • ${order.items.size} उत्पाद"
                        AppLanguage.ENGLISH -> "$dispCustomer • ${order.items.size} item(s)"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NotificationBanner(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, bg: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
                Text(desc, fontSize = 12.sp, color = com.example.ui.theme.RoyalTextPrimary)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f, fill = false))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerEditProductDialog(
    product: NurseryProduct,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (NurseryProduct) -> Unit
) {
    val isNarrow = isCompactNarrowScreen()
    var name by remember { mutableStateOf(product.productName) }
    var price by remember { mutableStateOf(product.regularPrice.toInt().toString()) }
    var discount by remember { mutableStateOf(product.discountPrice?.toInt()?.toString() ?: "") }
    var stock by remember { mutableStateOf(product.availableStock.toString()) }
    var desc by remember { mutableStateOf(product.description) }
    var size by remember { mutableStateOf(product.plantSize) }
    var type by remember { mutableStateOf(product.plantType) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .imePadding(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        L10n.t("form_edit_product_title", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = L10n.t("btn_cancel", currentLanguage))
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(L10n.t("field_product_name", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isNarrow) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text(L10n.t("field_regular_price", currentLanguage)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = discount,
                            onValueChange = { discount = it },
                            label = { Text(L10n.t("field_discount_price", currentLanguage)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text(L10n.t("field_regular_price", currentLanguage)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = discount,
                                onValueChange = { discount = it },
                                label = { Text(L10n.t("field_discount_price", currentLanguage)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text(L10n.t("field_available_stock", currentLanguage)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text(L10n.t("field_plant_size", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text(L10n.t("field_plant_type", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(L10n.t("field_product_desc", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val regP = price.toLocalizedDoubleOrNull() ?: product.regularPrice
                        val updated = product.copy(
                            productName = name,
                            regularPrice = regP,
                            discountPrice = discount.toLocalizedDoubleOrNull(),
                            availableStock = stock.toLocalizedIntOrNull() ?: product.availableStock,
                            plantSize = size,
                            plantType = type,
                            description = desc
                        )
                        onSave(updated)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(L10n.t("btn_save_product", currentLanguage))
                }
            }
        }
    }
}
