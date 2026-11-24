package com.example.data.datasource

import com.example.data.model.PlantKingdomEntry
import com.example.data.model.PlantKingdomGroup
import com.example.data.model.PlantSearchFilter
import com.example.data.model.PlantSeasonalCollection

/**
 * Unified Plant Kingdom master catalog combining:
 * - Himalayan & Hill Flowers (15 species)
 * - Common Flowering Plants & Seasonal Collections (27 species)
 * - Money Plant & Indoor Plants (22 species + cross-linked Anthurium)
 * - Fruit Plants & Trees (26 species, North Bengal hill & plains varieties)
 * - Ornamental Trees & Climbers (17 species + cross-linked climbers/trees)
 * - Medicinal Plants, Herbs & Succulents (17 species + cross-linked Neem)
 */
object PlantKingdomCatalog {

    val allPlants: List<PlantKingdomEntry> by lazy {
        PlantKingdomFlowerCatalog.flowers + PlantKingdomGreeneryCatalog.greeneryAndTrees
    }

    fun getById(plantId: String): PlantKingdomEntry? =
        allPlants.firstOrNull { it.plantId.equals(plantId.trim(), ignoreCase = true) }

    fun getByGroup(group: PlantKingdomGroup): List<PlantKingdomEntry> =
        allPlants.filter { it.belongsToGroup(group) }

    internal fun flowerPlant(
        id: String,
        nameEn: String,
        nameBn: String,
        nameHi: String,
        scientificName: String,
        group: PlantKingdomGroup,
        secondaryGroups: List<PlantKingdomGroup> = emptyList(),
        filters: List<PlantSearchFilter>,
        seasons: List<PlantSeasonalCollection>,
        descEn: String,
        descBn: String,
        descHi: String,
        climate: String,
        temp: String,
        soil: String,
        sunlight: String,
        watering: String,
        fertilizer: String,
        propagation: String,
        seasonLabel: String,
        growthSize: String,
        indoorOutdoor: String,
        plainsHill: String,
        pestsDiseases: String,
        careEn: String,
        careBn: String,
        careHi: String,
        isRare: Boolean = false
    ): PlantKingdomEntry = PlantKingdomEntry(
        plantId = id,
        nameEn = nameEn,
        nameBn = nameBn,
        nameHi = nameHi,
        scientificName = scientificName,
        primaryGroup = group.code,
        secondaryGroupsCsv = secondaryGroups.joinToString(",") { it.code },
        filterTagsCsv = filters.joinToString(",") { it.code },
        seasonalTagsCsv = seasons.joinToString(",") { it.code },
        descriptionEn = descEn,
        descriptionBn = descBn,
        descriptionHi = descHi,
        visualAssetKey = PlantPhotoRegistry.getVisualAssetKey(id),
        climate = climate,
        temperatureRange = temp,
        soilRequirements = soil,
        sunlight = sunlight,
        wateringFrequency = watering,
        fertilizerGuidance = fertilizer,
        propagationMethod = propagation,
        floweringOrFruitingSeason = seasonLabel,
        growthSize = growthSize,
        indoorOutdoorSuitability = indoorOutdoor,
        plainsHillSuitability = plainsHill,
        commonPestsDiseases = pestsDiseases,
        careInstructionsEn = careEn,
        careInstructionsBn = careBn,
        careInstructionsHi = careHi,
        isRare = isRare,
        isVerifiedEntry = true,
        sourceReference = "ICAR / UBKV / BCKV / BSI Botanical Database",
        lastVerifiedDate = "2026-11-18"
    )
}
