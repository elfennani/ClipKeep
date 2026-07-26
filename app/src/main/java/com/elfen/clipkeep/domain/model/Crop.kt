package com.elfen.clipkeep.domain.model

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop as Media3Crop

data class Crop(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val aspectRatioLocked: Boolean = false
)

@OptIn(UnstableApi::class)
fun Crop.toMedia3Crop(frameWidth: Int, frameHeight: Int): Media3Crop {
    val rightPx = minOf(x + width, frameWidth.toFloat())
    val bottomPx = minOf(y + height, frameHeight.toFloat())

    val left = (x / frameWidth) * 2f - 1f
    val right = (rightPx / frameWidth) * 2f - 1f
    val bottom = 1f - (bottomPx / frameHeight) * 2f
    val top = 1f - (y / frameHeight) * 2f

    return Media3Crop(left, right, bottom, top)
}