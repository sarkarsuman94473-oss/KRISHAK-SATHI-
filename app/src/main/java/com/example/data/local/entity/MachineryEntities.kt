package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.MachineryDealerEntry
import com.example.data.model.MachineryDealerOffer
import com.example.data.model.MachineryInquiryRecord
import com.example.data.model.MachineryManufacturer
import com.example.data.model.MachineryProduct
import com.example.data.model.MachineryRentalRequest
import com.example.data.model.MachineryResearchQueueItem
import com.example.data.model.MachineryReviewRecord
import com.example.data.model.MachineryServiceRequest

@Entity(
    tableName = "machinery_products",
    indices = [
        Index(value = ["categoryCode", "approvalStatus"]),
        Index(value = ["sellerUserId"])
    ]
)
data class MachineryProductEntity(
    @PrimaryKey
    val id: String,
    val sellerUserId: String,
    val dealerId: String,
    val sellerBusinessName: String,
    val sellerLocation: String,
    val sellerLocalityCode: String,
    val sellerPhone: String?,
    val sellerWhatsapp: String?,
    val nameEn: String,
    val nameBn: String,
    val nameHi: String,
    val categoryCode: String,
    val subcategory: String,
    val brand: String,
    val modelNumber: String,
    val horsepowerOrCapacity: String,
    val engineOrMotorSpec: String,
    val fuelOrPowerType: String,
    val workingWidthOrCoverage: String,
    val weightKg: String,
    val dimensionsText: String,
    val suitableCrops: String,
    val suitableSoilOrTerrain: String,
    val landSizeSuitability: String,
    val condition: String,
    val forSale: Boolean,
    val forRent: Boolean,
    val salePriceInr: Double?,
    val rentalRateHourlyInr: Double?,
    val rentalRateDailyInr: Double?,
    val rentalRateWeeklyInr: Double?,
    val rentalRatePerAcreInr: Double?,
    val stockStatus: String,
    val warrantyInfo: String,
    val sparePartsInfo: String,
    val installationMaintenanceNotes: String,
    val safetyUsageGuidelines: String,
    val demoVideoUrl: String,
    val photosCsv: String,
    val listingOrigin: String,
    val approvalStatus: String,
    val verificationBadgeText: String,
    val requiresDgcaCompliance: Boolean,
    val requiresEarthmovingSafety: Boolean,
    val lastUpdatedDate: String,
    val updatedAtMillis: Long,
    val purposeEn: String = "",
    val purposeBn: String = "",
    val purposeHi: String = "",
    val suitableCropsBn: String = "",
    val suitableCropsHi: String = "",
    val operatingInstructionsEn: String = "",
    val operatingInstructionsBn: String = "",
    val operatingInstructionsHi: String = "",
    val safetyGuidelinesBn: String = "",
    val safetyGuidelinesHi: String = "",
    val brandVerified: Boolean = true,
    val modelVerified: Boolean = true,
    val pendingAdminReview: Boolean = false,
    val adminReviewNote: String = "",
    val hireServiceTypeCode: String = "",
    val firebaseStoragePathsCsv: String = "",
    val aliasesCsv: String = "",
    val applicableCategoriesCsv: String = "",
    val sourceUrl: String = "",
    val sourceType: String = "ENGINEERING_REFERENCE",
    val verificationDate: String = "2026-11-21",
    val infoVerificationStatus: String = "VERIFIED_CLASS",
    val priceVerificationStatus: String = "CONFIRM_WITH_SELLER",
    val imageUsageStatus: String = "APPROVED_IN_APP",
    val beginnerGuideJson: String = "",
    val compatibleWithIdsCsv: String = "",
    val subsidySchemeCodesCsv: String = "",
    val operatorTrainingRequired: Boolean = false
) {
    fun toModel(): MachineryProduct = MachineryProduct(
        id = id,
        sellerUserId = sellerUserId,
        dealerId = dealerId,
        sellerBusinessName = sellerBusinessName,
        sellerLocation = sellerLocation,
        sellerLocalityCode = sellerLocalityCode,
        sellerPhone = sellerPhone,
        sellerWhatsapp = sellerWhatsapp,
        nameEn = nameEn,
        nameBn = nameBn,
        nameHi = nameHi,
        categoryCode = categoryCode,
        subcategory = subcategory,
        brand = brand,
        modelNumber = modelNumber,
        horsepowerOrCapacity = horsepowerOrCapacity,
        engineOrMotorSpec = engineOrMotorSpec,
        fuelOrPowerType = fuelOrPowerType,
        workingWidthOrCoverage = workingWidthOrCoverage,
        weightKg = weightKg,
        dimensionsText = dimensionsText,
        suitableCrops = suitableCrops,
        suitableSoilOrTerrain = suitableSoilOrTerrain,
        landSizeSuitability = landSizeSuitability,
        condition = condition,
        forSale = forSale,
        forRent = forRent,
        salePriceInr = salePriceInr,
        rentalRateHourlyInr = rentalRateHourlyInr,
        rentalRateDailyInr = rentalRateDailyInr,
        rentalRateWeeklyInr = rentalRateWeeklyInr,
        rentalRatePerAcreInr = rentalRatePerAcreInr,
        stockStatus = stockStatus,
        warrantyInfo = warrantyInfo,
        sparePartsInfo = sparePartsInfo,
        installationMaintenanceNotes = installationMaintenanceNotes,
        safetyUsageGuidelines = safetyUsageGuidelines,
        demoVideoUrl = demoVideoUrl,
        photosCsv = photosCsv,
        listingOrigin = listingOrigin,
        approvalStatus = approvalStatus,
        verificationBadgeText = verificationBadgeText,
        requiresDgcaCompliance = requiresDgcaCompliance,
        requiresEarthmovingSafety = requiresEarthmovingSafety,
        lastUpdatedDate = lastUpdatedDate,
        updatedAtMillis = updatedAtMillis,
        purposeEn = purposeEn,
        purposeBn = purposeBn,
        purposeHi = purposeHi,
        suitableCropsBn = suitableCropsBn,
        suitableCropsHi = suitableCropsHi,
        operatingInstructionsEn = operatingInstructionsEn,
        operatingInstructionsBn = operatingInstructionsBn,
        operatingInstructionsHi = operatingInstructionsHi,
        safetyGuidelinesBn = safetyGuidelinesBn,
        safetyGuidelinesHi = safetyGuidelinesHi,
        brandVerified = brandVerified,
        modelVerified = modelVerified,
        pendingAdminReview = pendingAdminReview,
        adminReviewNote = adminReviewNote,
        hireServiceTypeCode = hireServiceTypeCode,
        firebaseStoragePathsCsv = firebaseStoragePathsCsv,
        aliasesCsv = aliasesCsv,
        applicableCategoriesCsv = applicableCategoriesCsv,
        sourceUrl = sourceUrl,
        sourceType = sourceType,
        verificationDate = verificationDate,
        infoVerificationStatus = infoVerificationStatus,
        priceVerificationStatus = priceVerificationStatus,
        imageUsageStatus = imageUsageStatus,
        beginnerGuideJson = beginnerGuideJson,
        compatibleWithIdsCsv = compatibleWithIdsCsv,
        subsidySchemeCodesCsv = subsidySchemeCodesCsv,
        operatorTrainingRequired = operatorTrainingRequired
    )

    companion object {
        fun fromModel(m: MachineryProduct): MachineryProductEntity = MachineryProductEntity(
            id = m.id,
            sellerUserId = m.sellerUserId,
            dealerId = m.dealerId,
            sellerBusinessName = m.sellerBusinessName,
            sellerLocation = m.sellerLocation,
            sellerLocalityCode = m.sellerLocalityCode,
            sellerPhone = m.sellerPhone,
            sellerWhatsapp = m.sellerWhatsapp,
            nameEn = m.nameEn,
            nameBn = m.nameBn,
            nameHi = m.nameHi,
            categoryCode = m.categoryCode,
            subcategory = m.subcategory,
            brand = m.brand,
            modelNumber = m.modelNumber,
            horsepowerOrCapacity = m.horsepowerOrCapacity,
            engineOrMotorSpec = m.engineOrMotorSpec,
            fuelOrPowerType = m.fuelOrPowerType,
            workingWidthOrCoverage = m.workingWidthOrCoverage,
            weightKg = m.weightKg,
            dimensionsText = m.dimensionsText,
            suitableCrops = m.suitableCrops,
            suitableSoilOrTerrain = m.suitableSoilOrTerrain,
            landSizeSuitability = m.landSizeSuitability,
            condition = m.condition,
            forSale = m.forSale,
            forRent = m.forRent,
            salePriceInr = m.salePriceInr,
            rentalRateHourlyInr = m.rentalRateHourlyInr,
            rentalRateDailyInr = m.rentalRateDailyInr,
            rentalRateWeeklyInr = m.rentalRateWeeklyInr,
            rentalRatePerAcreInr = m.rentalRatePerAcreInr,
            stockStatus = m.stockStatus,
            warrantyInfo = m.warrantyInfo,
            sparePartsInfo = m.sparePartsInfo,
            installationMaintenanceNotes = m.installationMaintenanceNotes,
            safetyUsageGuidelines = m.safetyUsageGuidelines,
            demoVideoUrl = m.demoVideoUrl,
            photosCsv = m.photosCsv,
            listingOrigin = m.listingOrigin,
            approvalStatus = m.approvalStatus,
            verificationBadgeText = m.verificationBadgeText,
            requiresDgcaCompliance = m.requiresDgcaCompliance,
            requiresEarthmovingSafety = m.requiresEarthmovingSafety,
            lastUpdatedDate = m.lastUpdatedDate,
            updatedAtMillis = m.updatedAtMillis,
            purposeEn = m.purposeEn,
            purposeBn = m.purposeBn,
            purposeHi = m.purposeHi,
            suitableCropsBn = m.suitableCropsBn,
            suitableCropsHi = m.suitableCropsHi,
            operatingInstructionsEn = m.operatingInstructionsEn,
            operatingInstructionsBn = m.operatingInstructionsBn,
            operatingInstructionsHi = m.operatingInstructionsHi,
            safetyGuidelinesBn = m.safetyGuidelinesBn,
            safetyGuidelinesHi = m.safetyGuidelinesHi,
            brandVerified = m.brandVerified,
            modelVerified = m.modelVerified,
            pendingAdminReview = m.pendingAdminReview,
            adminReviewNote = m.adminReviewNote,
            hireServiceTypeCode = m.hireServiceTypeCode,
            firebaseStoragePathsCsv = m.firebaseStoragePathsCsv,
            aliasesCsv = m.aliasesCsv,
            applicableCategoriesCsv = m.applicableCategoriesCsv,
            sourceUrl = m.sourceUrl,
            sourceType = m.sourceType,
            verificationDate = m.verificationDate,
            infoVerificationStatus = m.infoVerificationStatus,
            priceVerificationStatus = m.priceVerificationStatus,
            imageUsageStatus = m.imageUsageStatus,
            beginnerGuideJson = m.beginnerGuideJson,
            compatibleWithIdsCsv = m.compatibleWithIdsCsv,
            subsidySchemeCodesCsv = m.subsidySchemeCodesCsv,
            operatorTrainingRequired = m.operatorTrainingRequired
        )
    }
}

