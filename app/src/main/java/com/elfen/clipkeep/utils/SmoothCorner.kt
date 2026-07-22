package com.elfen.clipkeep.utils

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.lang.Math.toRadians
import kotlin.math.*

import kotlin.math.*

/**
 * A class representing the points required to draw the curves in a smooth corner.
 * A smooth corner is made up of an arc surrounded by 2 diagonally symmetrical bezier curves.
 * Documentation on the makeup of a smooth curve:
 *
 * @param cornerRadius the size of the corners
 * @param smoothnessAsPercent a percentage representing how smooth the corners will be
 * @param maximumCurveStartDistanceFromVertex the maximum height/width this curve can have
 */
internal class SmoothCorner(
    private val cornerRadius: Float,
    private val smoothnessAsPercent: Int,
    private val maximumCurveStartDistanceFromVertex: Float
) {

    init {
        require(smoothnessAsPercent >= 0) {
            "The value for smoothness can never be negative."
        }
    }

    private val radius = min(cornerRadius, maximumCurveStartDistanceFromVertex)

    private val smoothness = smoothnessAsPercent / 100f

    // Distance from the first point of the curvature to the vertex of the corner
    private val curveStartDistance =
        min(maximumCurveStartDistanceFromVertex, (1 + smoothness) * radius)

    private val shouldCurveInterpolate = radius <= maximumCurveStartDistanceFromVertex / 2

    // This value is used to start interpolating between smooth corners and
    // round corners
    private val interpolationMultiplier =
        (radius - maximumCurveStartDistanceFromVertex / 2) / (maximumCurveStartDistanceFromVertex / 2)

    // Angle at second control point of the bezier curves
    private val angleAlpha =
        if (shouldCurveInterpolate)
            Math.toRadians(45.0 * smoothness).toFloat()
        else
            Math.toRadians(45.0 * smoothness * (1 - interpolationMultiplier)).toFloat()

    // Angle which dictates how much of the curve is going to be a slice of a circle
    private val angleBeta =
        if (shouldCurveInterpolate)
            Math.toRadians(90.0 * (1.0 - smoothness)).toFloat()
        else
            Math.toRadians(90.0 * (1 - smoothness * (1 - interpolationMultiplier))).toFloat()

    private val angleTheta = ((Math.toRadians(90.0) - angleBeta) / 2.0).toFloat()

    // Distance from second control point to end of Bezier curves
    private val distanceE = radius * tan(angleTheta / 2)

    // Distances in the x and y axis used to position end of Bezier
    // curves relative to it's second control point
    private val distanceC = distanceE * cos(angleAlpha)
    private val distanceD = distanceC * tan(angleAlpha)

    // Distances used to position second control point of Bezier curves
    // relative to their first control point
    private val distanceK = sin(angleBeta / 2) * radius
    private val distanceL = (distanceK * sqrt(2.0)).toFloat()
    private val distanceB =
        ((curveStartDistance - distanceL) - (1 + tan(angleAlpha)) * distanceC) / 3

    // Distance used to position first control point of Bezier curves
    // relative to their origin
    private val distanceA = 2 * distanceB

    // Represents the outer anchor points of the smooth curve
    val anchorPoint1 = PointRelativeToVertex(
        min(curveStartDistance, maximumCurveStartDistanceFromVertex),
        0f
    )

    // Represents the control point for point1
    val controlPoint1 = PointRelativeToVertex(
        anchorPoint1.distanceToFurthestSide - distanceA,
        0f
    )

    // Represents the control point for point2
    val controlPoint2 = PointRelativeToVertex(
        controlPoint1.distanceToFurthestSide - distanceB,
        0f
    )

    // Represents the inner anchor points of the smooth curve
    val anchorPoint2 = PointRelativeToVertex(
        controlPoint2.distanceToFurthestSide - distanceC,
        distanceD
    )

    val arcSection = Arc(
        radius = radius,
        arcStartAngle = angleTheta,
        arcSweepAngle = angleBeta
    )
}

/**
 * Represents a point positioned relative to a corner vertex, so that it can be used
 * to calculate a smooth curve independently of which quadrant of the rectangle this
 * curve will be inserted in.
 */
