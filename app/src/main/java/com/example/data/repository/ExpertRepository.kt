package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ConsultationAppointmentEntity
import com.example.data.local.entity.ConsultationMessageEntity
import com.example.data.local.entity.ExpertAuditLogEntity
import com.example.data.local.entity.ExpertCategories
import com.example.data.local.entity.ExpertEntity
import com.example.data.local.entity.ExpertReviewEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ExpertRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    val dao = db.expertConsultationDao()

    val verifiedExperts: Flow<List<ExpertEntity>> = dao.getVerifiedExperts()
    val allExperts: Flow<List<ExpertEntity>> = dao.getAllExperts()

    fun getAppointmentsForFarmer(farmerId: String): Flow<List<ConsultationAppointmentEntity>> {
        return dao.getAppointmentsForFarmer(farmerId)
    }

    fun getAppointmentsForExpert(expertId: String): Flow<List<ConsultationAppointmentEntity>> {
        return dao.getAppointmentsForExpert(expertId)
    }

    fun observeMessages(expertId: String, farmerId: String): Flow<List<ConsultationMessageEntity>> {
        return dao.getMessages(expertId, farmerId)
    }

    fun getReviews(expertId: String): Flow<List<ExpertReviewEntity>> {
        return dao.getReviewsForExpert(expertId)
    }

    fun getAuditLogs(): Flow<List<ExpertAuditLogEntity>> {
        return dao.getAuditLogs()
    }

    suspend fun bookAppointment(
        appointment: ConsultationAppointmentEntity,
        currentLanguage: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.BENGALI
    ): Result<Unit> {
        val conflict = dao.checkDoubleBooking(
            expertId = appointment.expertId,
            date = appointment.scheduledDate,
            timeSlot = appointment.scheduledTimeSlot
        )
        if (conflict != null) {
            val conflictMsg = when (currentLanguage) {
                com.example.data.model.AppLanguage.BENGALI -> "এই সময়সূচীতে বিশেষজ্ঞ অন্য কৃষকের সাথে পরামর্শে ব্যস্ত আছেন। অনুগ্রহ করে অন্য সময় বেছে নিন।"
                com.example.data.model.AppLanguage.HINDI -> "इस समय स्लॉट में विशेषज्ञ अन्य किसान के साथ परामर्श में व्यस्त हैं। कृपया कोई दूसरा समय चुनें।"
                com.example.data.model.AppLanguage.ENGLISH -> "This expert is already booked for the selected time slot. Please choose another slot."
            }
            return Result.failure(IllegalStateException(conflictMsg))
        }
        return try {
            dao.insertAppointment(appointment)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentStatus(id: String, status: String, notes: String = "") {
        dao.updateAppointmentStatus(id, status, notes)
    }

    suspend fun sendMessage(message: ConsultationMessageEntity) {
        dao.insertMessage(message)
    }

    suspend fun submitReview(review: ExpertReviewEntity) {
        dao.insertReview(review)
    }

    suspend fun applyForExpert(expert: ExpertEntity) {
        dao.insertExpert(expert)
    }

    suspend fun verifyOrRejectExpert(
        expertId: String,
        expertName: String,
        approve: Boolean,
        reason: String,
        adminName: String
    ) {
        dao.updateVerificationStatus(expertId, approve)
        dao.insertAuditLog(
            ExpertAuditLogEntity(
                id = UUID.randomUUID().toString(),
                expertId = expertId,
                expertName = expertName,
                adminAction = if (approve) "APPROVED" else "REJECTED",
                reason = reason,
                adminName = adminName
            )
        )
    }

    suspend fun seedInitialExpertsIfEmpty() {
        if (dao.getExpertCount() > 0) return

        val initialExperts = listOf(
            ExpertEntity(
                id = "exp_01",
                name = "ড. অরুণাভ রায় (Dr. Arunabha Roy)",
                designation = "সিনিয়র উদ্ভিদ রোগতত্ত্ববিদ (Senior Plant Pathologist)",
                qualification = "Ph.D. in Plant Pathology (BCKV Mohanpur), Post-Doc (IRRI)",
                category = ExpertCategories.PLANT_DISEASE,
                yearsExperience = 16,
                registrationId = "WB-AGRI-EXP-2018-0912",
                institution = "বিধান চন্দ্র কৃষি বিশ্ববিদ্যালয় (BCKV Mohanpur)",
                languages = "বাংলা, English, हिन्दी",
                districtsServed = "জলপাইগুড়ি, কোচবিহার, মালদা, নদিয়া, বর্ধমান",
                availabilityStatus = "ONLINE_NOW",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 4.9,
                reviewCount = 142,
                photoAvatar = "👨‍🔬",
                supportedCrops = "আলু, ধান, টমেটো, বেগুন, মরিচ",
                specializedProblems = "আলুর নাবী ধসা (Late Blight), ব্যাকটিরিয়াল উইল্ট, গোড়া পচা, শীথ ব্লাইট",
                phone = "033-2587-8163",
                email = "arunabha.pathology@bckv.edu.in",
                bio = "১৬ বছর ধরে উত্তরবঙ্গ ও দক্ষিণবঙ্গের কৃষকদের ছত্রাক ও ব্যাকটিরিয়াজনিত রোগ নির্ণয় ও নির্ভুল ডোজ নির্ধারণে সহায়তা করছেন।"
            ),
            ExpertEntity(
                id = "exp_02",
                name = "ড. প্রিয়াঙ্কা সেনগুপ্ত (Dr. Priyanka Sengupta)",
                designation = "মুখ্য ফসল সুরক্ষা বিশেষজ্ঞ (Chief Crop Protection Scientist)",
                qualification = "M.Sc. (Agri) Entomology (IARI New Delhi), Ph.D. (UBKV)",
                category = ExpertCategories.CROP_PROTECTION,
                yearsExperience = 12,
                registrationId = "WB-AGRI-EXP-2020-1428",
                institution = "উত্তর বঙ্গ কৃষি বিশ্ববিদ্যালয় (UBKV Pundibari)",
                languages = "বাংলা, English, हिन्दी",
                districtsServed = "জলপাইগুড়ি, আলিপুরদুয়ার, কোচবিহার, শিলিগুড়ি",
                availabilityStatus = "ONLINE_NOW",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 4.8,
                reviewCount = 98,
                photoAvatar = "👩‍🔬",
                supportedCrops = "সবজি, ফল, চা, ধান",
                specializedProblems = "মাজরা পোকা, শোষক পোকা, লাল মাকড়, থ্রিপস ও ফল ছিদ্রকারী পোকা দমন",
                phone = "03582-270-143",
                email = "priyanka.cropprotection@ubkv.ac.in",
                bio = "সমন্বিত বালাই ব্যবস্থাপনা (IPM) বিশেষজ্ঞ। রাসায়নিক ও জৈব বালাইনাশকের নিরাপদ ব্যবহারের পথপ্রদর্শক।"
            ),
            ExpertEntity(
                id = "exp_03",
                name = "প্রফেসর সমীরণ মন্ডল (Prof. Samiran Mondal)",
                designation = "মাটি পরীক্ষা ও পুষ্টি বিশেষজ্ঞ (Principal Soil Scientist)",
                qualification = "Ph.D. in Soil Science & Agri Chemistry (Calcutta University)",
                category = ExpertCategories.SOIL_SCIENCE,
                yearsExperience = 20,
                registrationId = "WB-AGRI-EXP-2015-0451",
                institution = "কৃষি বিজ্ঞান কেন্দ্র, জলপাইগুড়ি (KVK Jalpaiguri)",
                languages = "বাংলা, English",
                districtsServed = "জলপাইগুড়ি, মালদা, দিনাজপুর, মুর্শিদাবাদ",
                availabilityStatus = "AVAILABLE_TODAY",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 4.9,
                reviewCount = 185,
                photoAvatar = "👨‍🔬",
                supportedCrops = "মাঠ ফসল, ডালশস্য, তৈলবীজ, শাকসবজি",
                specializedProblems = "মাটির অম্লতা সংশোধন, জিঙ্ক ও বোরন ঘাটতি, সয়েল হেলথ কার্ড বিশ্লেষণ",
                phone = "03561-255-089",
                email = "s.mondal@kvkjalpaiguri.org",
                bio = "উত্তরবঙ্গের অ্যাসিডিক মাটির পিএইচ মাত্রা এবং জৈব কার্বন বৃদ্ধির পরামর্শে খ্যাতনামা।"
            ),
            ExpertEntity(
                id = "exp_04",
                name = "ড. শুভাশিস ভৌমিক (Dr. Subhasis Bhowmick)",
                designation = "আলু চাষ ও বীজ প্রত্যয়ন বিশেষজ্ঞ (Potato Agronomist)",
                qualification = "M.Sc. & Ph.D. Agronomy (CPRI Shimla)",
                category = ExpertCategories.POTATO_SPECIALIST,
                yearsExperience = 15,
                registrationId = "WB-AGRI-EXP-2019-3320",
                institution = "কেন্দ্রীয় আলু গবেষণা কেন্দ্র সহযোগী বিজ্ঞানী (CPRI Associate)",
                languages = "বাংলা, English, हिन्दी",
                districtsServed = "জলপাইগুড়ি, ধূপগুড়ি, ময়নাগুড়ি, হুগলি, বাঁকুড়া",
                availabilityStatus = "ONLINE_NOW",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 5.0,
                reviewCount = 210,
                photoAvatar = "🥔",
                supportedCrops = "আলু (জ্যোতি, চন্দ্রমুখী, পোখরাজ, কুফরি সূর্য)",
                specializedProblems = "বীজ আলু শোধন, ব্লাইট প্রতিরোধ, কন্দ পচা ও ফলন দ্বিগুণ করার কৌশল",
                phone = "03563-261-204",
                email = "subhasis.potato@cpri.res.in",
                bio = "ধূপগুড়ি ও হুগলির আলু চাষিদের অন্যতম বিশ্বস্ত উপদেষ্টা। আগাম আলু রোপণ ও রোগ ব্যবস্থাপনায় অভিজ্ঞ।"
            ),
            ExpertEntity(
                id = "exp_05",
                name = "ড. দেবব্রত চক্রবর্তী (Dr. Debabrata Chakraborty)",
                designation = "চা বাগান ও চারা বিশেষজ্ঞ (Tea Cultivation & Nursery Advisor)",
                qualification = "M.Sc. Horticulture, Tea Research Association (TRA Tocklai)",
                category = ExpertCategories.TEA_GARDEN_SPECIALIST,
                yearsExperience = 18,
                registrationId = "WB-AGRI-EXP-2017-0873",
                institution = "টি রিসার্চ অ্যাসোসিয়েশন (TRA North Bengal Advisor)",
                languages = "বাংলা, English, हिन्दी, Nepali",
                districtsServed = "জলপাইগুড়ি, ডুয়ার্স, আলিপুরদুয়ার, দার্জিলিং",
                availabilityStatus = "AVAILABLE_TODAY",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 4.9,
                reviewCount = 134,
                photoAvatar = "🍵",
                supportedCrops = "ক্ষুদ্র চা বাগান (Small Tea Gardens), ক্লোনাল নার্সারি",
                specializedProblems = "টি মসকুইটো বাগ, লাল মাকড়, প্রুনিং ক্যালেন্ডার, খরা ব্যবস্থাপনা",
                phone = "03562-230-551",
                email = "debabrata.tea@tocklai.net",
                bio = "উত্তরবঙ্গের ক্ষুদ্র চা চাষিদের রাসায়নিক অবশিষ্টাংশ কমানো এবং সবুজ পাতা বেশি পাওয়ার বিজ্ঞানসম্মত পরামর্শ দেন।"
            ),
            ExpertEntity(
                id = "exp_06",
                name = "ড. মৌসুমী দাস (Dr. Moushumi Das)",
                designation = "ধান চাষ ও বীজ বিজ্ঞানী (Rice Breeder & Agronomist)",
                qualification = "Ph.D. Plant Genetics (CRRI Cuttack)",
                category = ExpertCategories.RICE_SPECIALIST,
                yearsExperience = 14,
                registrationId = "WB-AGRI-EXP-2021-5082",
                institution = "রাইস রিসার্চ স্টেশন (Chinsurah Rice Station)",
                languages = "বাংলা, English",
                districtsServed = "বর্ধমান, জলপাইগুড়ি, নদিয়া, বীরভূম",
                availabilityStatus = "ONLINE_NOW",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 4.8,
                reviewCount = 112,
                photoAvatar = "🌾",
                supportedCrops = "আমন ধান, বোরো ধান, সুগন্ধি চাল (গোবিন্দভোগ)",
                specializedProblems = "ব্লাস্ট রোগ, বাদামি গাছফড়িং (BPH), ফলস স্মাট, সঠিক ইউরিয়া স্প্লিটিং",
                phone = "033-2680-3411",
                email = "moushumi.rice@crri.gov.in",
                bio = "উচ্চ ফলনশীল ও বন্যা সহনশীল ধান চাষে বহু কৃষককে ট্রেনিং দিয়েছেন।"
            ),
            ExpertEntity(
                id = "exp_07",
                name = "ড. অমিত কুমার বিশ্বাস (Dr. Amit Kumar Biswas)",
                designation = "সার ও পুষ্টি বিশেষজ্ঞ (Plant Nutrition Specialist)",
                qualification = "Ph.D. in Agronomy & Plant Nutrition (BCKV)",
                category = ExpertCategories.PLANT_NUTRITION,
                yearsExperience = 11,
                registrationId = "WB-AGRI-EXP-2022-7719",
                institution = "ডিপার্টমেন্ট অব এগ্রিকালচার, পশ্চিমবঙ্গ সরকার",
                languages = "বাংলা, English, हिन्दी",
                districtsServed = "সমগ্র পশ্চিমবঙ্গ (All Districts)",
                availabilityStatus = "AVAILABLE_TODAY",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 4.7,
                reviewCount = 89,
                photoAvatar = "🌿",
                supportedCrops = "শাকসবজি, আলু, ধান, ভুট্টা, সরিষা",
                specializedProblems = "NPK সারের সঠিক অনুপাত, ন্যানো ইউরিয়া, জিঙ্ক ও ম্যাগনেসিয়াম ঘাটতি",
                phone = "033-2225-8941",
                email = "amit.nutrition@wb.gov.in",
                bio = "কম খরচে জমির উর্বরতা বজায় রেখে সুষম সার প্রয়োগের ক্যালকুলেশন করে দেন।"
            ),
            ExpertEntity(
                id = "exp_08",
                name = "ড. তপোময় মুখার্জী (Dr. Tapomay Mukherjee)",
                designation = "সবজি ও সংকর জাত বিশেষজ্ঞ (Vegetable Specialist)",
                qualification = "M.Sc. Horticulture (Vegetable Science, IARI)",
                category = ExpertCategories.VEGETABLE_SPECIALIST,
                yearsExperience = 13,
                registrationId = "WB-AGRI-EXP-2020-6641",
                institution = "উদ্যানপালন মহাবিদ্যালয়, পুণ্ডিবাড়ী (Faculty of Horticulture, UBKV)",
                languages = "বাংলা, English",
                districtsServed = "জলপাইগুড়ি, মালদা, মুর্শিদাবাদ, নদিয়া",
                availabilityStatus = "ONLINE_NOW",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 4.9,
                reviewCount = 127,
                photoAvatar = "🥦",
                supportedCrops = "বাঁধাকপি, ফুলকপি, টমেটো, পটল, উচ্ছে, বেগুন",
                specializedProblems = "ড্যাম্পিং অফ, ফুলকপির ক্লাব রুট, আগাম শীতকালীন সবজির নার্সারি",
                phone = "03582-270-512",
                email = "tapomay.veg@ubkv.ac.in",
                bio = "শীতকালীন ও আগাম সবজি চাষের নার্সারি তৈরির প্রধান প্রশিক্ষক।"
            ),
            ExpertEntity(
                id = "exp_09",
                name = "ডাঃ পার্থ প্রতীম সাহা (Dr. Partha Pratim Saha)",
                designation = "ভেটেরিনারি সার্জন ও পশুপালন বিশেষজ্ঞ (Veterinary Doctor)",
                qualification = "B.V.Sc & A.H., M.V.Sc. (WBUAFS Belgachia)",
                category = ExpertCategories.VETERINARY,
                yearsExperience = 9,
                registrationId = "WB-VET-COUNCIL-2021-3990",
                institution = "জেলা প্রাণিসম্পদ বিকাশ বিভাগ, পশ্চিমবঙ্গ",
                languages = "বাংলা, English, हिन्दी",
                districtsServed = "জলপাইগুড়ি, শিলিগুড়ি, আলিপুরদুয়ার",
                availabilityStatus = "ONLINE_NOW",
                consultationFee = "বিনামূল্যে (Govt Veterinary Portal)",
                isVerified = true,
                rating = 4.9,
                reviewCount = 76,
                photoAvatar = "🐄",
                supportedCrops = "গরু, ছাগল, মুরগি, হাঁস পালন",
                specializedProblems = "দুগ্ধবতী গাভীর ম্যাস্টাইটিস, কৃমি নিয়ন্ত্রণ, সুষম সাইলেজ ও খাদ্য তালিকা",
                phone = "03561-224-118",
                email = "vet.partha@wbuafs.ac.in",
                bio = "কৃষকদের খামারের পশুদের টিকা প্রদান, সুষম খাদ্য ও আকস্মিক অসুস্থতায় ২৪ ঘণ্টা সাহায্য করেন।"
            ),
            ExpertEntity(
                id = "exp_10",
                name = "ড. ইন্দ্রনীল রায়চৌধুরী (Dr. Indranil Roychowdhury)",
                designation = "জৈব কৃষি ও ভার্মিকম্পোস্ট বিশেষজ্ঞ (Organic Farming Specialist)",
                qualification = "M.Sc. Organic Agriculture, NCOF Ghaziabad Certified",
                category = ExpertCategories.ORGANIC_FARMING,
                yearsExperience = 10,
                registrationId = "WB-AGRI-EXP-2023-8109",
                institution = "জৈব কৃষি বিকাশ ট্রাস্ট ও রাজ্য সমন্বয়ক",
                languages = "বাংলা, English",
                districtsServed = "দার্জিলিং, কালিম্পং, জলপাইগুড়ি, বর্ধমান",
                availabilityStatus = "AVAILABLE_TODAY",
                consultationFee = "বিনামূল্যে (Govt Supported Free)",
                isVerified = true,
                rating = 4.8,
                reviewCount = 64,
                photoAvatar = "♻️",
                supportedCrops = "জৈব চা, জৈব সবজি, মশলাপাতি, তুলসী ও ভেষজ",
                specializedProblems = "জীবাণু সার, ট্রাইকোডার্মা ভিরিডি, পঞ্চগব্য তৈরি ও জৈব সার্টিফিকেশন",
                phone = "0354-225-8812",
                email = "indranil.organic@wb.org",
                bio = "রাসায়নিক বিষমুক্ত চাষের সফল রূপকার। কেঁচো সার ও বায়ো-ফার্টিলাইজারে বিশেষজ্ঞ।"
            )
        )

        dao.insertExperts(initialExperts)

        // Seed some initial verified farmer reviews
        val initialReviews = listOf(
            ExpertReviewEntity(
                id = "rev_01",
                expertId = "exp_04",
                farmerId = "farmer_1",
                farmerName = "অসীম বর্মন (ধূপগুড়ি)",
                rating = 5,
                reviewText = "ড. শুভাশিস স্যারের পরামর্শে গত বছর আলুর নাবী ধসা থেকে পুরো ৩ বিঘা জমি রক্ষা পেয়েছিল। সঠিক ওষুধ ও পানির পরিমাপ বলে দিয়েছিলেন।",
                crop = "আলু"
            ),
            ExpertReviewEntity(
                id = "rev_02",
                expertId = "exp_01",
                farmerId = "farmer_2",
                farmerName = "বিপুল রায় (ময়নাগুড়ি)",
                rating = 5,
                reviewText = "টমেটোর পাতা কোঁকড়ানো ও গোড়া পচা সমস্যার মাত্র ২ দিনের মাথায় রোগ মুক্ত হয়েছিল। চমৎকার ব্যাখ্যা ও যত্নবান বিশেষজ্ঞ।",
                crop = "টমেটো"
            ),
            ExpertReviewEntity(
                id = "rev_03",
                expertId = "exp_05",
                farmerId = "farmer_3",
                farmerName = "গোপাল প্রধান (মালবাজার চা চাষি)",
                rating = 5,
                reviewText = "চা গাছের লাল মাকড় দমনে একদম সঠিক জৈব স্প্রে রুটিন দিয়েছেন। পাতার কুঁড়ি অনেক সতেজ হয়েছে।",
                crop = "চা"
            )
        )

        initialReviews.forEach { dao.insertReview(it) }

        // Initial sample appointment to demonstrate double booking prevention
        dao.insertAppointment(
            ConsultationAppointmentEntity(
                id = "appt_sample_booked",
                farmerId = "demo_farmer_99",
                farmerName = "রঞ্জিত ঘোষ",
                expertId = "exp_04",
                expertName = "ড. শুভাশিস ভৌমিক",
                expertCategory = ExpertCategories.POTATO_SPECIALIST,
                cropName = "আলু (কুফরি জ্যোতি)",
                problemDescription = "বীজ আলুর দাগ ও কাটা রোগ",
                consultationMethod = "VIDEO_CALL",
                scheduledDate = "2026-11-04",
                scheduledTimeSlot = "10:00 AM - 10:30 AM",
                status = "CONFIRMED",
                fee = "বিনামূল্যে (Free)"
            )
        )

        // Seed initial audit log
        dao.insertAuditLog(
            ExpertAuditLogEntity(
                id = "audit_01",
                expertId = "exp_01",
                expertName = "ড. অরুণাভ রায়",
                adminAction = "APPROVED",
                reason = "Verified Ph.D. credentials and registration with BCKV Department of Agriculture.",
                adminName = "West Bengal Agriculture Council Admin"
            )
        )
    }
}
