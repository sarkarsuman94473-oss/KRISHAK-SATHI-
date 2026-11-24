package com.example.data.datasource

import com.example.R
import com.example.data.model.MasterCrop

/**
 * Stable cropId-keyed photograph mapping registry.
 * Guarantees the exact same photograph is resolved regardless of the active UI language
 * (Bengali, Hindi, or English) and never maps unrelated/wrong crop photographs to another crop.
 */
object CropPhotoRegistry {

    private val dedicatedDrawablesByCropId: Map<String, Int> = mapOf(
        "tea" to R.drawable.img_tea_leaves_1793343045221,
        "palak" to R.drawable.img_palak_1793343281579,
        "cabbage" to R.drawable.img_cabbage_1793343062183,
        "cauliflower" to R.drawable.img_cauliflower_1793343216647,
        "tomato" to R.drawable.img_tomato_1793343030528,
        "potato" to R.drawable.img_potato_1793343095516,
        "chilli" to R.drawable.img_chilli_1793343108793,
        "brinjal" to R.drawable.img_brinjal_1793343230024,
        "okra" to R.drawable.img_okra_1793343244337,
        "mango" to R.drawable.img_mango_1793343080850,
        "banana" to R.drawable.img_banana_1793343260587,
        "rose" to R.drawable.img_rose_flower_1794803719416,
        "rice_paddy" to R.drawable.img_rice_paddy_1794803719617,
        "marigold" to R.drawable.img_marigold_flower_1794803720119,
        "turmeric" to R.drawable.img_turmeric_spice_1794803720155,
        "mustard" to R.drawable.img_mustard_crop_1794803721161
    )

    fun getPhotoResId(cropId: String): Int? =
        dedicatedDrawablesByCropId[cropId.trim().lowercase()]

    fun getGalleryResIds(crop: MasterCrop): List<Int> {
        val primary = crop.imageResId ?: getPhotoResId(crop.cropId)
        val combined = buildList {
            if (primary != null) add(primary)
            addAll(crop.galleryImageResIds)
        }
        return combined.distinct()
    }

    fun getAllMappedCropIds(): Set<String> = dedicatedDrawablesByCropId.keys
}
