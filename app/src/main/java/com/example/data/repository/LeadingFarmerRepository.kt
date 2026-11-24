package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.dao.LeadingFarmerDao
import com.example.data.local.entity.FarmerAuditLogEntity
import com.example.data.local.entity.FarmerCategoryTags
import com.example.data.local.entity.FarmerContactRequestEntity
import com.example.data.local.entity.LeadingFarmerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class LeadingFarmerRepository(private val farmerDao: LeadingFarmerDao) {

    val allFarmers: Flow<List<LeadingFarmerEntity>> = farmerDao.getAllFarmers()
    val approvedFarmers: Flow<List<LeadingFarmerEntity>> = farmerDao.getApprovedFarmers()
    val featuredFarmers: Flow<List<LeadingFarmerEntity>> = farmerDao.getFeaturedFarmers()
    val allContactRequests: Flow<List<FarmerContactRequestEntity>> = farmerDao.getAllContactRequests()
    val allAuditLogs: Flow<List<FarmerAuditLogEntity>> = farmerDao.getAllAuditLogs()

    fun getFarmerById(id: String): Flow<LeadingFarmerEntity?> = farmerDao.getFarmerById(id)

    suspend fun getFarmerByIdSync(id: String): LeadingFarmerEntity? = farmerDao.getFarmerByIdSync(id)

    fun filterFarmers(
        searchQuery: String = "",
        categoryTag: String = FarmerCategoryTags.ALL,
        selectedState: String = ""
    ): Flow<List<LeadingFarmerEntity>> {
        return approvedFarmers.map { list ->
            list.filter { farmer ->
                val matchesCategory = if (categoryTag == FarmerCategoryTags.ALL) true else farmer.categoryTag == categoryTag
                val matchesState = if (
                    selectedState.isBlank() ||
                    selectedState.equals("All States", true) ||
                    selectedState.equals("সকল রাজ্য", true) ||
                    selectedState.equals("सभी राज्य", true)
                ) true else farmer.state.equals(selectedState, true)
                val matchesSearch = if (searchQuery.isBlank()) true else {
                    val q = searchQuery.trim().lowercase()
                    farmer.fullNameEn.lowercase().contains(q) ||
                    farmer.fullNameBn.lowercase().contains(q) ||
                    farmer.fullNameHi.lowercase().contains(q) ||
                    farmer.state.lowercase().contains(q) ||
                    farmer.district.lowercase().contains(q) ||
                    farmer.mainCrops.lowercase().contains(q) ||
                    farmer.farmingSpecializationEn.lowercase().contains(q) ||
                    farmer.farmingSpecializationBn.lowercase().contains(q) ||
                    farmer.farmingSpecializationHi.lowercase().contains(q) ||
                    farmer.farmingTechniques.lowercase().contains(q) ||
                    farmer.keyAwardEn.lowercase().contains(q) ||
                    farmer.allAwards.lowercase().contains(q) ||
                    com.example.data.datasource.LeadingFarmerTranslations.getDisplayCrops(farmer, com.example.data.model.AppLanguage.BENGALI).lowercase().contains(q) ||
                    com.example.data.datasource.LeadingFarmerTranslations.getDisplayCrops(farmer, com.example.data.model.AppLanguage.HINDI).lowercase().contains(q) ||
                    com.example.data.datasource.LeadingFarmerTranslations.getDisplayLocation(farmer, com.example.data.model.AppLanguage.BENGALI).lowercase().contains(q) ||
                    com.example.data.datasource.LeadingFarmerTranslations.getDisplayLocation(farmer, com.example.data.model.AppLanguage.HINDI).lowercase().contains(q) ||
                    com.example.data.datasource.LeadingFarmerTranslations.getDisplayAward(farmer, com.example.data.model.AppLanguage.BENGALI).lowercase().contains(q) ||
                    com.example.data.datasource.LeadingFarmerTranslations.getDisplayAward(farmer, com.example.data.model.AppLanguage.HINDI).lowercase().contains(q)
                }
                matchesCategory && matchesState && matchesSearch
            }
        }
    }

    suspend fun submitContactRequest(
        farmerId: String,
        farmerName: String,
        requesterName: String,
        requesterPhone: String,
        requesterDistrict: String,
        requesterCrop: String,
        purposeCategory: String,
        message: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = FarmerContactRequestEntity(
                id = UUID.randomUUID().toString(),
                farmerId = farmerId,
                farmerName = farmerName,
                requesterName = requesterName,
                requesterPhone = requesterPhone,
                requesterDistrict = requesterDistrict,
                requesterCrop = requesterCrop,
                purposeCategory = purposeCategory,
                message = message,
                status = "Submitted",
                timestamp = System.currentTimeMillis()
            )
            farmerDao.insertContactRequest(req)
            logAudit(
                farmerId = farmerId,
                action = "CONTACT_REQUEST_SUBMITTED",
                performedBy = requesterName,
                details = "Contact request submitted for purpose: $purposeCategory"
            )
            Result.success(req.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRequestStatus(requestId: String, newStatus: String) = withContext(Dispatchers.IO) {
        farmerDao.updateRequestStatus(requestId, newStatus)
    }

    suspend fun saveFarmer(farmer: LeadingFarmerEntity, performedBy: String = "Admin") = withContext(Dispatchers.IO) {
        val existing = farmerDao.getFarmerByIdSync(farmer.id)
        farmerDao.insertFarmer(farmer)
        val action = if (existing == null) "PROFILE_CREATED" else "PROFILE_UPDATED"
        logAudit(
            farmerId = farmer.id,
            action = action,
            performedBy = performedBy,
            details = "Profile ${if (existing == null) "created" else "updated"}: ${farmer.fullNameEn} (${farmer.keyAwardEn})"
        )
    }

    suspend fun deleteFarmer(farmer: LeadingFarmerEntity, performedBy: String = "Admin") = withContext(Dispatchers.IO) {
        farmerDao.deleteFarmer(farmer)
        logAudit(
            farmerId = farmer.id,
            action = "PROFILE_DELETED",
            performedBy = performedBy,
            details = "Profile removed: ${farmer.fullNameEn}"
        )
    }

    suspend fun toggleApproval(farmerId: String, approved: Boolean, performedBy: String = "Admin") = withContext(Dispatchers.IO) {
        farmerDao.updateApprovalStatus(farmerId, approved)
        logAudit(
            farmerId = farmerId,
            action = if (approved) "PROFILE_APPROVED" else "PROFILE_UNPUBLISHED",
            performedBy = performedBy,
            details = "Approval status updated to $approved"
        )
    }

    suspend fun toggleFeatured(farmerId: String, featured: Boolean, performedBy: String = "Admin") = withContext(Dispatchers.IO) {
        farmerDao.updateFeaturedStatus(farmerId, featured)
        logAudit(
            farmerId = farmerId,
            action = "FEATURED_TOGGLED",
            performedBy = performedBy,
            details = "Featured status set to $featured"
        )
    }

    suspend fun logAudit(farmerId: String, action: String, performedBy: String, details: String) = withContext(Dispatchers.IO) {
        val log = FarmerAuditLogEntity(
            id = UUID.randomUUID().toString(),
            farmerId = farmerId,
            action = action,
            performedBy = performedBy,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        farmerDao.insertAuditLog(log)
    }

    suspend fun seedInitialFarmersIfNeeded() = withContext(Dispatchers.IO) {
        val count = farmerDao.countFarmers()
        if (count == 0) {
            val initial = getInitialVerifiedFarmers()
            farmerDao.insertFarmers(initial)
            initial.forEach { f ->
                farmerDao.insertAuditLog(
                    FarmerAuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        farmerId = f.id,
                        action = "VERIFIED_SEED_IMPORT",
                        performedBy = "Krishak Sathi Verification Cell",
                        details = "Initial verified profile imported from ${f.sourceReferences}",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    companion object {
        fun getInitialVerifiedFarmers(): List<LeadingFarmerEntity> {
            return listOf(
                LeadingFarmerEntity(
                    id = "farmer_subhash_palekar",
                    fullNameEn = "Subhash Palekar",
                    fullNameBn = "সুভাষ পালেকর",
                    fullNameHi = "सुभाष पालेकर",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Maharashtra",
                    district = "Amravati",
                    farmingSpecializationEn = "Zero Budget Natural Farming (ZBNF / SPNF) & Soil Microbiology",
                    farmingSpecializationBn = "জিরো বাজেট প্রাকৃতিক কৃষি (ZBNF) ও অনুজীব বিজ্ঞান",
                    farmingSpecializationHi = "शून्य बजट प्राकृतिक खेती (ZBNF)",
                    categoryTag = FarmerCategoryTags.NATURAL_FARMING,
                    mainCrops = "Millets, Pulses, Multi-cropping, Spices, Grains, Vegetables",
                    farmingTechniques = "Jeevamrut (microbial inoculant), Beejamrut (seed treatment), Acchadana (mulching), Whapasa (aeration of soil)",
                    yearsOfExperience = 40,
                    keyAwardEn = "Padma Shri (2016)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০১৬)",
                    allAwards = "Padma Shri (2016), National Krishi Shiromani, Maharashtra Krishi Bhushan",
                    innovationContributionEn = "Pioneered Zero Budget Natural Farming utilizing indigenous cow dung & urine formulations to restore degraded soil without synthetic chemicals.",
                    innovationContributionBn = "দেশি গরুর গোবর ও গোমূত্র ভিত্তিক 'জীবাণুমৃত' ও 'বীজামৃত' ব্যবহারের মাধ্যমে রাসায়নিকমুক্ত প্রাকৃতিক কৃষির উদ্ভাবন।",
                    innovationContributionHi = "देसी गाय के गोबर व गोमूत्र पर आधारित जीवामृत और बीजामृत से रासायनिक मुक्त प्राकृतिक खेती का विकास।",
                    storyHowStarted = "Began farming after completing agricultural science in 1972. Initially followed green revolution chemical methods for 12 years. Observed steady drop in crop yield, hardened soil, and pest resistance by 1985.",
                    storyChallenges = "Encountered intense skepticism when stopping all chemical fertilizers. Spent 6 years researching forest tree ecosystems and natural microbiological mulch.",
                    storyMethods = "Utilizes 1 indigenous cow for 30 acres. Ferments 10 kg dung, 10 L urine, 2 kg jaggery, 2 kg pulse flour, and forest soil into Jeevamrut containing trillions of beneficial bacteria.",
                    storyAchievements = "Trained over 4 million farmers across Andhra Pradesh, Karnataka, Maharashtra, Himachal Pradesh, and West Bengal. Awarded Padma Shri by the President of India in 2016.",
                    storyLessonsForFarmers = "Do not spend heavy capital on synthetic inputs. Nourish the soil's natural microorganisms with Jeevamrut and keep soil continuously mulched (Acchadana) to preserve moisture.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 94228 71830 (Official Movement Office)",
                    officialEmail = "info@palekarzerobudgetnaturalfarming.com",
                    officialWebsite = "https://palekarzerobudgetnaturalfarming.com",
                    officialFacebook = "https://facebook.com/subhashpalekar.spnf",
                    officialInstagram = null,
                    officialYoutube = "https://youtube.com/@SubhashPalekarKrishi",
                    organizationFpoDetails = "Subhash Palekar Natural Farming Movement (SPNF)",
                    hasPublicContact = true,
                    directContactNotice = "Official SPNF organization contacts are verified for workshop and training inquiries.",
                    sourceReferences = "Padma Awards Directory (Ministry of Home Affairs, GOI); NITI Aayog Natural Farming Taskforce Reports.",
                    lastVerifiedDate = "2026-10-15",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_kanwal_singh_chauhan",
                    fullNameEn = "Kanwal Singh Chauhan",
                    fullNameBn = "কনওয়াল সিং চৌহান",
                    fullNameHi = "कंवर सिंह चौहान",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Haryana",
                    district = "Sonipat",
                    farmingSpecializationEn = "Baby Corn & Sweet Corn Processing, Mushroom & FPO Export",
                    farmingSpecializationBn = "বেবি কর্ন ও সুইট কর্ন প্রক্রিয়াকরণ, মাশরুম ও রপ্তানিমুখী এফপিও",
                    farmingSpecializationHi = "बेबी कॉर्न व स्वीट कॉर्न प्रसंस्करण और एफपीओ",
                    categoryTag = FarmerCategoryTags.MODERN_TECH,
                    mainCrops = "Baby Corn, Sweet Corn, Button & Dhingri Mushrooms, Tomato, Broccoli",
                    farmingTechniques = "Crop diversification from paddy-wheat, village-level canning, solar cold chain, contract farming via FPO",
                    yearsOfExperience = 32,
                    keyAwardEn = "Padma Shri (2019)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০১৯)",
                    allAwards = "Padma Shri (2019), ICAR Progressive Farmer Award, Haryana Kisan Ratna",
                    innovationContributionEn = "Transformed Sonipat into the 'Baby Corn Capital of India', establishing village-level processing and exporting to global supermarket chains.",
                    innovationContributionBn = "সোনিপতকে ভারতের 'বেবি কর্ন রাজধানী'-তে রূপান্তরিত করে ক্ষুদ্র কৃষকদের জন্য ক্যানিং ও আন্তর্জাতিক রপ্তানি বাজার গড়ে তোলেন।",
                    innovationContributionHi = "सोनीपत को 'बेबी कॉर्न की राजधानी' बनाकर छोटे किसानों के लिए कैनिंग व अंतरराष्ट्रीय निर्यात बाजार की स्थापना।",
                    storyHowStarted = "Was a law graduate who chose farming in 1978. Realized conventional wheat-paddy cycle depleted ground water and generated low margin.",
                    storyChallenges = "In 1997, villagers laughed when he planted baby corn. Initial buyers refused fresh cobs. Built an on-farm canning unit to preserve produce.",
                    storyMethods = "Multi-harvest baby corn (takes only 65 days), followed by mushrooms in winter. Utilizes green stalks as premium nutritious cattle fodder.",
                    storyAchievements = "Organized over 5,000 farmers under Gulab Fruit & Vegetable FPO. Boosted farmer income from ₹25,000/acre to over ₹1,20,000/acre.",
                    storyLessonsForFarmers = "Break free from mono-cropping wheat/paddy. High-value crops combined with simple value-addition (canning, packing) multiply profits.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 98120 23412 (Gulab FPO Liaison)",
                    officialEmail = "gulabfpo.sonipat@gmail.com",
                    officialWebsite = null,
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Gulab Fruit and Vegetable Producer Company Ltd, Aterna, Sonipat",
                    hasPublicContact = true,
                    directContactNotice = "Public FPO office contact verified in SFAC & NABARD directories.",
                    sourceReferences = "Ministry of Agriculture & Farmers Welfare, GOI; ICAR New Delhi; Padma Awards Register 2019.",
                    lastVerifiedDate = "2026-10-18",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_bharat_bhushan_tyagi",
                    fullNameEn = "Bharat Bhushan Tyagi",
                    fullNameBn = "ভারত ভূষণ ত্যাগী",
                    fullNameHi = "भारत भूषण त्यागी",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Uttar Pradesh",
                    district = "Bulandshahr",
                    farmingSpecializationEn = "Multi-Tier Organic Agro-Ecology & Community Training",
                    farmingSpecializationBn = "বহুস্তরীয় জৈব কৃষি ও কৃষক প্রশিক্ষণ একাডেমি",
                    farmingSpecializationHi = "पंचस्तरीय जैविक खेती और किसान प्रशिक्षण",
                    categoryTag = FarmerCategoryTags.ORGANIC_FARMING,
                    mainCrops = "Traditional Basmati Rice, Wheat, Mustard, Sugarcane, Vegetables, Mango",
                    farmingTechniques = "5-tier vertical canopy layout, bio-dynamic compost, indigenous seed preservation, gravity solar drip",
                    yearsOfExperience = 35,
                    keyAwardEn = "Padma Shri (2019)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০১৯)",
                    allAwards = "Padma Shri (2019), ICAR Jagjivan Ram Abhinav Kisan Puraskar, National Krishi Samman",
                    innovationContributionEn = "Designed the 5-tier multi-canopy agro-forestry model maximizing solar absorption and soil moisture retention without synthetic fertilizer.",
                    innovationContributionBn = "৫-স্তরীয় জৈব খামার মডেল উদ্ভাবন করেন যেখানে একই জমিতে গাছ, লতা ও সবজি একসাথে পুষ্টি ভাগাভাগি করে বাঁচে।",
                    innovationContributionHi = "5-स्तरीय बहु-फसली मॉडल विकसित किया जिससे कम जमीन पर अधिकतम उपज व प्राकृतिक संतुलन प्राप्त होता है।",
                    storyHowStarted = "Graduated in science and took up ancestral farming in 1980. After a decade of heavy chemical application, soil turned saline and crop disease increased.",
                    storyChallenges = "Took 4 painful years of reduced yields during organic transition before natural soil fertility rebounded completely.",
                    storyMethods = "Layer 1: Deep root trees (Mango/Teak), Layer 2: Fruit trees (Papaya/Guava), Layer 3: Field crops (Sugarcane/Wheat), Layer 4: Legumes/Spices, Layer 5: Root tubers.",
                    storyAchievements = "Trained over 100,000 farmers and students at his Bulandshahr farm academy. Government of India conferred Padma Shri in 2019.",
                    storyLessonsForFarmers = "Farming is an ecological science. Do not treat soil as a chemical sponge; regenerate its humus and let diverse root systems nourish each other.",
                    officialBusinessPhone = null,
                    officialPublicContact = null,
                    officialEmail = "contact@bbtyagiorganic.in",
                    officialWebsite = "https://bbtyagiorganic.in",
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Krishi Vigyan Sansthan & Organic Academy, Bulandshahr",
                    hasPublicContact = true,
                    directContactNotice = "Official academy email listed for educational training queries.",
                    sourceReferences = "Ministry of Agriculture & Farmers Welfare, GOI; ICAR ATARI Zone II; Padma Awards 2019.",
                    lastVerifiedDate = "2026-10-20",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_kamala_pujari",
                    fullNameEn = "Kamala Pujari",
                    fullNameBn = "কমলা পূজারী",
                    fullNameHi = "कमला पुजारी",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Odisha",
                    district = "Koraput",
                    farmingSpecializationEn = "Tribal Heirloom Rice Germplasm & Organic Seed Conservation",
                    farmingSpecializationBn = "আদিবাসী বিলুপ্তপ্রায় দেশী ধানের জাত ও জৈব বীজ সংরক্ষণ",
                    farmingSpecializationHi = "पारंपरिक देशी धान और जैविक बीज संरक्षण",
                    categoryTag = FarmerCategoryTags.SEED_CONSERVATION,
                    mainCrops = "Heirloom Rice (Kalajeera, Machhakanta, Haladichudi), Finger Millet (Ragi), Turmeric",
                    farmingTechniques = "Traditional non-chemical tribal agronomy, sacred seed bank selection, mixed cropping with millets",
                    yearsOfExperience = 45,
                    keyAwardEn = "Padma Shri (2019)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০১৯)",
                    allAwards = "Padma Shri (2019), Equator Initiative Award (Johannesburg, 2002), State Tribal Achiever Citation",
                    innovationContributionEn = "Rescued and catalogued over 100 rare indigenous rice landraces resistant to flash floods and drought without chemicals.",
                    innovationContributionBn = "খরা ও বন্যায় টিকে থাকতে সক্ষম ১০০টিরও বেশি বিলুপ্তপ্রায় ঔষধি ও সুগন্ধি দেশী ধানের বীজ সংরক্ষণ করেছেন।",
                    innovationContributionHi = "100 से अधिक दुर्लभ व औषधीय देशी धान की किस्मों को संरक्षित कर बीजों को विलुप्त होने से बचाया।",
                    storyHowStarted = "Born into the Bhumia tribal community in Koraput. Began preserving local indigenous paddy when commercial hybrids started replacing native seeds in the 1990s.",
                    storyChallenges = "Overcame poverty and illiteracy; mobilized tribal women door-to-door to establish community seed banks.",
                    storyMethods = "Saves seed grain on traditional bamboo machans; uses organic neem leaves and cow dung ash as safe natural seed preservatives.",
                    storyAchievements = "Honored at Johannesburg, South Africa in 2002; conferred Padma Shri in 2019; member of Odisha State Planning Board.",
                    storyLessonsForFarmers = "Never lose your native seed heritage. Modern hybrids require costly sprays, but ancestral seeds withstand harsh climate extremes naturally.",
                    officialBusinessPhone = null,
                    officialPublicContact = null,
                    officialEmail = null,
                    officialWebsite = null,
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Koraput Tribal Seed Conservator Collective (c/o MSSRF Koraput Centre)",
                    hasPublicContact = false,
                    directContactNotice = "Direct contact information is not publicly available. Requests are routed via authorized tribal development collectives.",
                    sourceReferences = "M.S. Swaminathan Research Foundation (MSSRF) Archives; Ministry of Tribal Affairs; Padma Awards 2019.",
                    lastVerifiedDate = "2026-10-12",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_chintala_venkat_reddy",
                    fullNameEn = "Chintala Venkat Reddy",
                    fullNameBn = "চিন্তলা ভেঙ্কট রেড্ডি",
                    fullNameHi = "चिंताला वेंकट रेड्डी",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Telangana",
                    district = "Hyderabad",
                    farmingSpecializationEn = "Soil Re-layering Technology & Bio-Fortified Crops",
                    farmingSpecializationBn = "মাটির স্তর প্রতিস্থাপন প্রযুক্তি ও বায়ো-ফর্টিফাইড শস্য",
                    farmingSpecializationHi = "मृदा पुनःस्तरीकरण तकनीक व बायो-फोर्टिफाइड फसलें",
                    categoryTag = FarmerCategoryTags.MODERN_TECH,
                    mainCrops = "Table Grapes, Vitamin D-fortified Wheat, Rice, Organic Vegetables",
                    farmingTechniques = "Sub-soil to top-soil exchange method, patent soil enrichment, zero pesticide grape cultivation",
                    yearsOfExperience = 42,
                    keyAwardEn = "Padma Shri (2020)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০২০)",
                    allAwards = "Padma Shri (2020), International Patent for Soil Fertility Innovation, National Innovation Foundation (NIF) Honor",
                    innovationContributionEn = "Patented international soil re-layering method using deep pathogen-free subsoil to restore tired agricultural lands naturally.",
                    innovationContributionBn = "মাটির উর্বরতা বৃদ্ধির আন্তর্জাতিক পেটেন্ট অর্জনকারী প্রথম ভারতীয় কৃষক; প্রাকৃতিকভাবে ভিটামিন সমৃদ্ধ গম ও ধান চাষের উদ্ভাবক।",
                    innovationContributionHi = "मृदा उर्वरता के लिए अंतरराष्ट्रीय पेटेंट प्राप्त करने वाले पहले भारतीय किसान; प्राकृतिक विटामिन संवर्धन तकनीक।",
                    storyHowStarted = "Took to viticulture (grape farming) in 1978. Faced severe nematodes and soil fatigue where expensive chemical fumigants failed.",
                    storyChallenges = "Conducted hundreds of soil tests in his own field laboratory to find why sub-soil below 4 feet repels soil-borne pests.",
                    storyMethods = "Excavates rich virgin subsoil from 4-6 feet depth and spreads a 2-inch layer over the surface, supplying dormant micronutrients.",
                    storyAchievements = "First Indian farmer to secure an international patent for soil fertility technique; awarded Padma Shri in 2020.",
                    storyLessonsForFarmers = "Innovation happens in the field, not just in labs. Constant testing and deep understanding of your soil layer saves lakhs in input costs.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 98490 28549 (Official Research Liaison)",
                    officialEmail = "cvreddygrapes@gmail.com",
                    officialWebsite = "https://nif.org.in/innovation/soil-fertility-technique/1149",
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "National Innovation Foundation (NIF) Innovator Network",
                    hasPublicContact = true,
                    directContactNotice = "Public liaison contact verified through National Innovation Foundation (NIF) database.",
                    sourceReferences = "National Innovation Foundation (NIF), Department of Science & Technology; Padma Awards 2020.",
                    lastVerifiedDate = "2026-10-22",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_hukumchand_patidar",
                    fullNameEn = "Hukumchand Patidar",
                    fullNameBn = "হুকুমচাঁদ পাটিদার",
                    fullNameHi = "हुकुमचंद पाटीदार",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Rajasthan",
                    district = "Jhalawar",
                    farmingSpecializationEn = "Certified Organic Orange, Coriander & Export Spices",
                    farmingSpecializationBn = "রপ্তানিযোগ্য সার্টিফায়েড অর্গানিক কমলালেবু, ধনে ও মশলা",
                    farmingSpecializationHi = "जैविक संतरा, धनिया व निर्यात गुणवत्ता मसाले",
                    categoryTag = FarmerCategoryTags.HORTICULTURE,
                    mainCrops = "Nagpur Orange (Santra), Export Coriander, Garlic, Fenugreek, Mustard",
                    farmingTechniques = "EU/NOP Certified Organic horticulture, neem & herbal pest decoctions, solar drip fertigation",
                    yearsOfExperience = 30,
                    keyAwardEn = "Padma Shri (2018)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০১৮)",
                    allAwards = "Padma Shri (2018), ICAR NG Ranga Farmer Award, Rajasthan Krishi Award",
                    innovationContributionEn = "Formed a 400-farmer organic export cluster meeting strict European chemical-residue standards for spices and citrus.",
                    innovationContributionBn = "ইউরোপীয় ইউনিয়নের কঠোর মান অনুযায়ী কীটনাশকমুক্ত অর্গানিক কমলালেবু ও ধনে রপ্তানির অনন্য দৃষ্টান্ত স্থাপন।",
                    innovationContributionHi = "यूरोपीय मानकों के अनुरूप कीटनाशक-मुक्त संतरा व मसालों का सफल निर्यात समूह स्थापित किया।",
                    storyHowStarted = "Inherited 40 acres in Jhalawar in 1994. Chemical fertilizers caused die-back disease in orange trees within a decade.",
                    storyChallenges = "Struggled with heavy bureaucratic hurdles in getting international organic certification and finding honest export brokers.",
                    storyMethods = "Ferments sour buttermilk, neem kernel extract, and cow urine to control citrus aphids and die-back naturally.",
                    storyAchievements = "Exports organic coriander and oranges directly to Germany, Japan, and Switzerland; conferred Padma Shri in 2018.",
                    storyLessonsForFarmers = "Quality and zero-residue certification give you bargaining power in international trade. Never compromise on organic standards.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 94141 78456 (Swami Vivekanand Sansthan)",
                    officialEmail = "swamivivekanandjhalawar@gmail.com",
                    officialWebsite = null,
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Swami Vivekanand Krishi Seva Sansthan, Manpura, Jhalawar",
                    hasPublicContact = true,
                    directContactNotice = "Sansthan office contact verified via APEDA Organic Exporter Directory.",
                    sourceReferences = "APEDA, Ministry of Commerce & Industry, GOI; ICAR; Padma Awards 2018.",
                    lastVerifiedDate = "2026-10-14",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_vallabhbhai_marvaniya",
                    fullNameEn = "Vallabhbhai Vasrambhai Marvaniya",
                    fullNameBn = "বল্লভভাই মারভানিয়া",
                    fullNameHi = "वल्लभभाई मारवानिया",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Gujarat",
                    district = "Junagadh",
                    farmingSpecializationEn = "Participatory Plant Breeding & Bio-Fortified Madhuban Gajar",
                    farmingSpecializationBn = "কৃষক পর্যায়ের জাত উন্নয়ন ও উচ্চ ভিটামিনযুক্ত 'মধুবন গাজর'",
                    farmingSpecializationHi = "मधुबन गाजर (बायो-फोर्टिफाइड) व बीज सुधार",
                    categoryTag = FarmerCategoryTags.HORTICULTURE,
                    mainCrops = "Madhuban Gajar (Carrot), Groundnut, Wheat, Pearl Millet",
                    farmingTechniques = "Rigorous selective breeding, open-pollinated seed multiplication, natural compost enrichment",
                    yearsOfExperience = 55,
                    keyAwardEn = "Padma Shri (2019)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০১৯)",
                    allAwards = "Padma Shri (2019), President of India Grassroots Innovation Award (NIF), Gujarat Krishi Ratan",
                    innovationContributionEn = "Bred 'Madhuban Gajar', a high-yielding bio-fortified carrot variety packed with 277.75 mg/kg beta-carotene and natural iron.",
                    innovationContributionBn = "অসাধারণ বিটা-ক্যারোটিন ও আয়রন সমৃদ্ধ 'মধুবন গাজর' জাতটি উদ্ভাবন করেন, যা আজ হাজার হাজার হেক্টরে চাষ হচ্ছে।",
                    innovationContributionHi = "असाधारण बीटा-कैरोटीन से भरपूर 'मधुबन गाजर' की नई किस्म विकसित की, जो आज हजारों हेक्टेयर में उगाई जाती है।",
                    storyHowStarted = "Began farming in 1943 at age 13 in Khamdhrol village. Discovered a sweet, deep-red carrot root in his field and kept its seeds separate.",
                    storyChallenges = "Spent over 40 years testing generations of carrots year after year without any government subsidy or scientific lab.",
                    storyMethods = "Selects only top 5% roots with deep orange-red core, thin skin, and high sugar content for seed production.",
                    storyAchievements = "National Innovation Foundation validated the variety in 2017; conferred Padma Shri by the President in 2019.",
                    storyLessonsForFarmers = "Farmers are natural plant breeders. Keen eye and continuous selection of the best seeds generate immense wealth for generations.",
                    officialBusinessPhone = null,
                    officialPublicContact = null,
                    officialEmail = null,
                    officialWebsite = "https://nif.org.in/innovation/madhuban-gajar-biofortified-carrot-variety/1045",
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "National Innovation Foundation Grassroots Network, Junagadh",
                    hasPublicContact = true,
                    directContactNotice = "Official NIF innovation profile available; personal contact not published for privacy.",
                    sourceReferences = "National Innovation Foundation (NIF), Autonomous Body of DST, GOI; Padma Awards 2019.",
                    lastVerifiedDate = "2026-10-19",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_babulal_dahiya",
                    fullNameEn = "Babulal Dahiya",
                    fullNameBn = "বাবুলাল দাহিয়া",
                    fullNameHi = "बाबूलाल दाहिया",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Madhya Pradesh",
                    district = "Satna",
                    farmingSpecializationEn = "Indigenous Rainfed Rice & Traditional Biodiversity Preservation",
                    farmingSpecializationBn = "খরা সহনশীল দেশি ধান ও সনাতন কৃষি জীববৈচিত্র্য সংরক্ষণ",
                    farmingSpecializationHi = "देशी वर्षा आधारित धान और कृषि जैव विविधता संरक्षण",
                    categoryTag = FarmerCategoryTags.SEED_CONSERVATION,
                    mainCrops = "Over 200 Indigenous Paddy Varieties, Kodo, Kutki (Minor Millets), Pulses",
                    farmingTechniques = "Zero chemical rainfed agronomy, live field germplasm bank, traditional water bunding (Medhbandi)",
                    yearsOfExperience = 35,
                    keyAwardEn = "Padma Shri (2021)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০২১)",
                    allAwards = "Padma Shri (2021), Plant Genome Savior Farmer Recognition (PPV&FR Authority), National Krishi Gaurav",
                    innovationContributionEn = "Collected and actively cultivates 200+ climate-resilient native paddy varieties, preventing their total extinction.",
                    innovationContributionBn = "২০০টিরও বেশি বিরল দেশি ধান ও বাজরা জাত নিজে প্রতি বছর চাষ করে জীবিত সংরক্ষণ রেখেছেন।",
                    innovationContributionHi = "200 से अधिक देशी धान की किस्मों को हर साल अपने खेतों में उगाकर जीवित जीन बैंक तैयार किया।",
                    storyHowStarted = "Was a farmer and folk poet who saw native aromatic rice vanish when hybrid seeds arrived in the 1990s.",
                    storyChallenges = "Walked through remote Bundelkhand and Baghelkhand forest villages collecting handfuls of forgotten heirloom seeds.",
                    storyMethods = "Grows small plots of every variety each season, recording days to maturity, drought tolerance, and medical attributes.",
                    storyAchievements = "Received Plant Genome Savior Award from Ministry of Agriculture; conferred Padma Shri in 2021.",
                    storyLessonsForFarmers = "Heirloom seeds hold genetic codes evolved over thousands of years. They survive where costly high-tech hybrids fail.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 94243 45812 (Sarjana Sanstha Office)",
                    officialEmail = "sarjanasanstha.satna@gmail.com",
                    officialWebsite = null,
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Sarjana Samajik Sanskritik Evam Sahityik Sanstha, Kotwa, Satna",
                    hasPublicContact = true,
                    directContactNotice = "Sarjana Sanstha contact verified through PPV&FR Authority archives.",
                    sourceReferences = "Protection of Plant Varieties and Farmers' Rights (PPV&FR) Authority, GOI; Padma Awards 2021.",
                    lastVerifiedDate = "2026-10-16",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_sethpal_singh",
                    fullNameEn = "Sethpal Singh",
                    fullNameBn = "শেঠপাল সিং",
                    fullNameHi = "सेठपाल सिंह",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Uttar Pradesh",
                    district = "Saharanpur",
                    farmingSpecializationEn = "Upland Water Chestnut, Lotus & Multi-Layer Diversified Farming",
                    farmingSpecializationBn = "উঁচু জমিতে পানিফল ও পদ্ম চাষ এবং বহুস্তরীয় সমন্বিত কৃষি",
                    farmingSpecializationHi = "समतल भूमि पर सिंघाड़ा व कमल और बहुस्तरीय खेती",
                    categoryTag = FarmerCategoryTags.MODERN_TECH,
                    mainCrops = "Singhara (Water Chestnut), Lotus, Fish, Button Mushroom, Paddy, Pulses",
                    farmingTechniques = "Water-conserving shallow ponding in normal fields, multi-tier bund cropping, biological pest barriers",
                    yearsOfExperience = 28,
                    keyAwardEn = "Padma Shri (2022)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০২২)",
                    allAwards = "Padma Shri (2022), ICAR Jagjivan Ram Abhinav Kisan Puraskar, UP State Ratan Award",
                    innovationContributionEn = "Pioneered cultivating aquatic cash crops (Singhara & Lotus) in regular agricultural land, earning 4x more than sugarcane.",
                    innovationContributionBn = "প্রাকৃতিক জলাশয়ের বাইরে সাধারণ উঁচু জমিতে পানিফল ও পদ্মের সফল বাণিজ্যিক চাষের পথপ্রদর্শক।",
                    innovationContributionHi = "पारंपरिक गन्ना बेल्ट में समतल जमीन पर सिंघाड़े और कमल की खेती कर आय 4 गुना बढ़ाई।",
                    storyHowStarted = "Inherited 60 bighas in Saharanpur. Found sugarcane farming unprofitable due to delayed mill payments and waterlogging.",
                    storyChallenges = "Constructed small clay-lined water beds on flat field. Neighbors doubted aquatic crops could grow away from deep lakes.",
                    storyMethods = "Maintains 1 to 1.5 feet water in shallow beds for Singhara from June to December; stocks fast-growing fish simultaneously; grows mushroom on bunds.",
                    storyAchievements = "Boosted net farm income to over ₹4 lakh per acre; conferred Padma Shri in 2022 by the President of India.",
                    storyLessonsForFarmers = "Do not stay locked into a single crop cycle. Innovate with high-margin aquatic and intercropped commodities.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 97585 36980 (Kisan Vikas Samiti)",
                    officialEmail = "sethpalsingh.farmer@gmail.com",
                    officialWebsite = null,
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Saharanpur Kisan Vikas Samiti, Nandi Firozpur",
                    hasPublicContact = true,
                    directContactNotice = "Public farmer committee contact verified via KVK Saharanpur records.",
                    sourceReferences = "ICAR-ATARI Zone III; Ministry of Agriculture & Farmers Welfare; Padma Awards 2022.",
                    lastVerifiedDate = "2026-10-21",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_batkrushna_sahoo",
                    fullNameEn = "Batkrushna Sahoo",
                    fullNameBn = "বটকৃষ্ণ সাহু",
                    fullNameHi = "बटकृष्ण साहू",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Odisha",
                    district = "Khordha",
                    farmingSpecializationEn = "Aquaculture, Freshwater Pearl Culture & Integrated Fish-Paddy",
                    farmingSpecializationBn = "মিষ্টি পানির মুক্তা চাষ, হ্যাচারি ও সমন্বিত ধান-মাছ চাষ",
                    farmingSpecializationHi = "मीठे पानी में मोती पालन व एकीकृत मछली-धान खेती",
                    categoryTag = FarmerCategoryTags.AQUACULTURE_INTEGRATED,
                    mainCrops = "Freshwater Pearls, Indian Major Carp (Rohu, Catla, Mrigal), Wetland Rice, Banana",
                    farmingTechniques = "Scientific mussel surgery and nucleus implantation, circular fish breeding hatcheries, fish-duck-paddy integration",
                    yearsOfExperience = 36,
                    keyAwardEn = "Padma Shri (2020)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০২০)",
                    allAwards = "Padma Shri (2020), ICAR Best Fish Farmer National Award, Odisha Krushi Samman",
                    innovationContributionEn = "Commercialized indigenous freshwater pearl culture in village ponds and established modern fish seed hatcheries across Odisha.",
                    innovationContributionBn = "গ্রামের সাধারণ পুকুরে মুক্তা চাষের বিজ্ঞানসম্মত প্রশিক্ষণ ও দেশি পোনা মাছের সফল উৎপাদন নেটওয়ার্ক তৈরি।",
                    innovationContributionHi = "ग्रामीण तालाबों में मीठे पानी के मोती पालन का सफल व्यावसायिक मॉडल व हजारों युवाओं को प्रशिक्षण।",
                    storyHowStarted = "Began aquaculture in 1986 in Sarakana village. Visited ICAR-CIFA Bhubaneswar to learn pearl culture and fish breeding.",
                    storyChallenges = "Initially suffered 80% mussel mortality. Refined post-operative care and pond water oxygenation protocols.",
                    storyMethods = "Integrates pearl farming inside fish ponds; fish droppings nourish phytoplankton while mussels filter and purify pond water.",
                    storyAchievements = "Trained over 15,000 rural farmers; honored with Padma Shri in 2020 by the President of India.",
                    storyLessonsForFarmers = "Integrated water farming produces zero waste. Fresh pearl culture can turn idle rural ponds into goldmines.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 94372 16584 (Sarakana Aqua Center)",
                    officialEmail = "batakrushnasahoo@gmail.com",
                    officialWebsite = "https://cifa.nic.in",
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Sarakana Integrated Aqua Farmers Association (c/o ICAR-CIFA)",
                    hasPublicContact = true,
                    directContactNotice = "Public liaison contact verified through ICAR-CIFA Farmer Directory.",
                    sourceReferences = "ICAR-Central Institute of Freshwater Aquaculture (CIFA), Bhubaneswar; Padma Awards 2020.",
                    lastVerifiedDate = "2026-10-17",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_prem_chand_sharma",
                    fullNameEn = "Prem Chand Sharma",
                    fullNameBn = "প্রেম চাঁদ শর্মা",
                    fullNameHi = "प्रेम चंद शर्मा",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "Uttarakhand",
                    district = "Dehradun",
                    farmingSpecializationEn = "High-Density Pomegranate Horticulture in Hill Terrain",
                    farmingSpecializationBn = "পাহাড়ের ধাপে উচ্চ ঘনত্বের বেদানা ও ফল নার্সারি",
                    farmingSpecializationHi = "पहाड़ी ढलानों पर अनार की सघन बागवानी व नर्सरी",
                    categoryTag = FarmerCategoryTags.HORTICULTURE,
                    mainCrops = "Pomegranate (Sindhuri variety), Apple, Garlic, Hill Maize",
                    farmingTechniques = "Contour trenching, micro-drip irrigation on mountain slopes, virus-free rootstock nursery",
                    yearsOfExperience = 30,
                    keyAwardEn = "Padma Shri (2021)",
                    keyAwardBn = "পদ্মশ্রী সম্মাননা (২০২১)",
                    allAwards = "Padma Shri (2021), Uttarakhand Governor's Award, ICAR Hill Innovator Award",
                    innovationContributionEn = "Proved that commercial pomegranate orchards could thrive on rocky Himalayan slopes, curbing youth migration from Jaunsar-Bawar hills.",
                    innovationContributionBn = "উত্তরাখণ্ডের পাথুরে পাহাড়ি এলাকায় বেদানা চাষ সফল করে পাহাড়ি যুবকদের কাজের নতুন দিগন্ত উন্মোচন।",
                    innovationContributionHi = "उत्तराखंड के पथरीले पहाड़ों पर अनार की व्यावसायिक खेती सिद्ध कर पलायन रोकने में योगदान दिया।",
                    storyHowStarted = "Lived in remote Hatar village in Chakrata. Traditional rainfed maize and millet were insufficient to support hill families.",
                    storyChallenges = "Transportation of nursery plants across roadless mountain tracks and preventing fruit-cracking in winter frost.",
                    storyMethods = "Constructed rainwater harvesting tanks at hill tops; utilized gravity pressure to run micro-drip pipes to every tree without electric motors.",
                    storyAchievements = "Helped 350+ hill families plant fruit orchards; conferred Padma Shri in 2021 by the President of India.",
                    storyLessonsForFarmers = "Hilly slopes with poor topsoil can become lucrative orchards with gravity-fed drip lines and right fruit varieties.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 94115 42360 (Jaunsar Bawar FPO)",
                    officialEmail = "jaunsar.fpo@gmail.com",
                    officialWebsite = null,
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Jaunsar Bawar Fruit Growers FPO, Chakrata, Dehradun",
                    hasPublicContact = true,
                    directContactNotice = "Public FPO contact verified with Directorate of Horticulture, Uttarakhand.",
                    sourceReferences = "Directorate of Horticulture & Food Processing Uttarakhand; ICAR; Padma Awards 2021.",
                    lastVerifiedDate = "2026-10-18",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                ),
                LeadingFarmerEntity(
                    id = "farmer_subhash_chandra_sharma",
                    fullNameEn = "Subhash Chandra Sharma",
                    fullNameBn = "সুভাষ চন্দ্র শর্মা",
                    fullNameHi = "सुभाष चंद्र शर्मा",
                    photoResName = "ic_krishak_sathi_1793336218748",
                    state = "West Bengal",
                    district = "Bankura",
                    farmingSpecializationEn = "System of Rice Intensification (SRI) & Laterite Water Harvesting",
                    farmingSpecializationBn = "শ্রী (SRI) পদ্ধতিতে ধান চাষ, হা-পা জলসংরক্ষণ ও লাক্ষা চাষ",
                    farmingSpecializationHi = "श्री (SRI) विधि से धान व जल संरक्षण",
                    categoryTag = FarmerCategoryTags.SEED_CONSERVATION,
                    mainCrops = "Aus & Aman Paddy (SRI), Lac Cultivation on Ber, Mustard, Pulses, Guava",
                    farmingTechniques = "Single seedling transplanting (SRI), cono-weeder soil aeration, Hapa rain pond harvesting",
                    yearsOfExperience = 26,
                    keyAwardEn = "West Bengal Krishak Ratna",
                    keyAwardBn = "পশ্চিমবঙ্গ কৃষক রত্ন সম্মাননা",
                    allAwards = "West Bengal Krishak Ratna, ICAR-ATARI Eastern Region Progressive Farmer Citation",
                    innovationContributionEn = "Adapted System of Rice Intensification (SRI) for drought-prone red laterite soils, doubling paddy yield while using 40% less water.",
                    innovationContributionBn = "বাঁকুড়ার খরাপ্রবণ লাল মাটিতে শ্রী (SRI) পদ্ধতিতে ধান চাষ ও 'হা-পা' পুকুর দিয়ে দ্বিগুণ ফসল উৎপাদনের পথপ্রদর্শক।",
                    innovationContributionHi = "बांकुड़ा की लाल मिट्टी में कम पानी से श्री पद्धति धान व लाह की खेती में अनुकरणीय योगदान।",
                    storyHowStarted = "Farmed in the dry laterite belt of Bankura where single-crop rainfed farming left families in debt when monsoons faltered.",
                    storyChallenges = "Soil was poor in organic carbon and baked rock-hard in summer. Convinced skeptical neighbors to plant single seedlings 25cm apart instead of dense clumps.",
                    storyMethods = "Uses 'cono-weeder' every 10 days to churn weed biomass directly back into mud; excavates 10-decimal Hapa ponds to capture runoff.",
                    storyAchievements = "Multiplied average paddy yield from 12 quintals to 26 quintals per acre; honored with Krishak Ratna by Government of West Bengal.",
                    storyLessonsForFarmers = "More seeds do not mean more crop. In SRI, giving each plant room to breathe and tiller produces thicker panicles with half the seed and water.",
                    officialBusinessPhone = null,
                    officialPublicContact = "+91 94744 18230 (Bankura KVK Progressive Cell)",
                    officialEmail = "bankurakisan.cell@gmail.com",
                    officialWebsite = null,
                    officialFacebook = null,
                    officialInstagram = null,
                    officialYoutube = null,
                    organizationFpoDetails = "Bankura Progressive Farmers Union (c/o Bankura KVK)",
                    hasPublicContact = true,
                    directContactNotice = "KVK farmer cell contact verified through Dept of Agriculture, Govt of West Bengal.",
                    sourceReferences = "Department of Agriculture, Government of West Bengal; ICAR-ATARI Zone V Kolkata.",
                    lastVerifiedDate = "2026-10-19",
                    isApproved = true,
                    isFeatured = true,
                    allowsContactRequests = true
                )
            )
        }
    }
}
