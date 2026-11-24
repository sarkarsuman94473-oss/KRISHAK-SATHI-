package com.example.ui.info

import com.example.data.model.AppLanguage

data class PolicyDocumentEntry(
    val id: Int,
    val code: String,
    val titleEn: String,
    val titleBn: String,
    val titleHi: String,
    val summaryEn: String,
    val summaryBn: String,
    val summaryHi: String,
    val bodyEn: String,
    val bodyBn: String,
    val bodyHi: String,
    val requiresLegalCounselReview: Boolean = true
) {
    fun title(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> titleBn
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
    }

    fun summary(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> summaryBn
        AppLanguage.HINDI -> summaryHi
        AppLanguage.ENGLISH -> summaryEn
    }

    fun body(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bodyBn
        AppLanguage.HINDI -> bodyHi
        AppLanguage.ENGLISH -> bodyEn
    }
}

data class IndianComplianceMatrixItem(
    val id: String,
    val frameworkName: String,
    val applicability: String,
    val implementationStatus: String,
    val ownerRole: String,
    val reviewDate: String,
    val gapOrActionNote: String
)

object LegalPolicyCatalog {

    const val PLATFORM_LIABILITY_NOTICE_EN =
        "Platform Role & Statutory Notice (Draft — Pending Legal Counsel Review): KRISHAK SATHI operates as a digital agricultural information, advisory, and seller-discovery marketplace that does not collect online purchase payments or escrow funds. Mandatory statutory consumer rights, product-liability obligations, and regulatory duties under applicable Indian law are NOT waived or excluded. Farmers retain full recourse under applicable consumer protection statutes and official helplines (NCH 1915 / Cybercrime 1930)."

    const val PLATFORM_LIABILITY_NOTICE_BN =
        "প্ল্যাটফর্ম ভূমিকা ও সংবিধিবদ্ধ বিজ্ঞপ্তি (খসড়া — আইনি পর্যালোচনা বাকি): KRISHAK SATHI একটি ডিজিটাল কৃষি তথ্য, পরামর্শ ও বিক্রেতা-সংযোগ প্ল্যাটফর্ম যা অনলাইন পেমেন্ট বা অগ্রিম অর্থ সংগ্রহ করে না। ভারতীয় আইনের অধীনে প্রযোজ্য বাধ্যতামূলক ভোক্তা অধিকার ও সংবিধিবদ্ধ সুরক্ষা কোনোভাবেই বাতিল বা সীমিত করা হয় না। কৃষকেরা প্রয়োজনে NCH 1915 এবং সাইবার ক্রাইম 1930 হেল্পলাইনে অভিযোগ জানাতে পারবেন।"

    const val PLATFORM_LIABILITY_NOTICE_HI =
        "प्लेटफ़ॉर्म भूमिका एवं वैधानिक सूचना (प्रारूप — कानूनी समीक्षा लंबित): KRISHAK SATHI एक डिजिटल कृषि जानकारी, सलाह और विक्रेता-खोज मंच है जो ऑनलाइन भुगतान या अग्रिम धनराशि एकत्र नहीं करता है। भारतीय कानून के तहत लागू अनिवार्य उपभोक्ता अधिकार किसी भी रूप में समाप्त नहीं होते हैं। किसान आवश्यकता पड़ने पर NCH 1915 और साइबर क्राइम 1930 हेल्पलाइन पर सहायता ले सकते हैं।"