internal data class PointRelativeToVertex(
    val distanceToFurthestSide: Float,
    val distanceToClosestSide: Float
)

/**
 * Represents the arc section of a smooth corner curve
 *
 * @param arcStartAngle the start angle of the arc inside the first quadrant of rotation (0º to 90º)
 * @param arcSweepAngle the angle at the center point between the start and end of the arc
 */
internal data class Arc(
    val radius: Float,
    val arcStartAngle: Float,
    val arcSweepAngle: Float
)

/**
 * A shape describing a rectangle with smooth rounded corners sometimes called a
 * Squircle or Superellipse.
 *
 * This shape will not automatically mirror the corners in [LayoutDirection.Rtl], use
 * TODO [SmoothCornerShape] for the layout direction aware version of this shape.
 *
 * @param cornerRadiusTL the size of the top left corner
 * @param smoothnessAsPercentTL a percentage representing how smooth the top left corner will be
 * @param cornerRadiusTR the size of the top right corner
 * @param smoothnessAsPercentTR a percentage representing how smooth the top right corner will be
 * @param cornerRadiusBR the size of the bottom right corner
 * @param smoothnessAsPercentBR a percentage representing how smooth the bottom right corner will be
 * @param cornerRadiusBL the size of the bottom left corner
 * @param smoothnessAsPercentBL a percentage representing how smooth the bottom left corner will be
 */
