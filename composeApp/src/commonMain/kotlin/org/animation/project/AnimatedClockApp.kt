package org.animation.project

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun AnimatedClockApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF0D1A2B),
            surface = Color(0xFF1A2332),
            primary = Color(0xFF87CEEB),
            secondary = Color(0xFF4169E1)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1A2B), // Deep winter night
                            Color(0xFF1A2332), // Darker blue
                            Color(0xFF2C3E50)  // Slate blue
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Animated snowfall background
            SnowfallBackground()

            // Main clock content
            ProfessionalAnimatedClock()
        }
    }
}

@Composable
fun SnowfallBackground() {
    val snowflakes = remember { generateSnowflakes(150) }
    val infiniteTransition = rememberInfiniteTransition(label = "snowfall")

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow_animation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        snowflakes.forEach { snowflake ->
            val currentY = (snowflake.startY + (size.height + 100) * animationProgress) % (size.height + 200)
            val currentX = snowflake.startX + sin(currentY * 0.01f + snowflake.phase) * snowflake.amplitude

            drawCircle(
                color = Color.White.copy(alpha = snowflake.alpha),
                radius = snowflake.size,
                center = Offset(currentX, currentY)
            )
        }
    }
}

data class Snowflake(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val alpha: Float,
    val amplitude: Float,
    val phase: Float
)

fun generateSnowflakes(count: Int): List<Snowflake> {
    return (0 until count).map {
        Snowflake(
            startX = Random.nextFloat() * 1000f,
            startY = -Random.nextFloat() * 200f,
            size = Random.nextFloat() * 3f + 1f,
            alpha = Random.nextFloat() * 0.6f + 0.2f,
            amplitude = Random.nextFloat() * 30f + 10f,
            phase = Random.nextFloat() * 2f * PI.toFloat()
        )
    }
}

@Composable
fun ProfessionalAnimatedClock() {
    var currentTime by remember { mutableStateOf(getCurrentTime()) }

    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = getCurrentTime()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Digital time display with frosted effect
        FrostedDigitalTimeDisplay(currentTime)

        // Main analog clock with winter theme
        WinterAnalogClock(
            time = currentTime,
            modifier = Modifier.size(320.dp)
        )

        // Winter-themed time zone indicators
        WinterTimeZoneRow()
    }
}

@Composable
fun FrostedDigitalTimeDisplay(time: TimeData) {
    val animatedSeconds by animateFloatAsState(
        targetValue = time.second.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "seconds"
    )

    val frostGlow by animateFloatAsState(
        targetValue = if (time.second % 2 == 0) 0.8f else 0.4f,
        animationSpec = tween(1000),
        label = "frost_glow"
    )

    Card(
        modifier = Modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF87CEEB).copy(alpha = 0.4f),
                spotColor = Color(0xFF87CEEB).copy(alpha = 0.4f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2332).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent,
                            Color(0xFF87CEEB).copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrostedTimeUnit(time.hour, "HRS", frostGlow)
                FrostedTimeSeparator()
                FrostedTimeUnit(time.minute, "MIN", frostGlow)
                FrostedTimeSeparator()
                AnimatedFrostedTimeUnit(animatedSeconds.toInt(), "SEC", frostGlow)
            }
        }
    }
}

@Composable
fun FrostedTimeUnit(value: Int, label: String, glowIntensity: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (value < 10) "0$value" else "$value",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.95f),
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color = Color(0xFF87CEEB).copy(alpha = glowIntensity),
                    offset = Offset(0f, 0f),
                    blurRadius = 8f
                )
            )
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF87CEEB),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AnimatedFrostedTimeUnit(value: Int, label: String, glowIntensity: Float) {
    val scale by animateFloatAsState(
        targetValue = if (value == 0) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val iceGlow by animateFloatAsState(
        targetValue = if (value == 0) 1f else glowIntensity,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "ice_glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Text(
            text = if (value < 10) "0$value" else "$value",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (value == 0) Color(0xFF87CEEB) else Color.White.copy(alpha = 0.95f),
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color = Color(0xFF87CEEB).copy(alpha = iceGlow),
                    offset = Offset(0f, 0f),
                    blurRadius = 12f
                )
            )
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF87CEEB),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FrostedTimeSeparator() {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "separator"
    )

    Text(
        text = ":",
        fontSize = 28.sp,
        color = Color(0xFF4169E1).copy(alpha = alpha),
        modifier = Modifier.padding(horizontal = 12.dp),
        style = androidx.compose.ui.text.TextStyle(
            shadow = Shadow(
                color = Color(0xFF87CEEB).copy(alpha = 0.6f),
                offset = Offset(0f, 0f),
                blurRadius = 6f
            )
        )
    )
}

