package com.example.data.datasource

import com.example.data.model.NurseryVerificationStatus
import com.example.data.model.WbNurseryDirectoryEntry
import com.example.data.model.WbNurseryDistrict

/**
 * Initial 37 publicly listed nursery records for Siliguri, North Bengal & West Bengal.
 * STRICT HONESTY RULE:
 * - Every imported record is marked `PENDING_VERIFICATION` (`isVerified = false`).
 * - Stores `sourceReference` and `lastVerifiedDate`.
 * - NEVER invents phone numbers, WhatsApp, addresses, owner names, opening hours,
 *   inventory items, or GPS map coordinates when not explicitly supplied.
 */
object WbNurseryDirectorySeed {

    private const val SOURCE_INITIAL_PUBLIC_LIST = "Publicly listed initial directory import (Pending Verification)"
    private const val VERIFIED_CHECK_DATE = "2026-11-18"

    val initialPendingNurseries: List<WbNurseryDirectoryEntry> = listOf(
        pendingEntry("wb_nurs_01", "Muddy Hands Siliguri", WbNurseryDistrict.SILIGURI, "Siliguri"),
        pendingEntry("wb_nurs_02", "Das Nursery Siliguri", WbNurseryDistrict.SILIGURI, "Siliguri"),
        pendingEntry("wb_nurs_03", "Petunia", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_04", "Siliguri Plant Bloom", WbNurseryDistrict.SILIGURI, "Siliguri"),
        pendingEntry("wb_nurs_05", "Sarkar Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_06", "Blooming Greens", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_07", "Pushpanjali Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_08", "Green Keeper", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_09", "Blue Heaven Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_10", "Planter's Hub", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_11", "Garden Need", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_12", "Pab Andrew's Home", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_13", "Green Heaven Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_14", "Flower Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_15", "Jalpaiguri Agri Horti Nursery", WbNurseryDistrict.JALPAIGURI, "Jalpaiguri"),
        pendingEntry("wb_nurs_16", "Sharma Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_17", "Barman Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_18", "Minati Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_19", "Ayesha Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_20", "Green World Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_21", "Poppy Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_22", "Lakshmi Narayan Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_23", "Baba Lokenath Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_24", "Evergreen Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_25", "Bloom Story", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_26", "Janak Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_27", "Bengal Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_28", "Green Vine", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_29", "Ma Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_30", "Bloomingbliss Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_31", "Green Wave Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_32", "Raj Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_33", "Maiya Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_34", "Milan Nursery", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_35", "KPG Bulbs Traders", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_36", "Elohim's Garden", WbNurseryDistrict.OTHER_WB, null),
        pendingEntry("wb_nurs_37", "Yewang Flower & Plants", WbNurseryDistrict.OTHER_WB, null)
    )

    private fun pendingEntry(
        id: String,
        businessName: String,
        district: WbNurseryDistrict,
        locality: String?
    ): WbNurseryDirectoryEntry = WbNurseryDirectoryEntry(
        nurseryId = id,
        businessName = businessName,
        businessNameBn = businessName,
        businessNameHi = businessName,
        ownerName = null,
        fullAddress = null,
        districtCode = district.code,
        locality = locality,
        state = "West Bengal",
        phone = null,
        whatsapp = null,
        latitude = null,
        longitude = null,
        websiteOrSocial = null,
        openingHours = null,
        nurseryPhotosCsv = "",
        availablePlantCategoriesCsv = "",
        availablePlantIdsCsv = "",
        supportsWholesale = null,
        supportsRetail = null,
        deliveryAvailable = null,
        descriptionEn = "Publicly listed nursery entry. Contact details, address, opening hours, and inventory are pending owner or admin verification.",
        descriptionBn = "পাবলিক তালিকাভুক্ত নার্সারি। ফোন নম্বর, পূর্ণ ঠিকানা, সময়সূচি ও চারার তালিকা মালিক বা অ্যাডমিন যাচাইয়ের অপেক্ষায় রয়েছে।",
        descriptionHi = "सार्वजनिक सूचीबद्ध नर्सरी। फ़ोन नंबर, पूरा पता, समय और पौधों की सूची स्वामी या एडमिन सत्यापन के अधीन है।",
        verificationStatus = NurseryVerificationStatus.PENDING_VERIFICATION,
        isVerified = false,
        sourceReference = SOURCE_INITIAL_PUBLIC_LIST,
        lastVerifiedDate = VERIFIED_CHECK_DATE,
        linkedSellerUserId = null,
        updatedAtMillis = 1763424000000L
    )
}