@Entity(
    tableName = "machinery_dealers",
    indices = [Index(value = ["localityCode", "verificationStatus"])]
)
data class MachineryDealerEntity(
    @PrimaryKey
    val dealerId: String,
    val businessName: String,
    val businessNameBn: String,
    val businessNameHi: String,
    val partnerRoleCode: String,
    val localityCode: String,
    val district: String,
    val state: String,
    val fullAddress: String?,
    val pinCode: String?,
    val ownerName: String?,
    val phone: String?,
    val whatsapp: String?,
    val latitude: Double?,
    val longitude: Double?,
    val brandsCarriedCsv: String,
    val supportedCategoriesCsv: String,
    val servicesOfferedCsv: String,
    val isAuthorisedDealerVerified: Boolean,
    val offersSale: Boolean,
    val offersRent: Boolean,
    val offersRepair: Boolean,
    val verificationStatus: String,
    val ownerConsentStatus: String,
    val isVerified: Boolean,
    val claimedByUserId: String?,
    val claimNote: String?,
    val sourceReference: String,
    val notes: String,
    val lastVerifiedDate: String,
    val updatedAtMillis: Long
) {
    fun toModel(): MachineryDealerEntry = MachineryDealerEntry(
        dealerId = dealerId,
        businessName = businessName,
        businessNameBn = businessNameBn,
        businessNameHi = businessNameHi,
        partnerRoleCode = partnerRoleCode,
        localityCode = localityCode,
        district = district,
        state = state,
        fullAddress = fullAddress,
        pinCode = pinCode,
        ownerName = ownerName,
        phone = phone,
        whatsapp = whatsapp,
        latitude = latitude,
        longitude = longitude,
        brandsCarriedCsv = brandsCarriedCsv,
        supportedCategoriesCsv = supportedCategoriesCsv,
        servicesOfferedCsv = servicesOfferedCsv,
        isAuthorisedDealerVerified = isAuthorisedDealerVerified,
        offersSale = offersSale,
        offersRent = offersRent,
        offersRepair = offersRepair,
        verificationStatus = verificationStatus,
        ownerConsentStatus = ownerConsentStatus,
        isVerified = isVerified,
        claimedByUserId = claimedByUserId,
        claimNote = claimNote,
        sourceReference = sourceReference,
        notes = notes,
        lastVerifiedDate = lastVerifiedDate,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromModel(m: MachineryDealerEntry): MachineryDealerEntity = MachineryDealerEntity(
            dealerId = m.dealerId,
            businessName = m.businessName,
            businessNameBn = m.businessNameBn,
            businessNameHi = m.businessNameHi,
            partnerRoleCode = m.partnerRoleCode,
            localityCode = m.localityCode,
            district = m.district,
            state = m.state,
            fullAddress = m.fullAddress,
            pinCode = m.pinCode,
            ownerName = m.ownerName,
            phone = m.phone,
            whatsapp = m.whatsapp,
            latitude = m.latitude,
            longitude = m.longitude,
            brandsCarriedCsv = m.brandsCarriedCsv,
            supportedCategoriesCsv = m.supportedCategoriesCsv,
            servicesOfferedCsv = m.servicesOfferedCsv,
            isAuthorisedDealerVerified = m.isAuthorisedDealerVerified,
            offersSale = m.offersSale,
            offersRent = m.offersRent,
            offersRepair = m.offersRepair,
            verificationStatus = m.verificationStatus,
            ownerConsentStatus = m.ownerConsentStatus,
            isVerified = m.isVerified,
            claimedByUserId = m.claimedByUserId,
            claimNote = m.claimNote,
            sourceReference = m.sourceReference,
            notes = m.notes,
            lastVerifiedDate = m.lastVerifiedDate,
            updatedAtMillis = m.updatedAtMillis
        )
    }
}

