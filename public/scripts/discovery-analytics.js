/* Privacy-safe referral & discovery analytics for KRISHAK SATHI public pages.
   Stores strictly non-PII aggregate referrer buckets locally (zero IP, email, or user tracking). */
(function () {
  try {
    var ref = (document.referrer || "").toLowerCase();
    var bucket = "direct";
    if (ref.indexOf("google.") !== -1) bucket = "google_search";
    else if (ref.indexOf("chatgpt.com") !== -1 || ref.indexOf("openai.com") !== -1 || ref.indexOf("oai") !== -1) bucket = "chatgpt_search";
    else if (ref.indexOf("gemini.google") !== -1 || ref.indexOf("bard.google") !== -1) bucket = "gemini_discovery";
    else if (ref.indexOf("bing.") !== -1 || ref.indexOf("duckduckgo.") !== -1) bucket = "other_search";
    else if (ref.length > 0) bucket = "external_referral";

    var storageKey = "krishak_sathi_referral_buckets";
    var existing = {};
    try {
      existing = JSON.parse(localStorage.getItem(storageKey) || "{}");
    } catch (e) {
      existing = {};
    }
    var pageKey = window.location.pathname || "/";
    existing[bucket] = (existing[bucket] || 0) + 1;
    existing.lastPage = pageKey;
    existing.lastUpdatedIso = new Date().toISOString();
    localStorage.setItem(storageKey, JSON.stringify(existing));
  } catch (_) {
    // Graceful no-op in restricted browser contexts
  }
})();
