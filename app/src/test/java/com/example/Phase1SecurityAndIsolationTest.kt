package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.BookingLifecycleStatus
import com.example.data.model.FarmServiceType
import com.example.data.model.MachineryInquiryRecord
import com.example.data.model.MachineryRentalRequest
import com.example.data.model.MachineryServiceRequest
import com.example.data.model.NurseryOrder
import com.example.data.model.NurseryOrderItem
import com.example.data.model.NurseryProduct
import com.example.data.model.NurseryProductCategory
import com.example.data.model.RentalUnit
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.remote.NurseryProductRepository
import com.example.data.repository.MachineryRepository
import com.example.ui.nursery.NurserySellerDashboardScreen
import com.example.ui.theme.KrishakSathiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class Phase1SecurityAndIsolationTest {

    private lateinit var context: Context
    private lateinit var roomDb: AppDatabase
    private lateinit var machineryRepo: MachineryRepository

    private val composeRule = createComposeRule()

    private val dbResource = object : ExternalResource() {
        override fun before() {
            context = ApplicationProvider.getApplicationContext()
            UserSessionManager.clearSession(context)
            roomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            machineryRepo = MachineryRepository(context, roomDb)
            runBlocking { machineryRepo.ensureSeeded() }
        }

        override fun after() {
            UserSessionManager.clearSession(context)
            roomDb.close()
        }
    }

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(dbResource).around(composeRule)

    @Test
    fun `UserSessionManager prevents privilege escalation, sanitizes quick login, and persists sessions`() {
        // 1. Quick login cannot spoof admin email or admin/nursery role
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "local_farmer_offline",
                userName = "Test Farmer",
                email = UserSessionManager.ADMIN_EMAIL,
                role = UserSession.ROLE_ADMIN,
                authProvider = UserSession.PROVIDER_FARMER_DIRECT
            )
        )
        val sanitizedQuick = UserSessionManager.getSession(context)
        assertNotNull(sanitizedQuick)
        assertNull("Quick login must strip email", sanitizedQuick?.email)
        assertEquals(UserSession.ROLE_FARMER, sanitizedQuick?.role)
        assertFalse(UserSessionManager.isAdmin(context))
        assertFalse(UserSessionManager.isNurseryOwner(context))
        assertFalse(UserSessionManager.hasCloudFirebaseSession(context))
        assertTrue(UserSessionManager.isLocalQuickLogin(context))

        // Role escalation call must be ignored for quick login
        UserSessionManager.updateUserRole(context, UserSession.ROLE_ADMIN)
        assertFalse(UserSessionManager.isAdmin(context))
        assertEquals(UserSession.ROLE_FARMER, UserSessionManager.getSession(context)?.role)

        // 2. Non-admin Google account cannot hold ROLE_ADMIN
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "google_user_123",
                userName = "Normal User",
                email = "normal.farmer@example.com",
                role = UserSession.ROLE_ADMIN,
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )
        assertEquals(UserSession.ROLE_FARMER, UserSessionManager.getSession(context)?.role)
        assertFalse(UserSessionManager.isAdmin(context))
        assertTrue(UserSessionManager.hasCloudFirebaseSession(context))

        UserSessionManager.updateUserRole(context, UserSession.ROLE_ADMIN)
        assertFalse(UserSessionManager.isAdmin(context))

        // 3. Re-initializing from disk preserves the sanitized Google session cleanly
        val restored = UserSessionManager.init(context)
        assertEquals("google_user_123", restored?.userId)
        assertEquals(UserSession.ROLE_FARMER, restored?.role)

        // 4. Designated admin email via Google gets ROLE_ADMIN and passes isAdmin
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "admin_uid_1",
                userName = "Platform Admin",
                email = UserSessionManager.ADMIN_EMAIL,
                role = UserSession.ROLE_FARMER,
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )
        assertTrue(UserSessionManager.isAdmin(context))
        assertEquals(UserSession.ROLE_ADMIN, UserSessionManager.getSession(context)?.role)

        // 5. Token validation timestamp + clearSession
        UserSessionManager.recordTokenValidated(context, 1_760_000_000_000L)
        assertEquals(1_760_000_000_000L, UserSessionManager.getLastTokenValidatedAt(context))
        UserSessionManager.clearSession(context)
        assertFalse(UserSessionManager.isLoggedIn(context))
        assertNull(UserSessionManager.getSession(context))
    }

    @Test
    fun `NurseryProductRepository enforces strict tenant isolation and validation`() = runBlocking {
        val repo = NurseryProductRepository(context)

        // Unrelated seller must NOT inherit seeded "usr_pioneer_nursery_1" products or orders
        val unrelatedProducts = repo.observeProductsBySeller("seller_unrelated_B").first()
        val unrelatedOrders = repo.observeOrdersForSeller("seller_unrelated_B").first()
        val unrelatedMetrics = repo.getMetricsForSeller("seller_unrelated_B").first()
        val blankSellerProducts = repo.observeProductsBySeller("").first()

        assertTrue(unrelatedProducts.isEmpty())
        assertTrue(unrelatedOrders.isEmpty())
        assertTrue(blankSellerProducts.isEmpty())
        assertEquals(0, unrelatedMetrics.totalProducts)
        assertEquals(0, unrelatedMetrics.newOrdersCount)

        // Seeded pioneer seller DOES have products
        val pioneerProducts = repo.observeProductsBySeller("usr_pioneer_nursery_1").first()
        assertTrue(pioneerProducts.isNotEmpty())
        val pioneerProd = pioneerProducts.first()

        // Cross-tenant mutation attempts fail with SecurityException
        val updateOther = repo.updateProduct(pioneerProd.copy(productName = "Tampered"), "seller_unrelated_B", AppLanguage.ENGLISH)
        assertTrue(updateOther.isFailure)
        assertTrue(updateOther.exceptionOrNull() is SecurityException)

        val deleteOther = repo.deleteProduct(pioneerProd.id, "seller_unrelated_B", AppLanguage.ENGLISH)
        assertTrue(deleteOther.isFailure)
        assertTrue(deleteOther.exceptionOrNull() is SecurityException)

        val stockOther = repo.updatePriceAndStock(pioneerProd.id, "seller_unrelated_B", 120.0, null, 5)
        assertTrue(stockOther.isFailure)
        assertTrue(stockOther.exceptionOrNull() is SecurityException)

        val toggleOther = repo.toggleStockStatus(pioneerProd.id, "seller_unrelated_B", true)
        assertTrue(toggleOther.isFailure)
        assertTrue(toggleOther.exceptionOrNull() is SecurityException)

        // Invalid price / stock validation
        val invalidPrice = repo.updatePriceAndStock(pioneerProd.id, "usr_pioneer_nursery_1", 0.0, null, 5)
        assertTrue(invalidPrice.isFailure)
        val invalidDiscount = repo.updatePriceAndStock(pioneerProd.id, "usr_pioneer_nursery_1", 100.0, 150.0, 5)
        assertTrue(invalidDiscount.isFailure)
        val negativeStock = repo.updatePriceAndStock(pioneerProd.id, "usr_pioneer_nursery_1", 100.0, null, -1)
        assertTrue(negativeStock.isFailure)

        // Place an order for seller A and verify cross-seller order actions are blocked
        val sellerAProduct = NurseryProduct(
            id = "iso_prod_a",
            nurseryId = "nur_a",
            nurseryName = "Nursery A",
            sellerUserId = "seller_a",
            productName = "Grafted Himsagar Mango",
            category = NurseryProductCategory.FRUIT_PLANTS.code,
            regularPrice = 200.0,
            availableStock = 5
        )
        assertTrue(repo.addProduct(sellerAProduct, "seller_a", AppLanguage.ENGLISH, isApprovedPartner = true).isSuccess)

        val orderRes = repo.placeCustomerOrder(
            NurseryOrder(
                id = "iso_ord_1",
                nurseryId = "nur_a",
                nurseryName = "Nursery A",
                sellerUserId = "seller_a",
                customerId = "customer_1",
                customerName = "Raju Farmer",
                customerPhone = "+919800001111",
                items = listOf(NurseryOrderItem("iso_prod_a", "Grafted Himsagar Mango", "fruit_plants", 1, 200.0, 200.0)),
                totalAmount = 200.0
            ),
            lang = AppLanguage.ENGLISH
        )
        assertTrue(orderRes.isSuccess)

        // Seller B cannot accept/complete/cancel Seller A's order
        val acceptWrong = repo.acceptOrder("iso_ord_1", "seller_unrelated_B")
        assertTrue(acceptWrong.isFailure)
        assertTrue(acceptWrong.exceptionOrNull() is SecurityException)

        val completeWrong = repo.completeOrder("iso_ord_1", "seller_unrelated_B")
        assertTrue(completeWrong.isFailure)
        assertTrue(completeWrong.exceptionOrNull() is SecurityException)

        val cancelWrong = repo.cancelOrderAndRestoreStock("iso_ord_1", "hack", AppLanguage.ENGLISH, actorUserId = "seller_unrelated_B")
        assertTrue(cancelWrong.isFailure)
        assertTrue(cancelWrong.exceptionOrNull() is SecurityException)

        // Seller A completes the order; cancelling a COMPLETED order must then fail
        assertTrue(repo.acceptOrder("iso_ord_1", "seller_a").isSuccess)
        assertTrue(repo.completeOrder("iso_ord_1", "seller_a").isSuccess)
        val cancelCompleted = repo.cancelOrderAndRestoreStock("iso_ord_1", "late cancel", AppLanguage.ENGLISH, actorUserId = "seller_a")
        assertTrue(cancelCompleted.isFailure)
    }

    @Test
    fun `NurserySellerDashboardScreen blocks unapproved user instead of falling back to seeded nursery`() {
        val appRepo = NurseryApplicationRepository(context)
        composeRule.setContent {
            KrishakSathiTheme {
                NurserySellerDashboardScreen(
                    userId = "farmer_unapproved_99",
                    currentLanguage = AppLanguage.ENGLISH,
                    repository = appRepo,
                    onBackToHome = {},
                    onOpenPublicStore = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Seller Dashboard is reserved for approved nurseries only").assertIsDisplayed()
    }

    @Test
    fun `MachineryRepository enforces actor authorization on rentals, inquiries, and services`() = runBlocking {
        val rental = machineryRepo.submitRentalRequest(
            MachineryRentalRequest(
                id = "iso_rent_1",
                userId = "farmer_u1",
                farmerName = "Ratan",
                farmerPhone = "+919800005555",
                machineId = "ref_power_tiller_12hp",
                machineName = "Power Tiller",
                sellerUserId = "dealer_owner_1",
                locality = "Siliguri",
                rentalUnit = RentalUnit.DAILY.code,
                durationUnits = 1
            )
        ).getOrThrow()

        val inquiry = machineryRepo.submitInquiryOrReport(
            MachineryInquiryRecord(
                id = "iso_inq_1",
                userId = "farmer_u1",
                farmerName = "Ratan",
                farmerPhone = "+919800005555",
                machineId = "ref_power_tiller_12hp",
                machineName = "Power Tiller",
                sellerUserId = "dealer_owner_1",
                inquiryType = "QUOTATION",
                message = "Need price"
            )
        ).getOrThrow()

        val blankProviderService = machineryRepo.submitServiceRequest(
            MachineryServiceRequest(
                id = "iso_srv_blank",
                userId = "farmer_u1",
                farmerName = "Ratan",
                farmerPhone = "+919800005555",
                serviceTypeCode = FarmServiceType.ROTAVATOR_LAND_PREP.code,
                locality = "Matigara",
                landSizeText = "1 Acre",
                cropName = "Paddy",
                preferredDate = "2026-12-05",
                providerUserId = ""
            )
        ).getOrThrow()

        // Unrelated seller cannot mutate rental, inquiry, or unassigned blank-provider service
        val badRental = machineryRepo.updateRentalStatus(rental.id, BookingLifecycleStatus.CONFIRMED, actorUserId = "wrong_seller_x")
        assertTrue(badRental.isFailure)
        assertTrue(badRental.exceptionOrNull() is SecurityException)

        val badInquiry = machineryRepo.updateInquiryReply(inquiry.id, "Fake reply", BookingLifecycleStatus.COMPLETED, actorUserId = "wrong_seller_x")
        assertTrue(badInquiry.isFailure)
        assertTrue(badInquiry.exceptionOrNull() is SecurityException)

        val badBlankService = machineryRepo.updateServiceStatus(blankProviderService.id, BookingLifecycleStatus.CONFIRMED, actorUserId = "wrong_seller_x")
        assertTrue(badBlankService.isFailure)
        assertTrue(badBlankService.exceptionOrNull() is SecurityException)

        // Assigned seller CAN update rental & reply to inquiry; admin CAN update unassigned service
        assertTrue(machineryRepo.updateRentalStatus(rental.id, BookingLifecycleStatus.CONFIRMED, actorUserId = "dealer_owner_1").isSuccess)
        assertTrue(machineryRepo.updateInquiryReply(inquiry.id, "Quote sent", BookingLifecycleStatus.COMPLETED, actorUserId = "dealer_owner_1").isSuccess)
        assertTrue(machineryRepo.updateServiceStatus(blankProviderService.id, BookingLifecycleStatus.CONFIRMED, actorUserId = "admin", isAdmin = true).isSuccess)
    }

    @Test
    fun `AppDatabase MIGRATION_13_14 preserves existing v13 records and creates v14 trust tables without destructive reset`() {
        val dbName = "test_mig_13_14.db"
        context.deleteDatabase(dbName)

        val callback = object : SupportSQLiteOpenHelper.Callback(13) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS farmer_expenses (
                        localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        firestoreId TEXT NOT NULL,
                        cropName TEXT NOT NULL,
                        category TEXT NOT NULL,
                        amount REAL NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        note TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO farmer_expenses (firestoreId, cropName, category, amount, dateMillis, note) VALUES ('fs_1', 'Aman Paddy', 'Seeds', 1250.0, 1760000000000, 'Preserved v13 row')"
                )
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(callback)
                .build()
        )

        val sqliteDb = helper.writableDatabase
        AppDatabase.MIGRATION_13_14.migrate(sqliteDb)

        // Existing v13 record must remain completely intact
        sqliteDb.query("SELECT cropName, amount, note FROM farmer_expenses WHERE firestoreId = 'fs_1'").use { cursor ->
            assertTrue("Pre-existing v13 row must survive MIGRATION_13_14", cursor.moveToFirst())
            assertEquals("Aman Paddy", cursor.getString(0))
            assertEquals(1250.0, cursor.getDouble(1), 0.001)
            assertEquals("Preserved v13 row", cursor.getString(2))
        }

        // All 5 new v14 tables must exist and accept inserts
        val expectedV14Tables = listOf(
            "fraud_reports",
            "grievance_tickets",
            "seller_suspensions",
            "trust_audit_logs",
            "inquiry_activity_logs"
        )
        expectedV14Tables.forEach { tbl ->
            sqliteDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tbl'").use { cursor ->
                assertTrue("Expected v14 table $tbl after MIGRATION_13_14", cursor.moveToFirst())
            }
        }

        sqliteDb.execSQL(
            """
            INSERT INTO grievance_tickets (
                id, userId, userName, category, subject, description, relatedEntityId, status, adminResponse, createdAtMillis, updatedAtMillis
            ) VALUES (
                'grv_1', 'u_1', 'Farmer', 'DELIVERY', 'Delayed sapling', 'Needs review', 'ord_1', 'OPEN', '', 1760000001000, 1760000001000
            )
            """.trimIndent()
        )
        sqliteDb.query("SELECT subject FROM grievance_tickets WHERE id='grv_1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Delayed sapling", cursor.getString(0))
        }

        sqliteDb.close()
        helper.close()
        context.deleteDatabase(dbName)
    }
}
