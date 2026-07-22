package com.elfen.clipkeep.presentation.component

import android.icu.util.TimeZone
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.elfen.clipkeep.domain.model.Clip
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import com.elfen.clipkeep.utils.AbsoluteSmoothCornerShape
import com.elfen.clipkeep.utils.msToText
import com.elfen.clipkeep.utils.readableBytes
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import java.time.LocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@OptIn(ExperimentalTime::class)
private fun Instant.toFormatted(): String {
    val formatter = DateTimeComponents.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        day()
        char(',')
        char(' ')
        hour()
        char(':')
        minute()
    }

    return this.format(formatter)
}

fun Instant.timeAgo(now: Instant = Clock.System.now()): String {
    val duration = now - this
    val seconds = duration.inWholeSeconds.coerceAtLeast(0)

    val (value, unit) = when {
        seconds < 60 ->
            seconds to "second"

        seconds < 60 * 60 ->
            seconds / 60 to "minute"

        seconds < 24 * 60 * 60 ->
            seconds / (60 * 60) to "hour"

        seconds < 7 * 24 * 60 * 60 ->
            seconds / (24 * 60 * 60) to "day"

        seconds < 30 * 24 * 60 * 60 ->
            seconds / (7 * 24 * 60 * 60) to "week"

        seconds < 365 * 24 * 60 * 60 ->
            seconds / (30 * 24 * 60 * 60) to "month"

        else ->
            seconds / (365 * 24 * 60 * 60) to "year"
    }

    return "$value $unit${if (value != 1L) "s" else ""} ago"
}

@OptIn(ExperimentalTime::class)
@Composable
fun ClipCard(modifier: Modifier = Modifier, clip: Clip) {
    Box(
        modifier = modifier.clip(
            AbsoluteSmoothCornerShape(
                cornerRadiusTL = 24.dp,
                cornerRadiusBL = 24.dp,
                cornerRadiusBR = 24.dp,
                cornerRadiusTR = 24.dp
            )
        )
    ) {
        AsyncImage(
            model = clip.thumbnail,
            contentDescription = clip.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .aspectRatio((clip.width.toFloat() / clip.height).coerceAtMost(16f / 9))
        )

        if (clip.title != null)
            Text(
                clip.title,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.25f),
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),

            ) {
            Text(
                clip.createdAt.timeAgo(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Start,
                color = Color.White
            )
            Text(
                "${clip.duration.msToText()} • ${clip.size.readableBytes}",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Start,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ClipCardPrev() {
    ClipKeepTheme() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("My Clips")
                    }
                )
            }
        ) { innerPadding ->
            LazyVerticalStaggeredGrid(
                modifier = Modifier.padding(innerPadding),
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(Clip.samples) { clip ->
                    ClipCard(
                        modifier = Modifier.fillMaxWidth(0.33f),
                        clip = clip
                    )
                }
            }
        }
    }
}