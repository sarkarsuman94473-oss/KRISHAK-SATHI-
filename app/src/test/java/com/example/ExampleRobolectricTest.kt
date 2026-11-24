package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.CropMasterDatabase
import com.example.data.datasource.StarterNurseryDatabase
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CropEntity
import com.example.data.local.entity.NurseryEntity
import com.example.data.local.entity.PlantCatalogEntity
import com.example.data.local.entity.SeedListingEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CropCategory
import com.example.util.L10n
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Krishak Sathi", appName)
  }

  @Test
  fun `verify expanded crop master database contains leafy vegetables and root crops`() {
    val crops = CropMasterDatabase.crops
    assertTrue("Crops count should be > 30", crops.size >= 30)

    val spinach = CropMasterDatabase.getCropById("palak")
    assertNotNull(spinach)
    assertEquals("পালং শাক (Spinach)", spinach?.nameBn)

    val tomato = CropMasterDatabase.getCropOrDefault("টমেটো", null)
    assertNotNull(tomato)

    val tea = CropMasterDatabase.getCropById("tea")
    assertNotNull(tea)
    assertEquals(CropCategory.TEA, tea?.category)
  }

  @Test
  fun `verify Siliguri starter directory contains nurseries and seed dealers`() {
    val nurseries = StarterNurseryDatabase.starterNurseries
    assertTrue("Nurseries count should be at least 20", nurseries.size >= 20)

    val pushpanjali = nurseries.find { it.nameEn.contains("Pushpanjali", ignoreCase = true) }
    assertNotNull("Pushpanjali Nursery should exist", pushpanjali)
    assertTrue("Pushpanjali should be verified", pushpanjali?.isVerified == true)
    assertTrue("Pushpanjali should have wholesale enabled", pushpanjali?.isWholesale == true)

    val jaiswal = nurseries.find { it.nameEn.contains("Jaiswal", ignoreCase = true) }
    assertNotNull("Jaiswal Seeds should exist", jaiswal)

    val ggCrop = nurseries.find { it.nameEn.contains("GG Crop", ignoreCase = true) }
    assertNotNull("GG Crop Science should exist", ggCrop)

    val seeds = StarterNurseryDatabase.starterSeeds
    assertTrue("Seeds count should be > 5", seeds.size >= 5)
    val hybridTomato = seeds.find { it.cropNameEn.contains("Tomato", ignoreCase = true) }
    assertNotNull("Tomato hybrid seed should exist", hybridTomato)
    assertTrue("Seed germination rate should be >= 80%", (hybridTomato?.germinationRatePercent ?: 0) >= 80)
  }

  @Test
  fun `verify nursery localization keys`() {
    val bnTitle = L10n.t("card_nursery_title", AppLanguage.BENGALI)
    assertTrue("Bangla nursery title should contain নার্সারি", bnTitle.contains("নার্সারি"))

    val enTitle = L10n.t("card_nursery_title", AppLanguage.ENGLISH)
    assertTrue("English nursery title should contain Nursery", enTitle.contains("Nursery"))
  }

  @Test
  fun `verify Room relational database entities for Nursery, PlantCatalog, and SeedListing`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    // 1. Insert Nursery Entity (Seller Information)
    val nursery = NurseryEntity(
      id = "nurs_sil_test_01",
      nameBn = "পুস্পাঞ্জলি নার্সারি",
      nameEn = "Pushpanjali Nursery",
      ownerName = "সুব্রত রায়",
      phone = "9434012845",
      whatsapp = "9434012845",
      addressBn = "সেভোক রোড, ২য় মাইল",
      city = "Siliguri",
      district = "Darjeeling",
      pinCode = "734001",
      sellerType = "NURSERY",
      isVerified = true,
      isWholesale = true,
      isRetail = true,
      categoriesCsv = "FRUITS,VEGETABLE_SAPLINGS,FLOWERS_ORNAMENTAL",
      rating = 4.9,
      reviewCount = 86,
      badgeText = "যাচাইকৃত নার্সারি"
    )
    db.nurseryDao().insertNurseries(listOf(nursery))
    val retrievedNursery = db.nurseryDao().getNurseryById("nurs_sil_test_01")
    assertNotNull(retrievedNursery)
    assertEquals("Pushpanjali Nursery", retrievedNursery?.nameEn)
    assertEquals("সুব্রত রায়", retrievedNursery?.ownerName)

    // 2. Insert PlantCatalog Entity (Category & Stock Status & Relationships)
    val plantMango = PlantCatalogEntity(
      id = "plant_mango_01",
      nurseryId = "nurs_sil_test_01",
      category = "FRUITS",
      plantNameBn = "আম্রপালি কলম চারা",
      plantNameEn = "Amrapali Mango Grafted",
      varietyBn = "আম্রপালি",
      scientificName = "Mangifera indica",
      saplingType = "GRAFTED",
      stockStatus = "IN_STOCK",
      availableQuantity = 250,
      retailPrice = 120.0,
      wholesalePrice = 75.0,
      minWholesaleQty = 30,
      sellerName = "Pushpanjali Nursery",
      sellerPhone = "9434012845"
    )
    val plantTomato = PlantCatalogEntity(
      id = "plant_tomato_01",
      nurseryId = "nurs_sil_test_01",
      category = "VEGETABLE_SAPLINGS",
      plantNameBn = "টমেটো হাইব্রিড চারা",
      plantNameEn = "Tomato Seedlings",
      varietyBn = "সাহো ৩২৫১",
      saplingType = "TISSUE_CULTURE",
      stockStatus = "LIMITED",
      availableQuantity = 15,
      retailPrice = 4.0,
      wholesalePrice = 2.2,
      minWholesaleQty = 500,
      sellerName = "Pushpanjali Nursery"
    )
    db.plantCatalogDao().insertPlants(listOf(plantMango, plantTomato))

    // Verify Category and Stock Status queries
    val fruitPlants = db.plantCatalogDao().getPlantsByCategory("FRUITS").first()
    assertEquals(1, fruitPlants.size)
    assertEquals("আম্রপালি কলম চারা", fruitPlants[0].plantNameBn)

    val limitedPlants = db.plantCatalogDao().getPlantsByStockStatus("LIMITED").first()
    assertEquals(1, limitedPlants.size)
    assertEquals("plant_tomato_01", limitedPlants[0].id)

    // 3. Insert SeedListing Entity (Category, Stock Status & Certification)
    val seedChilli = SeedListingEntity(
      id = "seed_chilli_01",
      nurseryId = "nurs_sil_test_01",
      nurseryName = "Pushpanjali Nursery",
      cropNameBn = "লঙ্কা হাইব্রিড বীজ",
      cropNameEn = "Chilli Hybrid Seeds",
      varietyBn = "ভিএনআর সানিয়া",
      companyOrBrand = "VNR",
      seedCategory = "HYBRID",
      packetSize = "10g",
      retailPrice = 380.0,
      wholesalePrice = 310.0,
      minWholesalePackets = 15,
      stockStatus = "IN_STOCK",
      stockPacketsAvailable = 50,
      germinationRatePercent = 88,
      isCertified = true,
      sellerPhone = "9434012845",
      sellerCity = "Siliguri"
    )
    db.seedListingDao().insertSeedListings(listOf(seedChilli))

    val hybridSeeds = db.seedListingDao().getSeedsByCategory("HYBRID").first()
    assertEquals(1, hybridSeeds.size)
    assertEquals("ভিএনআর সানিয়া", hybridSeeds[0].varietyBn)

    // 4. Verify Relational 1-to-Many Join (NurseryWithFullInventory)
    val fullInventory = db.nurseryDao().getNurseryWithFullInventory("nurs_sil_test_01").first()
    assertNotNull(fullInventory)
    assertEquals(2, fullInventory?.plantCatalog?.size)
    assertEquals(1, fullInventory?.seedListings?.size)
    assertEquals("Pushpanjali Nursery", fullInventory?.nursery?.nameEn)

    // 5. Verify PlantWithSeller and SeedWithSeller joins
    val plantWithSeller = db.plantCatalogDao().getPlantWithSeller("plant_mango_01").first()
    assertNotNull(plantWithSeller)
    assertEquals("Pushpanjali Nursery", plantWithSeller?.seller?.nameEn)

    val seedWithSeller = db.seedListingDao().getSeedWithSeller("seed_chilli_01").first()
    assertNotNull(seedWithSeller)
    assertEquals("9434012845", seedWithSeller?.seller?.phone)

    db.close()
  }

  @Test
  fun `verify Indian field crops grains millets pulses oilseeds and 10-stage potato`() {
    val crops = CropMasterDatabase.crops
    assertTrue("Total crops library should be > 60 crops", crops.size >= 60)

    // 1. Verify Rice
    val rice = CropMasterDatabase.getCropById("rice_paddy")
    assertNotNull("Rice should be in database", rice)
    assertEquals("ধান (Rice / Paddy)", rice?.nameBn)
    assertEquals(CropCategory.GRAINS_FIELD_CROPS, rice?.category)
    assertTrue("Rice should have cultivation steps", (rice?.steps?.size ?: 0) >= 6)

    // 2. Verify Potato single expanded entry with 10 stages
    val potatoList = crops.filter { it.cropId == "potato" }
    assertEquals("There must be EXACTLY ONE potato record (no duplicates)", 1, potatoList.size)
    val potato = potatoList[0]
    assertEquals("আলু (Potato)", potato.nameBn)
    assertEquals(10, potato.steps.size)
    assertEquals("১. বীজ আলু নির্বাচন ও সুপ্ততা ভাঙা", potato.steps[0].titleBn)
    assertEquals("১০. আলু তোলা, কিউরিং ও কোল্ড স্টোরেজ", potato.steps[9].titleBn)

    // 3. Verify Alias search
    val foundByPaddy = CropMasterDatabase.getCropOrDefault("Paddy", null)
    assertEquals("rice_paddy", foundByPaddy.cropId)

    val foundByTur = CropMasterDatabase.getCropOrDefault("Tur", null)
    assertEquals("arhar_pigeon_pea", foundByTur.cropId)

    val foundByAlu = CropMasterDatabase.getCropOrDefault("আলু", null)
    assertEquals("potato", foundByAlu.cropId)

    // 4. Verify Millets, Pulses, Oilseeds, Commercial, Fodder
    assertNotNull("Bajra millet should exist", CropMasterDatabase.getCropById("bajra"))
    assertNotNull("Ragi millet should exist", CropMasterDatabase.getCropById("ragi"))
    assertNotNull("Lentil pulse should exist", CropMasterDatabase.getCropById("lentil"))
    assertNotNull("Mustard oilseed should exist", CropMasterDatabase.getCropById("mustard"))
    assertNotNull("Groundnut should exist", CropMasterDatabase.getCropById("groundnut"))
    assertNotNull("Sugarcane commercial crop should exist", CropMasterDatabase.getCropById("sugarcane"))
    assertNotNull("Jute fiber crop should exist", CropMasterDatabase.getCropById("jute"))
    assertNotNull("Berseem fodder should exist", CropMasterDatabase.getCropById("berseem"))
    assertNotNull("Hybrid Napier should exist", CropMasterDatabase.getCropById("napier_grass"))

    // 5. Verify Field Crop Treatments in VerifiedTreatmentDatabase
    val treatments = com.example.data.datasource.VerifiedTreatmentDatabase.treatments
    val riceStemBorer = treatments.find { it.problemId == "rice_stem_borer" }
    assertNotNull("Rice stem borer verified treatment should exist", riceStemBorer)
    assertTrue("Active ingredient must be non-empty", riceStemBorer?.activeIngredient?.isNotBlank() == true)

    val wheatRust = treatments.find { it.problemId == "wheat_rust" }
    assertNotNull("Wheat rust verified treatment should exist", wheatRust)
  }

  @Test
  fun `verify crop medicine database authenticity verification and land calculator`() {
    val db = com.example.data.datasource.VerifiedTreatmentDatabase

    // 1. Verify crop specific treatment lookup
    val tomatoTreatments = db.getCropTreatments("টমেটো")
    assertTrue("Tomato must have verified treatments", tomatoTreatments.isNotEmpty())
    val lateBlight = tomatoTreatments.find { it.problemId == "tomato_late_blight" }
    assertNotNull("Tomato late blight treatment must exist", lateBlight)
    assertEquals("50% WP", lateBlight?.formulation)
    assertEquals("Acrobat 50% WP", lateBlight?.productName)
    assertEquals("BASF India", lateBlight?.manufacturer)
    assertTrue("Should have biological option", lateBlight?.biologicalOptionBn?.isNotBlank() == true)

    // 2. Verify Land Calculator for Bigha, Katha, Acre, Decimal
    // Late blight has official rate 1000g per Hectare
    val bighaDose = lateBlight?.calculateRequirementForBigha(1.0)?.first ?: 0.0
    // 1000 / 7.47 ≈ 133.87 g
    assertTrue("Bigha requirement should be ~133.87g", bighaDose in 130.0..140.0)

    val kathaDose = lateBlight?.calculateRequirementForKatha(1.0)?.first ?: 0.0
    // 1000 / 149.4 ≈ 6.69 g
    assertTrue("Katha requirement should be ~6.69g", kathaDose in 6.0..7.5)

    val acreDose = lateBlight?.calculateRequirementForAcre(1.0)?.first ?: 0.0
    // 1000 / 2.471 ≈ 404.69 g
    assertTrue("Acre requirement should be ~404.69g", acreDose in 400.0..410.0)

    val decimalDose = lateBlight?.calculateRequirementForDecimal(1.0)?.first ?: 0.0
    // 1000 / 247.1 ≈ 4.05 g
    assertTrue("Decimal requirement should be ~4.05g", decimalDose in 3.8..4.3)

    // 3. Verify Authenticity Verification (Scanner Logic)
    // Active verified product
    val acrobatVerification = db.verifyProductAuthenticity("Acrobat")
    assertTrue("Acrobat should be authentic verified", acrobatVerification.isAuthenticVerified)
    assertNotNull("Matched treatment should exist", acrobatVerification.matchedTreatment)

    // Active ingredient match
    val dimethoVerification = db.verifyProductAuthenticity("Dimethomorph")
    assertTrue("Dimethomorph should be verified", dimethoVerification.isAuthenticVerified)

    // Banned product check
    val monocrotophosVerification = db.verifyProductAuthenticity("Monocrotophos")
    assertTrue("Monocrotophos should trigger banned warning", monocrotophosVerification.isBanned)

    // Unknown product check
    val fakeVerification = db.verifyProductAuthenticity("Super Fake Miracle Tonic 999")
    assertTrue("Unknown product must not be authentic verified", !fakeVerification.isAuthenticVerified)
    assertTrue("Should show warning", fakeVerification.statusTitleBn.isNotBlank())

    // 4. Verify Admin duplication prevention
    val initialCount = db.treatments.size
    val duplicateTreatment = lateBlight!!.copy(
      problemId = "tomato_late_blight_dup"
    )
    db.addAdminVerifiedTreatment(duplicateTreatment)
    assertEquals("Duplicate treatment should not be added", initialCount, db.treatments.size)

    // New unique item addition succeeds
    val uniqueTreatment = com.example.data.model.VerifiedTreatment(
      problemId = "rice_blast_new_test",
      cropName = "Rice",
      problemNameBn = "ধানের ব্লাস্ট রোগ",
      problemNameHi = "धान का झुलसा रोग",
      problemNameEn = "Rice Blast (Pyricularia oryzae)",
      symptomsBn = "পাতায় চোখের মতো দাগ",
      symptomsHi = "पत्तियों पर आंख के आकार के धब्बे",
      symptomsEn = "Diamond shaped lesions on leaves",
      possibleCauseBn = "ছত্রাকজনিত রোগ",
      firstActionBn = "জমির জল নিষ্কাশন ও নাইট্রোজেন সার কমানো",
      biologicalOptionBn = "সিউডোমোনাস ফ্লুরোসেন্স স্প্রে",
      activeIngredient = "Azoxystrobin + Difenoconazole",
      brandExamples = "Amistar Top",
      dosePerHectareMlOrGm = 500.0,
      safetyInstructionsBn = "স্প্রে করার সময় গ্লাভস ও মাস্ক ব্যবহার করুন",
      formulation = "18.2% + 11.4% SC",
      manufacturer = "Syngenta India"
    )
    db.addAdminVerifiedTreatment(uniqueTreatment)
    assertEquals("New verified treatment should increase count by 1", initialCount + 1, db.treatments.size)
  }

  @Test
  fun `verify HashRouter navigation and GeneralAiService Google Search fallback URL`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Verify HashRouter
    val router = com.example.ui.navigation.HashRouter(com.example.ui.navigation.HashRoutes.HOME)
    assertEquals("#home", router.currentRoute)
    assertEquals(false, router.canGoBack())

    router.navigate(com.example.ui.navigation.HashRoutes.AI_ASSISTANT)
    assertEquals("#ai-assistant", router.currentRoute)
    assertEquals(true, router.canGoBack())

    router.navigate(com.example.ui.navigation.HashRoutes.MARKETPLACE)
    assertEquals("#marketplace", router.currentRoute)

    router.navigate(com.example.ui.navigation.HashRoutes.RESOURCE_LIBRARY)
    assertEquals("#resource-library", router.currentRoute)

    assertTrue(router.goBack())
    assertEquals("#marketplace", router.currentRoute)

    assertTrue(router.goBack())
    assertEquals("#ai-assistant", router.currentRoute)

    assertTrue(router.goBack())
    assertEquals("#home", router.currentRoute)
    assertEquals(false, router.canGoBack())

    // 2. Verify Google Search fallback URL generation & safety
    val aiService = com.example.data.remote.GeneralAiService(context)
    val query = "টমেটো গাছের পাতার দাগ রোগ"
    val searchUrl = aiService.buildGoogleSearchUrl(query)
    assertTrue("Search URL should start with google.com/search?q=", searchUrl.startsWith("https://www.google.com/search?q="))
    assertTrue("Search URL should be properly encoded", !searchUrl.contains(" "))

    assertTrue("Search detection for live queries should trigger", aiService.shouldSuggestGoogleSearch("আজকের বাজার দর কত"))
    assertEquals(false, aiService.shouldSuggestGoogleSearch("সৌরজগত সম্পর্কে বলো"))

    // 3. Verify Room GeneralAiDao isolation by userId
    val roomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = roomDb.generalAiDao()
    val session1 = com.example.data.local.entity.GeneralAiSessionEntity(
      id = "sess_01",
      userId = "user_suman",
      title = "AI Chat 1"
    )
    val session2 = com.example.data.local.entity.GeneralAiSessionEntity(
      id = "sess_02",
      userId = "user_other",
      title = "Private Chat"
    )
    dao.insertSession(session1)
    dao.insertSession(session2)

    val msgUser1 = com.example.data.local.entity.GeneralAiMessageEntity(
      id = "msg_01",
      sessionId = "sess_01",
      userId = "user_suman",
      sender = "user",
      text = "What is Kotlin coroutine?",
      searchQuery = "What is Kotlin coroutine?"
    )
    val msgOther = com.example.data.local.entity.GeneralAiMessageEntity(
      id = "msg_02",
      sessionId = "sess_02",
      userId = "user_other",
      sender = "user",
      text = "Secret notes",
      searchQuery = "Secret notes"
    )
    dao.insertMessage(msgUser1)
    dao.insertMessage(msgOther)

    // Ensure user isolation: user_suman can only see their own messages
    val sumanMessages = dao.getMessagesList("sess_01", "user_suman")
    assertEquals(1, sumanMessages.size)
    assertEquals("What is Kotlin coroutine?", sumanMessages[0].text)

    val stolenAttempt = dao.getMessagesList("sess_02", "user_suman")
    assertEquals(0, stolenAttempt.size)

    roomDb.close()
  }

  @Test
  fun `verify persistent user session survives restart and prevents repeated login`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Clear any previous test session
    com.example.data.local.session.UserSessionManager.clearSession(context)
    assertEquals(false, com.example.data.local.session.UserSessionManager.isLoggedIn(context))

    // Farmer signs in
    val session = com.example.data.local.session.UserSession(
      userId = "farmer_suman_sarkar_77",
      userName = "সুমন সরকার (Suman Sarkar)",
      email = "suman.farmer@krishaksathi.in",
      phone = "9876543210",
      role = com.example.data.local.session.UserSession.ROLE_FARMER,
      authProvider = com.example.data.local.session.UserSession.PROVIDER_GOOGLE
    )
    com.example.data.local.session.UserSessionManager.saveSession(context, session)

    // Immediately logged in
    assertTrue(com.example.data.local.session.UserSessionManager.isLoggedIn(context))
    val restored = com.example.data.local.session.UserSessionManager.getSession(context)
    assertNotNull(restored)
    assertEquals("farmer_suman_sarkar_77", restored?.userId)
    assertEquals("সুমন সরকার (Suman Sarkar)", restored?.userName)
    assertEquals(com.example.data.local.session.UserSession.PROVIDER_GOOGLE, restored?.authProvider)

    // Simulate process cold launch by re-reading from SharedPreferences
    val coldStartRestored = com.example.data.local.session.UserSessionManager.init(context)
    assertNotNull("Session should persist on cold start", coldStartRestored)
    assertEquals("farmer_suman_sarkar_77", coldStartRestored?.userId)

    // Explicit sign out clears the session
    com.example.data.local.session.UserSessionManager.clearSession(context)
    assertEquals(false, com.example.data.local.session.UserSessionManager.isLoggedIn(context))
  }

  @Test
  fun `verify agriculture expert consultation system 15 categories double booking prevention and chat`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = db.expertConsultationDao()

    // 1. Verify 15 Specialist Categories
    val categories = com.example.data.local.entity.ExpertCategories.ALL_CATEGORIES
    assertEquals(15, categories.size)
    val plantDisease = categories.find { it.key == com.example.data.local.entity.ExpertCategories.PLANT_DISEASE }
    assertNotNull(plantDisease)
    assertEquals("উদ্ভিদ রোগ বিশেষজ্ঞ", plantDisease?.bengaliName)

    val potatoSpec = categories.find { it.key == com.example.data.local.entity.ExpertCategories.POTATO_SPECIALIST }
    assertNotNull(potatoSpec)
    assertEquals("আলু চাষ বিশেষজ্ঞ", potatoSpec?.bengaliName)

    // 2. Insert experts: one verified and one pending
    val expert1 = com.example.data.local.entity.ExpertEntity(
      id = "exp_test_01",
      name = "ড. অরুণাভ রায়",
      designation = "সিনিয়র উদ্ভিদ রোগতত্ত্ববিদ",
      qualification = "Ph.D. Plant Pathology (BCKV)",
      category = com.example.data.local.entity.ExpertCategories.PLANT_DISEASE,
      yearsExperience = 16,
      registrationId = "WB-AGRI-EXP-TEST-01",
      institution = "BCKV Mohanpur",
      languages = "বাংলা, English",
      districtsServed = "জলপাইগুড়ি",
      availabilityStatus = "ONLINE_NOW",
      consultationFee = "বিনামূল্যে (Free)",
      isVerified = true,
      rating = 4.9,
      reviewCount = 50
    )
    val expert2Pending = com.example.data.local.entity.ExpertEntity(
      id = "exp_test_02",
      name = "অনিল শর্মা",
      designation = "সহকারী গবেষক",
      qualification = "B.Sc. Agri",
      category = com.example.data.local.entity.ExpertCategories.PLANT_DISEASE,
      yearsExperience = 2,
      registrationId = "WB-AGRI-PENDING-02",
      institution = "Private Farm",
      languages = "বাংলা",
      districtsServed = "মালদা",
      availabilityStatus = "AVAILABLE_TODAY",
      consultationFee = "বিনামূল্যে",
      isVerified = false
    )
    dao.insertExpert(expert1)
    dao.insertExpert(expert2Pending)

    // Verified query only retrieves verified experts
    val verifiedList = dao.getVerifiedExperts().first()
    assertEquals(1, verifiedList.size)
    assertEquals("exp_test_01", verifiedList[0].id)

    // 3. Double-Booking Prevention Test
    val appt1 = com.example.data.local.entity.ConsultationAppointmentEntity(
      id = "appt_01",
      farmerId = "farmer_01",
      farmerName = "সুমন সরকার",
      expertId = "exp_test_01",
      expertName = "ড. অরুণাভ রায়",
      expertCategory = com.example.data.local.entity.ExpertCategories.PLANT_DISEASE,
      cropName = "আলু",
      problemDescription = "নাবী ধসা রোগ",
      consultationMethod = "VIDEO_CALL",
      scheduledDate = "2026-11-06",
      scheduledTimeSlot = "10:00 AM - 10:30 AM",
      status = "CONFIRMED"
    )
    dao.insertAppointment(appt1)

    // Check conflict for the exact same expert, date, and slot
    val conflict = dao.checkDoubleBooking("exp_test_01", "2026-11-06", "10:00 AM - 10:30 AM")
    assertNotNull("Double booking should be detected for identical slot", conflict)

    // Another slot on the same day should NOT conflict
    val noConflict = dao.checkDoubleBooking("exp_test_01", "2026-11-06", "11:00 AM - 11:30 AM")
    assertEquals(null, noConflict)

    // 4. Chat messages isolation & ordering
    val msg1 = com.example.data.local.entity.ConsultationMessageEntity(
      id = "msg_c_01",
      expertId = "exp_test_01",
      farmerId = "farmer_01",
      senderRole = "FARMER",
      senderName = "সুমন সরকার",
      messageText = "আলুর পাতায় কালো ছোপ পড়েছে, কী করব?",
      timestamp = 1000L
    )
    val msg2 = com.example.data.local.entity.ConsultationMessageEntity(
      id = "msg_c_02",
      expertId = "exp_test_01",
      farmerId = "farmer_01",
      senderRole = "EXPERT",
      senderName = "ড. অরুণাভ রায়",
      messageText = "এটি নাবী ধসার লক্ষণ। দ্রুত ম্যানকোজেব স্প্রে করুন।",
      timestamp = 2000L
    )
    dao.insertMessage(msg1)
    dao.insertMessage(msg2)

    val chatHistory = dao.getMessages("exp_test_01", "farmer_01").first()
    assertEquals(2, chatHistory.size)
    assertEquals("FARMER", chatHistory[0].senderRole)
    assertEquals("EXPERT", chatHistory[1].senderRole)

    // 5. Admin verification test
    dao.updateVerificationStatus("exp_test_02", true)
    val updatedPending = dao.getExpertById("exp_test_02")
    assertTrue(updatedPending?.isVerified == true)

    db.close()
  }

  @Test
  fun `verify government registration, honest approval status, release info, and footer metadata`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Initial Default State
    val config = com.example.data.local.config.AppConfigManager.init(context)
    assertEquals("KRISHAK SATHI", config.appName)
    assertTrue("Tagline should contain কৃষকের পাশে", config.appSubtitleBn.contains("কৃষকের পাশে"))
    assertTrue("Description should mention digital agriculture", config.appDescription.contains("digital agriculture"))

    // Release date and last updated date must be separate
    assertEquals("January 15, 2026", config.releaseDate)
    assertEquals("1.0.0", config.currentVersion)
    assertEquals("November 3, 2026", config.lastUpdatedDate)

    // Government Approval must NOT be claimed without verification (honest disclaimer)
    assertEquals(false, config.isGovtApproved)
    assertTrue(
      "Unverified status should clearly state verification requirement",
      config.govtRegistrationStatusText.contains("Government registration or approval details will be published after official verification")
    )
    assertTrue("Certificate number should be empty when unverified", config.govtCertificateNo.isEmpty())

    // Twitter URL should be empty by default so unconfigured icon is hidden
    assertTrue("Unconfigured Twitter URL should be empty", config.twitterUrl.isEmpty())

    // 2. Admin Updates Configuration
    val updatedConfig = config.copy(
      releaseDate = "February 1, 2026",
      currentVersion = "1.1.0",
      isGovtApproved = true,
      govtCertificateNo = "WB-AGRI-GOVT-2026-9921",
      govtIssuingAuthority = "পশ্চিমবঙ্গ কৃষি বিভাগ",
      govtApprovalDate = "2026-10-20",
      govtReferenceNo = "REF-AGRI-WB-8810",
      twitterUrl = "https://x.com/krishaksathi",
      supportPhone = "1800-180-1551"
    )

    com.example.data.local.config.AppConfigManager.updateConfig(context, updatedConfig)

    // 3. Verify Live Persistence
    val current = com.example.data.local.config.AppConfigManager.getConfig(context)
    assertEquals("February 1, 2026", current.releaseDate)
    assertEquals("1.1.0", current.currentVersion)
    assertTrue("Government approval flag should now be true", current.isGovtApproved)
    assertEquals("WB-AGRI-GOVT-2026-9921", current.govtCertificateNo)
    assertEquals("https://x.com/krishaksathi", current.twitterUrl)
    assertEquals("1800-180-1551", current.supportPhone)

    // Reset back for clean state
    com.example.data.local.config.AppConfigManager.updateConfig(context, config)
  }

  @Test
  fun `verify leading farmers seed data and verified public recognitions`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    val dao = db.leadingFarmerDao()
    val repo = com.example.data.repository.LeadingFarmerRepository(dao)

    repo.seedInitialFarmersIfNeeded()

    val farmers = repo.approvedFarmers.first()
    assertTrue("Should seed at least 10 recognized Indian farmers", farmers.size >= 10)

    val palekar = farmers.find { it.id == "farmer_subhash_palekar" }
    assertNotNull("Subhash Palekar should exist", palekar)
    assertTrue("Subhash Palekar should have Padma Shri (2016)", palekar!!.keyAwardEn.contains("Padma Shri (2016)"))
    assertTrue("Should reference official source", palekar.sourceReferences.contains("Padma Awards Directory"))

    val kamala = farmers.find { it.id == "farmer_kamala_pujari" }
    assertNotNull("Kamala Pujari should exist", kamala)
    assertEquals(false, kamala!!.hasPublicContact)
    assertTrue("Should protect unverified personal contacts", kamala.directContactNotice.contains("Direct contact information is not publicly available"))

    db.close()
  }

  @Test
  fun `verify leading farmers search and category filters`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    val dao = db.leadingFarmerDao()
    val repo = com.example.data.repository.LeadingFarmerRepository(dao)
    repo.seedInitialFarmersIfNeeded()

    // Filter by Natural Farming
    val naturalFarmers = repo.filterFarmers(categoryTag = com.example.data.local.entity.FarmerCategoryTags.NATURAL_FARMING).first()
    assertTrue(naturalFarmers.any { it.fullNameEn == "Subhash Palekar" })

    // Filter by State
    val rajasthanFarmers = repo.filterFarmers(selectedState = "Rajasthan").first()
    assertEquals(1, rajasthanFarmers.size)
    assertEquals("Hukumchand Patidar", rajasthanFarmers.first().fullNameEn)

    // Search by crop "Carrot"
    val carrotFarmers = repo.filterFarmers(searchQuery = "Carrot").first()
    assertTrue(carrotFarmers.any { it.fullNameEn.contains("Vallabhbhai") })

    // Search by technique "SRI"
    val sriFarmers = repo.filterFarmers(searchQuery = "SRI").first()
    assertTrue(sriFarmers.any { it.fullNameEn.contains("Subhash Chandra Sharma") })

    db.close()
  }

  @Test
  fun `verify contact request submission and admin audit logging`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    val dao = db.leadingFarmerDao()
    val repo = com.example.data.repository.LeadingFarmerRepository(dao)
    repo.seedInitialFarmersIfNeeded()

    // 1. Submit contact request
    val result = repo.submitContactRequest(
      farmerId = "farmer_kanwal_singh_chauhan",
      farmerName = "Kanwal Singh Chauhan",
      requesterName = "Ramesh Mondal",
      requesterPhone = "9876543210",
      requesterDistrict = "Nadia",
      requesterCrop = "Sweet Corn",
      purposeCategory = "Training & Methods",
      message = "I want to learn village-level baby corn canning."
    )
    assertTrue("Contact request should submit successfully", result.isSuccess)
    val requestId = result.getOrThrow()

    // Verify request stored
    val requests = repo.allContactRequests.first()
    assertEquals(1, requests.size)
    assertEquals("Ramesh Mondal", requests.first().requesterName)
    assertEquals("Submitted", requests.first().status)

    // Update status
    repo.updateRequestStatus(requestId, "Under Review")
    val updatedRequests = repo.allContactRequests.first()
    assertEquals("Under Review", updatedRequests.first().status)

    // 2. Admin toggle featured & audit logging
    repo.toggleFeatured("farmer_sethpal_singh", false, performedBy = "Admin Test")
    val sethpal = repo.getFarmerByIdSync("farmer_sethpal_singh")
    assertNotNull(sethpal)
    assertEquals(false, sethpal!!.isFeatured)

    val logs = repo.allAuditLogs.first()
    assertTrue("Should record audit logs", logs.size >= 2)
    assertTrue("Audit log should mention contact request or featured toggle", logs.any { it.action == "FEATURED_TOGGLED" || it.action == "CONTACT_REQUEST_SUBMITTED" })

    db.close()
  }

  @Test
  fun `verify leaf check history persistence and bundled Plus Jakarta Sans font`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.local.LeafCheckHistoryStore.clearRecords(context)
    assertEquals(0, com.example.data.local.LeafCheckHistoryStore.getRecords(context).size)

    val saved = com.example.data.local.LeafCheckHistoryStore.addRecord(
      context,
      com.example.data.local.LeafCheckRecord(
        id = "chk_test_1",
        cropId = "tomato",
        cropName = "🍅 Tomato",
        landArea = "1 Bigha",
        diagnosisText = "Early Blight leaf spots detected. Apply Neem Oil 10,000 ppm.",
        timestampMs = 1763200000000L
      )
    )
    assertEquals(1, saved.size)
    assertEquals("tomato", com.example.data.local.LeafCheckHistoryStore.getRecords(context).first().cropId)

    com.example.data.local.LeafCheckHistoryStore.clearRecords(context)
    assertEquals(0, com.example.data.local.LeafCheckHistoryStore.getRecords(context).size)
    assertTrue("Bundled font resource id should be non-zero", R.font.plus_jakarta_sans != 0)
  }

  @Test
  fun `verify expanded 12-group Master Library, photo registry accuracy, regional filters, and runtime custom crop persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.repository.CustomCropRepository.clearAll(context)

    val allCrops = CropMasterDatabase.crops
    assertTrue("Master library should contain >= 150 entries", allCrops.size >= 150)

    // 1. Cucumber / shosha deduplication
    val cucumberMatches = allCrops.filter { it.cropId == "cucumber" || it.cropId == "shosha" }
    assertEquals("Must have exactly 1 cucumber record (no shosha duplicate)", 1, cucumberMatches.size)
    assertEquals("cucumber", CropMasterDatabase.getCropOrDefault("shosha", null).cropId)

    // 2. Strict cropId photo registry (16 bundled photos; unphotographed crops must return null, never wrong photos)
    val cropsWithBundledPhotos = allCrops.filter { it.getResolvedImageResId() != null }
    assertEquals("Exactly 16 crops have dedicated bundled photographs", 16, cropsWithBundledPhotos.size)

    listOf("rose", "marigold", "turmeric", "mustard", "rice_paddy", "tomato", "tea").forEach { id ->
      val c = CropMasterDatabase.getCropById(id)
      assertNotNull("Crop $id must exist", c)
      assertNotNull("Crop $id must have dedicated photo drawable", c?.getResolvedImageResId())
    }
    listOf("snake_plant", "kathal", "dhaincha").forEach { id ->
      val c = CropMasterDatabase.getCropById(id)
      assertNotNull("Crop $id must exist", c)
      assertEquals("Unphotographed crop $id must return null photo so Botanical Illustration renders", null, c?.getResolvedImageResId())
    }

    // 3. Plant-type classification & region/season filters
    val rose = CropMasterDatabase.getCropById("rose")!!
    assertTrue(com.example.data.datasource.CropDomainAdvisor.isFloriculture(rose))
    assertTrue(com.example.data.model.CropRegion.SILIGURI_NORTH_BENGAL in rose.getResolvedRegions())

    val snakePlant = CropMasterDatabase.getCropById("snake_plant")!!
    assertTrue(com.example.data.datasource.CropDomainAdvisor.isIndoorOrNursery(snakePlant))
    assertTrue(com.example.data.model.CropSeason.YEAR_ROUND in snakePlant.getResolvedSeasons())

    val dhaincha = CropMasterDatabase.getCropById("dhaincha")!!
    assertTrue(com.example.data.datasource.CropDomainAdvisor.isAgroforestryOrGreenManure(dhaincha))

    val northBengalCount = allCrops.count {
      com.example.data.model.CropRegion.SILIGURI_NORTH_BENGAL in it.getResolvedRegions()
    }
    assertTrue("Siliguri & North Bengal filter should include >= 100 crops", northBengalCount >= 100)

    // 4. Runtime Admin CustomCropRepository persistence & reload
    val baseCount = allCrops.size
    val customCrop = com.example.data.model.MasterCrop(
      cropId = "blue_pea_vine_test",
      nameBn = "অপরাজিতা (Blue Pea)",
      nameHi = "अपराजिता (Blue Pea)",
      nameEn = "Butterfly Pea",
      scientificName = "Clitoria ternatea",
      category = CropCategory.FLOWERS,
      subCategory = "Vine Flower",
      seasons = listOf(com.example.data.model.CropSeason.YEAR_ROUND),
      regions = listOf(com.example.data.model.CropRegion.SILIGURI_NORTH_BENGAL, com.example.data.model.CropRegion.PAN_INDIA),
      bestSeasonBn = "সারা বছর",
      soilBn = "দোআঁশ মাটি",
      seedRatePerBighaBn = "২৫০ গ্রাম/বিঘা",
      spacingBn = "৬০ সেমি",
      harvestDays = 55,
      expectedYieldBn = "সারা বছর নীল ফুল",
      summaryBn = "ভেষজ ও পূজার নীল ফুল।",
      summaryHi = "औषधीय व पूजा का नीला फूल।",
      summaryEn = "Medicinal and ornamental blue flowering vine.",
      isCustomAdded = true
    )
    val customLocale = com.example.data.datasource.CropLocaleData(
      seasonHi = "साल भर",
      seasonEn = "Year-Round",
      soilHi = "दोमट मिट्टी",
      soilEn = "Well-drained loamy soil",
      seedRateHi = "250 ग्राम/बीघा",
      seedRateEn = "250 g/bigha",
      spacingHi = "60 सेमी",
      spacingEn = "60 cm",
      yieldHi = "लगातार फूल",
      yieldEn = "Continuous blooms"
    )
    com.example.data.repository.CustomCropRepository.saveCustomCrop(
      context,
      com.example.data.repository.CustomCropRepository.CustomCropRecord(customCrop, customLocale)
    )
    assertEquals(baseCount + 1, CropMasterDatabase.crops.size)
    assertNotNull(CropMasterDatabase.getCropById("blue_pea_vine_test"))

    // Reload from SharedPreferences
    com.example.data.repository.CustomCropRepository.loadAndSyncAll(context)
    val reloaded = CropMasterDatabase.getCropById("blue_pea_vine_test")
    assertNotNull("Custom crop must reload from SharedPreferences", reloaded)
    assertEquals("Well-drained loamy soil", reloaded?.getSoil(AppLanguage.ENGLISH))
    assertEquals("दोमट मिट्टी", reloaded?.getSoil(AppLanguage.HINDI))

    // Cleanup
    com.example.data.repository.CustomCropRepository.deleteCustomCrop(context, "blue_pea_vine_test")
    assertEquals(baseCount, CropMasterDatabase.crops.size)
    com.example.data.repository.CustomCropRepository.clearAll(context)
  }

  @Test
  fun `verify Royal Green Premium Theme palette, Home popular crops, and trilingual navigation keys`() {
    // 1. Exact Royal Green hex tokens
    assertEquals(androidx.compose.ui.graphics.Color(0xFF062D20), com.example.ui.theme.RoyalGreenPrimaryBg)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF0B3B2B), com.example.ui.theme.RoyalGreenSecondaryBg)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF104A35), com.example.ui.theme.RoyalGreenCardBg)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF087A50), com.example.ui.theme.RoyalGreenEmerald)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF32A66A), com.example.ui.theme.RoyalGreenHighlight)
    assertEquals(androidx.compose.ui.graphics.Color(0xFFD6B45C), com.example.ui.theme.RoyalGold)
    assertEquals(androidx.compose.ui.graphics.Color(0xFFF0D98B), com.example.ui.theme.RoyalSoftGold)
    assertEquals(androidx.compose.ui.graphics.Color(0xFFFFFFFF), com.example.ui.theme.RoyalTextPrimary)
    assertEquals(androidx.compose.ui.graphics.Color(0xFFC4D5CA), com.example.ui.theme.RoyalTextSecondary)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF286348), com.example.ui.theme.RoyalBorder)
    assertEquals(androidx.compose.ui.graphics.Color(0xFF123F30), com.example.ui.theme.RoyalInputBg)

    // 2. ColorScheme wiring
    val scheme = com.example.ui.theme.RoyalGreenColorScheme
    assertEquals(com.example.ui.theme.RoyalGreenPrimaryBg, scheme.background)
    assertEquals(com.example.ui.theme.RoyalGreenCardBg, scheme.surface)
    assertEquals(com.example.ui.theme.RoyalGreenSecondaryBg, scheme.surfaceVariant)
    assertEquals(com.example.ui.theme.RoyalGreenHighlight, scheme.primary)
    assertEquals(com.example.ui.theme.RoyalGold, scheme.tertiary)
    assertEquals(com.example.ui.theme.RoyalBorder, scheme.outline)
    assertEquals(com.example.ui.theme.RoyalTextPrimary, scheme.onBackground)
    assertEquals(com.example.ui.theme.RoyalTextSecondary, scheme.onSurfaceVariant)

    // 3. Home Popular Crops strip items all resolve cleanly with dedicated bundled photos
    listOf("tomato", "potato", "rice_paddy", "tea", "rose", "mango").forEach { id ->
      val crop = CropMasterDatabase.getCropById(id)
      assertNotNull("Popular crop $id must exist", crop)
      assertNotNull("Popular crop $id must have bundled photo", crop?.getResolvedImageResId())
    }

    // 4. Bottom Navigation & Royal Green Dashboard keys exist in all 3 languages
    listOf(
      "nav_explore", "nav_market", "home_quick_access_title",
      "card_govt_title", "home_popular_crops_title",
      "home_seasonal_guide_title", "home_updates_title", "home_tips_title"
    ).forEach { key ->
      AppLanguage.values().forEach { lang ->
        val value = L10n.t(key, lang)
        assertTrue("Translation for '$key' in $lang must be non-blank", value.isNotBlank())
      }
    }
  }
}


