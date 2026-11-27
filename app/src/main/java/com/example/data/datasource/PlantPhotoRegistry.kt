package com.example.data.datasource

import androidx.annotation.DrawableRes
import com.example.R

enum class BotanicalMorphology {
    ORCHID,
    BLUE_POPPY,
    RHODODENDRON,
    MAGNOLIA,
    PRIMULA_CLUSTER,
    BELL_HYDRANGEA,
    POTHOS_HEART,
    MONSTERA_FENESTRATED,
    SNAKE_SWORD,
    ZZ_GLOSSY,
    PALM_FROND,
    FEATHER_FERN,
    CACTUS_RIBBED,
    ROSETTE_SUCCULENT,
    HERB_AROMATIC,
    FLOWER_ROSE_LAYERS,
    FLOWER_TRUMPET,
    FLOWER_DAISY_STAR,
    FLOWER_SPIKE,
    CITRUS_ORCHARD,
    TROPICAL_FRUIT,
    TEMPERATE_ORCHARD,
    CANOPY_TREE,
    CLIMBER_VINE
}

data class PlantVisualProfile(
    val visualAssetKey: String,
    @DrawableRes val exactPhotoResId: Int? = null,
    val morphology: BotanicalMorphology,
    val primaryHex: Long,
    val secondaryHex: Long,
    val foliageHex: Long,
    val petalCount: Int = 5,
    val variegated: Boolean = false
)

/**
 * Strict 1-to-1 botanical photograph & artwork registry keyed exclusively by canonical plantId.
 * Real .jpg drawables are linked ONLY to exact matching species and NEVER repeated on unrelated plants.
 * Every species without a bundled .jpg receives a distinct morphology + color botanical profile keyed by `plant_art_<plantId>`.
 */
object PlantPhotoRegistry {

    private val exactRealPhotosByPlantId: Map<String, Int> = mapOf(
        "rose" to R.drawable.img_rose_flower_1794803719416,
        "marigold" to R.drawable.img_marigold_flower_1794803720119,
        "mango" to R.drawable.img_mango_1793343080850,
        "banana" to R.drawable.img_banana_1793343260587,
        "litchi" to R.drawable.img_litchi_orchard_bunch_1795520199060,
        "guava" to R.drawable.img_guava_sapling_fruit_1795520198847,
        "darjeeling_orange" to R.drawable.img_citrus_orchard_fresh_1795520199038
    )

