package com.elfen.clipkeep.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elfen.clipkeep.R
import com.elfen.clipkeep.presentation.state.PlayerState
import com.elfen.clipkeep.utils.msToText

@Composable
fun PlayerExternalControls(
    modifier: Modifier = Modifier,
    playerState: PlayerState,
    offsetBy: Long = 0,
    overrideDuration: Long? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { playerState.togglePlayback() }) {
            Icon(
                painterResource(if (playerState.isPlaying) R.drawable.sharp_pause_24 else R.drawable.sharp_play_arrow_24),
                null
            )
        }

        Text(
            (playerState.currentPosition + offsetBy).msToText(
                (playerState.duration ?: 0) >= 1_000 * 60 * 60
            ),
            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum")
        )

        if ((overrideDuration ?: playerState.duration) != null)
            Text(
                "/ ${(overrideDuration ?: playerState.duration!!).msToText()}",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFeatureSettings = "tnum"
                )
            )

        Spacer(Modifier.weight(1f))
        IconButton(onClick = { playerState.seekBy(-10_000) }) {
            Icon(painterResource(R.drawable.sharp_replay_10_24), null)
        }
        IconButton(onClick = { playerState.seekBy(10_000) }) {
            Icon(painterResource(R.drawable.sharp_forward_10_24), null)
        }
    }
}