data class AbsoluteSmoothCornerShape(
    private val cornerRadiusTL: Dp = 0.dp,
    private val smoothnessAsPercentTL: Int = 60,
    private val cornerRadiusTR: Dp = 0.dp,
    private val smoothnessAsPercentTR: Int = 60,
    private val cornerRadiusBR: Dp = 0.dp,
    private val smoothnessAsPercentBR: Int = 60,
    private val cornerRadiusBL: Dp = 0.dp,
    private val smoothnessAsPercentBL: Int = 60
) : CornerBasedShape(
    topStart = CornerSize(cornerRadiusTL),
    topEnd = CornerSize(cornerRadiusTR),
    bottomEnd = CornerSize(cornerRadiusBR),
    bottomStart = CornerSize(cornerRadiusBL)
) {
    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection
    ) = when {
        topStart + topEnd + bottomEnd + bottomStart == 0.0f -> {
            Outline.Rectangle(size.toRect())
        }
        smoothnessAsPercentTL + smoothnessAsPercentTR +
                smoothnessAsPercentBR + smoothnessAsPercentBL == 0 -> {
            Outline.Rounded(
                RoundRect(
                    rect = size.toRect(),
                    topLeft = CornerRadius(topStart),
                    topRight = CornerRadius(topEnd),
                    bottomRight = CornerRadius(bottomEnd),
                    bottomLeft = CornerRadius(bottomStart)
                )
            )
        }
        else -> {
            Outline.Generic(
                Path().apply {
                    val halfOfShortestSide = min(size.height, size.width) / 2
                    val smoothCornersMap = mutableMapOf<String, SmoothCorner>()

                    var selectedSmoothCorner =
                        smoothCornersMap["$topStart - $smoothnessAsPercentTL"] ?: SmoothCorner(
                            topStart,
                            smoothnessAsPercentTL,
                            halfOfShortestSide
                        )

                    // Top Left Corner
                    moveTo(
                        selectedSmoothCorner.anchorPoint1.distanceToClosestSide,
                        selectedSmoothCorner.anchorPoint1.distanceToFurthestSide
                    )

                    cubicTo(
                        selectedSmoothCorner.controlPoint1.distanceToClosestSide,
                        selectedSmoothCorner.controlPoint1.distanceToFurthestSide,
                        selectedSmoothCorner.controlPoint2.distanceToClosestSide,
                        selectedSmoothCorner.controlPoint2.distanceToFurthestSide,
                        selectedSmoothCorner.anchorPoint2.distanceToClosestSide,
                        selectedSmoothCorner.anchorPoint2.distanceToFurthestSide
                    )

                    arcToRad(
                        rect = Rect(
                            top = 0f,
                            left = 0f,
                            right = selectedSmoothCorner.arcSection.radius * 2,
                            bottom = selectedSmoothCorner.arcSection.radius * 2
                        ),
                        startAngleRadians =
                            (toRadians(180.0) + selectedSmoothCorner.arcSection.arcStartAngle)
                                .toFloat(),
                        sweepAngleRadians = selectedSmoothCorner.arcSection.arcSweepAngle,
                        forceMoveTo = false
                    )

                    cubicTo(
                        selectedSmoothCorner.controlPoint2.distanceToFurthestSide,
                        selectedSmoothCorner.controlPoint2.distanceToClosestSide,
                        selectedSmoothCorner.controlPoint1.distanceToFurthestSide,
                        selectedSmoothCorner.controlPoint1.distanceToClosestSide,
                        selectedSmoothCorner.anchorPoint1.distanceToFurthestSide,
                        selectedSmoothCorner.anchorPoint1.distanceToClosestSide
                    )

                    selectedSmoothCorner =
                        smoothCornersMap["$topEnd - $smoothnessAsPercentTR"] ?: SmoothCorner(
                            topEnd,
                            smoothnessAsPercentTR,
                            halfOfShortestSide
                        )

                    lineTo(
                        size.width - selectedSmoothCorner.anchorPoint1.distanceToFurthestSide,
                        selectedSmoothCorner.anchorPoint1.distanceToClosestSide
                    )

                    // Top Right Corner
                    cubicTo(
                        size.width - selectedSmoothCorner.controlPoint1.distanceToFurthestSide,
                        selectedSmoothCorner.controlPoint1.distanceToClosestSide,
                        size.width - selectedSmoothCorner.controlPoint2.distanceToFurthestSide,
                        selectedSmoothCorner.controlPoint2.distanceToClosestSide,
                        size.width - selectedSmoothCorner.anchorPoint2.distanceToFurthestSide,
                        selectedSmoothCorner.anchorPoint2.distanceToClosestSide,
                    )

                    arcToRad(
                        rect = Rect(
                            top = 0f,
                            left = size.width - selectedSmoothCorner.arcSection.radius * 2,
                            right = size.width,
                            bottom = selectedSmoothCorner.arcSection.radius * 2
                        ),
                        startAngleRadians =
                            (toRadians(270.0) + selectedSmoothCorner.arcSection.arcStartAngle)
                                .toFloat(),
                        sweepAngleRadians = selectedSmoothCorner.arcSection.arcSweepAngle,
                        forceMoveTo = false
                    )

                    cubicTo(
                        size.width - selectedSmoothCorner.controlPoint2.distanceToClosestSide,
                        selectedSmoothCorner.controlPoint2.distanceToFurthestSide,
                        size.width - selectedSmoothCorner.controlPoint1.distanceToClosestSide,
                        selectedSmoothCorner.controlPoint1.distanceToFurthestSide,
                        size.width - selectedSmoothCorner.anchorPoint1.distanceToClosestSide,
                        selectedSmoothCorner.anchorPoint1.distanceToFurthestSide,
                    )

                    selectedSmoothCorner =
                        smoothCornersMap["$bottomEnd - $smoothnessAsPercentBR"] ?: SmoothCorner(
                            bottomEnd,
                            smoothnessAsPercentBR,
                            halfOfShortestSide
                        )

                    lineTo(
                        size.width - selectedSmoothCorner.anchorPoint1.distanceToClosestSide,
                        size.height - selectedSmoothCorner.anchorPoint1.distanceToFurthestSide
                    )

                    // Bottom Right Corner
                    cubicTo(
                        size.width - selectedSmoothCorner.controlPoint1.distanceToClosestSide,
                        size.height - selectedSmoothCorner.controlPoint1.distanceToFurthestSide,
                        size.width - selectedSmoothCorner.controlPoint2.distanceToClosestSide,
                        size.height - selectedSmoothCorner.controlPoint2.distanceToFurthestSide,
                        size.width - selectedSmoothCorner.anchorPoint2.distanceToClosestSide,
                        size.height - selectedSmoothCorner.anchorPoint2.distanceToFurthestSide
                    )

                    arcToRad(
                        rect = Rect(
                            top = size.height - selectedSmoothCorner.arcSection.radius * 2,
                            left = size.width - selectedSmoothCorner.arcSection.radius * 2,
                            right = size.width,
                            bottom = size.height
                        ),
                        startAngleRadians =
                            (toRadians(0.0) + selectedSmoothCorner.arcSection.arcStartAngle)
                                .toFloat(),
                        sweepAngleRadians = selectedSmoothCorner.arcSection.arcSweepAngle,
                        forceMoveTo = false
                    )

                    cubicTo(
                        size.width - selectedSmoothCorner.controlPoint2.distanceToFurthestSide,
                        size.height - selectedSmoothCorner.controlPoint2.distanceToClosestSide,
                        size.width - selectedSmoothCorner.controlPoint1.distanceToFurthestSide,
                        size.height - selectedSmoothCorner.controlPoint1.distanceToClosestSide,
                        size.width - selectedSmoothCorner.anchorPoint1.distanceToFurthestSide,
                        size.height - selectedSmoothCorner.anchorPoint1.distanceToClosestSide
                    )

                    selectedSmoothCorner =
                        smoothCornersMap["$bottomStart - $smoothnessAsPercentBL"] ?: SmoothCorner(
                            bottomStart,
                            smoothnessAsPercentBL,
                            halfOfShortestSide
                        )

                    lineTo(
                        selectedSmoothCorner.anchorPoint1.distanceToFurthestSide,
                        size.height - selectedSmoothCorner.anchorPoint1.distanceToClosestSide
                    )

                    // Bottom Left Corner
                    cubicTo(
                        selectedSmoothCorner.controlPoint1.distanceToFurthestSide,
                        size.height - selectedSmoothCorner.controlPoint1.distanceToClosestSide,
                        selectedSmoothCorner.controlPoint2.distanceToFurthestSide,
                        size.height - selectedSmoothCorner.controlPoint2.distanceToClosestSide,
                        selectedSmoothCorner.anchorPoint2.distanceToFurthestSide,
                        size.height - selectedSmoothCorner.anchorPoint2.distanceToClosestSide,
                    )

                    arcToRad(
                        rect = Rect(
                            top = size.height - selectedSmoothCorner.arcSection.radius * 2,
                            left = 0f,
                            right = selectedSmoothCorner.arcSection.radius * 2,
                            bottom = size.height
                        ),
                        startAngleRadians =
                            (toRadians(90.0) + selectedSmoothCorner.arcSection.arcStartAngle)
                                .toFloat(),
                        sweepAngleRadians = selectedSmoothCorner.arcSection.arcSweepAngle,
                        forceMoveTo = false
                    )

                    cubicTo(
                        selectedSmoothCorner.controlPoint2.distanceToClosestSide,
                        size.height - selectedSmoothCorner.controlPoint2.distanceToFurthestSide,
                        selectedSmoothCorner.controlPoint1.distanceToClosestSide,
                        size.height - selectedSmoothCorner.controlPoint1.distanceToFurthestSide,
                        selectedSmoothCorner.anchorPoint1.distanceToClosestSide,
                        size.height - selectedSmoothCorner.anchorPoint1.distanceToFurthestSide
                    )

                    close()

                }
            )
        }
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize
    ) = AbsoluteSmoothCornerShape(
        cornerRadiusTL,
        smoothnessAsPercentTL,
        cornerRadiusTR,
        smoothnessAsPercentTR,
        cornerRadiusBR,
        smoothnessAsPercentBR,
        cornerRadiusBL,
        smoothnessAsPercentBL
    )
}

/**
 * Creates AbsoluteSmoothCornerShape with the same corner radius and smoothness applied for
 * all four corners.
 *
 * @param cornerRadius the size of the corners for the shape
 * @param smoothnessAsPercent a percentage representing how smooth the corners will be
 */
fun AbsoluteSmoothCornerShape(cornerRadius: Dp, smoothnessAsPercent: Int) =
    AbsoluteSmoothCornerShape(
        cornerRadiusTL = cornerRadius,
        smoothnessAsPercentTL = smoothnessAsPercent,
        cornerRadiusTR = cornerRadius,
        smoothnessAsPercentTR = smoothnessAsPercent,
        cornerRadiusBR = cornerRadius,
        smoothnessAsPercentBR = smoothnessAsPercent,
        cornerRadiusBL = cornerRadius,
        smoothnessAsPercentBL = smoothnessAsPercent
    )