@Composable
fun WinterAnalogClock(
    time: TimeData,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "winter_clock_effects")

    val iceGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ice_glow"
    )

    val crystalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "crystal_rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Multi-layered glow effect
        Box(
            modifier = Modifier
                .size(360.dp)
                .blur(25.dp)
                .background(
                    Color(0xFF87CEEB).copy(alpha = iceGlow * 0.3f),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(340.dp)
                .blur(15.dp)
                .background(
                    Color(0xFF4169E1).copy(alpha = iceGlow * 0.2f),
                    CircleShape
                )
        )

        // Main clock face with winter effects
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            drawWinterClockFace(time, iceGlow, crystalRotation)
        }
    }
}

fun DrawScope.drawWinterClockFace(time: TimeData, iceGlow: Float, crystalRotation: Float) {
    val center = size.center
    val radius = size.minDimension / 2 * 0.85f

    // Draw layered ice background
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2C3E50).copy(alpha = 0.9f),
                Color(0xFF1A2332).copy(alpha = 0.95f),
                Color(0xFF0D1A2B)
            ),
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Draw ice crystal pattern
    rotate(crystalRotation, center) {
        for (i in 0 until 6) {
            val angle = i * 60.0 * PI / 180
            drawLine(
                color = Color(0xFF87CEEB).copy(alpha = 0.2f),
                start = center,
                end = Offset(
                    (center.x + cos(angle) * radius * 0.7f).toFloat(),
                    (center.y + sin(angle) * radius * 0.7f).toFloat()
                ),
                strokeWidth = 1f
            )
        }
    }

    // Draw frozen hour markers with ice effect
    for (i in 1..12) {
        val angle = (i * 30 - 90) * PI / 180
        val startRadius = radius * 0.82f
        val endRadius = radius * 0.92f
        val isMainHour = i % 3 == 0

        // Ice accumulation effect
        val iceThickness = if (isMainHour) 10f else 6f

        // Draw ice base
        drawLine(
            color = Color(0xFF87CEEB).copy(alpha = 0.3f),
            start = Offset(
                (center.x + cos(angle) * (startRadius - 2)).toFloat(),
                (center.y + sin(angle) * (startRadius - 2)).toFloat()
            ),
            end = Offset(
                (center.x + cos(angle) * (endRadius + 2)).toFloat(),
                (center.y + sin(angle) * (endRadius + 2)).toFloat()
            ),
            strokeWidth = iceThickness + 4f,
            cap = StrokeCap.Round
        )

        // Main marker
        drawLine(
            color = if (isMainHour) Color(0xFF87CEEB) else Color(0xFF4169E1),
            start = Offset(
                (center.x + cos(angle) * startRadius).toFloat(),
                (center.y + sin(angle) * startRadius).toFloat()
            ),
            end = Offset(
                (center.x + cos(angle) * endRadius).toFloat(),
                (center.y + sin(angle) * endRadius).toFloat()
            ),
            strokeWidth = iceThickness,
            cap = StrokeCap.Round
        )

        // Ice crystals on markers
        if (isMainHour) {
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = 3f,
                center = Offset(
                    (center.x + cos(angle) * endRadius).toFloat(),
                    (center.y + sin(angle) * endRadius).toFloat()
                )
            )
        }
    }

    // Draw minute markers with subtle ice effect
    for (i in 1..60) {
        if (i % 5 != 0) {
            val angle = (i * 6 - 90) * PI / 180
            val startRadius = radius * 0.88f
            val endRadius = radius * 0.92f

            drawLine(
                color = Color(0xFF4A6B8A).copy(alpha = 0.7f),
                start = Offset(
                    (center.x + cos(angle) * startRadius).toFloat(),
                    (center.y + sin(angle) * startRadius).toFloat()
                ),
                end = Offset(
                    (center.x + cos(angle) * endRadius).toFloat(),
                    (center.y + sin(angle) * endRadius).toFloat()
                ),
                strokeWidth = 2f
            )
        }
    }

    // Calculate hand angles
    val hourAngle = (time.hour % 12) * 30f + time.minute * 0.5f
    val minuteAngle = time.minute * 6f + time.second * 0.1f
    val secondAngle = time.second * 6f

    // Draw frozen hour hand
    rotate(hourAngle, center) {
        val handEnd = Offset(center.x, center.y - radius * 0.5f)

        // Ice coating shadow
        drawLine(
            color = Color(0xFF87CEEB).copy(alpha = 0.4f),
            start = Offset(center.x - 2, center.y + 2),
            end = Offset(handEnd.x - 2, handEnd.y + 2),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )

        // Main hand with ice texture
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF87CEEB),
                    Color.White.copy(alpha = 0.9f),
                    Color(0xFF4169E1)
                )
            ),
            start = center,
            end = handEnd,
            strokeWidth = 12f,
            cap = StrokeCap.Round
        )

        // Ice crystals on hand
        repeat(3) { i ->
            val pos = center.y - (radius * 0.5f * (i + 1) / 4)
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 2f,
                center = Offset(center.x, pos)
            )
        }
    }

    // Draw frozen minute hand
    rotate(minuteAngle, center) {
        val handEnd = Offset(center.x, center.y - radius * 0.7f)

        // Ice coating shadow
        drawLine(
            color = Color(0xFF4169E1).copy(alpha = 0.4f),
            start = Offset(center.x - 1, center.y + 1),
            end = Offset(handEnd.x - 1, handEnd.y + 1),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )

        // Main hand with ice texture
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4169E1),
                    Color.White.copy(alpha = 0.8f),
                    Color(0xFF87CEEB)
                )
            ),
            start = center,
            end = handEnd,
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )

        // Ice crystals on hand
        repeat(4) { i ->
            val pos = center.y - (radius * 0.7f * (i + 1) / 5)
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = 1.5f,
                center = Offset(center.x, pos)
            )
        }
    }

    // Draw second hand with animated ice effect
    rotate(secondAngle, center) {
        val handEnd = Offset(center.x, center.y - radius * 0.8f)
        val counterEnd = Offset(center.x, center.y + radius * 0.2f)

        // Animated ice trail
        drawLine(
            color = Color(0xFFFF6B6B).copy(alpha = iceGlow * 0.3f),
            start = Offset(center.x - 1, center.y + 1),
            end = Offset(handEnd.x - 1, handEnd.y + 1),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Main second hand
        drawLine(
            color = Color(0xFFFF4444),
            start = center,
            end = handEnd,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Counterweight with ice
        drawLine(
            color = Color(0xFFFF4444),
            start = center,
            end = counterEnd,
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Ice crystal on tip
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 3f,
            center = handEnd
        )
    }

    // Multi-layered center with ice effect
    drawCircle(
        color = Color(0xFF87CEEB).copy(alpha = iceGlow * 0.6f),
        radius = 18f,
        center = center
    )
    drawCircle(
        color = Color(0xFF4169E1),
        radius = 12f,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFF87CEEB))
        ),
        radius = 8f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = center
    )
}