@Entity(tableName = "machinery_favourites")
data class MachineryFavouriteEntity(
    @PrimaryKey
    val compositeId: String, // userId::machineId
    val userId: String,
    val machineId: String,
    val savedAtMillis: Long
)

@Entity(tableName = "machinery_inquiries")
data class MachineryInquiryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val farmerName: String,
    val farmerPhone: String,
    val machineId: String,
    val machineName: String,
    val sellerUserId: String,
    val inquiryType: String,
    val message: String,
    val status: String,
    val sellerReply: String,
    val createdAtMillis: Long
) {
    fun toModel(): MachineryInquiryRecord = MachineryInquiryRecord(
        id, userId, farmerName, farmerPhone, machineId, machineName, sellerUserId, inquiryType, message, status, sellerReply, createdAtMillis
    )

    companion object {
        fun fromModel(m: MachineryInquiryRecord) = MachineryInquiryEntity(
            m.id, m.userId, m.farmerName, m.farmerPhone, m.machineId, m.machineName, m.sellerUserId, m.inquiryType, m.message, m.status, m.sellerReply, m.createdAtMillis
        )
    }
}

@Entity(tableName = "machinery_rentals")
data class MachineryRentalEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val farmerName: String,
    val farmerPhone: String,
    val machineId: String,
    val machineName: String,
    val sellerUserId: String,
    val locality: String,
    val rentalUnit: String,
    val durationUnits: Int,
    val withOperator: Boolean,
    val deliveryRequired: Boolean,
    val fuelPolicyNote: String,
    val securityDepositNote: String,
    val notes: String,
    val status: String,
    val createdAtMillis: Long
) {
    fun toModel(): MachineryRentalRequest = MachineryRentalRequest(
        id, userId, farmerName, farmerPhone, machineId, machineName, sellerUserId, locality, rentalUnit, durationUnits, withOperator, deliveryRequired, fuelPolicyNote, securityDepositNote, notes, status, createdAtMillis
    )

    companion object {
        fun fromModel(m: MachineryRentalRequest) = MachineryRentalEntity(
            m.id, m.userId, m.farmerName, m.farmerPhone, m.machineId, m.machineName, m.sellerUserId, m.locality, m.rentalUnit, m.durationUnits, m.withOperator, m.deliveryRequired, m.fuelPolicyNote, m.securityDepositNote, m.notes, m.status, m.createdAtMillis
        )
    }
}

