package com.example.data.datasource

import androidx.annotation.DrawableRes
import com.example.R
import com.example.data.model.AppLanguage

enum class MachineryImageSourceType(
    val code: String,
    val labelEn: String,
    val labelBn: String,
    val labelHi: String
) {
    UPLOADED_ORIGINAL(
        code = "UPLOADED_ORIGINAL",
        labelEn = "Original Photo",
        labelBn = "আসল ছবি",
        labelHi = "असली फोटो"
    ),
    POSTER_EDUCATIONAL_ILLUSTRATION(
        code = "POSTER_EDUCATIONAL_ILLUSTRATION",
        labelEn = "Educational Illustration",
        labelBn = "পরিচিতি চিত্র",
        labelHi = "परिचय चित्र"
    ),
    DEALER_UPLOAD(
        code = "DEALER_UPLOAD",
        labelEn = "Partner Upload",
        labelBn = "ডিলারের ছবি",
        labelHi = "डीलर फोटो"
    );

    fun getDisplayLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> labelBn
        AppLanguage.HINDI -> labelHi
        AppLanguage.ENGLISH -> labelEn
    }
}

data class MachineryImageAsset(
    val token: String,
    @DrawableRes val drawableRes: Int,
    val sourceType: MachineryImageSourceType,
    val usagePermission: String,
    val reviewStatus: String,
    val captionEn: String
)

/**
 * Central Registry mapping:
 * - All 24 original agricultural equipment photographs (`drawable:mach_*`) returned by [allUploadedPhotoTokens]
 * - All 64 clean individual WebP illustrations extracted from user-provided category posters (`drawable:<slug>`)
 *   returned by [allPosterIllustrationTokens]
 *
 * Keeps image provenance transparent (Original Photo vs Educational Illustration) so farmers and admins
 * can audit image usage safely without watermarked/labeled posters.
 */
object MachineryPhotoRegistry {

    const val PREFIX_DRAWABLE = "drawable:"

    private val uploadedOriginalByKey: Map<String, Int> = linkedMapOf(
        "drawable:mach_01_mahindra_mahavator_rotavator" to R.drawable.mach_01_mahindra_mahavator_rotavator,
        "drawable:mach_02_vst_shakti_130di_power_tiller" to R.drawable.mach_02_vst_shakti_130di_power_tiller,
        "drawable:mach_03_virat_rotavator" to R.drawable.mach_03_virat_rotavator,
        "drawable:mach_04_pammi_samrala_rotavator" to R.drawable.mach_04_pammi_samrala_rotavator,
        "drawable:mach_05_yanmar_mini_excavator" to R.drawable.mach_05_yanmar_mini_excavator,
        "drawable:mach_06_fieldking_seed_fertilizer_drill" to R.drawable.mach_06_fieldking_seed_fertilizer_drill,
        "drawable:mach_07_four_furrow_disc_plough" to R.drawable.mach_07_four_furrow_disc_plough,
        "drawable:mach_08_irrigation_filter_manifold" to R.drawable.mach_08_irrigation_filter_manifold,
        "drawable:mach_09_lancer_dhanush_composite" to R.drawable.mach_09_lancer_dhanush_composite,
        "drawable:mach_10_sonalika_tiger_mb_plough" to R.drawable.mach_10_sonalika_tiger_mb_plough,
        "drawable:mach_11_farmtrac_60_epi_mb_plough" to R.drawable.mach_11_farmtrac_60_epi_mb_plough,
        "drawable:mach_12_kubota_super_one_rotavator" to R.drawable.mach_12_kubota_super_one_rotavator,
        "drawable:mach_13_hydraulic_5furrow_mb_plough" to R.drawable.mach_13_hydraulic_5furrow_mb_plough,
        "drawable:mach_14_wegmaan_reversible_plough" to R.drawable.mach_14_wegmaan_reversible_plough,
        "drawable:mach_15_tractor_boom_sprayer" to R.drawable.mach_15_tractor_boom_sprayer,
        "drawable:mach_16_knapsack_power_sprayer" to R.drawable.mach_16_knapsack_power_sprayer,
        "drawable:mach_17_electric_centrifugal_pump" to R.drawable.mach_17_electric_centrifugal_pump,
        "drawable:mach_18_farmtrac_60_epi_rotavator" to R.drawable.mach_18_farmtrac_60_epi_rotavator,
        "drawable:mach_19_kawashima_earth_auger" to R.drawable.mach_19_kawashima_earth_auger,
        "drawable:mach_20_malkit_combine_harvester" to R.drawable.mach_20_malkit_combine_harvester,
        "drawable:mach_21_orchard_mini_excavator" to R.drawable.mach_21_orchard_mini_excavator,
        "drawable:mach_22_handheld_earth_auger_field" to R.drawable.mach_22_handheld_earth_auger_field,
        "drawable:mach_23_pneumatic_12row_planter" to R.drawable.mach_23_pneumatic_12row_planter,
        "drawable:mach_24_rideon_paddy_transplanter" to R.drawable.mach_24_rideon_paddy_transplanter
    )

