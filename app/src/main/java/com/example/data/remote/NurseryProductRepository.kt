package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.NurseryOrder
import com.example.data.model.NurseryOrderItem
import com.example.data.model.NurseryProduct
import com.example.data.model.NurseryProductCategory
import com.example.data.model.SellerDashboardMetrics
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "NurseryProductRepo"

class NurseryProductRepository(private val context: Context) {

    private val db: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firestore: ${e.message}")
            null
        }
    }

    private val prefs = context.applicationContext.getSharedPreferences("krishak_sathi_nursery_products", Context.MODE_PRIVATE)

    private val _productsFlow = MutableStateFlow<List<NurseryProduct>>(emptyList())
    val allProductsFlow: Flow<List<NurseryProduct>> = _productsFlow.asStateFlow()

    private val _ordersFlow = MutableStateFlow<List<NurseryOrder>>(emptyList())
    val allOrdersFlow: Flow<List<NurseryOrder>> = _ordersFlow.asStateFlow()

    init {
        loadDataFromDisk()
    }

    private fun loadDataFromDisk() {
        try {
            val prodRaw = prefs.getString("cached_products", null)
            if (!prodRaw.isNullOrBlank()) {
                val array = JSONArray(prodRaw)
                val list = mutableListOf<NurseryProduct>()
                for (i in 0 until array.length()) {
                    list.add(jsonToProduct(array.getJSONObject(i)))
                }
                _productsFlow.value = list
            } else {
                // Pre-seed realistic certified nursery products covering all 10 categories
                val seedProducts = createInitialSeedProducts()
                _productsFlow.value = seedProducts
                saveProductsToDisk(seedProducts)
            }

            val ordersRaw = prefs.getString("cached_orders", null)
            if (!ordersRaw.isNullOrBlank()) {
                val array = JSONArray(ordersRaw)
                val list = mutableListOf<NurseryOrder>()
                for (i in 0 until array.length()) {
                    list.add(jsonToOrder(array.getJSONObject(i)))
                }
                _ordersFlow.value = list
            } else {
                val seedOrders = createInitialSeedOrders()
                _ordersFlow.value = seedOrders
                saveOrdersToDisk(seedOrders)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading nursery product data: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Seller Specific Queries & Analytics
    // -------------------------------------------------------------

    fun observeProductsBySeller(sellerUserId: String): Flow<List<NurseryProduct>> {
        return _productsFlow.asStateFlow().map { list ->
            // If seller has products, return them; otherwise match any fallback demo seller
            val matched = list.filter { it.sellerUserId == sellerUserId }
            if (matched.isNotEmpty()) matched else list
        }
    }

    fun observeApprovedMarketplaceProducts(): Flow<List<NurseryProduct>> {
        return _productsFlow.asStateFlow().map { list ->
            list.filter { it.isApproved && it.status != "ARCHIVED" }
        }
    }

    fun observeOrdersForSeller(sellerUserId: String): Flow<List<NurseryOrder>> {
        return _ordersFlow.asStateFlow().map { list ->
            val matched = list.filter { it.sellerUserId == sellerUserId }
            if (matched.isNotEmpty()) matched else list
        }
    }

    fun getMetricsForSeller(sellerUserId: String): Flow<SellerDashboardMetrics> {
        return _productsFlow.asStateFlow().map { products ->
            val sellerProducts = products.filter { it.sellerUserId == sellerUserId || products.size <= 8 }
            val orders = _ordersFlow.value.filter { it.sellerUserId == sellerUserId || _ordersFlow.value.size <= 4 }

            val totalProducts = sellerProducts.size
            val activeProducts = sellerProducts.count { it.status == "ACTIVE" && it.freeStock > 0 }
            val outOfStock = sellerProducts.count { it.status == "OUT_OF_STOCK" || it.freeStock == 0 }

            val newOrders = orders.count { it.status == "NEW" }
            val acceptedOrders = orders.count { it.status == "ACCEPTED" || it.status == "DISPATCHED" }
            val completedOrders = orders.count { it.status == "COMPLETED" }
            val totalSales = orders.filter { it.status in listOf("ACCEPTED", "DISPATCHED", "COMPLETED") }
                .sumOf { it.totalAmount }

            SellerDashboardMetrics(
                totalProducts = totalProducts,
                activeProducts = activeProducts,
                outOfStockProducts = outOfStock,
                newOrdersCount = newOrders,
                acceptedOrdersCount = acceptedOrders,
                completedOrdersCount = completedOrders,
                totalSalesAmount = totalSales
            )
        }
    }

    // -------------------------------------------------------------
    // Product Management (CRUD & Stock Operations)
    // -------------------------------------------------------------

    suspend fun addProduct(
        product: NurseryProduct,
        sellerUserId: String,
        lang: AppLanguage = AppLanguage.BENGALI,
        isApprovedPartner: Boolean = true
    ): Result<String> {
        return try {
            if (!isApprovedPartner) {
                val msg = when (lang) {
                    AppLanguage.BENGALI -> "শুধুমাত্র অনুমোদিত নার্সারি পার্টনাররাই বাণিজ্যিক গাছের তালিকা প্রকাশ করতে পারবেন"
                    AppLanguage.HINDI -> "केवल स्वीकृत नर्सरी पार्टनर ही व्यावसायिक पौधों की लिस्टिंग प्रकाशित कर सकते हैं"
                    AppLanguage.ENGLISH -> "Only approved nursery partners can publish commercial plant listings"
                }
                return Result.failure(SecurityException(msg))
            }
            if (product.productName.isBlank()) {
                val msg = when (lang) {
                    AppLanguage.BENGALI -> "পণ্যের নাম আবশ্যক"
                    AppLanguage.HINDI -> "उत्पाद का नाम आवश्यक है"
                    AppLanguage.ENGLISH -> "Product name is required"
                }
                return Result.failure(IllegalArgumentException(msg))
            }
            if (product.regularPrice <= 0.0) {
                val msg = when (lang) {
                    AppLanguage.BENGALI -> "সঠিক নিয়মিত মূল্য প্রদান করুন"
                    AppLanguage.HINDI -> "सही नियमित मूल्य दर्ज करें"
                    AppLanguage.ENGLISH -> "Please enter a valid regular price"
                }
                return Result.failure(IllegalArgumentException(msg))
            }
            val finalId = if (product.id.isNotBlank()) product.id else "prod_${System.currentTimeMillis()}"
            val finalProduct = product.copy(
                id = finalId,
                sellerUserId = sellerUserId,
                isApproved = true, // Sellers approved in Phase 2 have auto-listing
                status = if (product.availableStock > 0) "ACTIVE" else "OUT_OF_STOCK",
                createdAtMillis = System.currentTimeMillis(),
                updatedAtMillis = System.currentTimeMillis()
            )

            val currentList = _productsFlow.value.toMutableList()
            currentList.add(0, finalProduct)
            _productsFlow.value = currentList
            saveProductsToDisk(currentList)

            // Sync to Firestore
            syncProductToFirestore(finalProduct)

            Result.success(finalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: NurseryProduct, sellerUserId: String, lang: AppLanguage = AppLanguage.BENGALI): Result<Unit> {
        return try {
            val currentList = _productsFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == product.id }
            if (index < 0) {
                val msg = when (lang) {
                    AppLanguage.BENGALI -> "পণ্য খুঁজে পাওয়া যায়নি"
                    AppLanguage.HINDI -> "उत्पाद नहीं मिला"
                    AppLanguage.ENGLISH -> "Product not found"
                }
                return Result.failure(NoSuchElementException(msg))
            }

            // Security check
            val existing = currentList[index]
            if (existing.sellerUserId.isNotBlank() && existing.sellerUserId != sellerUserId) {
                val msg = when (lang) {
                    AppLanguage.BENGALI -> "অন্য নার্সারির পণ্য সম্পাদনা করার অনুমতি নেই"
                    AppLanguage.HINDI -> "किसी अन्य नर्सरी का उत्पाद संपादित करने की अनुमति नहीं है"
                    AppLanguage.ENGLISH -> "Not authorized to edit another nursery's product"
                }
                return Result.failure(SecurityException(msg))
            }

            val updated = product.copy(
                sellerUserId = existing.sellerUserId,
                nurseryId = existing.nurseryId,
                nurseryName = existing.nurseryName,
                status = if (product.availableStock > 0) "ACTIVE" else "OUT_OF_STOCK",
                updatedAtMillis = System.currentTimeMillis()
            )
            currentList[index] = updated
            _productsFlow.value = currentList
            saveProductsToDisk(currentList)

            syncProductToFirestore(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String, sellerUserId: String, lang: AppLanguage = AppLanguage.BENGALI): Result<Unit> {
        return try {
            val currentList = _productsFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == productId }
            if (index >= 0) {
                val existing = currentList[index]
                if (existing.sellerUserId.isNotBlank() && existing.sellerUserId != sellerUserId) {
                    val msg = when (lang) {
                        AppLanguage.BENGALI -> "অনুমতি নেই"
                        AppLanguage.HINDI -> "अनुमति नहीं है"
                        AppLanguage.ENGLISH -> "Permission denied"
                    }
                    return Result.failure(SecurityException(msg))
                }
                currentList.removeAt(index)
                _productsFlow.value = currentList
                saveProductsToDisk(currentList)

                try {
                    db?.collection("nursery_products")?.document(productId)?.delete()?.await()
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting product from Firestore: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePriceAndStock(
        productId: String,
        sellerUserId: String,
        regularPrice: Double,
        discountPrice: Double?,
        newStock: Int
    ): Result<Unit> {
        return try {
            val currentList = _productsFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == productId }
            if (index >= 0) {
                val existing = currentList[index]
                val updatedStatus = if (newStock > 0) "ACTIVE" else "OUT_OF_STOCK"
                val updated = existing.copy(
                    regularPrice = regularPrice,
                    discountPrice = discountPrice,
                    availableStock = newStock,
                    status = updatedStatus,
                    updatedAtMillis = System.currentTimeMillis()
                )
                currentList[index] = updated
                _productsFlow.value = currentList
                saveProductsToDisk(currentList)
                syncProductToFirestore(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleStockStatus(productId: String, sellerUserId: String, isOutOfStock: Boolean): Result<Unit> {
        return try {
            val currentList = _productsFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == productId }
            if (index >= 0) {
                val existing = currentList[index]
                val updated = existing.copy(
                    status = if (isOutOfStock) "OUT_OF_STOCK" else "ACTIVE",
                    updatedAtMillis = System.currentTimeMillis()
                )
                currentList[index] = updated
                _productsFlow.value = currentList
                saveProductsToDisk(currentList)
                syncProductToFirestore(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Inventory Safe Stock Reservation & Order Placement
    // -------------------------------------------------------------

    /**
     * Places a customer order and atomically deducts available stock.
     * Prevents overselling when stock is insufficient.
     */
    suspend fun placeCustomerOrder(order: NurseryOrder, lang: AppLanguage = AppLanguage.BENGALI): Result<NurseryOrder> {
        val affectedProducts = mutableListOf<NurseryProduct>()
        val finalOrder: NurseryOrder = synchronized(this) {
            val productsList = _productsFlow.value.toMutableList()

            // Step 1: Verify all items have sufficient available stock
            for (item in order.items) {
                val pIndex = productsList.indexOfFirst { it.id == item.productId }
                if (pIndex < 0) {
                    val msg = when (lang) {
                        AppLanguage.BENGALI -> "পণ্য '${item.productName}' পাওয়া যায়নি"
                        AppLanguage.HINDI -> "उत्पाद '${item.productName}' नहीं मिला"
                        AppLanguage.ENGLISH -> "Product '${item.productName}' not found"
                    }
                    return Result.failure(IllegalArgumentException(msg))
                }
                val prod = productsList[pIndex]
                if (prod.freeStock < item.quantity) {
                    val dispName = prod.getDisplayName(lang)
                    val msg = when (lang) {
                        AppLanguage.BENGALI -> "দুঃখিত, '$dispName'-এর পর্যাপ্ত স্টক নেই। উপলব্ধ: ${prod.freeStock}"
                        AppLanguage.HINDI -> "क्षमा करें, '$dispName' का पर्याप्त स्टॉक नहीं है। उपलब्ध: ${prod.freeStock}"
                        AppLanguage.ENGLISH -> "Sorry, insufficient stock for '$dispName'. Available: ${prod.freeStock}"
                    }
                    return Result.failure(IllegalStateException(msg))
                }
            }

            // Step 2: Safely deduct stock & update product status
            for (item in order.items) {
                val pIndex = productsList.indexOfFirst { it.id == item.productId }
                if (pIndex >= 0) {
                    val prod = productsList[pIndex]
                    val newAvailable = (prod.availableStock - item.quantity).coerceAtLeast(0)
                    val updated = prod.copy(
                        availableStock = newAvailable,
                        status = if (newAvailable == 0) "OUT_OF_STOCK" else prod.status,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    productsList[pIndex] = updated
                    affectedProducts.add(updated)
                }
            }
            _productsFlow.value = productsList
            saveProductsToDisk(productsList)

            // Step 3: Record new order
            val orderId = if (order.id.isNotBlank()) order.id else "ord_${System.currentTimeMillis()}"
            val ord = order.copy(
                id = orderId,
                status = "NEW",
                orderDateMillis = System.currentTimeMillis(),
                updatedDateMillis = System.currentTimeMillis()
            )

            val ordersList = _ordersFlow.value.toMutableList()
            ordersList.add(0, ord)
            _ordersFlow.value = ordersList
            saveOrdersToDisk(ordersList)

            ord
        }

        for (p in affectedProducts) {
            syncProductToFirestore(p)
        }
        syncOrderToFirestore(finalOrder)

        return Result.success(finalOrder)
    }

    suspend fun acceptOrder(orderId: String, sellerUserId: String): Result<Unit> {
        return try {
            val ordersList = _ordersFlow.value.toMutableList()
            val index = ordersList.indexOfFirst { it.id == orderId }
            if (index >= 0) {
                val updated = ordersList[index].copy(
                    status = "ACCEPTED",
                    updatedDateMillis = System.currentTimeMillis()
                )
                ordersList[index] = updated
                _ordersFlow.value = ordersList
                saveOrdersToDisk(ordersList)
                syncOrderToFirestore(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeOrder(orderId: String, sellerUserId: String): Result<Unit> {
        return try {
            val ordersList = _ordersFlow.value.toMutableList()
            val index = ordersList.indexOfFirst { it.id == orderId }
            if (index >= 0) {
                val updated = ordersList[index].copy(
                    status = "COMPLETED",
                    updatedDateMillis = System.currentTimeMillis()
                )
                ordersList[index] = updated
                _ordersFlow.value = ordersList
                saveOrdersToDisk(ordersList)
                syncOrderToFirestore(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancels an order and automatically RESTORES the deducted stock back to the products.
     */
    suspend fun cancelOrderAndRestoreStock(orderId: String, reason: String = "", lang: AppLanguage = AppLanguage.BENGALI): Result<Unit> {
        val restoredProducts = mutableListOf<NurseryProduct>()
        val updatedOrder: NurseryOrder? = synchronized(this) {
            val ordersList = _ordersFlow.value.toMutableList()
            val oIndex = ordersList.indexOfFirst { it.id == orderId }
            if (oIndex < 0) return Result.failure(NoSuchElementException("Order not found"))

            val order = ordersList[oIndex]
            if (order.status == "CANCELLED") {
                return Result.success(Unit) // Already cancelled
            }

            // Restore stock back to products
            val productsList = _productsFlow.value.toMutableList()
            for (item in order.items) {
                val pIndex = productsList.indexOfFirst { it.id == item.productId }
                if (pIndex >= 0) {
                    val prod = productsList[pIndex]
                    val restoredStock = prod.availableStock + item.quantity
                    val updatedProd = prod.copy(
                        availableStock = restoredStock,
                        status = if (restoredStock > 0 && prod.status == "OUT_OF_STOCK") "ACTIVE" else prod.status,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    productsList[pIndex] = updatedProd
                    restoredProducts.add(updatedProd)
                }
            }
            _productsFlow.value = productsList
            saveProductsToDisk(productsList)

            // Mark order cancelled
            val cancelPrefix = when (lang) {
                AppLanguage.BENGALI -> "বাতিল"
                AppLanguage.HINDI -> "रद्द"
                AppLanguage.ENGLISH -> "Cancelled"
            }
            val upd = order.copy(
                status = "CANCELLED",
                notes = if (reason.isNotBlank()) "$cancelPrefix: $reason" else order.notes,
                updatedDateMillis = System.currentTimeMillis()
            )
            ordersList[oIndex] = upd
            _ordersFlow.value = ordersList
            saveOrdersToDisk(ordersList)

            upd
        }

        if (updatedOrder != null) {
            for (p in restoredProducts) {
                syncProductToFirestore(p)
            }
            syncOrderToFirestore(updatedOrder)
        }

        return Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Firestore Sync
    // -------------------------------------------------------------

    private suspend fun syncProductToFirestore(p: NurseryProduct) {
        try {
            if (db != null && Firebase.auth.currentUser != null) {
                val payload = hashMapOf<String, Any>(
                    "id" to p.id,
                    "nurseryId" to p.nurseryId,
                    "nurseryName" to p.nurseryName,
                    "sellerUserId" to p.sellerUserId,
                    "productName" to p.productName,
                    "category" to p.category,
                    "images" to p.images,
                    "description" to p.description,
                    "regularPrice" to p.regularPrice,
                    "discountPrice" to (p.discountPrice ?: 0.0),
                    "availableStock" to p.availableStock,
                    "reservedStock" to p.reservedStock,
                    "plantSize" to p.plantSize,
                    "plantType" to p.plantType,
                    "plantCareInstructions" to p.plantCareInstructions,
                    "deliveryAvailable" to p.deliveryAvailable,
                    "deliveryRadiusKm" to p.deliveryRadiusKm,
                    "productLocation" to p.productLocation,
                    "isApproved" to p.isApproved,
                    "status" to p.status,
                    "linkedPlantId" to p.linkedPlantId,
                    "createdAtMillis" to p.createdAtMillis,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db?.collection("nursery_products")?.document(p.id)?.set(payload)?.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync product error: ${e.message}")
        }
    }

    private suspend fun syncOrderToFirestore(o: NurseryOrder) {
        try {
            if (db != null && Firebase.auth.currentUser != null) {
                val payload = hashMapOf<String, Any>(
                    "id" to o.id,
                    "orderNumber" to o.orderNumber,
                    "nurseryId" to o.nurseryId,
                    "nurseryName" to o.nurseryName,
                    "sellerUserId" to o.sellerUserId,
                    "customerId" to o.customerId,
                    "customerName" to o.customerName,
                    "customerPhone" to o.customerPhone,
                    "deliveryAddress" to o.deliveryAddress,
                    "totalAmount" to o.totalAmount,
                    "status" to o.status,
                    "paymentMethod" to o.paymentMethod,
                    "orderDateMillis" to o.orderDateMillis,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db?.collection("nursery_orders")?.document(o.id)?.set(payload)?.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync order error: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // Disk Persistence
    // -------------------------------------------------------------

    private fun saveProductsToDisk(list: List<NurseryProduct>) {
        val array = JSONArray()
        for (item in list) {
            array.put(productToJson(item))
        }
        prefs.edit().putString("cached_products", array.toString()).apply()
    }

    private fun saveOrdersToDisk(list: List<NurseryOrder>) {
        val array = JSONArray()
        for (item in list) {
            array.put(orderToJson(item))
        }
        prefs.edit().putString("cached_orders", array.toString()).apply()
    }

    private fun productToJson(p: NurseryProduct): JSONObject {
        val o = JSONObject()
        o.put("id", p.id)
        o.put("nurseryId", p.nurseryId)
        o.put("nurseryName", p.nurseryName)
        o.put("sellerUserId", p.sellerUserId)
        o.put("productName", p.productName)
        o.put("category", p.category)
        o.put("images", JSONArray(p.images))
        o.put("description", p.description)
        o.put("regularPrice", p.regularPrice)
        if (p.discountPrice != null) o.put("discountPrice", p.discountPrice)
        o.put("availableStock", p.availableStock)
        o.put("reservedStock", p.reservedStock)
        o.put("plantSize", p.plantSize)
        o.put("plantType", p.plantType)
        o.put("plantCareInstructions", p.plantCareInstructions)
        o.put("deliveryAvailable", p.deliveryAvailable)
        o.put("deliveryRadiusKm", p.deliveryRadiusKm)
        o.put("productLocation", p.productLocation)
        o.put("isApproved", p.isApproved)
        o.put("status", p.status)
        o.put("linkedPlantId", p.linkedPlantId)
        o.put("createdAtMillis", p.createdAtMillis)
        o.put("updatedAtMillis", p.updatedAtMillis)
        return o
    }

    private fun jsonToProduct(o: JSONObject): NurseryProduct {
        val imagesList = mutableListOf<String>()
        val arr = o.optJSONArray("images")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                imagesList.add(arr.getString(i))
            }
        }
        return NurseryProduct(
            id = o.optString("id", ""),
            nurseryId = o.optString("nurseryId", ""),
            nurseryName = o.optString("nurseryName", ""),
            sellerUserId = o.optString("sellerUserId", ""),
            productName = o.optString("productName", ""),
            category = o.optString("category", NurseryProductCategory.FRUIT_PLANTS.code),
            images = imagesList,
            description = o.optString("description", ""),
            regularPrice = o.optDouble("regularPrice", 0.0),
            discountPrice = if (o.has("discountPrice")) o.optDouble("discountPrice") else null,
            availableStock = o.optInt("availableStock", 10),
            reservedStock = o.optInt("reservedStock", 0),
            plantSize = o.optString("plantSize", "1-2 ft"),
            plantType = o.optString("plantType", "Grafted"),
            plantCareInstructions = o.optString("plantCareInstructions", ""),
            deliveryAvailable = o.optBoolean("deliveryAvailable", true),
            deliveryRadiusKm = o.optInt("deliveryRadiusKm", 30),
            productLocation = o.optString("productLocation", "Siliguri"),
            isApproved = o.optBoolean("isApproved", true),
            status = o.optString("status", "ACTIVE"),
            linkedPlantId = o.optString("linkedPlantId", ""),
            createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis()),
            updatedAtMillis = o.optLong("updatedAtMillis", System.currentTimeMillis())
        )
    }

    private fun orderToJson(ord: NurseryOrder): JSONObject {
        val o = JSONObject()
        o.put("id", ord.id)
        o.put("orderNumber", ord.orderNumber)
        o.put("nurseryId", ord.nurseryId)
        o.put("nurseryName", ord.nurseryName)
        o.put("sellerUserId", ord.sellerUserId)
        o.put("customerId", ord.customerId)
        o.put("customerName", ord.customerName)
        o.put("customerPhone", ord.customerPhone)
        o.put("deliveryAddress", ord.deliveryAddress)
        val itemsArr = JSONArray()
        for (it in ord.items) {
            val io = JSONObject()
            io.put("productId", it.productId)
            io.put("productName", it.productName)
            io.put("category", it.category)
            io.put("quantity", it.quantity)
            io.put("unitPrice", it.unitPrice)
            io.put("totalPrice", it.totalPrice)
            itemsArr.put(io)
        }
        o.put("items", itemsArr)
        o.put("totalAmount", ord.totalAmount)
        o.put("status", ord.status)
        o.put("paymentMethod", ord.paymentMethod)
        o.put("orderDateMillis", ord.orderDateMillis)
        o.put("updatedDateMillis", ord.updatedDateMillis)
        o.put("notes", ord.notes)
        return o
    }

    private fun jsonToOrder(o: JSONObject): NurseryOrder {
        val itemsList = mutableListOf<NurseryOrderItem>()
        val arr = o.optJSONArray("items")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val io = arr.getJSONObject(i)
                itemsList.add(
                    NurseryOrderItem(
                        productId = io.optString("productId"),
                        productName = io.optString("productName"),
                        category = io.optString("category"),
                        quantity = io.optInt("quantity", 1),
                        unitPrice = io.optDouble("unitPrice", 0.0),
                        totalPrice = io.optDouble("totalPrice", 0.0)
                    )
                )
            }
        }
        return NurseryOrder(
            id = o.optString("id", ""),
            orderNumber = o.optString("orderNumber", "ORD-101"),
            nurseryId = o.optString("nurseryId", ""),
            nurseryName = o.optString("nurseryName", ""),
            sellerUserId = o.optString("sellerUserId", ""),
            customerId = o.optString("customerId", ""),
            customerName = o.optString("customerName", ""),
            customerPhone = o.optString("customerPhone", ""),
            deliveryAddress = o.optString("deliveryAddress", ""),
            items = itemsList,
            totalAmount = o.optDouble("totalAmount", 0.0),
            status = o.optString("status", "NEW"),
            paymentMethod = o.optString("paymentMethod", "CASH_ON_DELIVERY"),
            orderDateMillis = o.optLong("orderDateMillis", System.currentTimeMillis()),
            updatedDateMillis = o.optLong("updatedDateMillis", System.currentTimeMillis()),
            notes = o.optString("notes", "")
        )
    }

    private fun createInitialSeedProducts(): List<NurseryProduct> {
        val now = System.currentTimeMillis()
        return listOf(
            NurseryProduct(
                id = "np_01",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                productName = "আম্রপালি আম কলম চারা (Grafted Amrapali)",
                category = NurseryProductCategory.FRUIT_PLANTS.code,
                images = listOf("mango", "amrapali_grafted"),
                description = "উচ্চফলনশীল মিষ্টি আম্রপালি জাতের ১ বছর বয়সী সুস্থ কলম। ১ম বছর থেকেই মুকুল ও ফলন আসে।",
                regularPrice = 180.0,
                discountPrice = 140.0,
                availableStock = 45,
                reservedStock = 0,
                plantSize = "২-৩ ফুট (স্বাভাবিক বৃদ্ধি)",
                plantType = "কলম চারা (Grafted)",
                plantCareInstructions = "পর্যাপ্ত রোদে রাখুন। প্রথম ১৫ দিন নিয়মিত সকালে হালকা পানি দিন। বর্ষায় জল নিষ্কাশন ব্যবস্থা রাখুন।",
                deliveryAvailable = true,
                deliveryRadiusKm = 50,
                productLocation = "শিলিগুড়ি, দার্জিলিং",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now - 86400000L * 7
            ),
            NurseryProduct(
                id = "np_02",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                productName = "টিভি-২৯ অনুমোদিত চা চারা (TV-29 Tea Clone)",
                category = NurseryProductCategory.OUTDOOR_PLANTS.code,
                images = listOf("tea_clone", "tea_leaves"),
                description = "টি রিসার্চ অ্যাসোসিয়েশন (TRA) অনুমোদিত খরা-সহনশীল উচ্চফলনশীল টিভি-২৯ চা ক্লোন চারা।",
                regularPrice = 28.0,
                discountPrice = 20.0,
                availableStock = 500,
                reservedStock = 50,
                plantSize = "১-১.৫ ফুট (প্রোট্রে প্যাক)",
                plantType = "ভেরিফায়েড ক্লোন (TRA Approved)",
                plantCareInstructions = "চা বাগানের ছায়া গাছের নিচে রোপণ করুন। অম্লীয় মাটিতে (pH 4.5-5.5) ভালো ফলন।",
                deliveryAvailable = true,
                deliveryRadiusKm = 100,
                productLocation = "জলপাইগুড়ি / ডুয়ার্স",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now - 86400000L * 5
            ),
            NurseryProduct(
                id = "np_03",
                nurseryId = "app_kisan_phul",
                nurseryName = "মা তারা হাইটেক প্ল্যান্ট নার্সারি",
                sellerUserId = "usr_kisan_phul_2",
                productName = "ড্রাগন ফ্রুট চারা (রেড ভেলভেট ভ্যারাইটি)",
                category = NurseryProductCategory.FRUIT_PLANTS.code,
                images = listOf("dragon_fruit", "pitaya_plant"),
                description = "ভিয়েতনামি লাল মাংসল মিষ্টি ড্রাগন ফ্রুট চারা। সিমেন্টের খুঁটিতে সহজেই রোপণযোগ্য।",
                regularPrice = 120.0,
                discountPrice = 95.0,
                availableStock = 60,
                reservedStock = 0,
                plantSize = "১.৫ ফুট কাটিং চারা",
                plantType = "মাদার প্ল্যান্ট কাটিং",
                plantCareInstructions = "বেলে-দোআঁশ মাটিতে রোপণ করুন। অতিবৃষ্টিতে জল জমতে দেবেন না।",
                deliveryAvailable = true,
                deliveryRadiusKm = 40,
                productLocation = "ময়নাগুড়ি, জলপাইগুড়ি",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now - 86400000L * 4
            ),
            NurseryProduct(
                id = "np_04",
                nurseryId = "app_kisan_phul",
                nurseryName = "মা তারা হাইটেক প্ল্যান্ট নার্সারি",
                sellerUserId = "usr_kisan_phul_2",
                productName = "হাইব্রিড জবা ও থাই গোলাপের চারা",
                category = NurseryProductCategory.FLOWER_PLANTS.code,
                images = listOf("rose_flower", "hibiscus_hybrid"),
                description = "বারোমাসি রঙিন ফুলের গুচ্ছ। ব্যালকনি ও বাগানের সৌন্দর্যের জন্য আদর্শ।",
                regularPrice = 150.0,
                discountPrice = null,
                availableStock = 30,
                reservedStock = 0,
                plantSize = "টব সহ মাঝারি",
                plantType = "লেয়ারিং / কলম",
                plantCareInstructions = "প্রতিদিন ৪-৫ ঘণ্টা সরাসরি রোদ এবং মাসে একবার জৈব সার দিন।",
                deliveryAvailable = true,
                deliveryRadiusKm = 25,
                productLocation = "ময়নাগুড়ি",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now - 86400000L * 3
            ),
            NurseryProduct(
                id = "np_05",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                productName = "স্নেক প্ল্যান্ট ও এরিকা পাম (ইনডোর এয়ার পিউরিফায়ার)",
                category = NurseryProductCategory.INDOOR_PLANTS.code,
                images = listOf("snake_plant", "areca_palm"),
                description = "ঘরের ভেতরের বাতাস পরিশোধক ও অক্সিজেন সরবরাহকারী কম যত্নের ইনডোর গাছ।",
                regularPrice = 320.0,
                discountPrice = 270.0,
                availableStock = 25,
                reservedStock = 0,
                plantSize = "২ ফুট (সিরামিক টব সহ)",
                plantType = "ইনডোর অর্নামেন্টাল",
                plantCareInstructions = "সরাসরি কড়া রোদ এড়িয়ে ঘরের উজ্জ্বল কোণে রাখুন। মাটিতে আঙুল দিয়ে শুকনা লাগলে পানি দিন।",
                deliveryAvailable = true,
                deliveryRadiusKm = 50,
                productLocation = "শিলিগুড়ি",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now - 86400000L * 2
            ),
            NurseryProduct(
                id = "np_06",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                productName = "প্রোট্রে টমেটো ও বেগুন হাইব্রিড চারা (৫০ পিস ট্রে)",
                category = NurseryProductCategory.VEGETABLE_PLANTS.code,
                images = listOf("tomato_seedlings", "brinjal_tray"),
                description = "কোকোপিট ও ভার্মিকম্পোস্টে তৈরি জীবাণুমুক্ত ৫০ পিস উচ্চফলনশীল সবজি চারা ট্রে।",
                regularPrice = 160.0,
                discountPrice = 125.0,
                availableStock = 80,
                reservedStock = 0,
                plantSize = "২৫ দিন বয়সী রেডি চারা",
                plantType = "প্রোট্রে হাইব্রিড চারা",
                plantCareInstructions = "জমিতে সরাসরি বিকেলে রোপণ করুন এবং গোড়ায় পরিমিত সেচ দিন।",
                deliveryAvailable = true,
                deliveryRadiusKm = 60,
                productLocation = "দার্জিলিং",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now - 86400000L * 1
            ),
            NurseryProduct(
                id = "np_07",
                nurseryId = "app_kisan_phul",
                nurseryName = "মা তারা হাইটেক প্ল্যান্ট নার্সারি",
                sellerUserId = "usr_kisan_phul_2",
                productName = "কালমেঘ ও অশ্বগন্ধা ঔষধি চারা",
                category = NurseryProductCategory.MEDICINAL_PLANTS.code,
                images = listOf("medicinal_herbs", "ashwagandha"),
                description = "আয়ুর্বেদিক ও রোগপ্রতিরোধক গুণসম্পন্ন প্রাকৃতিক ঔষধি চারা।",
                regularPrice = 90.0,
                discountPrice = 75.0,
                availableStock = 40,
                reservedStock = 0,
                plantSize = "১ ফুট",
                plantType = "দেশি ভেষজ",
                plantCareInstructions = "সহজে যে কোনো মাটিতে বাঁচে। অতিরিক্ত জল প্রয়োজন নেই।",
                deliveryAvailable = true,
                deliveryRadiusKm = 30,
                productLocation = "জলপাইগুড়ি",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now
            ),
            NurseryProduct(
                id = "np_08",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                productName = "ভার্মিকম্পোস্ট ও ট্রাইকোডার্মা জৈব সার (৫ কেজি প্যাক)",
                category = NurseryProductCategory.FERTILIZERS.code,
                images = listOf("vermicompost", "bio_fertilizer"),
                description = "গাছের দ্রুত বৃদ্ধি ও মাটির উর্বরতা বৃদ্ধির জন্য ১০০% বিশুদ্ধ কেঁচো সার।",
                regularPrice = 200.0,
                discountPrice = 160.0,
                availableStock = 120,
                reservedStock = 0,
                plantSize = "৫ কেজি সিল প্যাক",
                plantType = "জৈব সার",
                plantCareInstructions = "প্রতিটি গাছের গোড়ার মাটিতে মাসে ১০০-১৫০ গ্রাম মিশিয়ে দিন।",
                deliveryAvailable = true,
                deliveryRadiusKm = 50,
                productLocation = "শিলিগুড়ি",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now
            ),
            NurseryProduct(
                id = "np_09",
                nurseryId = "app_kisan_phul",
                nurseryName = "মা তারা হাইটেক প্ল্যান্ট নার্সারি",
                sellerUserId = "usr_kisan_phul_2",
                productName = "প্রুনিং কাঁচি ও স্প্রেয়ার সেট (Gardening Tools)",
                category = NurseryProductCategory.GARDENING_TOOLS.code,
                images = listOf("pruning_shear", "spray_bottle"),
                description = "গাছের ডাল ছাঁটাই ও কীটনাশক স্প্রে করার মজবুত স্টেইনলেস স্টিল টুল সেট।",
                regularPrice = 450.0,
                discountPrice = 380.0,
                availableStock = 18,
                reservedStock = 0,
                plantSize = "স্ট্যান্ডার্ড সাইজ",
                plantType = "যন্ত্রপাতি",
                plantCareInstructions = "ব্যবহারের পর শুকনা কাপড় দিয়ে মুছে রাখুন।",
                deliveryAvailable = true,
                deliveryRadiusKm = 40,
                productLocation = "জলপাইগুড়ি",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now
            ),
            NurseryProduct(
                id = "np_10",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                productName = "১০ ইঞ্চি টেরাকোটা ও ইউভি প্লাস্টিক টব সেট",
                category = NurseryProductCategory.POTS_ACCESSORIES.code,
                images = listOf("pots_set", "terracotta_planter"),
                description = "ছাদবাগান ও বারান্দায় গাছ লাগানোর জন্য উপযুক্ত ড্রেনেজ ছিদ্রযুক্ত টেকসই টব।",
                regularPrice = 250.0,
                discountPrice = 190.0,
                availableStock = 35,
                reservedStock = 0,
                plantSize = "১০ ইঞ্চি ড্রেনেজ প্যাক",
                plantType = "টব ও আনুষাঙ্গিক",
                plantCareInstructions = "সহজে পরিষ্কার করা যায়। রোদে টেকসই।",
                deliveryAvailable = true,
                deliveryRadiusKm = 50,
                productLocation = "শিলিগুড়ি",
                isApproved = true,
                status = "ACTIVE",
                createdAtMillis = now
            )
        )
    }

    private fun createInitialSeedOrders(): List<NurseryOrder> {
        val now = System.currentTimeMillis()
        return listOf(
            NurseryOrder(
                id = "ord_101",
                orderNumber = "KS-NUR-9201",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                customerId = "cust_suman_01",
                customerName = "সুমন সরকার (Suman Sarkar)",
                customerPhone = "9832014587",
                deliveryAddress = "শালুগাড়া রোড, চম্পাসারি, শিলিগুড়ি",
                items = listOf(
                    NurseryOrderItem(
                        productId = "np_01",
                        productName = "আম্রপালি আম কলম চারা",
                        category = NurseryProductCategory.FRUIT_PLANTS.code,
                        quantity = 3,
                        unitPrice = 140.0,
                        totalPrice = 420.0
                    )
                ),
                totalAmount = 420.0,
                status = "NEW",
                paymentMethod = "CASH_ON_DELIVERY",
                orderDateMillis = now - 3600000L * 4,
                updatedDateMillis = now - 3600000L * 4,
                notes = "জরুরি ডেলিভারি প্রয়োজন। ধন্যবাদ।"
            ),
            NurseryOrder(
                id = "ord_102",
                orderNumber = "KS-NUR-9188",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                customerId = "cust_pranab_02",
                customerName = "প্রণব কুমার দাস",
                customerPhone = "9434088214",
                deliveryAddress = "বীরপাড়া, আলিপুরদুয়ার",
                items = listOf(
                    NurseryOrderItem(
                        productId = "np_02",
                        productName = "টিভি-২৯ অনুমোদিত চা চারা",
                        category = NurseryProductCategory.OUTDOOR_PLANTS.code,
                        quantity = 50,
                        unitPrice = 20.0,
                        totalPrice = 1000.0
                    )
                ),
                totalAmount = 1000.0,
                status = "ACCEPTED",
                paymentMethod = "CASH_ON_DELIVERY",
                orderDateMillis = now - 86400000L * 2,
                updatedDateMillis = now - 86400000L * 1,
                notes = "টি গার্ডেন সম্প্রসারণের জন্য।"
            ),
            NurseryOrder(
                id = "ord_103",
                orderNumber = "KS-NUR-9012",
                nurseryId = "app_siliguri_green",
                nurseryName = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                sellerUserId = "usr_pioneer_nursery_1",
                customerId = "cust_anita_03",
                customerName = "অনিতা রায়",
                customerPhone = "9749128834",
                deliveryAddress = "সেভোক রোড, শিলিগুড়ি",
                items = listOf(
                    NurseryOrderItem(
                        productId = "np_05",
                        productName = "স্নেক প্ল্যান্ট ও এরিকা পাম",
                        category = NurseryProductCategory.INDOOR_PLANTS.code,
                        quantity = 2,
                        unitPrice = 270.0,
                        totalPrice = 540.0
                    )
                ),
                totalAmount = 540.0,
                status = "COMPLETED",
                paymentMethod = "CASH_ON_DELIVERY",
                orderDateMillis = now - 86400000L * 6,
                updatedDateMillis = now - 86400000L * 5,
                notes = "ডেলিভারি সফল ও নগদ মূল্য গ্রহণ করা হয়েছে।"
            )
        )
    }
}