@Entity(tableName = "machinery_service_requests")
data class MachineryServiceRequestEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val farmerName: String,
    val farmerPhone: String,
    val serviceTypeCode: String,
    val locality: String,
    val landSizeText: String,
    val cropName: String,
    val preferredDate: String,
    val notes: String,
    val providerUserId: String,
    val status: String,
    val createdAtMillis: Long
) {
    fun toModel(): MachineryServiceRequest = MachineryServiceRequest(
        id, userId, farmerName, farmerPhone, serviceTypeCode, locality, landSizeText, cropName, preferredDate, notes, providerUserId, status, createdAtMillis
    )

    companion object {
        fun fromModel(m: MachineryServiceRequest) = MachineryServiceRequestEntity(
            m.id, m.userId, m.farmerName, m.farmerPhone, m.serviceTypeCode, m.locality, m.landSizeText, m.cropName, m.preferredDate, m.notes, m.providerUserId, m.status, m.createdAtMillis
        )
    }
}

@Entity(tableName = "machinery_manufacturers")
data class MachineryManufacturerEntity(
    @PrimaryKey
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val nameHi: String,
    val countryOrOrigin: String,
    val focusSummaryEn: String,
    val officialPortalUrl: String,
    val dealerLocatorUrl: String,
    val servicePortalUrl: String,
    val customerCareHelpline: String,
    val supportedCategoriesCsv: String,
    val isVerifiedOfficialLink: Boolean,
    val verificationStatus: String,
    val sourceUrl: String,
    val lastVerifiedDate: String
) {
    fun toModel(): MachineryManufacturer = MachineryManufacturer(
        id = id,
        nameEn = nameEn,
        nameBn = nameBn,
        nameHi = nameHi,
        countryOrOrigin = countryOrOrigin,
        focusSummaryEn = focusSummaryEn,
        officialPortalUrl = officialPortalUrl,
        dealerLocatorUrl = dealerLocatorUrl,
        servicePortalUrl = servicePortalUrl,
        customerCareHelpline = customerCareHelpline,
        supportedCategoriesCsv = supportedCategoriesCsv,
        isVerifiedOfficialLink = isVerifiedOfficialLink,
        verificationStatus = verificationStatus,
        sourceUrl = sourceUrl,
        lastVerifiedDate = lastVerifiedDate
    )

    companion object {
        fun fromModel(m: MachineryManufacturer) = MachineryManufacturerEntity(
            id = m.id,
            nameEn = m.nameEn,
            nameBn = m.nameBn,
            nameHi = m.nameHi,
            countryOrOrigin = m.countryOrOrigin,
            focusSummaryEn = m.focusSummaryEn,
            officialPortalUrl = m.officialPortalUrl,
            dealerLocatorUrl = m.dealerLocatorUrl,
            servicePortalUrl = m.servicePortalUrl,
            customerCareHelpline = m.customerCareHelpline,
            supportedCategoriesCsv = m.supportedCategoriesCsv,
            isVerifiedOfficialLink = m.isVerifiedOfficialLink,
            verificationStatus = m.verificationStatus,
            sourceUrl = m.sourceUrl,
            lastVerifiedDate = m.lastVerifiedDate
        )
    }
}

