package com.elfen.clipkeep.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elfen.clipkeep.R
import com.elfen.clipkeep.domain.model.EditingClipPart
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import com.elfen.clipkeep.utils.msToText
import java.util.Locale

@Composable
fun EditPartCard(
    modifier: Modifier = Modifier,
    part: EditingClipPart,
    onClick: () -> Unit = {},
    onEditTitle: () -> Unit = {},
    onToggle: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick, enabled = part.enabled)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            modifier = Modifier
                .align(Alignment.Top)
                .offset(y = (-8).dp),
            checked = part.enabled,
            onCheckedChange = { onToggle() }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (part.enabled) 1f else 0.5f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = part.enabled) { onEditTitle() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!part.name.isNullOrBlank()) {
                    Text(part.name, style = MaterialTheme.typography.titleSmall)
                } else {
                    Text(
                        "Untitled",
                        style = MaterialTheme.typography.titleSmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Icon(
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.outline,
                    painter = painterResource(R.drawable.sharp_edit_24),
                    contentDescription = null
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "Starts".uppercase(Locale.ROOT),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        part.startMs.msToText(),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "Finishes".uppercase(Locale.ROOT),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        part.finishMs.msToText(),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                    )
                }

            }
        }
        Icon(
            modifier = Modifier.alpha(if (part.enabled) 1f else 0.5f),
            painter = painterResource(R.drawable.sharp_keyboard_arrow_right_24),
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun EditPartCardPrev() {
    ClipKeepTheme() {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            EditingClipPart.samples.forEach { part ->
                EditPartCard(
                    modifier = Modifier.fillMaxWidth(),
                    part = part
                )
            }
        }
    }
}