    private val posterIllustrationByKey: Map<String, Int> = linkedMapOf(
        // Land Preparation
        "drawable:tractor" to R.drawable.tractor,
        "drawable:mini_tractor" to R.drawable.mini_tractor,
        "drawable:power_tiller" to R.drawable.power_tiller,
        "drawable:rotavator" to R.drawable.rotavator,
        "drawable:cultivator" to R.drawable.cultivator,
        "drawable:plough" to R.drawable.plough,
        "drawable:disc_harrow" to R.drawable.disc_harrow,
        "drawable:land_leveller" to R.drawable.land_leveller,
        // Seeding & Planting
        "drawable:seed_drill" to R.drawable.seed_drill,
        "drawable:multi_crop_planter" to R.drawable.multi_crop_planter,
        "drawable:zero_till_seed_drill" to R.drawable.zero_till_seed_drill,
        "drawable:vegetable_seeder" to R.drawable.vegetable_seeder,
        "drawable:paddy_transplanter" to R.drawable.paddy_transplanter,
        "drawable:nursery_tray_seeder" to R.drawable.nursery_tray_seeder,
        "drawable:precision_planter" to R.drawable.precision_planter,
        "drawable:maize_planter" to R.drawable.maize_planter,
        // Irrigation & Water Management
        "drawable:diesel_pump" to R.drawable.diesel_pump,
        "drawable:electric_pump" to R.drawable.electric_pump,
        "drawable:solar_pump" to R.drawable.solar_pump,
        "drawable:submersible_pump" to R.drawable.submersible_pump,
        "drawable:drip_irrigation" to R.drawable.drip_irrigation,
        "drawable:sprinkler_system" to R.drawable.sprinkler_system,
        "drawable:rain_gun" to R.drawable.rain_gun,
        "drawable:filters_pipes" to R.drawable.filters_pipes,
        // Crop Protection & Spraying
        "drawable:knapsack_sprayer" to R.drawable.knapsack_sprayer,
        "drawable:battery_sprayer" to R.drawable.battery_sprayer,
        "drawable:power_sprayer" to R.drawable.power_sprayer,
        "drawable:mist_blower" to R.drawable.mist_blower,
        "drawable:boom_sprayer" to R.drawable.boom_sprayer,
        "drawable:drone_sprayer" to R.drawable.drone_sprayer,
        "drawable:fogging_machine" to R.drawable.fogging_machine,
        "drawable:fertilizer_spreader" to R.drawable.fertilizer_spreader,
        // Post-Harvest & Processing
        "drawable:mini_rice_mill" to R.drawable.mini_rice_mill,
        "drawable:flour_mill" to R.drawable.flour_mill,
        "drawable:maize_sheller" to R.drawable.maize_sheller,
        "drawable:dal_mill" to R.drawable.dal_mill,
        "drawable:oil_expeller" to R.drawable.oil_expeller,
        "drawable:grain_cleaner" to R.drawable.grain_cleaner,
        "drawable:chaff_cutter" to R.drawable.chaff_cutter,
        "drawable:grain_dryer" to R.drawable.grain_dryer,
        // Smart Farming & Precision Agriculture
        "drawable:agricultural_drone" to R.drawable.agricultural_drone,
        "drawable:crop_monitoring_drone" to R.drawable.crop_monitoring_drone,
        "drawable:gps_survey_device" to R.drawable.gps_survey_device,
        "drawable:soil_testing_kit" to R.drawable.soil_testing_kit,
        "drawable:soil_moisture_sensor" to R.drawable.soil_moisture_sensor,
        "drawable:weather_station" to R.drawable.weather_station,
        "drawable:automatic_irrigation_controller" to R.drawable.automatic_irrigation_controller,
        "drawable:farm_camera" to R.drawable.farm_camera,
        // Gardening, Tea Garden & Horticulture
        "drawable:brush_cutter" to R.drawable.brush_cutter,
        "drawable:grass_cutter" to R.drawable.grass_cutter,
        "drawable:hedge_trimmer" to R.drawable.hedge_trimmer,
        "drawable:chainsaw" to R.drawable.chainsaw,
        "drawable:lawn_mower" to R.drawable.lawn_mower,
        "drawable:leaf_blower" to R.drawable.leaf_blower,
        "drawable:tea_pruning_machine" to R.drawable.tea_pruning_machine,
        "drawable:greenhouse_equipment" to R.drawable.greenhouse_equipment,
        // Spare Parts & Workshop Tools
        "drawable:tractor_tyre" to R.drawable.tractor_tyre,
        "drawable:tractor_battery" to R.drawable.tractor_battery,
        "drawable:machinery_belts" to R.drawable.machinery_belts,
        "drawable:machinery_bearings" to R.drawable.machinery_bearings,
        "drawable:machinery_filters" to R.drawable.machinery_filters,
        "drawable:engine_parts" to R.drawable.engine_parts,
        "drawable:sprayer_nozzle" to R.drawable.sprayer_nozzle,
        "drawable:repair_tools" to R.drawable.repair_tools
    )

