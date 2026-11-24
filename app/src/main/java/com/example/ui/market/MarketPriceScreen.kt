package com.example.ui.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.MarketPriceDatabase
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriPriceGold
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.ui.theme.AgriWarningAmber
import com.example.util.L10n

@Composable
fun MarketPriceScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allPrices = remember { MarketPriceDatabase.prices }

    val filteredPrices = remember(searchQuery, currentLanguage) {
        if (searchQuery.isBlank()) allPrices else {
            allPrices.filter {
                it.getProduct(AppLanguage.BENGALI).contains(searchQuery, ignoreCase = true) ||
                        it.getProduct(AppLanguage.HINDI).contains(searchQuery, ignoreCase = true) ||
                        it.getProduct(AppLanguage.ENGLISH).contains(searchQuery, ignoreCase = true) ||
                        it.getMarketName(currentLanguage).contains(searchQuery, ignoreCase = true) ||
                        it.getDistrict(currentLanguage).contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("market_price_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "💰 ${L10n.t("market_price_title", currentLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = L10n.t("market_price_notice", currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(L10n.t("market_price_search_hint", currentLanguage)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("market_search_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (filteredPrices.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "এই অনুসন্ধানের জন্য কোনো বাজার দর পাওয়া যায়নি।"
                                    AppLanguage.HINDI -> "इस खोज के लिए कोई मंडी भाव नहीं मिला।"
                                    AppLanguage.ENGLISH -> "No market prices match your search query."
                                },
                                modifier = Modifier.padding(16.dp),
                                color = AgriTextSecondary
                            )
                        }
                    }
                } else {
                    items(filteredPrices) { p ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = p.getProduct(currentLanguage),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Trend Icon
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        when (p.trend) {
                                            "UP" -> Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Up", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                            "DOWN" -> Icon(imageVector = Icons.Default.TrendingDown, contentDescription = "Down", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                                            else -> Icon(imageVector = Icons.Default.TrendingFlat, contentDescription = "Stable", tint = AgriWarningAmber, modifier = Modifier.size(22.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = p.getTrendLabel(currentLanguage),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (p.trend == "UP") MaterialTheme.colorScheme.primary else if (p.trend == "DOWN") MaterialTheme.colorScheme.error else AgriWarningAmber
                                        )
                                    }
                                }

                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "গড় দর: ₹${p.modalPrice} ${p.getUnit(currentLanguage)}"
                                        AppLanguage.HINDI -> "औसत भाव: ₹${p.modalPrice} ${p.getUnit(currentLanguage)}"
                                        AppLanguage.ENGLISH -> "Modal Price: ₹${p.modalPrice} ${p.getUnit(currentLanguage)}"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriPriceGold
                                )

                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সর্বনিম্ন: ₹${p.minPrice} • সর্বোচ্চ: ₹${p.maxPrice}"
                                        AppLanguage.HINDI -> "न्यूनतम: ₹${p.minPrice} • अधिकतम: ₹${p.maxPrice}"
                                        AppLanguage.ENGLISH -> "Min: ₹${p.minPrice} • Max: ₹${p.maxPrice}"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = AgriTextSecondary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "বাজার: ${p.getMarketName(currentLanguage)} (${p.getDistrict(currentLanguage)})"
                                            AppLanguage.HINDI -> "मंडी: ${p.getMarketName(currentLanguage)} (${p.getDistrict(currentLanguage)})"
                                            AppLanguage.ENGLISH -> "Market: ${p.getMarketName(currentLanguage)} (${p.getDistrict(currentLanguage)})"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = AgriTextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = p.getLastUpdated(currentLanguage),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "উৎস: ${p.getSource(currentLanguage)}"
                                        AppLanguage.HINDI -> "स्रोत: ${p.getSource(currentLanguage)}"
                                        AppLanguage.ENGLISH -> "Source: ${p.getSource(currentLanguage)}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = AgriTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