    fun liabilityNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> PLATFORM_LIABILITY_NOTICE_BN
        AppLanguage.HINDI -> PLATFORM_LIABILITY_NOTICE_HI
        AppLanguage.ENGLISH -> PLATFORM_LIABILITY_NOTICE_EN
    }

    val releaseBlockers: List<String> = listOf(
        "Appoint & publish real statutory Grievance Officer name, designation, email, phone & postal office address in India.",
        "Appoint & publish real Nodal Contact Person (Indian resident) for law-enforcement / regulatory coordination.",
        "Enter verified Legal Entity Name, CIN/LLPIN/Proprietorship registration, and GSTIN (if taxable threshold met).",
        "Obtain formal Indian Legal Counsel review & sign-off for all 27 policy drafts before Play Store production rollout.",
        "Verify Play Console Data Safety declaration, privacy policy HTTPS URL, and account-deletion web URL match app behavior.",
        "Verify any future statutory amendment milestones (including proposed E-Commerce amendment effective date if notified) before Jan 1, 2027."
    )

    val complianceMatrix: List<IndianComplianceMatrixItem> = listOf(
        IndianComplianceMatrixItem(
            id = "CPA_2019",
            frameworkName = "Consumer Protection Act, 2019",
            applicability = "Farmer & buyer rights, unfair trade practices, defective product disclosures",
            implementationStatus = "PARTIALLY IMPLEMENTED",
            ownerRole = "Compliance & Product Lead",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Seller disclosures, NCH 1915 link & Grievance Ticket flow live; legal counsel sign-off pending."
        ),
        IndianComplianceMatrixItem(
            id = "ECOMMERCE_2020_2026",
            frameworkName = "Consumer Protection (E-Commerce) Rules, 2020 & Proposed 2026 Amendment",
            applicability = "Marketplace entity disclosures, seller KYC, grievance officer, no deceptive ranking",
            implementationStatus = "PENDING BUSINESS KYC",
            ownerRole = "Legal & Business Owner",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Real Grievance Officer, Nodal Officer & Entity GSTIN must be entered before production release; re-verify any 2026 notified rule before Jan 1, 2027."
        ),
        IndianComplianceMatrixItem(
            id = "DPDP_2023_2025",
            frameworkName = "DPDP Act, 2023 & DPDP Rules, 2025",
            applicability = "Personal data notice, consent timestamp, correction & deletion workflows",
            implementationStatus = "TECHNICAL CONTROLS IMPLEMENTED",
            ownerRole = "Android & Security Lead",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Consent timestamp, profile edit, local data clear & deletion request logging implemented; counsel review of notice required."
        ),
        IndianComplianceMatrixItem(
            id = "IT_ACT_2000_2021",
            frameworkName = "IT Act, 2000 & Intermediary Guidelines, 2021",
            applicability = "Intermediary safe harbor duties, takedown workflow, fraud reporting & grievance SLA",
            implementationStatus = "WORKFLOW IMPLEMENTED",
            ownerRole = "Admin & Trust Lead",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Fraud reporting, seller suspension & 48h/30d ticket SLA UI live; formal Officer details pending."
        ),
        IndianComplianceMatrixItem(
            id = "DARK_PATTERNS_2023",
            frameworkName = "CCPA Guidelines for Prevention & Regulation of Dark Patterns, 2023",
            applicability = "Zero false urgency, no fake discounts, no unverified badges, no forced actions",
            implementationStatus = "IMPLEMENTED",
            ownerRole = "UI/UX & Marketplace Lead",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Verified icons strictly gated on verified records; null prices stay 'Price on request'."
        ),
        IndianComplianceMatrixItem(
            id = "LEGAL_METROLOGY",
            frameworkName = "Legal Metrology (Packaged Commodities) Rules",
            applicability = "Clear net quantity, MRP vs dealer offer & condition labels on packaged inputs",
            implementationStatus = "ADVISORY DISCLOSED",
            ownerRole = "Seller Operations",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Sellers must declare condition, net weight/capacity & invoice terms on every listing."
        ),
        IndianComplianceMatrixItem(
            id = "AGRI_INPUT_ACTS",
            frameworkName = "Fertiliser Control Order, Seeds Act & Insecticides Act Awareness",
            applicability = "Safe agricultural advisories, patch test & licensed dealer verification",
            implementationStatus = "ADVISORY DISCLOSED",
            ownerRole = "Agronomy & Compliance",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Nursery & agrochemical sections display safety & official licensing verification notes."
        ),
        IndianComplianceMatrixItem(
            id = "DGCA_DRONE_RULES",
            frameworkName = "DGCA Drone Rules (Agricultural Spraying Drones)",
            applicability = "Type-certified drones, Remote Pilot Certificate & airspace check reminders",
            implementationStatus = "IMPLEMENTED",
            ownerRole = "Machinery Catalog Lead",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Drone listings flag statutory DGCA operator & certification warnings."
        ),
        IndianComplianceMatrixItem(
            id = "CERT_IN_2022",
            frameworkName = "CERT-In Cyber Security Directions (Incident Auditability)",
            applicability = "Security logging, NTP-aligned timestamps & cyber incident reporting readiness",
            implementationStatus = "PARTIALLY IMPLEMENTED",
            ownerRole = "Security Engineer",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Immutable trust_audit_logs & Cybercrime 1930 link live; server log retention SOP pending ops."
        ),
        IndianComplianceMatrixItem(
            id = "WB_AGRI_MARKETING",
            frameworkName = "West Bengal Agricultural Produce Marketing & Mandi Norms",
            applicability = "Siliguri / North Bengal mandi price sourcing & honest non-guaranteed quotes",
            implementationStatus = "IMPLEMENTED",
            ownerRole = "Market Data Lead",
            reviewDate = "2026-11-22",
            gapOrActionNote = "AGMARKNET / e-NAM / MSP distinguished cleanly from private buyer offers."
        ),
        IndianComplianceMatrixItem(
            id = "PLAY_STORE_SAFETY",
            frameworkName = "Google Play Developer Policy & Data Safety Disclosure",
            applicability = "Zero-permission Photo Picker, least privilege, account deletion & honest claims",
            implementationStatus = "APP COMPLIANCE BUILT",
            ownerRole = "Release Manager",
            reviewDate = "2026-11-22",
            gapOrActionNote = "Zero-permission Photo Picker used; Play Console Data Safety web form pending manual upload."
        )
    )

    val all27Policies: List<PolicyDocumentEntry> = listOf(
        policy(1, "TERMS_OF_USE", "Terms of Use & Platform Access", "ব্যবহারের নিয়ম ও শর্তাবলী", "उपयोग के नियम व शर्तें",
            "Rules for accessing KRISHAK SATHI crop guides, advisories, and marketplace listings."),
        policy(2, "PRIVACY_POLICY", "Privacy Policy & Data Notice", "গোপনীয়তা নীতি", "गोपनीयता नीति",
            "How farmer profile, crop logs, and device permissions (camera/mic/location) are handled minimally."),
        policy(3, "DPDP_CONSENT", "DPDP Consent & Notice Policy", "তথ্য সম্মতি (DPDP) নীতি", "डेटा सहमति (DPDP) नीति",
            "Explicit user consent tracking, clear purpose notice, and withdrawal rights without service penalty."),
        policy(4, "DATA_RETENTION_DELETION", "Data Retention & Account Deletion Policy", "ডেটা সংরক্ষণ ও অ্যাকাউন্ট মুছে ফেলার নীতি", "डेटा संरक्षण व खाता विलोपन नीति",
            "Right to clear local app session records and request full cloud account erasure."),
        policy(5, "COOKIES_TELEMETRY", "Diagnostics & Telemetry Policy", "অ্যাপ ডায়াগনস্টিকস ও টেলিমেট্রি নীতি", "ऐप डायग्नोस्टिक्स व टेलीमेट्री नीति",
            "No invasive cross-app advertising trackers; only crash/session stability diagnostics."),
        policy(6, "MARKETPLACE_INTERMEDIARY", "Marketplace Role & No-Escrow Payment Policy", "মার্কেটপ্লেস ভূমিকা ও সরাসরি লেনদেন নীতি", "मार्केटप्लेस भूमिका व प्रत्यक्ष लेनदेन नीति",
            "KRISHAK SATHI does not process online advance payments or hold buyer funds in escrow."),
        policy(7, "ANTI_FRAUD_POLICY", "Anti-Fraud & Advance Payment Protection", "প্রতারণা প্রতিরোধ ও নিরাপদ পেমেন্ট সতর্কতা", "धोखाधड़ी रोकथाम व सुरक्षा नीति",
            "Zero OTP/UPI PIN requests; direct NCH 1915 and Cybercrime 1930 escalation access."),
        policy(8, "SELLER_VERIFICATION", "Seller & Dealer Verification Standard", "বিক্রেতা ও ডিলার যাচাইকরণ নীতি", "विक्रेता व डीलर सत्यापन नीति",
            "Verified badges shown strictly after KYC, consent, and admin approval; unverified kept neutral."),
        policy(9, "AUTHORISED_DEALER_EVIDENCE", "Authorised Brand Dealer Badge Policy", "অনুমোদিত ব্র্যান্ড ডিলার প্রমাণ নীতি", "अधिकृत ब्रांड डीलर साक्ष्य नीति",
            "Authorised Dealer badge requires documented manufacturer authorisation proof."),
        policy(10, "SELLER_LEGAL_AGREEMENT", "Mandatory Seller Legal Agreement (SELLER_TERMS_2026_V1)", "বিক্রেতার বাধ্যতামূলক আইনি চুক্তি", "अनिवार्य विक्रेता कानूनी समझौता",
            "Version-controlled agreement confirming genuine stock, accurate specs, and fair pricing before listing."),
        policy(11, "PRODUCT_QUALITY_CONDITION", "Machinery Quality, Condition & Authenticity Policy", "যন্ত্রপাতির গুণমান ও অবস্থা প্রকাশ নীতি", "मशीनरी गुणवत्ता व स्थिति नीति",
            "Mandatory New/Used/Refurbished, OEM vs compatible, defect, and invoice/warranty disclosure."),
        policy(12, "RENTAL_OPERATOR_SAFETY", "Machinery Rental & Operator Safety Policy", "যন্ত্র ভাড়া ও অপারেটর সুরক্ষা নীতি", "मशीनरी किराया व ऑपरेटर सुरक्षा नीति",
            "Written rate, fuel, deposit, and operator training verification before starting field work."),
        policy(13, "DRONE_DGCA_ADVISORY", "Agricultural Drone & Aerial Spraying Advisory", "কৃষি ড্রোন ও DGCA নির্দেশিকা", "कृषि ड्रोन व DGCA दिशानिर्देश",
            "Requires DGCA registration, Remote Pilot Certificate, and safe buffer distances near water/homes."),
        policy(14, "EARTHMOVING_SAFETY", "Excavator & Heavy Earthmoving Safety Policy", "এক্সকাভেটর ও মাটি খনন সুরক্ষা নীতি", "एक्स्कावेटर व अर्थमूविंग सुरक्षा नीति",
            "Underground utility checks, slope stability, and trained operator compliance requirements."),
        policy(15, "NURSERY_INPUT_QUALITY", "Nursery Saplings, Seeds & Bio-Input Policy", "নার্সারি চারা, বীজ ও সার নীতি", "नर्सरी पौधे, बीज व उर्वरक नीति",
            "Lot/germination transparency and local agricultural input licensing reminders."),
        policy(16, "AGROCHEMICAL_SAFETY", "Pesticide & Dosage Safety Disclaimer", "কীটনাশক ও সার প্রয়োগ সতর্কতা", "कीटनाशक व उर्वरक सुरक्षा अस्वीकरण",
            "Always read product label, use PPE, follow waiting period, and consult local agronomists."),
        policy(17, "AI_ADVISORY_DISCLAIMER", "AI Assistant (KRISHAK AI) Limitations", "AI তথ্য ও সীমাবদ্ধতা সতর্কতা", "AI सहायक सीमाएं व अस्वीकरण",
            "AI outputs are preliminary references only; verify dosages and financial decisions independently."),
        policy(18, "EXPERT_TELE_ADVISORY", "Expert Consultation & Tele-Advisory Policy", "বিশেষজ্ঞ পরামর্শ ও টেলি-সহায়তা নীতি", "विशेषज्ञ परामर्श व टेली-सलाह नीति",
            "Remote advice depends on user photos and notes and cannot substitute lab soil/pathology tests."),
        policy(19, "WEATHER_ADVISORY_POLICY", "Weather & Climate Alert Disclaimer", "আবহাওয়া ও সতর্কতা বার্তা নীতি", "मौसम व चेतावनी अस्वीकरण",
            "Forecasts are informational; follow official IMD bulletins for severe cyclone/flood action."),
        policy(20, "GOVT_SCHEME_PORTALS", "Government Scheme & Subsidy Link Policy", "সরকারি প্রকল্প ও ভর্তুকি লিংক নীতি", "सरकारी योजना व सब्सिडी लिंक नीति",
            "Links point strictly to official .gov.in / .nic.in portals; eligibility is decided solely by officials."),
        policy(21, "FAIR_PRICE_MARKET", "Fair Price, Mandi Rate & MSP Transparency", "ন্যায্য মূল্য, বাজার দর ও MSP স্বচ্ছতা নীতি", "उचित मूल्य, मंडी भाव व MSP पारदर्शिता",
            "MSP, AGMARKNET/mandi quotes, and buyer offers are shown separately with source and date."),
        policy(22, "ANTI_DARK_PATTERNS", "Zero Dark Patterns & Honest Reviews Policy", "ডার্ক প্যাটার্ন বর্জন ও সৎ রিভিউ নীতি", "डार्क पैटर्न रोकथाम व निष्पक्ष समीक्षा",
            "No countdown pressure, pre-checked paid add-ons, or synthetic ratings/reviews."),
        policy(23, "GRIEVANCE_REDRESSAL", "Grievance Redressal (48h / 30d SLA) Policy", "অভিযোগ নিষ্পত্তি নীতি (৪৮ ঘণ্টা / ৩০ দিন)", "शिकायत निवारण नीति (48 घंटे / 30 दिन)",
            "Structured grievance ticket tracking with 48-hour acknowledgement and 30-day resolution goal."),
        policy(24, "SUSPENSION_AUDIT", "Seller Suspension, Takedown & Audit Policy", "বিক্রেতা স্থগিতকরণ ও অডিট লগ নীতি", "विक्रेता निलंबन व ऑडिट लॉग नीति",
            "Fraud-flagged accounts can be suspended with immutable admin audit logs."),
        policy(25, "CHILD_SAFETY_CONTENT", "Community Conduct & Child Safety Standards", "কমিউনিটি আচরণ ও শিশু সুরক্ষা নীতি", "समुदाय आचरण व बाल सुरक्षा नीति",
            "Zero tolerance for abusive posts, misleading scams, or exploitative content in forums."),
        policy(26, "IP_IMAGE_ATTRIBUTION", "Intellectual Property & Photo Provenance Policy", "ছবি ও মেধাস্বত্ব (IP) নীতি", "बौद्धिक संपदा व फोटो प्रमाण नीति",
            "Distinguishes user-uploaded equipment photos from educational visual-guide illustrations."),
        policy(27, "PLATFORM_LIABILITY", "Statutory Liability & Governing Law Notice", "প্ল্যাটফর্ম দায়বদ্ধতা ও আইনি এখতিয়ার", "प्लेटफ़ॉर्म दायित्व व लागू कानून सूचना",
            "Draft notice preserving mandatory Indian consumer statutory rights; subject to legal counsel review.")
    )

    private fun policy(
        id: Int,
        code: String,
        titleEn: String,
        titleBn: String,
        titleHi: String,
        summaryEn: String
    ): PolicyDocumentEntry {
        val summaryBn = "$titleBn: $summaryEn"
        val summaryHi = "$titleHi: $summaryEn"
        return PolicyDocumentEntry(
            id = id,
            code = code,
            titleEn = "$id. $titleEn",
            titleBn = "$id. $titleBn",
            titleHi = "$id. $titleHi",
            summaryEn = summaryEn,
            summaryBn = summaryBn,
            summaryHi = summaryHi,
            bodyEn = buildString {
                append("Policy Code: $code (Draft v2026.1 — Pending Indian Legal Counsel Sign-Off)\n\n")
                append("Scope: $summaryEn\n\n")
                append("Farmer Safeguard: Users have the right to transparent seller identity, original vs illustration provenance, fair price source dates, and zero deceptive urgency. Mandatory consumer remedies under Indian law remain unaffected.")
            },
            bodyBn = buildString {
                append("নীতি কোড: $code (খসড়া v2026.1 — ভারতীয় আইনি পরামর্শকের চূড়ান্ত পর্যালোচনা বাকি)\n\n")
                append("মূল বিষয়: $summaryEn\n\n")
                append("কৃষক সুরক্ষা: ব্যবহারকারীরা স্বচ্ছ বিক্রেতার তথ্য, ছবির উৎস, ন্যায্য বাজার দরের তারিখ ও অভিযোগ জানানোর পূর্ণ অধিকার সংরক্ষণ করেন।")
            },
            bodyHi = buildString {
                append("नीति कोड: $code (प्रारूप v2026.1 — भारतीय कानूनी सलाहकार समीक्षा लंबित)\n\n")
                append("मुख्य विषय: $summaryEn\n\n")
                append("किसान सुरक्षा: उपयोगकर्ताओं को पारदर्शी विक्रेता पहचान, फोटो स्रोत, मंडी भाव तिथि और शिकायत निवारण का पूरा अधिकार सुरक्षित है।")
            }
        )
    }
}