    private val speciesStyles: Map<String, PlantVisualProfile> = mapOf(
        // Himalayan & Hill Flowers (15)
        "rhododendron" to profile("rhododendron", BotanicalMorphology.RHODODENDRON, 0xFFD81B60, 0xFFFF80AB, 0xFF1B5E20, 7),
        "magnolia" to profile("magnolia", BotanicalMorphology.MAGNOLIA, 0xFFFCE4EC, 0xFFF48FB1, 0xFF2E7D32, 9),
        "himalayan_blue_poppy" to profile("himalayan_blue_poppy", BotanicalMorphology.BLUE_POPPY, 0xFF1E88E5, 0xFFFFD54F, 0xFF33691E, 4),
        "primula" to profile("primula", BotanicalMorphology.PRIMULA_CLUSTER, 0xFF8E24AA, 0xFFCE93D8, 0xFF2E7D32, 8),
        "cymbidium_orchid" to profile("cymbidium_orchid", BotanicalMorphology.ORCHID, 0xFFFDD835, 0xFFAD1457, 0xFF1B5E20, 5),
        "dendrobium_orchid" to profile("dendrobium_orchid", BotanicalMorphology.ORCHID, 0xFFAB47BC, 0xFFFFFFFF, 0xFF2E7D32, 5),
        "coelogyne_orchid" to profile("coelogyne_orchid", BotanicalMorphology.ORCHID, 0xFFFFFFFF, 0xFFFFB300, 0xFF388E3C, 5),
        "pleione_orchid" to profile("pleione_orchid", BotanicalMorphology.ORCHID, 0xFFEC407A, 0xFFFFCA28, 0xFF2E7D32, 5),
        "paphiopedilum_orchid" to profile("paphiopedilum_orchid", BotanicalMorphology.ORCHID, 0xFF7CB342, 0xFF6D4C41, 0xFF1B5E20, 4, variegated = true),
        "himalayan_begonia" to profile("himalayan_begonia", BotanicalMorphology.POTHOS_HEART, 0xFFC2185B, 0xFFE8EAF6, 0xFF4A148C, 4, variegated = true),
        "impatiens" to profile("impatiens", BotanicalMorphology.FLOWER_ROSE_LAYERS, 0xFFFF5252, 0xFFFFCDD2, 0xFF2E7D32, 5),
        "hydrangea" to profile("hydrangea", BotanicalMorphology.BELL_HYDRANGEA, 0xFF42A5F5, 0xFFB39DDB, 0xFF1B5E20, 12),
        "arisaema" to profile("arisaema", BotanicalMorphology.FLOWER_TRUMPET, 0xFF558B2F, 0xFF4E342E, 0xFF1B5E20, 3),
        "himalayan_lily" to profile("himalayan_lily", BotanicalMorphology.FLOWER_TRUMPET, 0xFFFFF8E1, 0xFF880E4F, 0xFF2E7D32, 6),
        "himalayan_iris" to profile("himalayan_iris", BotanicalMorphology.ORCHID, 0xFF3949AB, 0xFFFFD54F, 0xFF33691E, 6),

        // Common Flowering Plants (27)
        "rose" to profile("rose", BotanicalMorphology.FLOWER_ROSE_LAYERS, 0xFFC62828, 0xFFFF8A80, 0xFF1B5E20, 12),
        "marigold" to profile("marigold", BotanicalMorphology.FLOWER_ROSE_LAYERS, 0xFFFF8F00, 0xFFFFD54F, 0xFF2E7D32, 16),
        "hibiscus" to profile("hibiscus", BotanicalMorphology.FLOWER_TRUMPET, 0xFFD50000, 0xFFFFD600, 0xFF1B5E20, 5),
        "tuberose" to profile("tuberose", BotanicalMorphology.FLOWER_SPIKE, 0xFFF5F5F5, 0xFFC8E6C9, 0xFF2E7D32, 8),
        "jasmine" to profile("jasmine", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFFFFFFF, 0xFFFFF59D, 0xFF1B5E20, 7),
        "mogra" to profile("mogra", BotanicalMorphology.FLOWER_ROSE_LAYERS, 0xFFFAFAFA, 0xFFE6EE9C, 0xFF2E7D32, 10),
        "gardenia" to profile("gardenia", BotanicalMorphology.MAGNOLIA, 0xFFFFFDE7, 0xFFFFECB3, 0xFF004D40, 9),
        "tagar" to profile("tagar", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFFFFFFF, 0xFFFDD835, 0xFF1B5E20, 5),
        "aparajita" to profile("aparajita", BotanicalMorphology.CLIMBER_VINE, 0xFF1565C0, 0xFFFFF9C4, 0xFF2E7D32, 5),
        "periwinkle" to profile("periwinkle", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFE91E63, 0xFFFFFFFF, 0xFF1B5E20, 5),
        "night_flowering_jasmine" to profile("night_flowering_jasmine", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFFFFFFF, 0xFFFF6D00, 0xFF2E7D32, 6),
        "champa" to profile("champa", BotanicalMorphology.MAGNOLIA, 0xFFFFB300, 0xFFFFE082, 0xFF1B5E20, 8),
        "kamini" to profile("kamini", BotanicalMorphology.PRIMULA_CLUSTER, 0xFFF9FBE7, 0xFFC0CA33, 0xFF1B5E20, 6),
        "bougainvillea" to profile("bougainvillea", BotanicalMorphology.CLIMBER_VINE, 0xFFD81B60, 0xFFFF80AB, 0xFF2E7D32, 6),
        "dahlia" to profile("dahlia", BotanicalMorphology.FLOWER_ROSE_LAYERS, 0xFFAD1457, 0xFFFFAB91, 0xFF1B5E20, 14),
        "chrysanthemum" to profile("chrysanthemum", BotanicalMorphology.FLOWER_ROSE_LAYERS, 0xFFFBC02D, 0xFFFFF176, 0xFF2E7D32, 18),
        "petunia" to profile("petunia", BotanicalMorphology.FLOWER_TRUMPET, 0xFF7B1FA2, 0xFFF8BBD0, 0xFF33691E, 5),
        "pansy" to profile("pansy", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFF4A148C, 0xFFFFD600, 0xFF2E7D32, 5),
        "calendula" to profile("calendula", BotanicalMorphology.FLOWER_ROSE_LAYERS, 0xFFEF6C00, 0xFFFFB74D, 0xFF33691E, 14),
        "gladiolus" to profile("gladiolus", BotanicalMorphology.FLOWER_SPIKE, 0xFFE53935, 0xFFFFCDD2, 0xFF2E7D32, 7),
        "zinnia" to profile("zinnia", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFF4511E, 0xFFFFCC80, 0xFF2E7D32, 12),
        "cosmos" to profile("cosmos", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFEC407A, 0xFFFFD54F, 0xFF388E3C, 8),
        "salvia" to profile("salvia", BotanicalMorphology.FLOWER_SPIKE, 0xFFB71C1C, 0xFFFF5252, 0xFF1B5E20, 9),
        "dianthus" to profile("dianthus", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFC2185B, 0xFFFFFFFF, 0xFF00695C, 8),
        "gerbera" to profile("gerbera", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFFF7043, 0xFFFFECB3, 0xFF2E7D32, 16),
        "lily" to profile("lily", BotanicalMorphology.FLOWER_TRUMPET, 0xFFFFA726, 0xFFFFE0B2, 0xFF1B5E20, 6),
        "anthurium" to profile("anthurium", BotanicalMorphology.POTHOS_HEART, 0xFFD32F2F, 0xFFFFEB3B, 0xFF1B5E20, 3),

        // Money Plant & Indoor Collection (22)
        "golden_pothos" to profile("golden_pothos", BotanicalMorphology.POTHOS_HEART, 0xFF66BB6A, 0xFFFFD54F, 0xFF1B5E20, 4, variegated = true),
        "money_plant" to profile("money_plant", BotanicalMorphology.POTHOS_HEART, 0xFF2E7D32, 0xFF81C784, 0xFF0B3B2B, 4),
        "marble_queen_pothos" to profile("marble_queen_pothos", BotanicalMorphology.POTHOS_HEART, 0xFFE8F5E9, 0xFF43A047, 0xFF1B5E20, 4, variegated = true),
        "neon_pothos" to profile("neon_pothos", BotanicalMorphology.POTHOS_HEART, 0xFFAEEA00, 0xFFDCE775, 0xFF33691E, 4),
        "manjula_pothos" to profile("manjula_pothos", BotanicalMorphology.POTHOS_HEART, 0xFFF1F8E9, 0xFF2E7D32, 0xFF004D40, 4, variegated = true),
        "cebu_blue_pothos" to profile("cebu_blue_pothos", BotanicalMorphology.POTHOS_HEART, 0xFF4DB6AC, 0xFF80CBC4, 0xFF004D40, 4),
        "snake_plant" to profile("snake_plant", BotanicalMorphology.SNAKE_SWORD, 0xFF2E7D32, 0xFFFDD835, 0xFF0B3B2B, 5, variegated = true),
        "zz_plant" to profile("zz_plant", BotanicalMorphology.ZZ_GLOSSY, 0xFF1B5E20, 0xFF66BB6A, 0xFF062D20, 6),
        "monstera" to profile("monstera", BotanicalMorphology.MONSTERA_FENESTRATED, 0xFF1B5E20, 0xFF4CAF50, 0xFF062D20, 5),
        "philodendron" to profile("philodendron", BotanicalMorphology.POTHOS_HEART, 0xFF00695C, 0xFF4DB6AC, 0xFF004D40, 5),
        "syngonium" to profile("syngonium", BotanicalMorphology.POTHOS_HEART, 0xFFA5D6A7, 0xFFE91E63, 0xFF2E7D32, 3, variegated = true),
        "aglaonema" to profile("aglaonema", BotanicalMorphology.SNAKE_SWORD, 0xFFD81B60, 0xFF81C784, 0xFF1B5E20, 4, variegated = true),
        "peace_lily" to profile("peace_lily", BotanicalMorphology.FLOWER_TRUMPET, 0xFFFFFFFF, 0xFFFFEE58, 0xFF1B5E20, 3),
        "spider_plant" to profile("spider_plant", BotanicalMorphology.SNAKE_SWORD, 0xFF66BB6A, 0xFFF5F5F5, 0xFF2E7D32, 7, variegated = true),
        "rubber_plant" to profile("rubber_plant", BotanicalMorphology.ZZ_GLOSSY, 0xFF4A148C, 0xFF2E7D32, 0xFF1B5E20, 4),
        "dracaena" to profile("dracaena", BotanicalMorphology.SNAKE_SWORD, 0xFF33691E, 0xFFD32F2F, 0xFF1B5E20, 6, variegated = true),
        "areca_palm" to profile("areca_palm", BotanicalMorphology.PALM_FROND, 0xFF43A047, 0xFFC0CA33, 0xFF1B5E20, 9),
        "parlor_palm" to profile("parlor_palm", BotanicalMorphology.PALM_FROND, 0xFF2E7D32, 0xFF81C784, 0xFF0B3B2B, 8),
        "boston_fern" to profile("boston_fern", BotanicalMorphology.FEATHER_FERN, 0xFF558B2F, 0xFF9CCC65, 0xFF1B5E20, 10),
        "ficus" to profile("ficus", BotanicalMorphology.CANOPY_TREE, 0xFF2E7D32, 0xFFA5D6A7, 0xFF0B3B2B, 6),
        "croton" to profile("croton", BotanicalMorphology.SNAKE_SWORD, 0xFFFF6F00, 0xFFD32F2F, 0xFF33691E, 5, variegated = true),
        "calathea" to profile("calathea", BotanicalMorphology.ZZ_GLOSSY, 0xFF1B5E20, 0xFFC5E1A5, 0xFF4A148C, 4, variegated = true),

        // Fruit Plants & Trees (26)
        "mango" to profile("mango", BotanicalMorphology.TROPICAL_FRUIT, 0xFFFF8F00, 0xFFE65100, 0xFF1B5E20, 5),
        "litchi" to profile("litchi", BotanicalMorphology.TROPICAL_FRUIT, 0xFFC62828, 0xFFFF8A80, 0xFF1B5E20, 6),
        "guava" to profile("guava", BotanicalMorphology.TROPICAL_FRUIT, 0xFF8BC34A, 0xFFF48FB1, 0xFF2E7D32, 4),
        "lemon" to profile("lemon", BotanicalMorphology.CITRUS_ORCHARD, 0xFFFDD835, 0xFFAEEA00, 0xFF1B5E20, 5),
        "darjeeling_orange" to profile("darjeeling_orange", BotanicalMorphology.CITRUS_ORCHARD, 0xFFFB8C00, 0xFFFFCC80, 0xFF1B5E20, 5),
        "malta" to profile("malta", BotanicalMorphology.CITRUS_ORCHARD, 0xFFFFA726, 0xFFFDD835, 0xFF2E7D32, 5),
        "amla" to profile("amla", BotanicalMorphology.CITRUS_ORCHARD, 0xFF9CCC65, 0xFFDCE775, 0xFF33691E, 6),
        "jackfruit" to profile("jackfruit", BotanicalMorphology.TROPICAL_FRUIT, 0xFF689F38, 0xFFFBC02D, 0xFF1B5E20, 3),
        "banana" to profile("banana", BotanicalMorphology.TROPICAL_FRUIT, 0xFFFBC02D, 0xFFFFF59D, 0xFF2E7D32, 4),
        "papaya" to profile("papaya", BotanicalMorphology.TROPICAL_FRUIT, 0xFFFF7043, 0xFFFFB74D, 0xFF1B5E20, 4),
        "pomegranate" to profile("pomegranate", BotanicalMorphology.CITRUS_ORCHARD, 0xFFB71C1C, 0xFFFF5252, 0xFF2E7D32, 5),
        "jamun" to profile("jamun", BotanicalMorphology.TROPICAL_FRUIT, 0xFF4A148C, 0xFF8E24AA, 0xFF1B5E20, 6),
        "wax_apple" to profile("wax_apple", BotanicalMorphology.TROPICAL_FRUIT, 0xFFE91E63, 0xFFF8BBD0, 0xFF2E7D32, 5),
        "avocado" to profile("avocado", BotanicalMorphology.TROPICAL_FRUIT, 0xFF33691E, 0xFFAED581, 0xFF1B5E20, 4),
        "dragon_fruit" to profile("dragon_fruit", BotanicalMorphology.CACTUS_RIBBED, 0xFFD81B60, 0xFFAEEA00, 0xFF2E7D32, 5),
        "pineapple" to profile("pineapple", BotanicalMorphology.ROSETTE_SUCCULENT, 0xFFFFA000, 0xFF558B2F, 0xFF1B5E20, 8),
        "coconut" to profile("coconut", BotanicalMorphology.PALM_FROND, 0xFF6D4C41, 0xFF8BC34A, 0xFF1B5E20, 8),
        "areca_nut" to profile("areca_nut", BotanicalMorphology.PALM_FROND, 0xFF558B2F, 0xFFD4E157, 0xFF0B3B2B, 9),
        "apple" to profile("apple", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFFD32F2F, 0xFFFFCDD2, 0xFF2E7D32, 5),
        "pear" to profile("pear", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFFC0CA33, 0xFFF0F4C3, 0xFF33691E, 5),
        "peach" to profile("peach", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFFFF8A65, 0xFFFFE0B2, 0xFF2E7D32, 5),
        "plum" to profile("plum", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFF6A1B9A, 0xFFCE93D8, 0xFF1B5E20, 5),
        "apricot" to profile("apricot", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFFFF9800, 0xFFFFCC80, 0xFF33691E, 5),
        "kiwi" to profile("kiwi", BotanicalMorphology.CLIMBER_VINE, 0xFF795548, 0xFF9CCC65, 0xFF1B5E20, 5),
        "walnut" to profile("walnut", BotanicalMorphology.CANOPY_TREE, 0xFF5D4037, 0xFFA1887F, 0xFF2E7D32, 5),
        "strawberry" to profile("strawberry", BotanicalMorphology.FLOWER_DAISY_STAR, 0xFFE53935, 0xFFFFFFFF, 0xFF1B5E20, 5),

        // Extended Tropical, Citrus, Commercial, Hill, Exotic & Nut Fruits (43)
        "ber" to profile("ber", BotanicalMorphology.TROPICAL_FRUIT, 0xFF9CCC65, 0xFFC0CA33, 0xFF33691E, 5),
        "sapodilla" to profile("sapodilla", BotanicalMorphology.TROPICAL_FRUIT, 0xFF8D6E63, 0xFFD7CCC8, 0xFF1B5E20, 4),
        "bael" to profile("bael", BotanicalMorphology.CITRUS_ORCHARD, 0xFFAFB42B, 0xFFF0F4C3, 0xFF2E7D32, 4),
        "star_fruit" to profile("star_fruit", BotanicalMorphology.TROPICAL_FRUIT, 0xFFFDD835, 0xFF9CCC65, 0xFF1B5E20, 5),
        "custard_apple" to profile("custard_apple", BotanicalMorphology.TROPICAL_FRUIT, 0xFF7CB342, 0xFFC5E1A5, 0xFF1B5E20, 6),
        "soursop" to profile("soursop", BotanicalMorphology.TROPICAL_FRUIT, 0xFF558B2F, 0xFFDCEDC8, 0xFF0B3B2B, 5),
        "tamarind" to profile("tamarind", BotanicalMorphology.CANOPY_TREE, 0xFF6D4C41, 0xFFFFB300, 0xFF2E7D32, 6),
        "burmese_grape" to profile("burmese_grape", BotanicalMorphology.TROPICAL_FRUIT, 0xFFFFCA28, 0xFFFF8F00, 0xFF1B5E20, 7),
        "indian_olive" to profile("indian_olive", BotanicalMorphology.CITRUS_ORCHARD, 0xFF689F38, 0xFFAED581, 0xFF1B5E20, 5),
        "hog_plum" to profile("hog_plum", BotanicalMorphology.TROPICAL_FRUIT, 0xFF8BC34A, 0xFFFBC02D, 0xFF2E7D32, 5),
        "karonda" to profile("karonda", BotanicalMorphology.TROPICAL_FRUIT, 0xFFC2185B, 0xFFF8BBD0, 0xFF1B5E20, 6),
        "phalsa" to profile("phalsa", BotanicalMorphology.TROPICAL_FRUIT, 0xFF4A148C, 0xFFAB47BC, 0xFF2E7D32, 6),
        "gondhoraj_lemon" to profile("gondhoraj_lemon", BotanicalMorphology.CITRUS_ORCHARD, 0xFF2E7D32, 0xFFC5E1A5, 0xFF0B3B2B, 5),
        "kagzi_lime" to profile("kagzi_lime", BotanicalMorphology.CITRUS_ORCHARD, 0xFFAEEA00, 0xFFFDD835, 0xFF1B5E20, 5),
        "pomelo" to profile("pomelo", BotanicalMorphology.CITRUS_ORCHARD, 0xFF9CCC65, 0xFFF48FB1, 0xFF1B5E20, 4),
        "sweet_orange" to profile("sweet_orange", BotanicalMorphology.CITRUS_ORCHARD, 0xFFFF7043, 0xFFFFCC80, 0xFF1B5E20, 5),
        "mosambi" to profile("mosambi", BotanicalMorphology.CITRUS_ORCHARD, 0xFFC0CA33, 0xFFFFF59D, 0xFF2E7D32, 5),
        "kinnow" to profile("kinnow", BotanicalMorphology.CITRUS_ORCHARD, 0xFFEF6C00, 0xFFFFB74D, 0xFF1B5E20, 5),
        "kumquat" to profile("kumquat", BotanicalMorphology.CITRUS_ORCHARD, 0xFFFF8F00, 0xFFFFE082, 0xFF1B5E20, 6),
        "date_palm" to profile("date_palm", BotanicalMorphology.PALM_FROND, 0xFF5D4037, 0xFFFFB300, 0xFF2E7D32, 9),
        "grapes" to profile("grapes", BotanicalMorphology.CLIMBER_VINE, 0xFF6A1B9A, 0xFF8BC34A, 0xFF1B5E20, 7),
        "passion_fruit" to profile("passion_fruit", BotanicalMorphology.CLIMBER_VINE, 0xFF7B1FA2, 0xFFFFD54F, 0xFF2E7D32, 6),
        "fig" to profile("fig", BotanicalMorphology.TROPICAL_FRUIT, 0xFF6A1B9A, 0xFFFF8A80, 0xFF1B5E20, 4),
        "mulberry" to profile("mulberry", BotanicalMorphology.TROPICAL_FRUIT, 0xFF4A148C, 0xFF8E24AA, 0xFF1B5E20, 6),
        "cherry" to profile("cherry", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFFB71C1C, 0xFFFF8A80, 0xFF1B5E20, 6),
        "persimmon" to profile("persimmon", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFFE65100, 0xFFFFAB40, 0xFF2E7D32, 5),
        "loquat" to profile("loquat", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFFFFA000, 0xFFFFE082, 0xFF1B5E20, 6),
        "blueberry" to profile("blueberry", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFF1E88E5, 0xFF90CAF9, 0xFF2E7D32, 7),
        "raspberry" to profile("raspberry", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFFD81B60, 0xFFFF80AB, 0xFF1B5E20, 6),
        "blackberry" to profile("blackberry", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFF311B92, 0xFF7E57C2, 0xFF1B5E20, 6),
        "rambutan" to profile("rambutan", BotanicalMorphology.TROPICAL_FRUIT, 0xFFD50000, 0xFFFFD54F, 0xFF1B5E20, 6),
        "longan" to profile("longan", BotanicalMorphology.TROPICAL_FRUIT, 0xFFA1887F, 0xFFD7CCC8, 0xFF2E7D32, 7),
        "mangosteen" to profile("mangosteen", BotanicalMorphology.TROPICAL_FRUIT, 0xFF4A148C, 0xFFFFFFFF, 0xFF0B3B2B, 5),
        "durian" to profile("durian", BotanicalMorphology.TROPICAL_FRUIT, 0xFF689F38, 0xFFFDD835, 0xFF1B5E20, 4),
        "breadfruit" to profile("breadfruit", BotanicalMorphology.CANOPY_TREE, 0xFF7CB342, 0xFFDCEDC8, 0xFF1B5E20, 5),
        "star_apple" to profile("star_apple", BotanicalMorphology.TROPICAL_FRUIT, 0xFF6A1B9A, 0xFF81C784, 0xFF1B5E20, 5),
        "acerola" to profile("acerola", BotanicalMorphology.TROPICAL_FRUIT, 0xFFE53935, 0xFFFF8A80, 0xFF1B5E20, 6),
        "salak" to profile("salak", BotanicalMorphology.PALM_FROND, 0xFF5D4037, 0xFF8D6E63, 0xFF1B5E20, 7),
        "jabuticaba" to profile("jabuticaba", BotanicalMorphology.TROPICAL_FRUIT, 0xFF311B92, 0xFF9575CD, 0xFF1B5E20, 6),
        "cashew" to profile("cashew", BotanicalMorphology.CANOPY_TREE, 0xFFE65100, 0xFFFFD54F, 0xFF1B5E20, 5),
        "almond" to profile("almond", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFF8D6E63, 0xFFF8BBD0, 0xFF2E7D32, 5),
        "pistachio" to profile("pistachio", BotanicalMorphology.TEMPERATE_ORCHARD, 0xFF7CB342, 0xFFDCE775, 0xFF33691E, 5),
        "macadamia" to profile("macadamia", BotanicalMorphology.CANOPY_TREE, 0xFF558B2F, 0xFFD7CCC8, 0xFF1B5E20, 6),

        // Ornamental Trees & Climbers (17)
        "gulmohar" to profile("gulmohar", BotanicalMorphology.CANOPY_TREE, 0xFFD84315, 0xFFFFAB40, 0xFF2E7D32, 8),
        "krishnachura" to profile("krishnachura", BotanicalMorphology.CANOPY_TREE, 0xFFC62828, 0xFFFFD54F, 0xFF1B5E20, 7),
        "radhachura" to profile("radhachura", BotanicalMorphology.CANOPY_TREE, 0xFFFBC02D, 0xFFFFF176, 0xFF2E7D32, 8),
        "jarul" to profile("jarul", BotanicalMorphology.CANOPY_TREE, 0xFF8E24AA, 0xFFE1BEE7, 0xFF1B5E20, 7),
        "sonajhuri" to profile("sonajhuri", BotanicalMorphology.CANOPY_TREE, 0xFFFDD835, 0xFF9E9D24, 0xFF33691E, 6),
        "kadam" to profile("kadam", BotanicalMorphology.CANOPY_TREE, 0xFFFFB300, 0xFFFFFFFF, 0xFF1B5E20, 9),
        "bakul" to profile("bakul", BotanicalMorphology.CANOPY_TREE, 0xFFFFF9C4, 0xFF66BB6A, 0xFF0B3B2B, 8),
        "palash" to profile("palash", BotanicalMorphology.CANOPY_TREE, 0xFFFF5722, 0xFFFF8A65, 0xFF2E7D32, 6),
        "shimul" to profile("shimul", BotanicalMorphology.CANOPY_TREE, 0xFFD50000, 0xFFFF8A80, 0xFF33691E, 5),
        "neem" to profile("neem", BotanicalMorphology.CANOPY_TREE, 0xFF2E7D32, 0xFFA5D6A7, 0xFF0B3B2B, 9),
        "arjun" to profile("arjun", BotanicalMorphology.CANOPY_TREE, 0xFF558B2F, 0xFFDCE775, 0xFF1B5E20, 7),
        "teak" to profile("teak", BotanicalMorphology.CANOPY_TREE, 0xFF6D4C41, 0xFF81C784, 0xFF1B5E20, 6),
        "mahogany" to profile("mahogany", BotanicalMorphology.CANOPY_TREE, 0xFF4E342E, 0xFF66BB6A, 0xFF0B3B2B, 6),
        "bamboo" to profile("bamboo", BotanicalMorphology.SNAKE_SWORD, 0xFF689F38, 0xFFC5E1A5, 0xFF1B5E20, 6),
        "foxtail_palm" to profile("foxtail_palm", BotanicalMorphology.PALM_FROND, 0xFF2E7D32, 0xFF9CCC65, 0xFF0B3B2B, 10),
        "cycas" to profile("cycas", BotanicalMorphology.PALM_FROND, 0xFF1B5E20, 0xFF66BB6A, 0xFF062D20, 12),
        "madhabilata" to profile("madhabilata", BotanicalMorphology.CLIMBER_VINE, 0xFFE91E63, 0xFFFFFFFF, 0xFF2E7D32, 6),

        // Medicinal Plants, Herbs & Succulents (17)
        "tulsi" to profile("tulsi", BotanicalMorphology.HERB_AROMATIC, 0xFF2E7D32, 0xFF7B1FA2, 0xFF1B5E20, 6),
        "aloe_vera" to profile("aloe_vera", BotanicalMorphology.ROSETTE_SUCCULENT, 0xFF43A047, 0xFFC5E1A5, 0xFF1B5E20, 7, variegated = true),
        "thankuni" to profile("thankuni", BotanicalMorphology.HERB_AROMATIC, 0xFF4CAF50, 0xFFA5D6A7, 0xFF1B5E20, 5),
        "basak" to profile("basak", BotanicalMorphology.HERB_AROMATIC, 0xFF1B5E20, 0xFFFFFFFF, 0xFF0B3B2B, 6),
        "kalmegh" to profile("kalmegh", BotanicalMorphology.HERB_AROMATIC, 0xFF33691E, 0xFFF8BBD0, 0xFF1B5E20, 5),
        "sarpagandha" to profile("sarpagandha", BotanicalMorphology.HERB_AROMATIC, 0xFF2E7D32, 0xFFFF8A80, 0xFF0B3B2B, 5),
        "lemongrass" to profile("lemongrass", BotanicalMorphology.SNAKE_SWORD, 0xFF7CB342, 0xFFE6EE9C, 0xFF33691E, 8),
        "mint" to profile("mint", BotanicalMorphology.HERB_AROMATIC, 0xFF43A047, 0xFFB9F6CA, 0xFF1B5E20, 6),
        "rosemary" to profile("rosemary", BotanicalMorphology.HERB_AROMATIC, 0xFF00695C, 0xFF90CAF9, 0xFF004D40, 8),
        "lavender" to profile("lavender", BotanicalMorphology.FLOWER_SPIKE, 0xFF7E57C2, 0xFFD1C4E9, 0xFF33691E, 9),
        "curry_leaf" to profile("curry_leaf", BotanicalMorphology.HERB_AROMATIC, 0xFF1B5E20, 0xFF81C784, 0xFF062D20, 9),
        "adenium" to profile("adenium", BotanicalMorphology.FLOWER_TRUMPET, 0xFFD81B60, 0xFFFFFFFF, 0xFF2E7D32, 5),
        "jade_plant" to profile("jade_plant", BotanicalMorphology.ROSETTE_SUCCULENT, 0xFF2E7D32, 0xFFFF8A80, 0xFF1B5E20, 6),
        "echeveria" to profile("echeveria", BotanicalMorphology.ROSETTE_SUCCULENT, 0xFF80CBC4, 0xFFF48FB1, 0xFF00695C, 12),
        "haworthia" to profile("haworthia", BotanicalMorphology.ROSETTE_SUCCULENT, 0xFF2E7D32, 0xFFFFFFFF, 0xFF0B3B2B, 9, variegated = true),
        "cactus" to profile("cactus", BotanicalMorphology.CACTUS_RIBBED, 0xFF388E3C, 0xFFFFD54F, 0xFF1B5E20, 6),
        "kalanchoe" to profile("kalanchoe", BotanicalMorphology.ROSETTE_SUCCULENT, 0xFFFF5252, 0xFFFFD54F, 0xFF2E7D32, 8)
    )