@Composable
fun WinterTimeZoneRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        WinterTimeZoneCard("NYC", -5)
        WinterTimeZoneCard("LON", 0)
        WinterTimeZoneCard("TOK", 9)
    }
}

@Composable
fun WinterTimeZoneCard(city: String, offset: Int) {
    val currentTime = getCurrentTime()
    val adjustedHour = (currentTime.hour + offset + 24) % 24

    val shimmer by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3E50).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = Color(0xFF87CEEB).copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f * shimmer),
                            Color.Transparent,
                            Color(0xFF87CEEB).copy(alpha = 0.1f * shimmer)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = city,
                    fontSize = 14.sp,
                    color = Color(0xFF87CEEB),
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF87CEEB).copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 4f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (adjustedHour < 10) "0$adjustedHour" else "$adjustedHour"}:${if (currentTime.minute < 10) "0${currentTime.minute}" else "${currentTime.minute}"}",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF87CEEB).copy(alpha = 0.3f),
                            offset = Offset(0f, 0f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}

data class TimeData(
    val hour: Int,
    val minute: Int,
    val second: Int
)

// Demo time state that starts at 10:30:00 and updates every second
private var demoSeconds = 0

fun getCurrentTime(): TimeData {
    // Start at 10:30:00 for a nice demo time
    val baseHours = 10
    val baseMinutes = 30
    val baseSeconds = 0

    val totalDemoSeconds = baseHours * 3600 + baseMinutes * 60 + baseSeconds + demoSeconds

    val hours = (totalDemoSeconds / 3600) % 24
    val minutes = (totalDemoSeconds / 60) % 60
    val seconds = totalDemoSeconds % 60

    demoSeconds++

    return TimeData(
        hour = hours,
        minute = minutes,
        second = seconds
    )
}
