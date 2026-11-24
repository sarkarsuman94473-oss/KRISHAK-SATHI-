package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AiIntelligenceDao
import com.example.data.local.dao.CropDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ExpertConsultationDao
import com.example.data.local.dao.GeneralAiDao
import com.example.data.local.dao.GovernmentSchemeDao
import com.example.data.local.dao.LeadingFarmerDao
import com.example.data.local.dao.MachineryDao
import com.example.data.local.dao.NurseryDao
import com.example.data.local.dao.PestDiseaseDao
import com.example.data.local.dao.PlantCatalogDao
import com.example.data.local.dao.PlantKingdomDao
import com.example.data.local.dao.SeedListingDao
import com.example.data.local.dao.SeoDiscoveryDao
import com.example.data.local.dao.TreatmentDao
import com.example.data.local.dao.TrustSafetyDao
import com.example.data.model.PromoBannerDao
import com.example.data.model.PromoBannerEntity
import com.example.data.local.entity.AiFeedbackEntity
import com.example.data.local.entity.AiKnowledgeDocEntity
import com.example.data.local.entity.AiTelemetryLogEntity
import com.example.data.local.entity.ConsultationAppointmentEntity
import com.example.data.local.entity.ConsultationMessageEntity
import com.example.data.local.entity.CropEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ExpertAuditLogEntity
import com.example.data.local.entity.ExpertEntity
import com.example.data.local.entity.ExpertReviewEntity
import com.example.data.local.entity.FarmerAuditLogEntity
import com.example.data.local.entity.FarmerContactRequestEntity
import com.example.data.local.entity.FraudReportEntity
import com.example.data.local.entity.GeneralAiMessageEntity
import com.example.data.local.entity.GeneralAiSessionEntity
import com.example.data.local.entity.GovernmentSchemeBookmarkEntity
import com.example.data.local.entity.GovernmentSchemeEntity
import com.example.data.local.entity.GrievanceTicketEntity
import com.example.data.local.entity.InquiryActivityLogEntity
import com.example.data.local.entity.LeadingFarmerEntity
import com.example.data.local.entity.MachineryDealerEntity
import com.example.data.local.entity.MachineryDealerOfferEntity
import com.example.data.local.entity.MachineryFavouriteEntity
import com.example.data.local.entity.MachineryInquiryEntity
import com.example.data.local.entity.MachineryManufacturerEntity
import com.example.data.local.entity.MachineryProductEntity
import com.example.data.local.entity.MachineryRentalEntity
import com.example.data.local.entity.MachineryResearchQueueEntity
import com.example.data.local.entity.MachineryReviewEntity
import com.example.data.local.entity.MachineryServiceRequestEntity
import com.example.data.local.entity.NurseryEntity
import com.example.data.local.entity.PestDiseaseEntity
import com.example.data.local.entity.PlantCatalogEntity
import com.example.data.local.entity.PlantKingdomEntity
import com.example.data.local.entity.SeedListingEntity
import com.example.data.local.entity.SellerSuspensionEntity
import com.example.data.local.entity.SeoArticleEntity
import com.example.data.local.entity.SeoConfigEntity
import com.example.data.local.entity.SeoReferralLogEntity
import com.example.data.local.entity.TreatmentEntity
import com.example.data.local.entity.TrustAuditLogEntity
import com.example.data.local.entity.WbNurseryDirectoryEntity