    private fun profile(
        plantId: String,
        morphology: BotanicalMorphology,
        primaryHex: Long,
        secondaryHex: Long,
        foliageHex: Long,
        petalCount: Int,
        variegated: Boolean = false
    ): PlantVisualProfile = PlantVisualProfile(
        visualAssetKey = "plant_art_$plantId",
        exactPhotoResId = exactRealPhotosByPlantId[plantId],
        morphology = morphology,
        primaryHex = primaryHex,
        secondaryHex = secondaryHex,
        foliageHex = foliageHex,
        petalCount = petalCount,
        variegated = variegated
    )

    fun getVisualAssetKey(plantId: String): String {
        val clean = plantId.trim().lowercase()
        return if (exactRealPhotosByPlantId.containsKey(clean)) {
            "photo_real_$clean"
        } else {
            "plant_art_$clean"
        }
    }

    @DrawableRes
    fun getExactRealPhotoResId(plantId: String): Int? =
        exactRealPhotosByPlantId[plantId.trim().lowercase()]

    fun getProfile(plantId: String): PlantVisualProfile {
        val clean = plantId.trim().lowercase()
        return speciesStyles[clean] ?: PlantVisualProfile(
            visualAssetKey = getVisualAssetKey(clean),
            exactPhotoResId = getExactRealPhotoResId(clean),
            morphology = BotanicalMorphology.HERB_AROMATIC,
            primaryHex = 0xFF32A66A,
            secondaryHex = 0xFFD6B45C,
            foliageHex = 0xFF0B3B2B,
            petalCount = 5
        )
    }

    fun getAllExactPhotoMappings(): Map<String, Int> = exactRealPhotosByPlantId
}