@Entity(
    tableName = "machinery_dealer_offers",
    indices = [
        Index(value = ["machineId"]),
        Index(value = ["dealerId"])
    ]
)
data class MachineryDealerOfferEntity(
    @PrimaryKey
    val offerId: String,
    val machineId: String,
    val dealerId: String,
    val dealerName: String,
    val localityCode: String,
    val modelNumber: String,
    val condition: String,
    val offersSale: Boolean,
    val offersRent: Boolean,
    val mrpInr: Double?,
    val dealerPriceInr: Double?,
    val indicativePriceInr: Double?,
    val rentalHourlyInr: Double?,
    val rentalDailyInr: Double?,
    val rentalPerAcreInr: Double?,
    val operatorChargeNote: String,
    val transportChargeNote: String,
    val fuelIncludedNote: String,
    val taxNote: String,
    val warrantyText: String,
    val verifiedDiscountText: String,
    val stockStatus: String,
    val priceSource: String,
    val verificationStatus: String,
    val lastPriceUpdatedDate: String,
    val isOutdatedPrice: Boolean,
    val catalogUrl: String,
    val updatedAtMillis: Long
) {
    fun toModel(): MachineryDealerOffer = MachineryDealerOffer(
        offerId = offerId,
        machineId = machineId,
        dealerId = dealerId,
        dealerName = dealerName,
        localityCode = localityCode,
        modelNumber = modelNumber,
        condition = condition,
        offersSale = offersSale,
        offersRent = offersRent,
        mrpInr = mrpInr,
        dealerPriceInr = dealerPriceInr,
        indicativePriceInr = indicativePriceInr,
        rentalHourlyInr = rentalHourlyInr,
        rentalDailyInr = rentalDailyInr,
        rentalPerAcreInr = rentalPerAcreInr,
        operatorChargeNote = operatorChargeNote,
        transportChargeNote = transportChargeNote,
        fuelIncludedNote = fuelIncludedNote,
        taxNote = taxNote,
        warrantyText = warrantyText,
        verifiedDiscountText = verifiedDiscountText,
        stockStatus = stockStatus,
        priceSource = priceSource,
        verificationStatus = verificationStatus,
        lastPriceUpdatedDate = lastPriceUpdatedDate,
        isOutdatedPrice = isOutdatedPrice,
        catalogUrl = catalogUrl,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromModel(m: MachineryDealerOffer) = MachineryDealerOfferEntity(
            offerId = m.offerId,
            machineId = m.machineId,
            dealerId = m.dealerId,
            dealerName = m.dealerName,
            localityCode = m.localityCode,
            modelNumber = m.modelNumber,
            condition = m.condition,
            offersSale = m.offersSale,
            offersRent = m.offersRent,
            mrpInr = m.mrpInr,
            dealerPriceInr = m.dealerPriceInr,
            indicativePriceInr = m.indicativePriceInr,
            rentalHourlyInr = m.rentalHourlyInr,
            rentalDailyInr = m.rentalDailyInr,
            rentalPerAcreInr = m.rentalPerAcreInr,
            operatorChargeNote = m.operatorChargeNote,
            transportChargeNote = m.transportChargeNote,
            fuelIncludedNote = m.fuelIncludedNote,
            taxNote = m.taxNote,
            warrantyText = m.warrantyText,
            verifiedDiscountText = m.verifiedDiscountText,
            stockStatus = m.stockStatus,
            priceSource = m.priceSource,
            verificationStatus = m.verificationStatus,
            lastPriceUpdatedDate = m.lastPriceUpdatedDate,
            isOutdatedPrice = m.isOutdatedPrice,
            catalogUrl = m.catalogUrl,
            updatedAtMillis = m.updatedAtMillis
        )
    }
}

