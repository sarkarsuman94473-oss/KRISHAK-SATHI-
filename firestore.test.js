const { test, describe, before, after, beforeEach } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require(require.resolve("@firebase/rules-unit-testing", {
  paths: [process.cwd(), "/usr/local/lib/node_modules"],
}));
const {
  doc,
  getDoc,
  setDoc,
  updateDoc,
  deleteDoc,
  collection,
  getDocs,
  query,
  where,
  serverTimestamp,
  increment,
} = require(require.resolve("firebase/firestore", {
  paths: [
    process.cwd(),
    "/usr/local/lib/node_modules",
    "/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules",
  ],
}));

const PROJECT_ID = "krishak-sathi-forum-test";
const DATABASE_ID = "ai-studio-android-krishaks-f9a2a619-ec4e-4def-80a4-85ff2b225a77";
const ADMIN_EMAIL = "sarkarsuman94473@gmail.com";

let testEnv;

before(async () => {
  const [host, portStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host,
      port: Number(portStr),
      databaseId: DATABASE_ID,
      rules: fs.readFileSync("firestore.rules", "utf8"),
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

function farmerDb(uid = "farmer_alice") {
  return testEnv.authenticatedContext(uid, { email: `${uid}@example.com`, email_verified: true }).firestore();
}

function adminDb() {
  return testEnv.authenticatedContext("admin_suman", { email: ADMIN_EMAIL, email_verified: true }).firestore();
}

function validPostPayload(authorId = "farmer_alice") {
  return {
    authorId,
    authorName: "Ramesh Mandal",
    authorRole: "FARMER",
    title: "Early blight management on Tomato leaves",
    content: "Noticing concentric dark rings on lower tomato leaves after morning fog. Should I use copper oxychloride or neem oil first?",
    topic: "PEST_DISEASE",
    cropId: "tomato",
    cropName: "Tomato",
    likesCount: 0,
    commentsCount: 0,
    reportCount: 0,
    moderationStatus: "APPROVED",
    pinned: false,
    moderationReason: "",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

describe("Krishak Sathi Community Forum Security Rules", () => {
  test("Unauthenticated users are denied all read/write on forum collections", async () => {
    const anon = testEnv.unauthenticatedContext().firestore();
    await assertFails(getDocs(collection(anon, "forum_posts")));
    await assertFails(setDoc(doc(anon, "forum_posts", "post_1"), validPostPayload("anon")));
    await assertFails(getDoc(doc(anon, "forum_user_moderation", "farmer_alice")));
  });

  test("Signed-in farmer can create, query visible posts, like, comment, and edit own post", async () => {
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");

    await assertSucceeds(setDoc(doc(alice, "forum_posts", "post_1"), validPostPayload("farmer_alice")));

    // Aligned query matching rules (APPROVED + FLAGGED)
    await assertSucceeds(
      getDocs(query(collection(bob, "forum_posts"), where("moderationStatus", "in", ["APPROVED", "FLAGGED"])))
    );

    // Bob likes Alice's post and increments likesCount by +1
    await assertSucceeds(
      setDoc(doc(bob, "forum_posts/post_1/likes", "farmer_bob"), {
        userId: "farmer_bob",
        createdAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      updateDoc(doc(bob, "forum_posts", "post_1"), {
        likesCount: increment(1),
        updatedAt: serverTimestamp(),
      })
    );

    // Bob comments on Alice's post
    await assertSucceeds(
      setDoc(doc(bob, "forum_posts/post_1/comments", "cmt_1"), {
        postId: "post_1",
        authorId: "farmer_bob",
        authorName: "Bikash Roy",
        authorRole: "FARMER",
        content: "Remove lower infected leaves and spray copper oxychloride 3g/L.",
        moderationStatus: "APPROVED",
        reportCount: 0,
        moderationReason: "",
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      updateDoc(doc(bob, "forum_posts", "post_1"), {
        commentsCount: increment(1),
        updatedAt: serverTimestamp(),
      })
    );

    // Alice can edit her own post title/content/topic/crop
    await assertSucceeds(
      updateDoc(doc(alice, "forum_posts", "post_1"), {
        title: "Updated: Early blight management on Tomato",
        updatedAt: serverTimestamp(),
      })
    );
  });

  test("Cross-user edit/delete and arbitrary counter/pin tampering are rejected", async () => {
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");
    await assertSucceeds(setDoc(doc(alice, "forum_posts", "post_1"), validPostPayload("farmer_alice")));

    // Bob cannot edit Alice's title/content or pin the post or delete Alice's post
    await assertFails(updateDoc(doc(bob, "forum_posts", "post_1"), { title: "Hacked", updatedAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(bob, "forum_posts", "post_1"), { pinned: true, updatedAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(bob, "forum_posts", "post_1"), { likesCount: 999, updatedAt: serverTimestamp() }));
    await assertFails(deleteDoc(doc(bob, "forum_posts", "post_1")));
  });

  test("Email spoofing (unverified admin email token) is rejected from admin actions", async () => {
    const spoofed = testEnv.authenticatedContext("attacker", { email: ADMIN_EMAIL, email_verified: false }).firestore();
    const alice = farmerDb("farmer_alice");
    await assertSucceeds(setDoc(doc(alice, "forum_posts", "post_1"), validPostPayload("farmer_alice")));

    await assertFails(
      updateDoc(doc(spoofed, "forum_posts", "post_1"), {
        pinned: true,
        moderationStatus: "HIDDEN",
        moderationReason: "Spoofed",
        updatedAt: serverTimestamp(),
      })
    );
    await assertFails(
      setDoc(doc(spoofed, "forum_user_moderation", "farmer_alice"), {
        userId: "farmer_alice",
        userName: "Ramesh Mandal",
        status: "BANNED",
        warningCount: 3,
        moderationReason: "Spoofed",
        mutedUntilMillis: 0,
        updatedByAdmin: "attacker",
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );
  });

  test("Reports and Admin moderation (hide post, warn/mute/ban user, enforce mute block)", async () => {
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");
    const admin = adminDb();

    await assertSucceeds(setDoc(doc(alice, "forum_posts", "post_1"), validPostPayload("farmer_alice")));

    // Bob reports Alice's post
    await assertSucceeds(
      setDoc(doc(bob, "forum_reports", "rep_1"), {
        targetType: "POST",
        postId: "post_1",
        commentId: "",
        targetAuthorId: "farmer_alice",
        targetAuthorName: "Ramesh Mandal",
        targetPreview: "Early blight management...",
        reporterId: "farmer_bob",
        reporterName: "Bikash Roy",
        reason: "SPAM",
        details: "Promotional link inside",
        status: "PENDING",
        adminActionNote: "",
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );

    // Bob cannot self-resolve reports
    await assertFails(updateDoc(doc(bob, "forum_reports", "rep_1"), { status: "RESOLVED", updatedAt: serverTimestamp() }));

    // Verified Admin hides the post, resolves the report, and mutes Alice
    await assertSucceeds(
      updateDoc(doc(admin, "forum_posts", "post_1"), {
        moderationStatus: "HIDDEN",
        moderationReason: "Removed promotional content",
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      updateDoc(doc(admin, "forum_reports", "rep_1"), {
        status: "RESOLVED",
        adminActionNote: "Post hidden and author muted 7 days",
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      setDoc(doc(admin, "forum_user_moderation", "farmer_alice"), {
        userId: "farmer_alice",
        userName: "Ramesh Mandal",
        status: "MUTED",
        warningCount: 1,
        moderationReason: "Repeated promotional links",
        mutedUntilMillis: Date.now() + 7 * 86400000,
        updatedByAdmin: "admin_suman",
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );

    // Bob cannot read Alice's hidden post directly
    await assertFails(getDoc(doc(bob, "forum_posts", "post_1")));

    // Muted Alice cannot create a new post or comment!
    await assertFails(setDoc(doc(alice, "forum_posts", "post_2"), validPostPayload("farmer_alice")));
    await assertFails(
      setDoc(doc(alice, "forum_posts/post_1/comments", "cmt_muted"), {
        postId: "post_1",
        authorId: "farmer_alice",
        authorName: "Ramesh Mandal",
        authorRole: "FARMER",
        content: "Trying to bypass mute",
        moderationStatus: "APPROVED",
        reportCount: 0,
        moderationReason: "",
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );

    // Alice can read her own moderation notice, Bob cannot read Alice's moderation doc
    await assertSucceeds(getDoc(doc(alice, "forum_user_moderation", "farmer_alice")));
    await assertFails(getDoc(doc(bob, "forum_user_moderation", "farmer_alice")));

    // Admin restores Alice to ACTIVE -> Alice can post again
    await assertSucceeds(
      updateDoc(doc(admin, "forum_user_moderation", "farmer_alice"), {
        status: "ACTIVE",
        mutedUntilMillis: 0,
        moderationReason: "Appeal accepted",
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(setDoc(doc(alice, "forum_posts", "post_2"), validPostPayload("farmer_alice")));
  });
});

function validSchemePayload(overrides = {}) {
  return {
    govLevel: "WEST_BENGAL",
    category: "WEST_BENGAL_SCHEMES",
    extraCategories: ["FINANCIAL_ASSISTANCE"],
    farmerTypes: ["SMALL_MARGINAL", "TENANT_FARMER"],
    activityFocus: "FIELD_CROPS",
    northBengalPriority: 98,
    discontinued: false,
    applicationStatus: "OPEN",
    officialUrl: "https://krishakbandhu.wb.gov.in/",
    officialSourceUrl: "https://wb.gov.in/government-schemes.aspx",
    lastVerifiedDate: "2026-11-17",
    nameEn: "Krishak Bandhu (Natun)",
    nameBn: "কৃষকবন্ধু (নতুন) প্রকল্প",
    nameHi: "कृषक बंधु (नया) योजना",
    departmentEn: "Department of Agriculture, Govt of West Bengal",
    departmentBn: "কৃষি দপ্তর, পশ্চিমবঙ্গ সরকার",
    departmentHi: "कृषि विभाग, पश्चिम बंगाल सरकार",
    shortDescEn: "Annual financial assistance and life cover support for West Bengal farmers.",
    shortDescBn: "পশ্চিমবঙ্গের কৃষকদের বার্ষিক আর্থিক সহায়তা ও জীবন বিমা সুবিধা।",
    shortDescHi: "पश्चिम बंगाल के किसानों के लिए वार्षिक वित्तीय सहायता एवं बीमा।",
    updatedByAdmin: "admin_suman",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
    ...overrides,
  };
}

describe("Krishak Sathi Government Schemes Security Rules", () => {
  test("Unauthenticated users cannot read or write government_schemes", async () => {
    const anon = testEnv.unauthenticatedContext().firestore();
    await assertFails(getDocs(collection(anon, "government_schemes")));
    await assertFails(setDoc(doc(anon, "government_schemes", "kb_wb"), validSchemePayload()));
  });

  test("Signed-in farmers can read schemes but cannot create, update, or delete", async () => {
    const admin = adminDb();
    const farmer = farmerDb("farmer_alice");

    await assertSucceeds(setDoc(doc(admin, "government_schemes", "kb_wb"), validSchemePayload()));

    await assertSucceeds(getDoc(doc(farmer, "government_schemes", "kb_wb")));
    await assertSucceeds(getDocs(collection(farmer, "government_schemes")));

    await assertFails(setDoc(doc(farmer, "government_schemes", "farmer_fake"), validSchemePayload()));
    await assertFails(updateDoc(doc(farmer, "government_schemes", "kb_wb"), {
      officialUrl: "https://example.org/phish",
      updatedAt: serverTimestamp(),
    }));
    await assertFails(deleteDoc(doc(farmer, "government_schemes", "kb_wb")));
  });

  test("Spoofed unverified admin token cannot manage government_schemes", async () => {
    const spoofed = testEnv.authenticatedContext("attacker", { email: ADMIN_EMAIL, email_verified: false }).firestore();
    await assertFails(setDoc(doc(spoofed, "government_schemes", "spoof_scheme"), validSchemePayload()));
  });

  test("Admin must provide valid HTTPS URLs and valid category; can update status and delete", async () => {
    const admin = adminDb();

    // Non-HTTPS URL rejected
    await assertFails(setDoc(doc(admin, "government_schemes", "bad_url"), validSchemePayload({
      officialUrl: "http://krishakbandhu.wb.gov.in/",
    })));

    // Unknown category rejected
    await assertFails(setDoc(doc(admin, "government_schemes", "bad_cat"), validSchemePayload({
      category: "INVALID_CATEGORY",
    })));

    // Valid create, update, and delete succeed
    await assertSucceeds(setDoc(doc(admin, "government_schemes", "kb_wb"), validSchemePayload()));
    await assertSucceeds(updateDoc(doc(admin, "government_schemes", "kb_wb"), {
      applicationStatus: "DISCONTINUED",
      discontinued: true,
      lastVerifiedDate: "2026-11-18",
      updatedAt: serverTimestamp(),
    }));
    await assertSucceeds(deleteDoc(doc(admin, "government_schemes", "kb_wb")));
  });
});

function validAiConfigPayload(overrides = {}) {
  return {
    primaryProvider: "GEMINI",
    backupProviderEnabled: true,
    geminiModelName: "gemini-3.8-flash",
    openAiModelName: "gpt-4o-mini",
    openAiProxyUrl: "https://us-central1-krishaksathi.cloudfunctions.net/openaiProxy",
    timeoutMs: 35000,
    maxRetriesPerProvider: 1,
    rateLimitPerMinute: 8,
    dailyQuotaPerUser: 80,
    enableRagCitations: true,
    enableWeatherContext: true,
    updatedByAdmin: ADMIN_EMAIL,
    updatedAt: serverTimestamp(),
    ...overrides,
  };
}

function validKnowledgeDocPayload(overrides = {}) {
  return {
    titleEn: "ICAR Potato Late Blight Advisory",
    titleBn: "আলু নাবি ধসা পরামর্শ",
    titleHi: "आलू झुलसा सलाह",
    institution: "ICAR-CPRI",
    sourceUrl: "https://cpri.icar.gov.in/",
    publicationDate: "2026-11-17",
    verifiedDate: "2026-11-17",
    category: "CROP_PROTECTION",
    applicableCropIdsCsv: "potato",
    regionTag: "West Bengal",
    keywordsCsv: "potato,blight",
    summaryEn: "Use disease-free seed tubers and preventive field sanitation before chemical control.",
    summaryBn: "রোগমুক্ত বীজ ও পরিষ্কার ক্ষেত বজায় রাখুন।",
    summaryHi: "रोगमुक्त बीज कंद का उपयोग करें।",
    isActive: true,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
    ...overrides,
  };
}

describe("Krishak Sathi AI Intelligence Security Rules", () => {
  test("Only verified admin can configure AI providers, HTTPS proxy, and zero secrets allowed in config", async () => {
    const admin = adminDb();
    const farmer = farmerDb("farmer_alice");
    const anon = testEnv.unauthenticatedContext().firestore();

    // Unauthenticated cannot read or write
    await assertFails(getDoc(doc(anon, "ai_admin_config", "active")));
    await assertFails(setDoc(doc(anon, "ai_admin_config", "active"), validAiConfigPayload()));

    // Non-admin farmer cannot write AI config
    await assertFails(setDoc(doc(farmer, "ai_admin_config", "active"), validAiConfigPayload()));

    // Admin cannot accidentally store raw API key fields or non-HTTPS URLs
    await assertFails(setDoc(doc(admin, "ai_admin_config", "active"), validAiConfigPayload({
      openAiApiKey: "sk-proj-forbidden",
    })));
    await assertFails(setDoc(doc(admin, "ai_admin_config", "active"), validAiConfigPayload({
      openAiProxyUrl: "http://insecure.example.com/proxy",
    })));

    // Valid Admin write succeeds and signed-in farmer can read non-secret routing config
    await assertSucceeds(setDoc(doc(admin, "ai_admin_config", "active"), validAiConfigPayload()));
    await assertSucceeds(getDoc(doc(farmer, "ai_admin_config", "active")));
  });

  test("Only verified admin can manage ai_knowledge_docs and must use HTTPS links", async () => {
    const admin = adminDb();
    const farmer = farmerDb("farmer_alice");

    await assertFails(setDoc(doc(farmer, "ai_knowledge_docs", "doc_1"), validKnowledgeDocPayload()));
    await assertFails(setDoc(doc(admin, "ai_knowledge_docs", "doc_bad"), validKnowledgeDocPayload({
      sourceUrl: "http://cpri.icar.gov.in/",
    })));

    await assertSucceeds(setDoc(doc(admin, "ai_knowledge_docs", "doc_1"), validKnowledgeDocPayload()));
    await assertSucceeds(getDoc(doc(farmer, "ai_knowledge_docs", "doc_1")));
    await assertSucceeds(deleteDoc(doc(admin, "ai_knowledge_docs", "doc_1")));
  });

  test("Telemetry logs enforce per-user isolation, immutability, and forbid storing raw user prompts", async () => {
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");
    const admin = adminDb();

    const validTelemetry = {
      userId: "farmer_alice",
      assistantMode: "FARMER_AI",
      cropId: "rice_paddy",
      providerUsed: "GEMINI",
      modelUsed: "gemini-3.8-flash",
      fallbackUsed: false,
      success: true,
      errorCode: "NONE",
      errorSummary: "",
      latencyMs: 620,
      estimatedTokens: 310,
      estimatedCostUsd: 0.0001,
      createdAt: serverTimestamp(),
    };

    // Reject telemetry with raw prompt field
    await assertFails(setDoc(doc(alice, "ai_telemetry_logs", "t_leak"), {
      ...validTelemetry,
      prompt: "private farmer question",
    }));

    // Cannot spoof another userId
    await assertFails(setDoc(doc(bob, "ai_telemetry_logs", "t_spoof"), validTelemetry));

    // Owner writes valid metric; Bob cannot read Alice's telemetry; Admin can read
    await assertSucceeds(setDoc(doc(alice, "ai_telemetry_logs", "t_1"), validTelemetry));
    await assertSucceeds(getDoc(doc(alice, "ai_telemetry_logs", "t_1")));
    await assertFails(getDoc(doc(bob, "ai_telemetry_logs", "t_1")));
    await assertSucceeds(getDoc(doc(admin, "ai_telemetry_logs", "t_1")));

    // Telemetry is immutable for farmers
    await assertFails(updateDoc(doc(alice, "ai_telemetry_logs", "t_1"), { success: false }));
  });

  test("AI helpfulness feedback enforces user ownership and admin-only review transitions", async () => {
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");
    const admin = adminDb();

    const validFeedback = {
      userId: "farmer_alice",
      assistantMode: "PLANT_PROBLEM",
      cropId: "potato",
      providerUsed: "OPENAI",
      helpful: true,
      messagePreview: "Safe early preventive actions for blight.",
      farmerNote: "",
      reviewStatus: "OPEN",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    };

    await assertSucceeds(setDoc(doc(alice, "ai_feedback", "fb_1"), validFeedback));
    await assertFails(getDoc(doc(bob, "ai_feedback", "fb_1")));
    await assertFails(updateDoc(doc(alice, "ai_feedback", "fb_1"), {
      reviewStatus: "REVIEWED",
      updatedAt: serverTimestamp(),
    }));
    await assertSucceeds(updateDoc(doc(admin, "ai_feedback", "fb_1"), {
      reviewStatus: "REVIEWED",
      updatedAt: serverTimestamp(),
    }));
  });
});

describe("Krishak Sathi SEO & Discovery Security Rules", () => {
  test("Public/farmers can read PUBLISHED seo_articles, while AI_DRAFT_PENDING_REVIEW and writes are admin-only", async () => {
    const admin = adminDb();
    const alice = farmerDb("farmer_alice");
    const anon = testEnv.unauthenticatedContext().firestore();

    const baseArticle = {
      slug: "rice",
      category: "CROP_GUIDE",
      cropId: "rice",
      status: "PUBLISHED",
      titleEn: "Rice Cultivation Guide",
      titleBn: "ধান চাষ গাইড",
      titleHi: "धान की खेती गाइड",
      summaryEn: "ICAR & BCKV verified paddy cultivation & IPM guide.",
      summaryBn: "ধান চাষের নির্ভরযোগ্য গাইড।",
      summaryHi: "धान खेती की गाइड।",
      sourceName: "ICAR - NRRI",
      sourceUrl: "https://icar-nrri.in/",
      canonicalUrl: "https://ais-pre-2x4wg44pdpamhvdlijirfv-948907126237.asia-southeast1.run.app/crops/rice.html",
      lastUpdatedDate: "2026-11-18",
      isAiAssisted: false,
      reviewedBy: "agri_editorial",
      updatedAt: serverTimestamp(),
    };

    // Farmers cannot write SEO articles
    await assertFails(setDoc(doc(alice, "seo_articles", "art_rice"), baseArticle));

    // Admin can write PUBLISHED & AI_DRAFT_PENDING_REVIEW articles
    await assertSucceeds(setDoc(doc(admin, "seo_articles", "art_rice"), baseArticle));
    await assertSucceeds(setDoc(doc(admin, "seo_articles", "art_draft"), {
      ...baseArticle,
      slug: "draft-guide",
      status: "AI_DRAFT_PENDING_REVIEW",
      isAiAssisted: true,
    }));

    // Unauthenticated & signed-in farmers can read PUBLISHED, but NOT unpublished drafts
    await assertSucceeds(getDoc(doc(anon, "seo_articles", "art_rice")));
    await assertSucceeds(getDoc(doc(alice, "seo_articles", "art_rice")));
    await assertFails(getDoc(doc(anon, "seo_articles", "art_draft")));
    await assertFails(getDoc(doc(alice, "seo_articles", "art_draft")));
    await assertSucceeds(getDoc(doc(admin, "seo_articles", "art_draft")));
  });

  test("seo_config blocks secrets and restricts writes to admin", async () => {
    const admin = adminDb();
    const alice = farmerDb("farmer_alice");

    const validConfig = {
      baseWebUrl: "https://ais-pre-2x4wg44pdpamhvdlijirfv-948907126237.asia-southeast1.run.app",
      googleSearchConsoleToken: "gsc-verify-sample-123",
      allowGooglebot: true,
      allowOaiSearchBot: true,
      allowGptBot: true,
      playStoreStatus: "Coming Soon",
      updatedBy: "admin",
      updatedAt: serverTimestamp(),
    };

    await assertFails(setDoc(doc(alice, "seo_config", "default"), validConfig));
    await assertFails(setDoc(doc(admin, "seo_config", "default"), {
      ...validConfig,
      privateKey: "secret-should-fail",
    }));
    await assertSucceeds(setDoc(doc(admin, "seo_config", "default"), validConfig));
    await assertSucceeds(getDoc(doc(alice, "seo_config", "default")));
  });

  test("seo_analytics enforces zero-PII and admin-only reads", async () => {
    const admin = adminDb();
    const alice = farmerDb("farmer_alice");

    const validEvent = {
      sourceBucket: "google_search",
      targetSlug: "rice",
      languageCode: "bn",
      createdAt: serverTimestamp(),
    };

    // Reject PII fields
    await assertFails(setDoc(doc(alice, "seo_analytics", "ev_pii"), {
      ...validEvent,
      email: "farmer@example.com",
    }));
    await assertFails(setDoc(doc(alice, "seo_analytics", "ev_uid"), {
      ...validEvent,
      userId: "farmer_alice",
    }));

    // Valid event accepted; farmer cannot read analytics, admin can read
    await assertSucceeds(setDoc(doc(alice, "seo_analytics", "ev_ok"), validEvent));
    await assertFails(getDoc(doc(alice, "seo_analytics", "ev_ok")));
    await assertSucceeds(getDoc(doc(admin, "seo_analytics", "ev_ok")));
  });
});

describe("Krishak Sathi Plant Kingdom & WB Nursery Directory Security Rules", () => {
  function validPlantPayload(plantId = "himalayan_blue_poppy") {
    return {
      plantId,
      nameEn: "Himalayan Blue Poppy",
      nameBn: "হিমালয়ান নীল পপি",
      nameHi: "हिमालयन ब्लू पॉपी",
      scientificName: "Meconopsis betonicifolia",
      primaryGroup: "HIMALAYAN_HILL_FLOWERS",
      secondaryGroupsCsv: "SEASONAL_FLOWERS",
      filterTagsCsv: "FLOWERING,HILL_FLOWER,RARE",
      seasonalTagsCsv: "MONSOON,SHADE_LOVING",
      descriptionEn: "Iconic sky-blue Eastern Himalayan alpine wildflower.",
      descriptionBn: "পূর্ব হিমালয়ের বিখ্যাত আকাশী-নীল আল্পাইন ফুল।",
      descriptionHi: "पूर्वी हिमालय का प्रसिद्ध आसमानी-नीला अल्पाइन फूल।",
      visualAssetKey: `plant_art_${plantId}`,
      customPhotoUri: "",
      climate: "Cool humid Himalayan temperate",
      temperatureRange: "4°C - 18°C",
      soilRequirements: "Humus-rich leaf mould acidic soil",
      sunlight: "Dappled morning light, cool afternoon shade",
      wateringFrequency: "Consistent misting and moist roots",
      fertilizerGuidance: "Well-rotted leaf compost",
      propagationMethod: "Fresh seed sowing in cold frames",
      floweringOrFruitingSeason: "June - August",
      growthSize: "2 - 3.5 ft",
      indoorOutdoorSuitability: "Outdoor cool hill garden",
      plainsHillSuitability: "High Hills (2,200 - 3,800 m)",
      commonPestsDiseases: "Slugs, crown rot in heat",
      careInstructionsEn: "Keep roots cool and well drained.",
      careInstructionsBn: "শিকড় ঠান্ডা ও জলনিকাশি যুক্ত রাখুন।",
      careInstructionsHi: "जड़ों को ठंडा और जल-निकास युक्त रखें।",
      verifiedNurseryIdsCsv: "",
      isRare: true,
      isVerifiedEntry: true,
      sourceReference: "BSI / UBKV Eastern Himalayan Flora",
      lastVerifiedDate: "2026-11-18",
      updatedAt: serverTimestamp(),
    };
  }

  function validWbNurseryPayload(nurseryId = "wb_siliguri_muddy_hands") {
    return {
      nurseryId,
      businessName: "Muddy Hands Siliguri",
      businessNameBn: "মাডি হ্যান্ডস শিলিগুড়ি",
      businessNameHi: "मडी हैंड्स सिलीगुड़ी",
      ownerName: null,
      fullAddress: null,
      districtCode: "SILIGURI",
      locality: "Siliguri",
      state: "West Bengal",
      phone: null,
      whatsapp: null,
      latitude: null,
      longitude: null,
      websiteOrSocial: null,
      openingHours: null,
      availablePlantCategoriesCsv: "",
      availablePlantIdsCsv: "",
      supportsWholesale: null,
      supportsRetail: null,
      deliveryAvailable: null,
      verificationStatus: "PENDING_VERIFICATION",
      isVerified: false,
      sourceReference: "Supplied Public Nursery Name List (Pending Owner/Admin Verification)",
      lastVerifiedDate: "2026-11-18",
      updatedAt: serverTimestamp(),
    };
  }

  test("plant_kingdom allows signed-in reads and admin-only schema-validated writes", async () => {
    const anon = testEnv.unauthenticatedContext().firestore();
    const alice = farmerDb("farmer_alice");
    const spoofAdmin = testEnv
      .authenticatedContext("spoof_uid", { email: ADMIN_EMAIL, email_verified: false })
      .firestore();
    const admin = adminDb();

    const payload = validPlantPayload("himalayan_blue_poppy");

    // Unauthenticated & farmers & unverified admin cannot write
    await assertFails(setDoc(doc(anon, "plant_kingdom", "himalayan_blue_poppy"), payload));
    await assertFails(setDoc(doc(alice, "plant_kingdom", "himalayan_blue_poppy"), payload));
    await assertFails(setDoc(doc(spoofAdmin, "plant_kingdom", "himalayan_blue_poppy"), payload));

    // Admin cannot write mismatched plantId, extra @DocumentId 'id', or invalid primaryGroup
    await assertFails(
      setDoc(doc(admin, "plant_kingdom", "himalayan_blue_poppy"), {
        ...payload,
        plantId: "wrong_id",
      })
    );
    await assertFails(
      setDoc(doc(admin, "plant_kingdom", "himalayan_blue_poppy"), {
        ...payload,
        id: "himalayan_blue_poppy",
      })
    );
    await assertFails(
      setDoc(doc(admin, "plant_kingdom", "himalayan_blue_poppy"), {
        ...payload,
        primaryGroup: "INVALID_GROUP",
      })
    );

    // Verified admin succeeds; signed-in farmers can read; unauthenticated cannot read; farmers cannot delete
    await assertSucceeds(setDoc(doc(admin, "plant_kingdom", "himalayan_blue_poppy"), payload));
    await assertSucceeds(getDoc(doc(alice, "plant_kingdom", "himalayan_blue_poppy")));
    await assertSucceeds(getDocs(collection(alice, "plant_kingdom")));
    await assertFails(getDoc(doc(anon, "plant_kingdom", "himalayan_blue_poppy")));
    await assertFails(deleteDoc(doc(alice, "plant_kingdom", "himalayan_blue_poppy")));
    await assertSucceeds(deleteDoc(doc(admin, "plant_kingdom", "himalayan_blue_poppy")));
  });

  test("wb_nurseries accepts null contacts on PENDING_VERIFICATION and verifies admin transitions", async () => {
    const anon = testEnv.unauthenticatedContext().firestore();
    const alice = farmerDb("farmer_alice");
    const admin = adminDb();

    const pendingPayload = validWbNurseryPayload("wb_siliguri_muddy_hands");

    // Non-admins and unauthenticated cannot create/update wb_nurseries
    await assertFails(setDoc(doc(anon, "wb_nurseries", "wb_siliguri_muddy_hands"), pendingPayload));
    await assertFails(setDoc(doc(alice, "wb_nurseries", "wb_siliguri_muddy_hands"), pendingPayload));

    // Invalid verificationStatus rejected even for admin
    await assertFails(
      setDoc(doc(admin, "wb_nurseries", "wb_siliguri_muddy_hands"), {
        ...pendingPayload,
        verificationStatus: "FAKE_STATUS",
      })
    );

    // Out-of-bounds coordinates rejected
    await assertFails(
      setDoc(doc(admin, "wb_nurseries", "wb_siliguri_muddy_hands"), {
        ...pendingPayload,
        latitude: 145.0,
      })
    );

    // Admin creates valid PENDING_VERIFICATION record with null unverified contact fields
    await assertSucceeds(setDoc(doc(admin, "wb_nurseries", "wb_siliguri_muddy_hands"), pendingPayload));

    // Signed-in farmer can read, anon cannot
    await assertSucceeds(getDoc(doc(alice, "wb_nurseries", "wb_siliguri_muddy_hands")));
    await assertFails(getDoc(doc(anon, "wb_nurseries", "wb_siliguri_muddy_hands")));

    // Admin verifies nursery with real phone, address & coordinates
    await assertSucceeds(
      setDoc(doc(admin, "wb_nurseries", "wb_siliguri_muddy_hands"), {
        ...pendingPayload,
        fullAddress: "Sevoke Road, Siliguri",
        phone: "+919800000001",
        latitude: 26.742,
        longitude: 88.431,
        verificationStatus: "ADMIN_VERIFIED",
        ownerConsentStatus: "CONFIRMED",
        notes: "Verified via admin check",
        isVerified: true,
        updatedAt: serverTimestamp(),
      })
    );

    // Admin can upload CSV plant draft with publicationStatus
    await assertSucceeds(
      setDoc(doc(admin, "plant_kingdom", "KS-PL-0001"), {
        ...validPlantPayload("KS-PL-0001"),
        publicationStatus: "DRAFT_REVIEW_REQUIRED",
        isVerifiedEntry: false,
      })
    );
  });
});

describe("Krishak Sathi Promo Banners & Machinery Hub Security Rules", () => {
  test("promo_banners enforces language isolation codes and admin-only HTTPS/content URI writes", async () => {
    const anon = testEnv.unauthenticatedContext().firestore();
    const alice = farmerDb("farmer_alice");
    const admin = adminDb();

    const validBanner = {
      languageCode: "hi",
      orderIndex: 0,
      isActive: true,
      title: "कृषक साथी: स्मार्ट खेती समाधान",
      subtitle: "AI सलाह व नर्सरी",
      badgeText: "हिन्दी",
      ctaLabel: "खोलें",
      drawableResName: "img_banner_hi_1",
      imageUrl: "https://example.org/banner_hi_1.jpg",
      targetCardId: "",
      isPoster: true,
      hideTextOverlay: true,
      updatedAt: serverTimestamp(),
    };

    await assertFails(setDoc(doc(anon, "promo_banners", "hi_poster_1"), validBanner));
    await assertFails(setDoc(doc(alice, "promo_banners", "hi_poster_1"), validBanner));
    await assertFails(
      setDoc(doc(admin, "promo_banners", "bad_lang"), {
        ...validBanner,
        languageCode: "fr",
      })
    );
    await assertFails(
      setDoc(doc(admin, "promo_banners", "bad_url"), {
        ...validBanner,
        imageUrl: "http://insecure.example.org/banner.jpg",
      })
    );

    await assertSucceeds(setDoc(doc(admin, "promo_banners", "hi_poster_1"), validBanner));
    await assertSucceeds(getDoc(doc(alice, "promo_banners", "hi_poster_1")));
  });

  test("machinery_products prevents seller self-approval and hides unapproved listings from other farmers", async () => {
    const seller = farmerDb("seller_ram");
    const otherFarmer = farmerDb("farmer_bob");
    const admin = adminDb();

    const sellerSubmission = {
      sellerUserId: "seller_ram",
      dealerId: "",
      sellerBusinessName: "North Bengal Tractor House",
      sellerLocation: "Siliguri",
      sellerLocalityCode: "SILIGURI",
      sellerPhone: null,
      sellerWhatsapp: null,
      nameEn: "Compact 28HP Mini Tractor",
      nameBn: "কম্প্যাক্ট ২৮এইচপি মিনি ট্রাক্টর",
      nameHi: "कॉम्पैक्ट 28HP मिनी ट्रैक्टर",
      categoryCode: "TRACTORS_LAND_PREP",
      subcategory: "Mini Tractor",
      brand: "Kubota",
      modelNumber: "B2741",
      condition: "NEW",
      forSale: true,
      forRent: false,
      salePriceInr: null,
      listingOrigin: "SELLER_LISTING",
      approvalStatus: "PENDING_APPROVAL",
      requiresDgcaCompliance: false,
      requiresEarthmovingSafety: false,
      updatedAt: serverTimestamp(),
    };

    // Seller cannot self-approve upon creation
    await assertFails(
      setDoc(doc(seller, "machinery_products", "m_self_approve"), {
        ...sellerSubmission,
        approvalStatus: "APPROVED",
      })
    );

    // Valid seller submission starts PENDING_APPROVAL
    await assertSucceeds(setDoc(doc(seller, "machinery_products", "m_1"), sellerSubmission));
    await assertSucceeds(getDoc(doc(seller, "machinery_products", "m_1")));
    await assertFails(getDoc(doc(otherFarmer, "machinery_products", "m_1")));

    // Seller cannot flip PENDING_APPROVAL to APPROVED on update
    await assertFails(
      updateDoc(doc(seller, "machinery_products", "m_1"), {
        approvalStatus: "APPROVED",
        updatedAt: serverTimestamp(),
      })
    );

    // Admin approves -> other farmer can now read
    await assertSucceeds(
      updateDoc(doc(admin, "machinery_products", "m_1"), {
        approvalStatus: "APPROVED",
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(getDoc(doc(otherFarmer, "machinery_products", "m_1")));
  });

  test("machinery_dealers, rentals, inquiries, service requests, verification claims & admin logs enforce strict roles", async () => {
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");
    const seller = farmerDb("seller_ram");
    const admin = adminDb();

    // Only admin can write machinery_dealers
    await assertFails(
      setDoc(doc(alice, "machinery_dealers", "KS-MC-0001"), {
        dealerId: "KS-MC-0001",
        businessName: "M/s Sarkar Enterprise (Matigara)",
        localityCode: "MATIGARA",
        district: "Siliguri / Darjeeling",
        state: "West Bengal",
        fullAddress: null,
        pinCode: null,
        ownerName: null,
        phone: null,
        whatsapp: null,
        verificationStatus: "PENDING_VERIFICATION",
        ownerConsentStatus: "NOT_CONFIRMED",
        isVerified: false,
        isAuthorisedDealerVerified: false,
        updatedAt: serverTimestamp(),
      })
    );

    await assertSucceeds(
      setDoc(doc(admin, "machinery_dealers", "KS-MC-0001"), {
        dealerId: "KS-MC-0001",
        businessName: "M/s Sarkar Enterprise (Matigara)",
        localityCode: "MATIGARA",
        district: "Siliguri / Darjeeling",
        state: "West Bengal",
        fullAddress: null,
        pinCode: null,
        ownerName: null,
        phone: null,
        whatsapp: null,
        verificationStatus: "PENDING_VERIFICATION",
        ownerConsentStatus: "NOT_CONFIRMED",
        isVerified: false,
        isAuthorisedDealerVerified: false,
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(getDoc(doc(alice, "machinery_dealers", "KS-MC-0001")));

    // Business claim requires explicit ownerConsentConfirmed == true
    await assertFails(
      setDoc(doc(seller, "machinery_verification", "claim_KS-MC-0001_seller_ram"), {
        dealerId: "KS-MC-0001",
        userId: "seller_ram",
        claimNote: "Owner claim without consent",
        status: "PENDING_VERIFICATION",
        ownerConsentConfirmed: false,
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      setDoc(doc(seller, "machinery_verification", "claim_KS-MC-0001_seller_ram"), {
        dealerId: "KS-MC-0001",
        userId: "seller_ram",
        claimNote: "Owner claim with consent",
        status: "PENDING_VERIFICATION",
        ownerConsentConfirmed: true,
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );

    // Rental request isolates farmer/seller from unrelated Bob, and blocks skipping confirmation on create
    const rentalPayload = {
      userId: "farmer_alice",
      farmerName: "Alice Mondal",
      farmerPhone: "9800001234",
      machineId: "KS-M-01",
      machineName: "Power Tiller 12HP",
      sellerUserId: "seller_ram",
      locality: "Phansidewa",
      rentalUnit: "DAILY",
      durationUnits: 2,
      withOperator: true,
      deliveryRequired: false,
      notes: "Paddy field puddling",
      status: "REQUESTED_AWAITING_CONFIRMATION",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    };
    await assertFails(
      setDoc(doc(alice, "machinery_rentals", "r_bad"), {
        ...rentalPayload,
        status: "CONFIRMED",
      })
    );
    await assertSucceeds(setDoc(doc(alice, "machinery_rentals", "r_1"), rentalPayload));
    await assertFails(getDoc(doc(bob, "machinery_rentals", "r_1")));
    await assertSucceeds(
      updateDoc(doc(seller, "machinery_rentals", "r_1"), {
        status: "CONFIRMED",
        updatedAt: serverTimestamp(),
      })
    );

    // Admin audit log is immutable
    await assertSucceeds(
      setDoc(doc(admin, "machinery_admin_logs", "log_1"), {
        adminEmail: ADMIN_EMAIL,
        action: "VERIFY_DEALER",
        targetId: "KS-MC-0001",
        summary: "Test audit entry",
        createdAt: serverTimestamp(),
      })
    );
    await assertFails(
      updateDoc(doc(admin, "machinery_admin_logs", "log_1"), {
        action: "TAMPERED",
      })
    );

    // Rental cancellation by requester and service confirmation by provider
    await assertSucceeds(
      updateDoc(doc(alice, "machinery_rentals", "r_1"), {
        status: "CANCELLED",
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      setDoc(doc(alice, "machinery_inquiries", "inq_1"), {
        userId: "farmer_alice",
        farmerName: "Alice Mondal",
        farmerPhone: "9800001234",
        machineId: "KS-M-01",
        machineName: "Power Tiller 12HP",
        sellerUserId: "seller_ram",
        inquiryType: "QUOTATION",
        message: "Please share GST quotation",
        status: "REQUESTED_AWAITING_CONFIRMATION",
        sellerReply: "",
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      updateDoc(doc(seller, "machinery_inquiries", "inq_1"), {
        status: "RESPONDED",
        sellerReply: "Quotation shared via Siliguri desk.",
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      setDoc(doc(alice, "machinery_service_requests", "srv_1"), {
        userId: "farmer_alice",
        farmerName: "Alice Mondal",
        farmerPhone: "9800001234",
        serviceTypeCode: "ROTAVATOR_LAND_PREP",
        locality: "Matigara",
        landSizeText: "2 Acres",
        cropName: "Paddy",
        preferredDate: "2026-11-28",
        notes: "Morning slot",
        providerUserId: "seller_ram",
        status: "REQUESTED_AWAITING_CONFIRMATION",
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      updateDoc(doc(seller, "machinery_service_requests", "srv_1"), {
        status: "CONFIRMED",
        updatedAt: serverTimestamp(),
      })
    );
  });

  test("machinery_manufacturers, machinery_dealer_offers, machinery_reviews & machinery_research_queue enforce RBAC & validation", async () => {
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");
    const admin = adminDb();
    const anon = testEnv.unauthenticatedContext().firestore();

    // 1. Manufacturers: admin-only HTTPS portal writes, signed-in reads
    const validOem = {
      nameEn: "Mahindra Farm Equipment",
      nameBn: "মাহিন্দ্রা ফার্ম ইকুইপমেন্ট",
      nameHi: "महिंद्रा फार्म इक्विपमेंट",
      countryOrOrigin: "India",
      focusSummaryEn: "Tractors & Rotavators",
      officialPortalUrl: "https://www.mahindratractor.com/",
      dealerLocatorUrl: "https://www.mahindratractor.com/find-a-dealer",
      servicePortalUrl: "",
      customerCareHelpline: "1800-425-6576",
      supportedCategoriesCsv: "TRACTORS_LAND_PREP",
      isVerifiedOfficialLink: true,
      verificationStatus: "OFFICIAL_INFO_LINK_ONLY",
      updatedAt: serverTimestamp(),
    };
    await assertFails(setDoc(doc(alice, "machinery_manufacturers", "oem_mahindra"), validOem));
    await assertFails(
      setDoc(doc(admin, "machinery_manufacturers", "oem_bad"), {
        ...validOem,
        officialPortalUrl: "http://insecure.example.com/",
      })
    );
    await assertSucceeds(setDoc(doc(admin, "machinery_manufacturers", "oem_mahindra"), validOem));
    await assertSucceeds(getDoc(doc(alice, "machinery_manufacturers", "oem_mahindra")));
    await assertFails(getDoc(doc(anon, "machinery_manufacturers", "oem_mahindra")));

    // 2. Dealer offers: admin-only write, signed-in read
    const validOffer = {
      machineId: "ref_mahindra_mahavator_rotavator",
      dealerId: "KS-MC-0001",
      dealerName: "Hindustan Sales Agency",
      localityCode: "SILIGURI",
      modelNumber: "MAHAVATOR-HD",
      condition: "NEW",
      offersSale: true,
      offersRent: false,
      mrpInr: null,
      dealerPriceInr: null,
      indicativePriceInr: null,
      rentalHourlyInr: null,
      rentalDailyInr: null,
      rentalPerAcreInr: null,
      stockStatus: "Availability Not Confirmed",
      verificationStatus: "CATALOG_SUPPORTED_PENDING_CONFIRMATION",
      isOutdatedPrice: false,
      updatedAt: serverTimestamp(),
    };
    await assertFails(setDoc(doc(alice, "machinery_dealer_offers", "offer_1"), validOffer));
    await assertSucceeds(setDoc(doc(admin, "machinery_dealer_offers", "offer_1"), validOffer));
    await assertSucceeds(getDoc(doc(alice, "machinery_dealer_offers", "offer_1")));

    // 3. Farmer reviews: owner-scoped create, bounded rating 1..5, owner or admin delete
    const validReview = {
      machineId: "ref_mahindra_mahavator_rotavator",
      userId: "farmer_alice",
      userName: "Alice Mondal",
      rating: 5,
      title: "Strong puddling",
      comment: "Worked cleanly in Siliguri clay loam.",
      verifiedFarmerFlag: false,
      createdAt: serverTimestamp(),
    };
    await assertFails(
      setDoc(doc(bob, "machinery_reviews", "rev_spoof"), {
        ...validReview,
        userId: "farmer_alice",
      })
    );
    await assertFails(
      setDoc(doc(alice, "machinery_reviews", "rev_bad_rating"), {
        ...validReview,
        rating: 6,
      })
    );
    await assertSucceeds(setDoc(doc(alice, "machinery_reviews", "rev_1"), validReview));
    await assertSucceeds(getDoc(doc(bob, "machinery_reviews", "rev_1")));
    await assertFails(deleteDoc(doc(bob, "machinery_reviews", "rev_1")));
    await assertSucceeds(deleteDoc(doc(admin, "machinery_reviews", "rev_1")));

    // 4. Research queue: user can only submit PENDING_RESEARCH for self; only admin can approve/update
    const researchItem = {
      machineId: "ref_track_paddy_combine_harvester",
      equipmentName: "Rubber-Track Paddy Combine Harvester",
      categoryCode: "HARVESTING_MACHINERY",
      missingFieldsNote: "Verify real dealer photo",
      status: "PENDING_RESEARCH",
      submittedBy: "farmer_alice",
      updatedAt: serverTimestamp(),
    };
    await assertFails(
      setDoc(doc(alice, "machinery_research_queue", "rq_bad"), {
        ...researchItem,
        status: "APPROVED",
      })
    );
    await assertSucceeds(setDoc(doc(alice, "machinery_research_queue", "rq_1"), researchItem));
    await assertFails(
      updateDoc(doc(alice, "machinery_research_queue", "rq_1"), {
        status: "APPROVED",
        updatedAt: serverTimestamp(),
      })
    );
    await assertSucceeds(
      updateDoc(doc(admin, "machinery_research_queue", "rq_1"), {
        status: "APPROVED",
        updatedAt: serverTimestamp(),
      })
    );
  });
});

describe("Krishak Sathi Trust, Anti-Fraud, Suspensions & Grievance Security Rules", () => {
  test("fraud_reports enforces reporter ownership, initial OPEN status, and admin-only moderation", async () => {
    const anon = testEnv.unauthenticatedContext().firestore();
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");
    const admin = adminDb();

    const validReport = {
      targetType: "SELLER",
      targetId: "seller_shady_01",
      targetName: "Shady Agrotech",
      sellerUserId: "seller_shady_01",
      reasonCode: "ADVANCE_PAYMENT_SCAM",
      description: "Demanded UPI advance before delivering pump.",
      reporterUserId: "farmer_alice",
      reporterName: "Alice Mondal",
      status: "OPEN",
      adminNote: "",
      resolutionAction: "NONE",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    };

    await assertFails(setDoc(doc(anon, "fraud_reports", "fr_1"), validReport));
    await assertFails(setDoc(doc(bob, "fraud_reports", "fr_spoof"), validReport));
    await assertFails(setDoc(doc(alice, "fraud_reports", "fr_resolved"), {
      ...validReport,
      status: "ACTION_TAKEN",
    }));

    await assertSucceeds(setDoc(doc(alice, "fraud_reports", "fr_1"), validReport));
    await assertSucceeds(getDoc(doc(alice, "fraud_reports", "fr_1")));
    await assertFails(getDoc(doc(bob, "fraud_reports", "fr_1")));
    await assertFails(updateDoc(doc(alice, "fraud_reports", "fr_1"), {
      status: "ACTION_TAKEN",
      updatedAt: serverTimestamp(),
    }));

    await assertSucceeds(getDoc(doc(admin, "fraud_reports", "fr_1")));
    await assertSucceeds(updateDoc(doc(admin, "fraud_reports", "fr_1"), {
      status: "ACTION_TAKEN",
      resolutionAction: "SELLER_SUSPENDED",
      adminNote: "Confirmed advance fee scam; seller suspended.",
      updatedAt: serverTimestamp(),
    }));
  });

  test("grievance_tickets protects user privacy and enforces admin-only responses", async () => {
    const alice = farmerDb("farmer_alice");
    const bob = farmerDb("farmer_bob");
    const admin = adminDb();

    const ticket = {
      userId: "farmer_alice",
      userName: "Alice Mondal",
      category: "MARKETPLACE",
      subject: "Wrong delivery details",
      description: "Need support resolving dealer address mismatch.",
      relatedEntityId: "inq_101",
      status: "SUBMITTED",
      adminResponse: "",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    };

    await assertFails(setDoc(doc(bob, "grievance_tickets", "grv_spoof"), ticket));
    await assertSucceeds(setDoc(doc(alice, "grievance_tickets", "grv_1"), ticket));
    await assertSucceeds(getDoc(doc(alice, "grievance_tickets", "grv_1")));
    await assertFails(getDoc(doc(bob, "grievance_tickets", "grv_1")));
    await assertFails(updateDoc(doc(alice, "grievance_tickets", "grv_1"), {
      status: "RESOLVED",
      updatedAt: serverTimestamp(),
    }));

    await assertSucceeds(updateDoc(doc(admin, "grievance_tickets", "grv_1"), {
      status: "RESOLVED",
      adminResponse: "Updated dealer verified address and issued advisory.",
      updatedAt: serverTimestamp(),
    }));
  });

  test("seller_suspensions & trust_audit_logs enforce strict admin authority and log immutability", async () => {
    const anon = testEnv.unauthenticatedContext().firestore();
    const alice = farmerDb("farmer_alice");
    const admin = adminDb();

    const suspension = {
      sellerUserId: "seller_shady_01",
      businessName: "Shady Agrotech",
      isSuspended: true,
      reason: "Advance fee scam confirmed",
      suspendedByAdmin: ADMIN_EMAIL,
      updatedAt: serverTimestamp(),
    };

    await assertFails(setDoc(doc(alice, "seller_suspensions", "seller_shady_01"), suspension));
    await assertSucceeds(setDoc(doc(admin, "seller_suspensions", "seller_shady_01"), suspension));
    await assertSucceeds(getDoc(doc(alice, "seller_suspensions", "seller_shady_01")));
    await assertFails(getDoc(doc(anon, "seller_suspensions", "seller_shady_01")));

    const auditLog = {
      adminEmail: ADMIN_EMAIL,
      action: "SUSPEND_SELLER",
      targetType: "SELLER",
      targetId: "seller_shady_01",
      summary: "Suspended seller_shady_01",
      createdAt: serverTimestamp(),
    };
    await assertFails(setDoc(doc(alice, "trust_audit_logs", "taudit_1"), auditLog));
    await assertSucceeds(setDoc(doc(admin, "trust_audit_logs", "taudit_1"), auditLog));
    await assertFails(getDoc(doc(alice, "trust_audit_logs", "taudit_1")));
    await assertFails(updateDoc(doc(admin, "trust_audit_logs", "taudit_1"), { action: "TAMPERED" }));
    await assertFails(deleteDoc(doc(admin, "trust_audit_logs", "taudit_1")));
  });
});