    private val allDrawablesByKey: Map<String, Int> = uploadedOriginalByKey + posterIllustrationByKey

    @DrawableRes
    fun resolveDrawableRes(token: String?): Int? {
        if (token.isNullOrBlank()) return null
        val normalized = normalizeToken(token)
        return allDrawablesByKey[normalized]
    }

    fun normalizeToken(token: String): String {
        val trimmed = token.trim()
        return if (trimmed.startsWith(PREFIX_DRAWABLE)) trimmed else "$PREFIX_DRAWABLE$trimmed"
    }

    /**
     * Preserves exact 24 original uploaded equipment photos contract.
     */
    fun allUploadedPhotoTokens(): List<String> = uploadedOriginalByKey.keys.toList()

    /**
     * Returns all 64 clean individual WebP illustrations extracted from uploaded category posters.
     */
    fun allPosterIllustrationTokens(): List<String> = posterIllustrationByKey.keys.toList()

    /**
     * Returns complete provenance registry across all 88 bundled visual assets (24 JPG + 64 WebP).
     */
    fun allImageAssets(): List<MachineryImageAsset> = buildList {
        uploadedOriginalByKey.forEach { (token, resId) ->
            val slug = token.removePrefix(PREFIX_DRAWABLE).replace("_", " ")
            add(
                MachineryImageAsset(
                    token = token,
                    drawableRes = resId,
                    sourceType = MachineryImageSourceType.UPLOADED_ORIGINAL,
                    usagePermission = "USER_UPLOADED_ORIGINAL",
                    reviewStatus = "VERIFIED",
                    captionEn = "User-uploaded original equipment photograph ($slug)"
                )
            )
        }
        posterIllustrationByKey.forEach { (token, resId) ->
            val slug = token.removePrefix(PREFIX_DRAWABLE).replace("_", " ")
            add(
                MachineryImageAsset(
                    token = token,
                    drawableRes = resId,
                    sourceType = MachineryImageSourceType.POSTER_EDUCATIONAL_ILLUSTRATION,
                    usagePermission = "USER_PROVIDED_EDUCATIONAL_POSTER",
                    reviewStatus = "ILLUSTRATION_LABELED",
                    captionEn = "Educational visual guide illustration ($slug) — verify actual dealer stock photo before purchase"
                )
            )
        }
    }

    fun getImageSourceType(token: String?): MachineryImageSourceType {
        if (token.isNullOrBlank()) return MachineryImageSourceType.POSTER_EDUCATIONAL_ILLUSTRATION
        val norm = normalizeToken(token)
        return when {
            uploadedOriginalByKey.containsKey(norm) -> MachineryImageSourceType.UPLOADED_ORIGINAL
            posterIllustrationByKey.containsKey(norm) -> MachineryImageSourceType.POSTER_EDUCATIONAL_ILLUSTRATION
            else -> MachineryImageSourceType.DEALER_UPLOAD
        }
    }
}
