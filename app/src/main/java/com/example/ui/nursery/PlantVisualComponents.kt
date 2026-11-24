package com.example.ui.nursery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.datasource.BotanicalMorphology
import com.example.data.datasource.PlantPhotoRegistry
import com.example.data.datasource.PlantVisualProfile
import com.example.data.model.AppLanguage
import com.example.data.model.PlantKingdomEntry
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders species-authentic imagery for any PlantKingdomEntry:
 * 1. User/Admin verified PickVisualMedia URI or HTTPS photo URL (`plant.customPhotoUri`)
 * 2. Strict 1-to-1 matched bundled `.jpg` drawable (`PlantPhotoRegistry.getExactRealPhotoResId`)
 * 3. Distinct per-species procedural botanical illustration on Canvas keyed by `plant.plantId`
 *    so no unrelated plant ever shares another plant's mismatched photo.
 */
@Composable
fun PlantSpeciesArtwork(
    plant: PlantKingdomEntry,
    lang: AppLanguage,
    modifier: Modifier = Modifier,
    showAccuracyBadge: Boolean = true
) {
    val profile = PlantPhotoRegistry.getProfile(plant.plantId)
    val exactPhotoResId = PlantPhotoRegistry.getExactRealPhotoResId(plant.plantId)

    Box(modifier = modifier.clip(RoundedCornerShape(14.dp))) {
        when {
            plant.customPhotoUri.isNotBlank() -> {
                SubcomposeAsyncImage(
                    model = plant.customPhotoUri,
                    contentDescription = plant.getDisplayName(lang),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        BotanicalSpecimenCanvas(profile = profile, modifier = Modifier.fillMaxSize())
                    }
                )
                if (showAccuracyBadge) {
                    PlantPhotoAccuracyBadge(
                        isRealPhoto = true,
                        lang = lang,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
            }
            exactPhotoResId != null -> {
                Image(
                    painter = painterResource(id = exactPhotoResId),
                    contentDescription = plant.getDisplayName(lang),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (showAccuracyBadge) {
                    PlantPhotoAccuracyBadge(
                        isRealPhoto = true,
                        lang = lang,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
            }
            else -> {
                BotanicalSpecimenCanvas(profile = profile, modifier = Modifier.fillMaxSize())
                if (showAccuracyBadge) {
                    PlantPhotoAccuracyBadge(
                        isRealPhoto = false,
                        lang = lang,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlantPhotoAccuracyBadge(
    isRealPhoto: Boolean,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val label = if (isRealPhoto) {
        when (lang) {
            AppLanguage.BENGALI -> "📷 যাচাইকৃত ছবি"
            AppLanguage.HINDI -> "📷 सत्यापित फोटो"
            AppLanguage.ENGLISH -> "📷 Verified Photo"
        }
    } else {
        when (lang) {
            AppLanguage.BENGALI -> "🎨 উদ্ভিদ প্রোফাইল"
            AppLanguage.HINDI -> "🎨 वानस्पतिक प्रोफ़ाइल"
            AppLanguage.ENGLISH -> "🎨 Botanical Profile"
        }
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF031B13).copy(alpha = 0.72f),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = if (isRealPhoto) Color(0xFFA7F3D0) else Color(0xFFF0D98B),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun BotanicalSpecimenCanvas(
    profile: PlantVisualProfile,
    modifier: Modifier = Modifier
) {
    val primary = Color(profile.primaryHex)
    val secondary = Color(profile.secondaryHex)
    val foliage = Color(profile.foliageHex)
    val bgTop = foliage.copy(alpha = 0.92f)
    val bgBottom = Color(0xFF062D20)

    Box(
        modifier = modifier.background(
            Brush.linearGradient(colors = listOf(bgTop, bgBottom))
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.52f
            val cy = h * 0.46f
            val r = minOf(w, h) * 0.30f

            // Soft botanical backlight halo
            drawCircle(
                color = primary.copy(alpha = 0.14f),
                radius = r * 1.45f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = secondary.copy(alpha = 0.10f),
                radius = r * 1.15f,
                center = Offset(w * 0.18f, h * 0.82f)
            )

            // Stem base
            drawLine(
                color = foliage.copy(alpha = 0.95f),
                start = Offset(cx, h * 0.92f),
                end = Offset(cx, cy + r * 0.2f),
                strokeWidth = (minOf(w, h) * 0.045f).coerceAtLeast(3f),
                cap = StrokeCap.Round
            )

            when (profile.morphology) {
                BotanicalMorphology.ORCHID -> drawOrchidSpecimen(cx, cy, r, primary, secondary, foliage)
                BotanicalMorphology.BLUE_POPPY,
                BotanicalMorphology.FLOWER_DAISY_STAR -> drawStarPetals(cx, cy, r, profile.petalCount, primary, secondary, foliage)
                BotanicalMorphology.RHODODENDRON,
                BotanicalMorphology.PRIMULA_CLUSTER,
                BotanicalMorphology.BELL_HYDRANGEA -> drawClusterBloom(cx, cy, r, profile.petalCount, primary, secondary, foliage)
                BotanicalMorphology.MAGNOLIA,
                BotanicalMorphology.FLOWER_ROSE_LAYERS -> drawLayeredRosetteBloom(cx, cy, r, profile.petalCount, primary, secondary, foliage)
                BotanicalMorphology.FLOWER_TRUMPET -> drawTrumpetBloom(cx, cy, r, primary, secondary, foliage)
                BotanicalMorphology.FLOWER_SPIKE -> drawSpikeInflorescence(cx, cy, r, profile.petalCount, primary, secondary, foliage)
                BotanicalMorphology.POTHOS_HEART,
                BotanicalMorphology.CLIMBER_VINE -> drawHeartVineLeaves(cx, cy, r, primary, secondary, foliage, profile.variegated)
                BotanicalMorphology.MONSTERA_FENESTRATED -> drawMonsteraLeaf(cx, cy, r, primary, foliage)
                BotanicalMorphology.SNAKE_SWORD -> drawSwordBlades(cx, cy, r, primary, secondary, foliage, profile.variegated)
                BotanicalMorphology.ZZ_GLOSSY,
                BotanicalMorphology.HERB_AROMATIC -> drawGlossyBranchLeaves(cx, cy, r, profile.petalCount, primary, secondary, foliage, profile.variegated)
                BotanicalMorphology.PALM_FROND,
                BotanicalMorphology.FEATHER_FERN -> drawPalmOrFernFronds(cx, cy, r, profile.petalCount, primary, secondary)
                BotanicalMorphology.CACTUS_RIBBED -> drawCactusSpecimen(cx, cy, r, primary, secondary)
                BotanicalMorphology.ROSETTE_SUCCULENT -> drawSucculentRosette(cx, cy, r, profile.petalCount, primary, secondary, profile.variegated)
                BotanicalMorphology.CITRUS_ORCHARD,
                BotanicalMorphology.TROPICAL_FRUIT,
                BotanicalMorphology.TEMPERATE_ORCHARD -> drawFruitBranch(cx, cy, r, primary, secondary, foliage)
                BotanicalMorphology.CANOPY_TREE -> drawCanopyTree(cx, cy, r, primary, secondary, foliage)
            }
        }
    }
}

private fun DrawScope.drawOrchidSpecimen(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    foliage: Color
) {
    // 5 sepals/petals + distinct lip
    for (i in 0 until 5) {
        val angle = -90.0 + i * 72.0
        val rad = Math.toRadians(angle)
        val px = cx + (cos(rad) * r * 0.68f).toFloat()
        val py = cy + (sin(rad) * r * 0.68f).toFloat()
        drawCircle(color = primary.copy(alpha = 0.92f), radius = r * 0.42f, center = Offset(px, py))
    }
    drawCircle(color = secondary, radius = r * 0.34f, center = Offset(cx, cy + r * 0.18f))
    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = r * 0.14f, center = Offset(cx, cy))
    drawLeafPair(cx, cy + r * 0.9f, r * 0.7f, foliage.copy(alpha = 0.9f))
}

private fun DrawScope.drawStarPetals(
    cx: Float,
    cy: Float,
    r: Float,
    petals: Int,
    primary: Color,
    secondary: Color,
    foliage: Color
) {
    val count = petals.coerceIn(4, 16)
    for (i in 0 until count) {
        val angle = Math.toRadians(i * (360.0 / count) - 90.0)
        val px = cx + (cos(angle) * r * 0.62f).toFloat()
        val py = cy + (sin(angle) * r * 0.62f).toFloat()
        drawCircle(
            color = if (i % 2 == 0) primary else primary.copy(alpha = 0.85f),
            radius = if (count > 10) r * 0.28f else r * 0.44f,
            center = Offset(px, py)
        )
    }
    drawCircle(color = secondary, radius = r * 0.28f, center = Offset(cx, cy))
    drawLeafPair(cx, cy + r * 0.85f, r * 0.65f, foliage)
}

private fun DrawScope.drawClusterBloom(
    cx: Float,
    cy: Float,
    r: Float,
    petals: Int,
    primary: Color,
    secondary: Color,
    foliage: Color
) {
    val count = petals.coerceIn(6, 12)
    drawLeafPair(cx, cy + r * 0.85f, r * 0.8f, foliage)
    for (i in 0 until count) {
        val angle = Math.toRadians(i * (360.0 / count))
        val dist = if (i == 0) 0f else r * 0.55f
        val px = cx + (cos(angle) * dist).toFloat()
        val py = cy + (sin(angle) * dist).toFloat()
        drawCircle(
            color = if (i % 2 == 0) primary else secondary,
            radius = r * 0.34f,
            center = Offset(px, py)
        )
    }
    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = r * 0.14f, center = Offset(cx, cy))
}

private fun DrawScope.drawLayeredRosetteBloom(
    cx: Float,
    cy: Float,
    r: Float,
    petals: Int,
    primary: Color,
    secondary: Color,
    foliage: Color
) {
    drawLeafPair(cx, cy + r * 0.85f, r * 0.72f, foliage)
    val outerCount = petals.coerceIn(7, 16)
    for (i in 0 until outerCount) {
        val angle = Math.toRadians(i * (360.0 / outerCount))
        drawCircle(
            color = primary,
            radius = r * 0.44f,
            center = Offset(cx + (cos(angle) * r * 0.52f).toFloat(), cy + (sin(angle) * r * 0.52f).toFloat())
        )
    }
    for (i in 0 until 6) {
        val angle = Math.toRadians(i * 60.0 + 18.0)
        drawCircle(
            color = secondary.copy(alpha = 0.90f),
            radius = r * 0.30f,
            center = Offset(cx + (cos(angle) * r * 0.25f).toFloat(), cy + (sin(angle) * r * 0.25f).toFloat())
        )
    }
    drawCircle(color = Color(0xFFFFD54F), radius = r * 0.16f, center = Offset(cx, cy))
}

private fun DrawScope.drawTrumpetBloom(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    foliage: Color
) {
    drawLeafPair(cx, cy + r * 0.85f, r * 0.75f, foliage)
    val bell = Path().apply {
        moveTo(cx - r * 0.85f, cy - r * 0.55f)
        quadraticTo(cx, cy - r * 0.25f, cx + r * 0.85f, cy - r * 0.55f)
        quadraticTo(cx + r * 0.35f, cy + r * 0.65f, cx, cy + r * 0.65f)
        quadraticTo(cx - r * 0.35f, cy + r * 0.65f, cx - r * 0.85f, cy - r * 0.55f)
        close()
    }
    drawPath(path = bell, color = primary)
    drawCircle(color = secondary, radius = r * 0.22f, center = Offset(cx, cy - r * 0.18f))
}

private fun DrawScope.drawSpikeInflorescence(
    cx: Float,
    cy: Float,
    r: Float,
    petals: Int,
    primary: Color,
    secondary: Color,
    foliage: Color
) {
    drawLeafPair(cx, cy + r * 0.9f, r * 0.75f, foliage)
    val pairs = (petals / 2).coerceIn(3, 6)
    for (i in 0 until pairs) {
        val yPos = cy + r * 0.55f - i * (r * 0.32f)
        val spread = r * (0.34f - i * 0.04f).coerceAtLeast(0.14f)
        drawCircle(color = primary, radius = spread * 0.68f, center = Offset(cx - spread, yPos))
        drawCircle(color = secondary, radius = spread * 0.62f, center = Offset(cx + spread, yPos - r * 0.06f))
    }
    drawCircle(color = primary, radius = r * 0.22f, center = Offset(cx, cy - r * 0.95f))
}

private fun DrawScope.drawHeartVineLeaves(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    foliage: Color,
    variegated: Boolean
) {
    // Trailing vine arc + 3 distinct heart leaves
    drawArc(
        color = foliage,
        startAngle = 150f,
        sweepAngle = 210f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.9f, cy - r * 0.6f),
        size = Size(r * 1.8f, r * 1.4f),
        style = Stroke(width = 4f)
    )
    val centers = listOf(
        Offset(cx - r * 0.52f, cy + r * 0.18f) to r * 0.56f,
        Offset(cx + r * 0.52f, cy - r * 0.12f) to r * 0.52f,
        Offset(cx, cy - r * 0.42f) to r * 0.62f
    )
    centers.forEachIndexed { idx, (pos, lr) ->
        val leafColor = if (idx == 1 && variegated) secondary else primary
        drawCircle(color = leafColor, radius = lr * 0.62f, center = Offset(pos.x - lr * 0.2f, pos.y))
        drawCircle(color = leafColor, radius = lr * 0.62f, center = Offset(pos.x + lr * 0.2f, pos.y))
        if (variegated) {
            drawLine(
                color = secondary.copy(alpha = 0.8f),
                start = Offset(pos.x - lr * 0.2f, pos.y - lr * 0.15f),
                end = Offset(pos.x + lr * 0.22f, pos.y + lr * 0.25f),
                strokeWidth = 3f
            )
        }
    }
}

private fun DrawScope.drawMonsteraLeaf(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    foliage: Color
) {
    drawOval(
        color = primary,
        topLeft = Offset(cx - r * 0.95f, cy - r * 0.95f),
        size = Size(r * 1.9f, r * 1.8f)
    )
    // Fenestration cutouts & midrib
    drawLine(
        color = Color(0xFFA5D6A7),
        start = Offset(cx, cy + r * 0.85f),
        end = Offset(cx, cy - r * 0.85f),
        strokeWidth = 4f
    )
    for (i in -1..1) {
        val y = cy + i * r * 0.42f
        drawRoundRect(
            color = foliage.copy(alpha = 0.95f),
            topLeft = Offset(cx - r * 0.82f, y),
            size = Size(r * 0.52f, r * 0.14f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = foliage.copy(alpha = 0.95f),
            topLeft = Offset(cx + r * 0.30f, y),
            size = Size(r * 0.52f, r * 0.14f),
            cornerRadius = CornerRadius(8f, 8f)
        )
    }
}

private fun DrawScope.drawSwordBlades(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    foliage: Color,
    variegated: Boolean
) {
    val offsets = listOf(-0.48f, -0.22f, 0f, 0.24f, 0.48f)
    offsets.forEachIndexed { idx, xOff ->
        val bladeH = r * (1.45f + (idx % 2) * 0.35f)
        val bx = cx + xOff * r
        val col = if (idx % 2 == 0) primary else foliage
        drawRoundRect(
            color = col,
            topLeft = Offset(bx - r * 0.13f, cy + r * 0.7f - bladeH),
            size = Size(r * 0.26f, bladeH),
            cornerRadius = CornerRadius(r * 0.13f, r * 0.13f)
        )
        if (variegated) {
            drawLine(
                color = secondary.copy(alpha = 0.9f),
                start = Offset(bx - r * 0.10f, cy + r * 0.6f),
                end = Offset(bx - r * 0.10f, cy + r * 0.7f - bladeH * 0.9f),
                strokeWidth = 2.5f
            )
        }
    }
}

private fun DrawScope.drawGlossyBranchLeaves(
    cx: Float,
    cy: Float,
    r: Float,
    leafPairs: Int,
    primary: Color,
    secondary: Color,
    foliage: Color,
    variegated: Boolean
) {
    val pairs = leafPairs.coerceIn(3, 6)
    for (i in 0 until pairs) {
        val y = cy + r * 0.65f - i * (r * 0.34f)
        drawOval(
            color = if (variegated && i % 2 == 1) secondary else primary,
            topLeft = Offset(cx - r * 0.78f, y - r * 0.18f),
            size = Size(r * 0.70f, r * 0.34f)
        )
        drawOval(
            color = if (variegated && i % 2 == 0) secondary else foliage.copy(alpha = 0.9f),
            topLeft = Offset(cx + r * 0.08f, y - r * 0.18f),
            size = Size(r * 0.70f, r * 0.34f)
        )
    }
}

private fun DrawScope.drawPalmOrFernFronds(
    cx: Float,
    cy: Float,
    r: Float,
    fronds: Int,
    primary: Color,
    secondary: Color
) {
    val count = fronds.coerceIn(6, 12)
    for (i in 0 until count) {
        val angle = -165.0 + i * (150.0 / (count - 1).coerceAtLeast(1))
        val rad = Math.toRadians(angle)
        val endX = cx + (cos(rad) * r * 1.15f).toFloat()
        val endY = (cy + r * 0.45f) + (sin(rad) * r * 1.15f).toFloat()
        drawLine(
            color = if (i % 2 == 0) primary else secondary,
            start = Offset(cx, cy + r * 0.45f),
            end = Offset(endX, endY),
            strokeWidth = (r * 0.13f).coerceAtLeast(4f),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCactusSpecimen(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color
) {
    drawRoundRect(
        color = primary,
        topLeft = Offset(cx - r * 0.35f, cy - r * 0.9f),
        size = Size(r * 0.70f, r * 1.65f),
        cornerRadius = CornerRadius(r * 0.35f, r * 0.35f)
    )
    drawRoundRect(
        color = primary.copy(alpha = 0.92f),
        topLeft = Offset(cx - r * 0.85f, cy - r * 0.35f),
        size = Size(r * 0.40f, r * 0.72f),
        cornerRadius = CornerRadius(r * 0.2f, r * 0.2f)
    )
    drawRoundRect(
        color = primary.copy(alpha = 0.92f),
        topLeft = Offset(cx + r * 0.45f, cy - r * 0.52f),
        size = Size(r * 0.40f, r * 0.78f),
        cornerRadius = CornerRadius(r * 0.2f, r * 0.2f)
    )
    drawCircle(color = secondary, radius = r * 0.18f, center = Offset(cx, cy - r * 0.92f))
}

private fun DrawScope.drawSucculentRosette(
    cx: Float,
    cy: Float,
    r: Float,
    petals: Int,
    primary: Color,
    secondary: Color,
    variegated: Boolean
) {
    val count = petals.coerceIn(6, 12)
    for (i in 0 until count) {
        val rad = Math.toRadians(i * (360.0 / count))
        drawCircle(
            color = primary,
            radius = r * 0.42f,
            center = Offset(cx + (cos(rad) * r * 0.56f).toFloat(), cy + (sin(rad) * r * 0.56f).toFloat())
        )
    }
    for (i in 0 until 6) {
        val rad = Math.toRadians(i * 60.0 + 15.0)
        drawCircle(
            color = if (variegated) secondary else primary.copy(alpha = 0.82f),
            radius = r * 0.28f,
            center = Offset(cx + (cos(rad) * r * 0.26f).toFloat(), cy + (sin(rad) * r * 0.26f).toFloat())
        )
    }
    drawCircle(color = secondary, radius = r * 0.15f, center = Offset(cx, cy))
}

private fun DrawScope.drawFruitBranch(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    foliage: Color
) {
    drawLeafPair(cx, cy - r * 0.42f, r * 0.85f, foliage)
    drawCircle(color = primary, radius = r * 0.56f, center = Offset(cx - r * 0.16f, cy + r * 0.12f))
    drawCircle(color = secondary, radius = r * 0.36f, center = Offset(cx + r * 0.42f, cy + r * 0.28f))
    drawCircle(
        color = Color.White.copy(alpha = 0.32f),
        radius = r * 0.14f,
        center = Offset(cx - r * 0.32f, cy - r * 0.04f)
    )
}

private fun DrawScope.drawCanopyTree(
    cx: Float,
    cy: Float,
    r: Float,
    primary: Color,
    secondary: Color,
    foliage: Color
) {
    // Trunk
    drawRoundRect(
        color = Color(0xFF6D4C41),
        topLeft = Offset(cx - r * 0.14f, cy + r * 0.1f),
        size = Size(r * 0.28f, r * 0.95f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawCircle(color = foliage, radius = r * 0.72f, center = Offset(cx, cy - r * 0.18f))
    drawCircle(color = primary.copy(alpha = 0.88f), radius = r * 0.48f, center = Offset(cx - r * 0.48f, cy + r * 0.02f))
    drawCircle(color = secondary.copy(alpha = 0.86f), radius = r * 0.44f, center = Offset(cx + r * 0.48f, cy - r * 0.06f))
}

private fun DrawScope.drawLeafPair(cx: Float, cy: Float, r: Float, color: Color) {
    drawOval(
        color = color,
        topLeft = Offset(cx - r * 0.95f, cy - r * 0.22f),
        size = Size(r * 0.85f, r * 0.38f)
    )
    drawOval(
        color = color,
        topLeft = Offset(cx + r * 0.10f, cy - r * 0.22f),
        size = Size(r * 0.85f, r * 0.38f)
    )
}