@Entity(
    tableName = "machinery_reviews",
    indices = [Index(value = ["machineId"])]
)
data class MachineryReviewEntity(
    @PrimaryKey
    val id: String,
    val machineId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val title: String,
    val comment: String,
    val verifiedFarmerFlag: Boolean,
    val createdAtMillis: Long
) {
    fun toModel(): MachineryReviewRecord = MachineryReviewRecord(
        id = id,
        machineId = machineId,
        userId = userId,
        userName = userName,
        rating = rating,
        title = title,
        comment = comment,
        verifiedFarmerFlag = verifiedFarmerFlag,
        createdAtMillis = createdAtMillis
    )

    companion object {
        fun fromModel(m: MachineryReviewRecord) = MachineryReviewEntity(
            id = m.id,
            machineId = m.machineId,
            userId = m.userId,
            userName = m.userName,
            rating = m.rating,
            title = m.title,
            comment = m.comment,
            verifiedFarmerFlag = m.verifiedFarmerFlag,
            createdAtMillis = m.createdAtMillis
        )
    }
}

@Entity(
    tableName = "machinery_research_queue",
    indices = [Index(value = ["status"])]
)
data class MachineryResearchQueueEntity(
    @PrimaryKey
    val id: String,
    val machineId: String,
    val equipmentName: String,
    val categoryCode: String,
    val missingFieldsNote: String,
    val candidateSourceUrl: String,
    val status: String,
    val submittedBy: String,
    val updatedAtMillis: Long
) {
    fun toModel(): MachineryResearchQueueItem = MachineryResearchQueueItem(
        id = id,
        machineId = machineId,
        equipmentName = equipmentName,
        categoryCode = categoryCode,
        missingFieldsNote = missingFieldsNote,
        candidateSourceUrl = candidateSourceUrl,
        status = status,
        submittedBy = submittedBy,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromModel(m: MachineryResearchQueueItem) = MachineryResearchQueueEntity(
            id = m.id,
            machineId = m.machineId,
            equipmentName = m.equipmentName,
            categoryCode = m.categoryCode,
            missingFieldsNote = m.missingFieldsNote,
            candidateSourceUrl = m.candidateSourceUrl,
            status = m.status,
            submittedBy = m.submittedBy,
            updatedAtMillis = m.updatedAtMillis
        )
    }
}

