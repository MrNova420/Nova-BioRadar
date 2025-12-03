package com.bioradar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.bioradar.core.models.RadarTarget
import com.bioradar.core.models.TargetType
import com.bioradar.ui.theme.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Polar Radar Display Component
 * Displays targets on a circular radar with sweep animation
 */
@Composable
fun RadarDisplay(
    targets: List<RadarTarget>,
    isScanning: Boolean,
    maxRange: Float,
    modifier: Modifier = Modifier
) {
    // Sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )
    
    // Target fade animation
    val targetAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_alpha"
    )
    
    Box(
        modifier = modifier.background(BackgroundRadar)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 2 * 0.9f
            
            // Draw radar background
            drawRadarBackground(center, radius, maxRange)
            
            // Draw grid lines
            drawRadarGrid(center, radius)
            
            // Draw cardinal directions
            drawCardinalDirections(center, radius)
            
            // Draw sweep line (if scanning)
            if (isScanning) {
                drawSweepLine(center, radius, sweepAngle)
            }
            
            // Draw targets
            targets.forEach { target ->
                drawTarget(center, radius, target, maxRange, targetAlpha)
            }
            
            // Draw center point
            drawCircle(
                color = RadarGreen,
                radius = 6f,
                center = center
            )
        }
    }
}

private fun DrawScope.drawRadarBackground(center: Offset, radius: Float, maxRange: Float) {
    // Outer circle
    drawCircle(
        color = GridLineMajor,
        radius = radius,
        center = center,
        style = Stroke(width = 2f)
    )
    
    // Background glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                RadarGreen.copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawRadarGrid(center: Offset, radius: Float) {
    // Distance rings (4 concentric circles)
    val ringCount = 4
    for (i in 1..ringCount) {
        val ringRadius = radius * i / ringCount
        drawCircle(
            color = GridLine,
            radius = ringRadius,
            center = center,
            style = Stroke(width = 1f)
        )
    }
    
    // Angle lines (every 45 degrees)
    for (angle in 0 until 360 step 45) {
        val radian = Math.toRadians(angle.toDouble())
        val endX = center.x + radius * cos(radian).toFloat()
        val endY = center.y + radius * sin(radian).toFloat()
        
        drawLine(
            color = GridLine,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawCardinalDirections(center: Offset, radius: Float) {
    // North, East, South, West lines (brighter)
    for (angle in listOf(0, 90, 180, 270)) {
        val radian = Math.toRadians((angle - 90).toDouble()) // -90 to make 0 = North
        val endX = center.x + radius * cos(radian).toFloat()
        val endY = center.y + radius * sin(radian).toFloat()
        
        drawLine(
            color = GridLineMajor,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawSweepLine(center: Offset, radius: Float, angle: Float) {
    // Sweep gradient trail
    val sweepPath = Path().apply {
        moveTo(center.x, center.y)
        val startAngle = angle - 90 // Adjust for canvas coordinate system
        val sweepAngle = 30f // Trail length
        
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius
            ),
            startAngleDegrees = startAngle - sweepAngle,
            sweepAngleDegrees = sweepAngle,
            forceMoveTo = false
        )
        close()
    }
    
    drawPath(
        path = sweepPath,
        brush = Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                RadarGreen.copy(alpha = 0.3f)
            ),
            center = center
        )
    )
    
    // Main sweep line
    rotate(angle, pivot = center) {
        drawLine(
            color = RadarGreen,
            start = center,
            end = Offset(center.x, center.y - radius),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawTarget(
    center: Offset,
    radius: Float,
    target: RadarTarget,
    maxRange: Float,
    alpha: Float
) {
    // Calculate position
    val distance = target.distanceMeters ?: (maxRange / 2)
    val normalizedDistance = (distance / maxRange).coerceIn(0f, 1f)
    val angleRad = Math.toRadians((target.angleDegrees - 90).toDouble()) // -90 for North = 0
    
    val targetX = center.x + radius * normalizedDistance * cos(angleRad).toFloat()
    val targetY = center.y + radius * normalizedDistance * sin(angleRad).toFloat()
    val targetPos = Offset(targetX, targetY)
    
    // Target color based on confidence and type
    val targetColor = when {
        target.confidence > 0.75f -> TargetHighConfidence
        target.confidence > 0.5f -> TargetMediumConfidence
        else -> TargetLowConfidence
    }.copy(alpha = alpha)
    
    // Outer glow
    drawCircle(
        color = targetColor.copy(alpha = 0.3f * alpha),
        radius = 20f,
        center = targetPos
    )
    
    // Middle ring
    drawCircle(
        color = targetColor.copy(alpha = 0.5f * alpha),
        radius = 12f,
        center = targetPos
    )
    
    // Core dot
    drawCircle(
        color = targetColor,
        radius = 6f,
        center = targetPos
    )
    
    // Moving indicator
    if (target.isMoving) {
        drawCircle(
            color = targetColor,
            radius = 16f,
            center = targetPos,
            style = Stroke(width = 2f)
        )
    }
    
    // UWB indicator (special marker for precise targets)
    if (target.dataSources.contains(com.bioradar.core.models.DataSource.UWB)) {
        drawCircle(
            color = TargetUwb,
            radius = 10f,
            center = targetPos,
            style = Stroke(width = 2f)
        )
    }
}
