package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Standard Hash-based routing definitions for KRISHAK SATHI:
 * - #home
 * - #ai-assistant (General-purpose Gemini AI Assistant)
 * - #marketplace (Farmer Marketplace)
 * - #resource-library (Comprehensive Agricultural Library)
 * - #my-crops
 * - #profile
 * - #farmer-ai (Existing Crop/Farm Assistant)
 */
object HashRoutes {
    const val HOME = "#home"
    const val AI_ASSISTANT = "#ai-assistant"
    const val MARKETPLACE = "#marketplace"
    const val RESOURCE_LIBRARY = "#resource-library"
    const val MY_CROPS = "#my-crops"
    const val ADD_CROP = "#add-crop"
    const val CROP_DETAIL = "#crop-detail"
    const val PROFILE = "#profile"
    const val FARMER_AI = "#farmer-ai"
    const val NURSERY_MARKET = "#nursery-market"
    const val MEDICINE_HUB = "#medicine-hub"
    const val PLANT_PROBLEM = "#plant-problem"
    const val TEA_GARDEN = "#tea-garden"
    const val WEATHER = "#weather"
    const val MARKET_PRICE = "#market-price"
    const val DOSAGE_CALCULATOR = "#dosage-calculator"
    const val EXPENSE_PROFIT = "#expense-profit"
    const val EXPERT = "#expert"
    const val GOVT_INFO = "#govt-info"
    const val ABOUT_US = "#about-us"
    const val CONTACT_US = "#contact-us"
    const val LEGAL_PAGES = "#legal-pages"
    const val FAQ = "#faq"
    const val ADMIN_CONFIG = "#admin-config"
    const val LEADING_FARMERS = "#leading-farmers"
    const val LEADING_FARMERS_ADMIN = "#leading-farmers-admin"
    const val NURSERY_REGISTRATION = "#nursery-registration"
    const val SELLER_DASHBOARD = "#seller-dashboard"
    const val ADMIN_NURSERIES = "#admin-nurseries"
    const val NURSERY_PUBLIC_PROFILE = "#nursery-public-profile"
    const val SELL_PRODUCE = "#sell-produce"
    const val ADMIN_ADD_CROP = "#admin-add-crop"
    const val COMMUNITY_FORUM = "#community-forum"
    const val GOVT_SCHEMES = "#govt-schemes"
    const val DISCOVERY_HUB = "#discovery-hub"
    const val PLANT_KINGDOM = "#plant-kingdom"
    const val NURSERY_DIRECTORY = "#nursery-directory"
    const val NURSERY_MAP = "#nursery-map"
    const val MACHINERY_HUB = "#machinery-hub"
    const val MACHINERY_DETAIL = "#machinery-detail"
    const val MACHINERY_DEALERS = "#machinery-dealers"
    const val MACHINERY_MANUFACTURERS = "#machinery-manufacturers"
    const val ADMIN_MACHINERY = "#admin-machinery"
    const val ADMIN_BANNERS = "#admin-banners"
    const val ADMIN_TRUST_MODERATION = "#admin-trust-moderation"
    const val LAND_RIGHTS = "#land-rights"

    fun normalize(rawRoute: String): String {
        val trimmed = rawRoute.trim()
        return if (trimmed.startsWith("#")) trimmed else "#$trimmed"
    }
}

@Stable
class HashRouter(initialRoute: String = HashRoutes.HOME) {
    var currentRoute by mutableStateOf(HashRoutes.normalize(initialRoute))
        private set

    private val backStack = mutableStateListOf<String>()

    fun navigate(route: String, clearTop: Boolean = false) {
        val normalized = HashRoutes.normalize(route)
        if (normalized == currentRoute) return

        if (clearTop) {
            backStack.clear()
        } else {
            backStack.add(currentRoute)
        }
        currentRoute = normalized
    }

    fun canGoBack(): Boolean = backStack.isNotEmpty()

    fun goBack(): Boolean {
        if (backStack.isNotEmpty()) {
            val previous = backStack.removeAt(backStack.size - 1)
            currentRoute = previous
            return true
        }
        return false
    }

    fun resetToHome() {
        backStack.clear()
        currentRoute = HashRoutes.HOME
    }
}

@Composable
fun rememberHashRouter(initialRoute: String = HashRoutes.HOME): HashRouter {
    return remember { HashRouter(initialRoute) }
}