@Database(
    entities = [
        ExpenseEntity::class,
        CropEntity::class,
        PestDiseaseEntity::class,
        TreatmentEntity::class,
        NurseryEntity::class,
        PlantCatalogEntity::class,
        SeedListingEntity::class,
        GeneralAiSessionEntity::class,
        GeneralAiMessageEntity::class,
        ExpertEntity::class,
        ConsultationAppointmentEntity::class,
        ConsultationMessageEntity::class,
        ExpertReviewEntity::class,
        ExpertAuditLogEntity::class,
        LeadingFarmerEntity::class,
        FarmerContactRequestEntity::class,
        FarmerAuditLogEntity::class,
        GovernmentSchemeEntity::class,
        GovernmentSchemeBookmarkEntity::class,
        AiKnowledgeDocEntity::class,
        AiTelemetryLogEntity::class,
        AiFeedbackEntity::class,
        SeoArticleEntity::class,
        SeoConfigEntity::class,
        SeoReferralLogEntity::class,
        PlantKingdomEntity::class,
        WbNurseryDirectoryEntity::class,
        PromoBannerEntity::class,
        MachineryProductEntity::class,
        MachineryDealerEntity::class,
        MachineryFavouriteEntity::class,
        MachineryInquiryEntity::class,
        MachineryRentalEntity::class,
        MachineryServiceRequestEntity::class,
        MachineryManufacturerEntity::class,
        MachineryDealerOfferEntity::class,
        MachineryReviewEntity::class,
        MachineryResearchQueueEntity::class,
        FraudReportEntity::class,
        GrievanceTicketEntity::class,
        SellerSuspensionEntity::class,
        TrustAuditLogEntity::class,
        InquiryActivityLogEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun cropDao(): CropDao
    abstract fun pestDiseaseDao(): PestDiseaseDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun nurseryDao(): NurseryDao
    abstract fun plantCatalogDao(): PlantCatalogDao
    abstract fun seedListingDao(): SeedListingDao
    abstract fun generalAiDao(): GeneralAiDao
    abstract fun expertConsultationDao(): ExpertConsultationDao
    abstract fun leadingFarmerDao(): LeadingFarmerDao
    abstract fun governmentSchemeDao(): GovernmentSchemeDao
    abstract fun aiIntelligenceDao(): AiIntelligenceDao
    abstract fun seoDiscoveryDao(): SeoDiscoveryDao
    abstract fun plantKingdomDao(): PlantKingdomDao
    abstract fun promoBannerDao(): PromoBannerDao
    abstract fun machineryDao(): MachineryDao
    abstract fun trustSafetyDao(): TrustSafetyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE general_ai_sessions ADD COLUMN assistantType TEXT NOT NULL DEFAULT 'GENERAL_AI'")
                db.execSQL("ALTER TABLE general_ai_sessions ADD COLUMN cropId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE general_ai_sessions ADD COLUMN providerUsed TEXT NOT NULL DEFAULT 'GEMINI'")
                db.execSQL("ALTER TABLE general_ai_sessions ADD COLUMN modelUsed TEXT NOT NULL DEFAULT 'gemini-3.8-flash'")

                db.execSQL("ALTER TABLE general_ai_messages ADD COLUMN providerUsed TEXT NOT NULL DEFAULT 'GEMINI'")
                db.execSQL("ALTER TABLE general_ai_messages ADD COLUMN modelUsed TEXT NOT NULL DEFAULT 'gemini-3.8-flash'")
                db.execSQL("ALTER TABLE general_ai_messages ADD COLUMN fallbackUsed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE general_ai_messages ADD COLUMN isError INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE general_ai_messages ADD COLUMN cropId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE general_ai_messages ADD COLUMN feedbackVote INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE general_ai_messages ADD COLUMN feedbackNote TEXT NOT NULL DEFAULT ''")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ai_knowledge_docs (
                        id TEXT NOT NULL PRIMARY KEY,
                        titleEn TEXT NOT NULL,
                        titleBn TEXT NOT NULL,
                        titleHi TEXT NOT NULL,
                        institution TEXT NOT NULL,
                        sourceUrl TEXT NOT NULL,
                        publicationDate TEXT NOT NULL,
                        verifiedDate TEXT NOT NULL,
                        category TEXT NOT NULL,
                        applicableCropIdsCsv TEXT NOT NULL,
                        regionTag TEXT NOT NULL,
                        keywordsCsv TEXT NOT NULL,
                        summaryEn TEXT NOT NULL,
                        summaryBn TEXT NOT NULL,
                        summaryHi TEXT NOT NULL,
                        isCustomAdminAdded INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_knowledge_docs_category_isActive ON ai_knowledge_docs(category, isActive)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_knowledge_docs_updatedAtMillis ON ai_knowledge_docs(updatedAtMillis)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ai_telemetry_logs (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        assistantMode TEXT NOT NULL,
                        cropId TEXT NOT NULL,
                        providerUsed TEXT NOT NULL,
                        modelUsed TEXT NOT NULL,
                        fallbackUsed INTEGER NOT NULL,
                        success INTEGER NOT NULL,
                        errorCode TEXT NOT NULL,
                        errorSummary TEXT NOT NULL,
                        latencyMs INTEGER NOT NULL,
                        estimatedTokens INTEGER NOT NULL,
                        estimatedCostUsd REAL NOT NULL,
                        timestampMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_telemetry_logs_userId_timestampMillis ON ai_telemetry_logs(userId, timestampMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_telemetry_logs_providerUsed_timestampMillis ON ai_telemetry_logs(providerUsed, timestampMillis)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ai_feedback (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        assistantMode TEXT NOT NULL,
                        cropId TEXT NOT NULL,
                        providerUsed TEXT NOT NULL,
                        helpful INTEGER NOT NULL,
                        messagePreview TEXT NOT NULL,
                        farmerNote TEXT NOT NULL,
                        reviewStatus TEXT NOT NULL,
                        timestampMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_feedback_timestampMillis ON ai_feedback(timestampMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_feedback_reviewStatus ON ai_feedback(reviewStatus)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS seo_articles (
                        id TEXT NOT NULL PRIMARY KEY,
                        slug TEXT NOT NULL,
                        category TEXT NOT NULL,
                        cropId TEXT NOT NULL,
                        status TEXT NOT NULL,
                        titleEn TEXT NOT NULL,
                        titleBn TEXT NOT NULL,
                        titleHi TEXT NOT NULL,
                        metaDescriptionEn TEXT NOT NULL,
                        metaDescriptionBn TEXT NOT NULL,
                        metaDescriptionHi TEXT NOT NULL,
                        summaryEn TEXT NOT NULL,
                        summaryBn TEXT NOT NULL,
                        summaryHi TEXT NOT NULL,
                        bodyEn TEXT NOT NULL,
                        bodyBn TEXT NOT NULL,
                        bodyHi TEXT NOT NULL,
                        faqsSerialized TEXT NOT NULL,
                        sourceName TEXT NOT NULL,
                        sourceUrl TEXT NOT NULL,
                        canonicalUrl TEXT NOT NULL,
                        lastUpdatedDate TEXT NOT NULL,
                        isAiAssisted INTEGER NOT NULL,
                        reviewedByAdmin TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_seo_articles_slug ON seo_articles(slug)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_seo_articles_status_category ON seo_articles(status, category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_seo_articles_updatedAtMillis ON seo_articles(updatedAtMillis)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS seo_config (
                        id TEXT NOT NULL PRIMARY KEY,
                        baseWebUrl TEXT NOT NULL,
                        googleSearchConsoleToken TEXT NOT NULL,
                        allowGooglebot INTEGER NOT NULL,
                        allowOaiSearchBot INTEGER NOT NULL,
                        allowGptBot INTEGER NOT NULL,
                        playStoreStatus TEXT NOT NULL,
                        updatedByAdmin TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS seo_referral_logs (
                        id TEXT NOT NULL PRIMARY KEY,
                        sourceBucket TEXT NOT NULL,
                        targetSlug TEXT NOT NULL,
                        languageCode TEXT NOT NULL,
                        timestampMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_seo_referral_logs_sourceBucket ON seo_referral_logs(sourceBucket)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_seo_referral_logs_timestampMillis ON seo_referral_logs(timestampMillis)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS plant_kingdom (
                        plantId TEXT NOT NULL PRIMARY KEY,
                        nameEn TEXT NOT NULL,
                        nameBn TEXT NOT NULL,
                        nameHi TEXT NOT NULL,
                        scientificName TEXT NOT NULL,
                        primaryGroup TEXT NOT NULL,
                        secondaryGroupsCsv TEXT NOT NULL,
                        filterTagsCsv TEXT NOT NULL,
                        seasonalTagsCsv TEXT NOT NULL,
                        descriptionEn TEXT NOT NULL,
                        descriptionBn TEXT NOT NULL,
                        descriptionHi TEXT NOT NULL,
                        visualAssetKey TEXT NOT NULL,
                        customPhotoUri TEXT NOT NULL,
                        climate TEXT NOT NULL,
                        temperatureRange TEXT NOT NULL,
                        soilRequirements TEXT NOT NULL,
                        sunlight TEXT NOT NULL,
                        wateringFrequency TEXT NOT NULL,
                        fertilizerGuidance TEXT NOT NULL,
                        propagationMethod TEXT NOT NULL,
                        floweringOrFruitingSeason TEXT NOT NULL,
                        growthSize TEXT NOT NULL,
                        indoorOutdoorSuitability TEXT NOT NULL,
                        plainsHillSuitability TEXT NOT NULL,
                        commonPestsDiseases TEXT NOT NULL,
                        careInstructionsEn TEXT NOT NULL,
                        careInstructionsBn TEXT NOT NULL,
                        careInstructionsHi TEXT NOT NULL,
                        verifiedNurseryIdsCsv TEXT NOT NULL,
                        isRare INTEGER NOT NULL,
                        isVerifiedEntry INTEGER NOT NULL,
                        sourceReference TEXT NOT NULL,
                        lastVerifiedDate TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_plant_kingdom_primaryGroup ON plant_kingdom(primaryGroup)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_plant_kingdom_nameEn ON plant_kingdom(nameEn)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_plant_kingdom_scientificName ON plant_kingdom(scientificName)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wb_nursery_directory (
                        nurseryId TEXT NOT NULL PRIMARY KEY,
                        businessName TEXT NOT NULL,
                        businessNameBn TEXT NOT NULL,
                        businessNameHi TEXT NOT NULL,
                        ownerName TEXT,
                        fullAddress TEXT,
                        districtCode TEXT NOT NULL,
                        locality TEXT,
                        state TEXT NOT NULL,
                        phone TEXT,
                        whatsapp TEXT,
                        latitude REAL,
                        longitude REAL,
                        websiteOrSocial TEXT,
                        openingHours TEXT,
                        nurseryPhotosCsv TEXT NOT NULL,
                        availablePlantCategoriesCsv TEXT NOT NULL,
                        availablePlantIdsCsv TEXT NOT NULL,
                        supportsWholesale INTEGER,
                        supportsRetail INTEGER,
                        deliveryAvailable INTEGER,
                        descriptionEn TEXT NOT NULL,
                        descriptionBn TEXT NOT NULL,
                        descriptionHi TEXT NOT NULL,
                        verificationStatus TEXT NOT NULL,
                        isVerified INTEGER NOT NULL,
                        sourceReference TEXT NOT NULL,
                        lastVerifiedDate TEXT NOT NULL,
                        linkedSellerUserId TEXT,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_wb_nursery_directory_districtCode_verificationStatus ON wb_nursery_directory(districtCode, verificationStatus)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_wb_nursery_directory_businessName ON wb_nursery_directory(businessName)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_kingdom ADD COLUMN publicationStatus TEXT NOT NULL DEFAULT 'PUBLISHED'")
                db.execSQL("ALTER TABLE wb_nursery_directory ADD COLUMN ownerConsentStatus TEXT NOT NULL DEFAULT 'NOT_CONFIRMED'")
                db.execSQL("ALTER TABLE wb_nursery_directory ADD COLUMN notes TEXT NOT NULL DEFAULT ''")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS promo_banners (
                        id TEXT NOT NULL PRIMARY KEY,
                        languageCode TEXT NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        subtitle TEXT NOT NULL,
                        badgeText TEXT NOT NULL,
                        ctaLabel TEXT NOT NULL,
                        drawableResName TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        targetCardId TEXT NOT NULL,
                        isPoster INTEGER NOT NULL,
                        hideTextOverlay INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_promo_banners_languageCode_orderIndex ON promo_banners(languageCode, orderIndex)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_products (
                        id TEXT NOT NULL PRIMARY KEY,
                        sellerUserId TEXT NOT NULL,
                        dealerId TEXT NOT NULL,
                        sellerBusinessName TEXT NOT NULL,
                        sellerLocation TEXT NOT NULL,
                        sellerLocalityCode TEXT NOT NULL,
                        sellerPhone TEXT,
                        sellerWhatsapp TEXT,
                        nameEn TEXT NOT NULL,
                        nameBn TEXT NOT NULL,
                        nameHi TEXT NOT NULL,
                        categoryCode TEXT NOT NULL,
                        subcategory TEXT NOT NULL,
                        brand TEXT NOT NULL,
                        modelNumber TEXT NOT NULL,
                        horsepowerOrCapacity TEXT NOT NULL,
                        engineOrMotorSpec TEXT NOT NULL,
                        fuelOrPowerType TEXT NOT NULL,
                        workingWidthOrCoverage TEXT NOT NULL,
                        weightKg TEXT NOT NULL,
                        dimensionsText TEXT NOT NULL,
                        suitableCrops TEXT NOT NULL,
                        suitableSoilOrTerrain TEXT NOT NULL,
                        landSizeSuitability TEXT NOT NULL,
                        condition TEXT NOT NULL,
                        forSale INTEGER NOT NULL,
                        forRent INTEGER NOT NULL,
                        salePriceInr REAL,
                        rentalRateHourlyInr REAL,
                        rentalRateDailyInr REAL,
                        rentalRateWeeklyInr REAL,
                        rentalRatePerAcreInr REAL,
                        stockStatus TEXT NOT NULL,
                        warrantyInfo TEXT NOT NULL,
                        sparePartsInfo TEXT NOT NULL,
                        installationMaintenanceNotes TEXT NOT NULL,
                        safetyUsageGuidelines TEXT NOT NULL,
                        demoVideoUrl TEXT NOT NULL,
                        photosCsv TEXT NOT NULL,
                        listingOrigin TEXT NOT NULL,
                        approvalStatus TEXT NOT NULL,
                        verificationBadgeText TEXT NOT NULL,
                        requiresDgcaCompliance INTEGER NOT NULL,
                        requiresEarthmovingSafety INTEGER NOT NULL,
                        lastUpdatedDate TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_machinery_products_categoryCode_approvalStatus ON machinery_products(categoryCode, approvalStatus)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_machinery_products_sellerUserId ON machinery_products(sellerUserId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_dealers (
                        dealerId TEXT NOT NULL PRIMARY KEY,
                        businessName TEXT NOT NULL,
                        businessNameBn TEXT NOT NULL,
                        businessNameHi TEXT NOT NULL,
                        partnerRoleCode TEXT NOT NULL,
                        localityCode TEXT NOT NULL,
                        district TEXT NOT NULL,
                        state TEXT NOT NULL,
                        fullAddress TEXT,
                        pinCode TEXT,
                        ownerName TEXT,
                        phone TEXT,
                        whatsapp TEXT,
                        latitude REAL,
                        longitude REAL,
                        brandsCarriedCsv TEXT NOT NULL,
                        supportedCategoriesCsv TEXT NOT NULL,
                        servicesOfferedCsv TEXT NOT NULL,
                        isAuthorisedDealerVerified INTEGER NOT NULL,
                        offersSale INTEGER NOT NULL,
                        offersRent INTEGER NOT NULL,
                        offersRepair INTEGER NOT NULL,
                        verificationStatus TEXT NOT NULL,
                        ownerConsentStatus TEXT NOT NULL,
                        isVerified INTEGER NOT NULL,
                        claimedByUserId TEXT,
                        claimNote TEXT,
                        sourceReference TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        lastVerifiedDate TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_machinery_dealers_localityCode_verificationStatus ON machinery_dealers(localityCode, verificationStatus)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_favourites (
                        compositeId TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        machineId TEXT NOT NULL,
                        savedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_inquiries (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        farmerName TEXT NOT NULL,
                        farmerPhone TEXT NOT NULL,
                        machineId TEXT NOT NULL,
                        machineName TEXT NOT NULL,
                        sellerUserId TEXT NOT NULL,
                        inquiryType TEXT NOT NULL,
                        message TEXT NOT NULL,
                        status TEXT NOT NULL,
                        sellerReply TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_rentals (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        farmerName TEXT NOT NULL,
                        farmerPhone TEXT NOT NULL,
                        machineId TEXT NOT NULL,
                        machineName TEXT NOT NULL,
                        sellerUserId TEXT NOT NULL,
                        locality TEXT NOT NULL,
                        rentalUnit TEXT NOT NULL,
                        durationUnits INTEGER NOT NULL,
                        withOperator INTEGER NOT NULL,
                        deliveryRequired INTEGER NOT NULL,
                        fuelPolicyNote TEXT NOT NULL,
                        securityDepositNote TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        status TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_service_requests (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        farmerName TEXT NOT NULL,
                        farmerPhone TEXT NOT NULL,
                        serviceTypeCode TEXT NOT NULL,
                        locality TEXT NOT NULL,
                        landSizeText TEXT NOT NULL,
                        cropName TEXT NOT NULL,
                        preferredDate TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        providerUserId TEXT NOT NULL,
                        status TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN purposeEn TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN purposeBn TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN purposeHi TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN suitableCropsBn TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN suitableCropsHi TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN operatingInstructionsEn TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN operatingInstructionsBn TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN operatingInstructionsHi TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN safetyGuidelinesBn TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN safetyGuidelinesHi TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN brandVerified INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN modelVerified INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN pendingAdminReview INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN adminReviewNote TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN hireServiceTypeCode TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN firebaseStoragePathsCsv TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN aliasesCsv TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN applicableCategoriesCsv TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN sourceUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN sourceType TEXT NOT NULL DEFAULT 'ENGINEERING_REFERENCE'")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN verificationDate TEXT NOT NULL DEFAULT '2026-11-21'")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN infoVerificationStatus TEXT NOT NULL DEFAULT 'VERIFIED_CLASS'")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN priceVerificationStatus TEXT NOT NULL DEFAULT 'CONFIRM_WITH_SELLER'")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN imageUsageStatus TEXT NOT NULL DEFAULT 'APPROVED_IN_APP'")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN beginnerGuideJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN compatibleWithIdsCsv TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN subsidySchemeCodesCsv TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE machinery_products ADD COLUMN operatorTrainingRequired INTEGER NOT NULL DEFAULT 0")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_manufacturers (
                        id TEXT NOT NULL PRIMARY KEY,
                        nameEn TEXT NOT NULL,
                        nameBn TEXT NOT NULL,
                        nameHi TEXT NOT NULL,
                        countryOrOrigin TEXT NOT NULL,
                        focusSummaryEn TEXT NOT NULL,
                        officialPortalUrl TEXT NOT NULL,
                        dealerLocatorUrl TEXT NOT NULL,
                        servicePortalUrl TEXT NOT NULL,
                        customerCareHelpline TEXT NOT NULL,
                        supportedCategoriesCsv TEXT NOT NULL,
                        isVerifiedOfficialLink INTEGER NOT NULL,
                        verificationStatus TEXT NOT NULL,
                        sourceUrl TEXT NOT NULL,
                        lastVerifiedDate TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_dealer_offers (
                        offerId TEXT NOT NULL PRIMARY KEY,
                        machineId TEXT NOT NULL,
                        dealerId TEXT NOT NULL,
                        dealerName TEXT NOT NULL,
                        localityCode TEXT NOT NULL,
                        modelNumber TEXT NOT NULL,
                        condition TEXT NOT NULL,
                        offersSale INTEGER NOT NULL,
                        offersRent INTEGER NOT NULL,
                        mrpInr REAL,
                        dealerPriceInr REAL,
                        indicativePriceInr REAL,
                        rentalHourlyInr REAL,
                        rentalDailyInr REAL,
                        rentalPerAcreInr REAL,
                        operatorChargeNote TEXT NOT NULL,
                        transportChargeNote TEXT NOT NULL,
                        fuelIncludedNote TEXT NOT NULL,
                        taxNote TEXT NOT NULL,
                        warrantyText TEXT NOT NULL,
                        verifiedDiscountText TEXT NOT NULL,
                        stockStatus TEXT NOT NULL,
                        priceSource TEXT NOT NULL,
                        verificationStatus TEXT NOT NULL,
                        lastPriceUpdatedDate TEXT NOT NULL,
                        isOutdatedPrice INTEGER NOT NULL,
                        catalogUrl TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_machinery_dealer_offers_machineId ON machinery_dealer_offers(machineId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_machinery_dealer_offers_dealerId ON machinery_dealer_offers(dealerId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_reviews (
                        id TEXT NOT NULL PRIMARY KEY,
                        machineId TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        userName TEXT NOT NULL,
                        rating INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        comment TEXT NOT NULL,
                        verifiedFarmerFlag INTEGER NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_machinery_reviews_machineId ON machinery_reviews(machineId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_research_queue (
                        id TEXT NOT NULL PRIMARY KEY,
                        machineId TEXT NOT NULL,
                        equipmentName TEXT NOT NULL,
                        categoryCode TEXT NOT NULL,
                        missingFieldsNote TEXT NOT NULL,
                        candidateSourceUrl TEXT NOT NULL,
                        status TEXT NOT NULL,
                        submittedBy TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_machinery_research_queue_status ON machinery_research_queue(status)")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fraud_reports (
                        id TEXT NOT NULL PRIMARY KEY,
                        targetType TEXT NOT NULL,
                        targetId TEXT NOT NULL,
                        targetName TEXT NOT NULL,
                        sellerUserId TEXT NOT NULL,
                        reasonCode TEXT NOT NULL,
                        description TEXT NOT NULL,
                        reporterUserId TEXT NOT NULL,
                        reporterName TEXT NOT NULL,
                        status TEXT NOT NULL,
                        adminNote TEXT NOT NULL,
                        resolutionAction TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fraud_reports_status_createdAtMillis ON fraud_reports(status, createdAtMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fraud_reports_reporterUserId ON fraud_reports(reporterUserId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fraud_reports_sellerUserId ON fraud_reports(sellerUserId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS grievance_tickets (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        userName TEXT NOT NULL,
                        category TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        description TEXT NOT NULL,
                        relatedEntityId TEXT NOT NULL,
                        status TEXT NOT NULL,
                        adminResponse TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_grievance_tickets_userId_createdAtMillis ON grievance_tickets(userId, createdAtMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_grievance_tickets_status ON grievance_tickets(status)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS seller_suspensions (
                        sellerUserId TEXT NOT NULL PRIMARY KEY,
                        businessName TEXT NOT NULL,
                        isSuspended INTEGER NOT NULL,
                        reason TEXT NOT NULL,
                        suspendedByAdmin TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_seller_suspensions_isSuspended ON seller_suspensions(isSuspended)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS trust_audit_logs (
                        id TEXT NOT NULL PRIMARY KEY,
                        adminEmail TEXT NOT NULL,
                        action TEXT NOT NULL,
                        targetType TEXT NOT NULL,
                        targetId TEXT NOT NULL,
                        summary TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_trust_audit_logs_createdAtMillis ON trust_audit_logs(createdAtMillis)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS inquiry_activity_logs (
                        id TEXT NOT NULL PRIMARY KEY,
                        entityType TEXT NOT NULL,
                        entityId TEXT NOT NULL,
                        actorRole TEXT NOT NULL,
                        actorName TEXT NOT NULL,
                        statusLabel TEXT NOT NULL,
                        note TEXT NOT NULL,
                        timestampMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inquiry_activity_logs_entityId_timestampMillis ON inquiry_activity_logs(entityId, timestampMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inquiry_activity_logs_entityType ON inquiry_activity_logs(entityType)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "krishak_sathi.db"
                )
                    .addMigrations(
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
